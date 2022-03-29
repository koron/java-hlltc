package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

public class SparseTest {
    final int p = 14;
    final int pp = 25;

    @Test
    public void encodeDecode() {
        Random rnd = new Random();
        for (int i = 0; i < 1000000; i++) {
            long x = rnd.nextLong();
            checkEncoder(x);
        }
    }

    @Test
    public void encodeSpecialK1() {
        Random rnd = new Random();
        for (int i = 0; i < 1000000; i++) {
            long x = rnd.nextLong() & 0xfffc007fffffffffL;
            int k = checkEncoder(x);
            assertTrue((k & 1) != 0);
        }
    }

    int checkEncoder(long x) {
        int k = Sparse.encodeHash(x, p, pp);
        Sparse sp1 = Sparse.decodeHash(k, p, pp);
        Sparse sp2 = Sparse.getPosVal(x, p);
        if (!sp1.equals(sp2)) {
            fail(String.format("sparse doesn't match for x=%016x k=%08x: sp1=%s sp2=%s", x, k, sp1, sp2));
        }
        return k;
    }
}
