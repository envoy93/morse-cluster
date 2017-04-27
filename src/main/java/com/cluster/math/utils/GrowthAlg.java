package com.cluster.math.utils;

import com.cluster.math.TestExecutor;
import com.cluster.math.model.Bits;
import com.cluster.math.model.Conformation;
import matlabcontrol.MatlabInvocationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by envoy on 18.04.2017.
 */
public class GrowthAlg {

    public static Conformation buildBestConf(Bits bits, int n, int iterations) throws MatlabInvocationException {
        Conformation conf = null;

        Map<String, Conformation> results = new HashMap<>();
        buildConfRecursive(bits.getBites().toString(), n, iterations, results);
        for (Conformation conformation : results.values()) {
            if ((conf == null) || (conf.getEnergy() > conformation.getEnergy())) {
                conf = conformation;
            }
        }
        return conf;
    }

    private static void buildConfRecursive(final String bits, final int n, int iterations, final Map<String, Conformation> results) throws MatlabInvocationException {
        if (results.containsKey(bits)) {
            return;
        }

        int currSize = 0;
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') currSize++;
        }

        if (currSize == n) {
            results.put(bits, ClusterMath.calcWithStartConf(bits, false));
            return;
        }

        ArrayList<String> temp = findBestAdjacentAtom(bits);

        int itersMin = 1;
        if (iterations > 0) {
            itersMin = (temp.size() < iterations) ? temp.size() : iterations;
            iterations -= itersMin;
        }

        for (int i = 0; i < itersMin; i++) {
            buildConfRecursive(temp.get(i), n, iterations, results);
        }
    }

    public static ArrayList<String> findBestAdjacentAtom(String startBits) throws MatlabInvocationException {
        ArrayList<String> list = new ArrayList<>();

        ArrayList<String> adjacentList = new ArrayList<>();
        int adjacentMax = 0;
        StringBuilder sb = new StringBuilder(startBits);
        for (int i = 0; i < startBits.length(); i++) {
            if (startBits.charAt(i) == '0') {
                int temp = ClusterMath.calcAdjacentNunberWithStartConf(startBits, i);
                if (temp > adjacentMax) {
                    adjacentMax = temp;
                    adjacentList.clear();
                }
                if (temp == adjacentMax) {
                    sb.setCharAt(i, '1');
                    adjacentList.add(sb.toString());
                    sb.setCharAt(i, '0');
                }
            }
        }

        Map<String, Conformation> conformations = new HashMap<>();
        Conformation conf = null;
        double minEnergy = 0;
        for (String bits : adjacentList) {
            conf = ClusterMath.calcWithStartConf(bits, false);
            if (conf.getEnergy() < minEnergy) {
                minEnergy = conf.getEnergy();
            }
            conformations.put(bits, conf);
        }

        for (Map.Entry<String, Conformation> entry : conformations.entrySet()) {
            if (((Math.abs(entry.getValue().getEnergy() - minEnergy)) / minEnergy) < TestExecutor.getConfig().getTOP_MAX_ENERGY_DELTA()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }
}
