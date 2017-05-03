package com.cluster.math;

import com.cluster.math.model.Bits;
import com.cluster.math.model.Interval;
import com.cluster.math.utils.Efficiency;
import org.nevec.rjm.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by envoy on 15.04.2017.
 */
public class Strongin {
    private static final long m = 150;
    public static final BigDecimal log2 = BigDecimalMath.log(new BigDecimal(2).setScale(TestExecutor.getConfig().getBIG_DECIMAL_SCALE()));
    private ArrayList<Interval> intervals;
    private MinsRepository rep;
    private int iterations;
    private int sizeMins;
    private Bits b;
    private Bits a;

    private Strongin() {
    }

    public Strongin(Bits a, Bits b, final int iterations, int sizeMins) {
        this.a = a;
        this.b = b;
        this.iterations = iterations;
        this.sizeMins = sizeMins;
    }

    public static double calcF(BigInteger a, BigInteger b, double zA, double zB) {
        double logA = BigDecimalMath.log(new BigDecimal(a).setScale(TestExecutor.getConfig().getBIG_DECIMAL_SCALE())).divide(log2, RoundingMode.HALF_UP).doubleValue();
        double logB = BigDecimalMath.log(new BigDecimal(b).setScale(TestExecutor.getConfig().getBIG_DECIMAL_SCALE())).divide(log2, RoundingMode.HALF_UP).doubleValue();
        return m * (logB - logA) + (Math.pow(zB - zA, 2) / (m * (logB - logA))) - 2 * (zA + zB);
    }

    public MinsRepository solve() {
        if ((a == null) || (b == null) || (iterations <= 0) || (sizeMins <= 0)) {
            throw new IllegalArgumentException("Strongin params not exist");
        }
        return solve(a, b, iterations, sizeMins);
    }


    private MinsRepository solve(Bits a, Bits b, final int iterations, int sizeMins) {
        rep = new MinsRepository(sizeMins);
        intervals = new ArrayList<>(iterations);
        intervals.add(new Interval(rep, a, b));

        int ind = 0;
        Interval interval = null;
        Efficiency zX = null;
        Bits bitsX = null;
        boolean isUsed = false;
        Bits cacheB = null;
        Efficiency cacheZB = null;
        for (int i = 0; i < iterations; i++) {
            if (i % 100 == 0) {
                System.out.println("Strongin " + i);
            }
            interval = intervals.get(ind);
            double logA = BigDecimalMath.log(new BigDecimal(interval.getA().getNumber()).setScale(TestExecutor.getConfig().getBIG_DECIMAL_SCALE())).divide(log2, RoundingMode.HALF_UP).doubleValue();
            double logB = BigDecimalMath.log(new BigDecimal(interval.getB().getNumber()).setScale(TestExecutor.getConfig().getBIG_DECIMAL_SCALE())).divide(log2, RoundingMode.HALF_UP).doubleValue();
            BigInteger x = new BigDecimal(interval.getB().getNumber().add(interval.getA().getNumber())).setScale(TestExecutor.getConfig().getBIG_DECIMAL_SCALE()).divide(new BigDecimal(2)).setScale(0, BigDecimal.ROUND_HALF_UP).toBigInteger(); //Math.ceil(Math.pow(2, (logA + logB) / 2.0));
            bitsX = new Bits(TestExecutor.getConfig().getSTRONGIN_M(), x);
            zX = new Efficiency(rep, bitsX);
            isUsed = false;

            double logSize = BigDecimalMath.log(new BigDecimal(TestExecutor.getConfig().getSTRONGIN_EPS()).setScale(TestExecutor.getConfig().getBIG_DECIMAL_SCALE()).divide(new BigDecimal(interval.getA().getNumber()), RoundingMode.HALF_UP).add(BigDecimal.ONE)).divide(log2, RoundingMode.HALF_UP).doubleValue();
            if ((logB - logA) / 2.0 < logSize) { //todo check
                intervals.remove(ind);
            } else {
                cacheB = interval.getB();
                cacheZB = interval.getZB();
                if (!(zX.getXInf().getNumber().compareTo(interval.getA().getNumber()) < 0) && !(zX.getXInf().getNumber().compareTo(interval.getB().getNumber()) > 0)) {
                    interval.setB(bitsX, zX);
                    isUsed = true;
                }

                if (!(zX.getXSup().getNumber().compareTo(bitsX.getNumber()) < 0) && !(zX.getXSup().getNumber().compareTo(cacheB.getNumber()) > 0)) {
                    if (isUsed) {
                        intervals.add(new Interval(rep, bitsX, cacheB, zX, cacheZB));
                    } else {
                        interval.setA(bitsX, zX);
                        interval.setA(bitsX, zX);
                        isUsed = true;
                    }
                }

                if (!isUsed) {
                    intervals.remove(ind);
                }
            }

            if (intervals.size() == 0) {
                break;
            }

            ind = 0;
            interval = intervals.get(ind);
            for (int j = 0; j < intervals.size(); j++) {
                if (intervals.get(j).getF() > interval.getF()) {
                    ind = j;
                }
            }
        }

        return rep;
    }

    public ArrayList<Interval> getIntervals() {
        return intervals;
    }

    public MinsRepository getRep() {
        return rep;
    }
}
