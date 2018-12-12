package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

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

    static byte[] toBytes(long v) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(8);
             DataOutputStream out = new DataOutputStream(bout)) {
            out.writeLong(v);
            out.flush();
            return bout.toByteArray();
        }
    }

    @Test
    public void mergeSparse() throws Exception {
        Sketch sk = new Sketch(16);
        sk.insert(toBytes(0x00010fffffffffffL));
        sk.insert(toBytes(0x00020fffffffffffL));
        sk.insert(toBytes(0x00030fffffffffffL));
        sk.insert(toBytes(0x00040fffffffffffL));
        sk.insert(toBytes(0x00050fffffffffffL));
        sk.insert(toBytes(0x00050fffffffffffL));

        Sketch sk2 = new Sketch(16);
        sk2.merge(sk);
        assertEquals(5, sk2.estimate());

        assertTrue(sk2.sparse);
        assertTrue(sk.sparse);

        sk2.merge(sk);
        assertEquals(5, sk2.estimate());

        sk.insert(toBytes(0x00060fffffffffffL));
        sk.insert(toBytes(0x00070fffffffffffL));
        sk.insert(toBytes(0x00080fffffffffffL));
        sk.insert(toBytes(0x00090fffffffffffL));
        sk.insert(toBytes(0x000a0fffffffffffL));
        sk.insert(toBytes(0x000a0fffffffffffL));
        assertEquals(10, sk.estimate());

        sk2.merge(sk);
        assertEquals(10, sk.estimate());
    }

    @Test
    public void mergeRebase() {
        Sketch sk1 = new Sketch(16, false);
        Sketch sk2 = new Sketch(16, false);

        sk1.regs.set(13, 7);
        sk2.regs.set(13, 1);
        sk1.merge(sk2);
        assertEquals(7, sk1.regs.get(13));

        sk2.regs.set(13, 8);
        sk1.merge(sk2);
        assertEquals(8, sk2.regs.get(13));
        assertEquals(8, sk1.regs.get(13));

        sk1.b = 12;
        sk2.regs.set(13, 12);
        sk1.merge(sk2);
        assertEquals(8, sk1.regs.get(13));

        sk2.b = 13;
        sk2.regs.set(13, 12);
        sk1.merge(sk2);
        assertEquals(12, sk1.regs.get(13));
    }

    @Test
    public void mergeComplex() {
        System.out.println("mergeComplex");
        Sketch sk1 = new Sketch(14);
        Sketch sk2 = new Sketch(14);
        Sketch sk3 = new Sketch(14);

        HashSet<String> unique = new HashSet<>();
        for (int i = 1; i <= 10000000; i++) {
            String str = String.format("flow-%d", i);
            sk1.insert(str.getBytes());
            if ((i % 2) == 0) {
                sk2.insert(str.getBytes());
            }
            unique.add(str);
        }

        assertErrorRatio(sk1, unique.size(), 2);

        assertErrorRatio(sk2, unique.size() / 2, 2);

        System.out.println("HERE_A0");
        sk2.merge(sk1);
        assertErrorRatio(sk2, unique.size(), 2);

        for (int i = 1; i < 500000; i++) {
            String str = String.format("stream-%d", i);
            sk2.insert(str.getBytes());
            unique.add(str);
        }

        sk2.merge(sk3);
        assertErrorRatio(sk2, unique.size(), 1);
    }
}
