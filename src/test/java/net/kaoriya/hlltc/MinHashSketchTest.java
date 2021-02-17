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
        Random rnd = new Random(1);
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
}
