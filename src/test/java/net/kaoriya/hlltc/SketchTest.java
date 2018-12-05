package net.kaoriya.hlltc;

import java.util.HashSet;
import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.*;

public class SketchTest {

    static double estimateError(long got, long exp) {
        long delta = Math.abs(got - exp);
        return (double)delta / (double)exp;
    }

    static void assertErrorRatio(Sketch sk, long exp, double th) {
        long res = sk.estimate();
        double ratio = 100 * estimateError(res, exp);
        if (ratio > th) {
            fail(String.format("exact %d, got %d which is %.2f%% error", exp, res, ratio));
        }
    }

    @Test
    public void cardinalityHashed() {
        Sketch sk = new Sketch(14);
        int step = 10;
        HashSet<String> uniq = new HashSet<>();

        for (int i = 0; uniq.size() < 10000000; ++i) {
            String key = String.format("flow-%d", i);
            sk.insert(key.getBytes());
            uniq.add(key);

            if (uniq.size() % step == 0) {
                step *= 5;
                assertErrorRatio(sk, uniq.size(), 2);
            }
        }
        assertErrorRatio(sk, uniq.size(), 2);
    }
}
