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

}
