package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

public class SparseTest {
    @Test
    public void encodeDecode() {
        final int p = 14;
        final int pp = 25;
        Random rnd = new Random();
        for (int i = 0; i < 1000000; i++) {
            long x = rnd.nextLong();
            int k = Sparse.encodeHash(x, p, pp);
            Sparse sp1 = Sparse.decodeHash(k, p, pp);
            Sparse sp2 = Sparse.getPosVal(x, p);
            if (!sp1.equals(sp2)) {
                fail(String.format("sparse doesn't match for i=%d x=%016x k=%08x: sp1=%s sp2=%s", i, x, k, sp1, sp2));
            }
        }
    }
}
