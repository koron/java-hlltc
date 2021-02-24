package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Random;

public class MinHashSketchTest {

    static double estimateError(long got, long exp) {
        long delta = Math.abs(got - exp);
        return (double)delta / (double)exp;
    }

    @Test
    public void zeros() {
        MinHashSketch sk = new MinHashSketch();
        int exp = 0;
        Random rnd = new Random();
        for (int i = 0; i < sk.regs.length; i++) {
            sk.regs[i].setShort(rnd.nextInt(1 << 16));
            if (sk.regs[i].lz == 0) {
                exp++;
            }
        }
        MinHashSketch.SumAndZeros sum = sk.regSumAndZeros();
        assertEquals(exp, sum.zeros);
    }

    @Test
    public void allZeros() {
        MinHashSketch sk = new MinHashSketch();
        int exp = 16384;
        MinHashSketch.SumAndZeros sum = sk.regSumAndZeros();
        assertEquals(exp, sum.zeros);
    }

    final static byte[] letterBytes = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").getBytes();

    static byte[] randBytes(Random rnd, int n) {
        byte[] d = new byte[n];
        for (int i = 0; i < n; i++) {
            d[i] = letterBytes[rnd.nextInt(letterBytes.length)];
        }
        return d;
    }

    @Test
    public void cardinality() {
        MinHashSketch sk = new MinHashSketch();
        int step = 10000;
        HashSet<String> unique = new HashSet<>();
        Random rnd = new Random();
        for (int i = 0; unique.size() <= 1000000; i++) {
            byte[] d = randBytes(rnd, rnd.nextInt(32));
            sk.add(d);
            unique.add(new String(d));
            if (unique.size() % step == 0) {
                long exact = (long)unique.size();
                long res = sk.cardinality();
                double ratio = 100 * estimateError(res, exact);
                if (ratio > 2) {
                    fail(String.format("exact %d, got %d which is %.2f%% error", exact, res, ratio));
                }
                step *= 10;
            }
        }
    }

    @Test
    public void merge() {
        MinHashSketch sk1 = new MinHashSketch();
        MinHashSketch sk2 = new MinHashSketch();
        HashSet<String> unique = new HashSet<>();
        Random rnd = new Random();
        for (int i = 0; i < 1000000; i++) {
            byte[] v1 = randBytes(rnd, rnd.nextInt(32));
            sk1.add(v1);
            unique.add(new String(v1));
            byte[] v2 = randBytes(rnd, rnd.nextInt(32));
            sk1.add(v2);
            unique.add(new String(v2));
        }

        // merge and check cardinality
        MinHashSketch sk3 = sk1.clone();
        sk3.merge(sk2);
        long exact = (long)unique.size();
        long res = sk3.cardinality();
        double ratio = 100 * estimateError(res, exact);
        if (ratio > 2) {
            fail(String.format("exact %d, got %d which is %.2f%% error", exact, res, ratio));
        }

        // merging merged set, causes no effects on cardinality.
        sk3.merge(sk2);
        exact = res;
        res = sk3.cardinality();
        ratio = 100 * estimateError(res, exact);
        if (ratio > 2) {
            fail(String.format("exact %d, got %d which is %.2f%% error", exact, res, ratio));
        }
    }

    @Test
    public void intersection() {
        final long iters = 20;
        final int k = 1000000;

        MinHashSketch sk1 = new MinHashSketch();
        for (int i = 0; i < k; i++) {
            String v = Integer.toString(i);
            sk1.add(v.getBytes());
        }

        for (int j = 1; j <= iters; j++) {
            double frac = (double)j / (double)iters;
            int col = (int)((double)k * frac);
            MinHashSketch sk2 = new MinHashSketch();
            for (int i = col; i < k * 2; i++) {
                String v = Integer.toString(i);
                sk2.add(v.getBytes());
            }

            long exact = (long)(k - col);
            long res = sk1.intersection(sk2);

            double ratio = 100 * estimateError(res, exact);
            if (ratio > 10) {
                fail(String.format("exact %d, got %d which is %.2f%% error (j=%d k=%d frac=%.2f)", exact, res, ratio, j, k, frac));
            }
        }
    }

    @Test
    public void noIntersection() {
        MinHashSketch sk1 = new MinHashSketch();
        MinHashSketch sk2 = new MinHashSketch();
        for (int i = 0; i < 1000000; i++) {
            sk1.add(Integer.toString(i).getBytes());
        }
        for (int i = 1000000; i < 2000000; i++) {
            sk2.add(Integer.toString(i).getBytes());
        }
        long got = sk1.intersection(sk2);
        if (got != 0) {
            fail(String.format("expected no intesection, got %d", got));
        }
    }

    @Test
    public void marshal() throws Exception {
        MinHashSketch sk = new MinHashSketch();
        final int N = 1000000;
        for (int i = 0; i < 1000000; i++) {
            sk.add(Integer.toString(i).getBytes());
        }
        byte[] bin = sk.toBytes();
        MinHashSketch sk2 = MinHashSketch.fromBytes(bin);
        long exp = sk.cardinality();
        long act = sk2.cardinality();
        assertEquals("should equal cardinality between marshalling", act, exp);
    }
}
