package net.kaoriya.hlltc;

import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.*;

public class BenchmarkTest {

    static double estimateError(long got, long exp) {
        long delta = Math.abs(got - exp);
        return (double)delta / (double)exp;
    }

    @Test
    @Ignore
    public void benchmark() throws Exception {
        Sketch sk14 = new Sketch(14);
        Sketch sk15 = new Sketch(15);
        int step = 10;
        int unique = 0;
        final int MAX = 10000000;
        for (int i = 1; i <= MAX; i++) {
            byte[] key = String.format("stream-%d", i).getBytes();
            sk14.insert(key);
            sk15.insert(key);
            unique++;
            if ((unique % step) == 0 || unique == MAX) {
                step *= 5;
                long exact = unique;
                long res14 = sk14.estimate();
                double ratio14 = estimateError(res14, unique) * 100;
                long res15 = sk15.estimate();
                double ratio15 = estimateError(res15, unique) * 100;
                System.out.printf("Exact %d, got:\n", exact);
                System.out.printf("\thlltc-14 HLL %d (%.4f%% off)\n",
                        res14, ratio14);
                System.out.printf("\thlltc-15 HLL %d (%.4f%% off)\n",
                        res15, ratio15);
            }
        }
        byte[] data14 = sk14.toBytes();
        byte[] data15 = sk15.toBytes();
        System.out.printf("hlltc-14 HLL total size:\t%d\n", data14.length);
        System.out.printf("hlltc-15 HLL total size:\t%d\n", data15.length);
    }

    @Test
    @Ignore
    public void benchmarkPrecision() throws Exception {
        final int PRE_MIN = 4;
        final int PRE_MAX = 18;
        Sketch[] sketches = new Sketch[PRE_MAX - PRE_MIN + 1];
        for (int i = PRE_MIN; i <= PRE_MAX; i++) {
            sketches[i - PRE_MIN] = new Sketch(i);
        }

        // print header
        System.out.print("Exact");
        for (int i = PRE_MIN; i <= PRE_MAX; i++) {
            System.out.printf(", Count-%d, Error-%d", i, i);
        }
        System.out.println("");

        int step = 10;
        int unique = 0;
        final int MAX = 10000000;
        for (int i = 1; i <= MAX; i++) {
            byte[] key = String.format("stream-%d", i).getBytes();
            for (int j = PRE_MIN; j <= PRE_MAX; j++) {
                sketches[j - PRE_MIN].insert(key);
            }
            unique++;

            if ((unique % step) == 0 || unique == MAX) {
                step *= 5;
                long exact = unique;
                System.out.printf("%d", exact);
                for (int j = PRE_MIN; j <= PRE_MAX; j++) {
                    Sketch sk = sketches[j - PRE_MIN];
                    long res = sk.estimate();
                    double ratio = estimateError(res, unique) * 100;
                    System.out.printf(", %d, %.4f%%", res, ratio);
                }
                System.out.println("");
            }
        }

        // print footer
        System.out.print("Size");
        for (int i = PRE_MIN; i <= PRE_MAX; i++) {
            byte[] data = sketches[i - PRE_MIN].toBytes();
            System.out.printf(", %d, %d", data.length, data.length);
        }
        System.out.println("");
    }

    @Test
    @Ignore
    public void benchmarkPrecisionDense() throws Exception {
        final int PRE_MIN = 4;
        final int PRE_MAX = 18;
        Sketch[] sketches = new Sketch[PRE_MAX - PRE_MIN + 1];
        for (int i = PRE_MIN; i <= PRE_MAX; i++) {
            sketches[i - PRE_MIN] = new Sketch(i, false);
        }

        // print header
        System.out.print("Exact");
        for (int i = PRE_MIN; i <= PRE_MAX; i++) {
            System.out.printf(", Count-%d, Error-%d", i, i);
        }
        System.out.println("");

        int step = 10;
        int unique = 0;
        final int MAX = 10000000;
        for (int i = 1; i <= MAX; i++) {
            byte[] key = String.format("stream-%d", i).getBytes();
            for (int j = PRE_MIN; j <= PRE_MAX; j++) {
                sketches[j - PRE_MIN].insert(key);
            }
            unique++;

            if ((unique % step) == 0 || unique == MAX) {
                step *= 5;
                long exact = unique;
                System.out.printf("%d", exact);
                for (int j = PRE_MIN; j <= PRE_MAX; j++) {
                    Sketch sk = sketches[j - PRE_MIN];
                    long res = sk.estimate();
                    double ratio = estimateError(res, unique) * 100;
                    System.out.printf(", %d, %.4f%%", res, ratio);
                }
                System.out.println("");
            }
        }

        // print footer
        System.out.print("Size");
        for (int i = PRE_MIN; i <= PRE_MAX; i++) {
            byte[] data = sketches[i - PRE_MIN].toBytes();
            System.out.printf(", %d, %d", data.length, data.length);
        }
        System.out.println("");
    }
}
