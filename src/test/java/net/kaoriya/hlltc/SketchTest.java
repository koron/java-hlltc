package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

public class SketchTest {

    static double estimateError(long got, long exp) {
        long delta = Math.abs(got - exp);
        return (double)delta / (double)exp;
    }

    static void assertErrorRatio(Sketch sk, long exp, double th) {
        assertErrorRatio(sk, exp, th, false);
    }

    static void assertErrorRatio(Sketch sk, long exp, double th, boolean verbose) {
        long res = sk.estimate();
        double ratio = 100 * estimateError(res, exp);
        if (ratio > th) {
            fail(String.format("exact %d, got %d which is %.2f%% error", exp, res, ratio));
        }
        if (verbose) {
            System.out.printf("OK: exact %d, got %d which is %.2f%% error\n", exp, res, ratio);
        }
    }

    @Test
    public void cardinalityHashed() {
        Sketch sk = new Sketch(14);
        int step = 10;
        long uniq = 0;

        for (int i = 0; uniq < 10000000; ++i) {
            String key = String.format("flow-%d", i);
            sk.insert(key.getBytes());
            uniq++;

            if (uniq % step == 0) {
                step *= 5;
                assertErrorRatio(sk, uniq, 2);
            }
        }
        assertErrorRatio(sk, uniq, 2);
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
        Sketch sk1 = new Sketch(14);
        Sketch sk2 = new Sketch(14);
        Sketch sk3 = new Sketch(14);

        long unique = 0;
        for (int i = 1; i <= 10000000; i++) {
            String str = String.format("flow-%d", i);
            sk1.insert(str.getBytes());
            if ((i % 2) == 0) {
                sk2.insert(str.getBytes());
            }
            unique++;
        }

        assertErrorRatio(sk1, unique, 2);
        assertErrorRatio(sk2, unique / 2, 2);

        sk2.merge(sk1);
        assertErrorRatio(sk2, unique, 2);

        for (int i = 1; i < 500000; i++) {
            String str = String.format("stream-%d", i);
            sk2.insert(str.getBytes());
            unique++;
        }

        sk2.merge(sk3);
        assertErrorRatio(sk2, unique, 1);
    }

    @Test
    public void insertRebase() {
        Sketch sk = new Sketch(14);
        long exp = 0;
        int b = 0;
        for (int i = 1; i <= 10000000; i++) {
            String str = String.format("flow-%d", i);
            sk.insert(str.getBytes());
            if (sk.b > b) {
                assertErrorRatio(sk, i, 2);
                b = sk.b;
            }
        }
        assertErrorRatio(sk, 10000000, 2);
    }

    static void assertSketch(Sketch exp, Sketch act) {
        assertSketch(exp, act, false);
    }

    static void assertSketch(Sketch exp, Sketch act, boolean verbose) {
        assertEquals(exp.sparse, act.sparse);
        assertEquals(exp.p, act.p);
        assertEquals(exp.b, act.b);
        assertEquals(exp.m, act.m);
        assertEquals(exp.alpha, act.alpha, 0.01f);
        assertEquals(exp.tmpSet, act.tmpSet);
        assertTrue("sparseList should equal",
                CompressedList.equals(exp.sparseList, act.sparseList));
        assertTrue("regs should equals",
                Registers.equals(exp.regs, act.regs));
    }

    @Test
    public void toFromBytesSparse() throws Exception {
        Random rnd = new Random();
        for (int i = 0; i < 100; i++) {
            Sketch sk = new Sketch(4, true);
            assertTrue(sk.sparse);
            sk.tmpSet.add(26);
            sk.tmpSet.add(40);

            for (int j = 0; j < 10; j++) {
                sk.sparseList.add(rnd.nextInt());
            }

            byte[] data = sk.toBytes();
            assertEquals(Sketch.VERSION, data[0]);
            assertEquals((byte)0x04, data[1]); // p
            assertEquals((byte)0x00, data[2]); // b
            assertEquals((byte)0x01, data[3]); // sparse

            Sketch res = Sketch.fromBytes(data);
            assertSketch(sk, res);
        }
    }

    static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (byte b : bytes) {
            if (!first) {
                sb.append(' ');
            }
            sb.append(String.format("%02x", b));
            first = false;
        }
        return sb.toString();
    }

    @Test
    public void toFromBytesDense() throws Exception {
        Random rnd = new Random();
        for (int i = 0; i < 100; i++) {
            Sketch sk = new Sketch(4, false);
            assertFalse(sk.sparse);

            for (int j = 0; j < 10; j++) {
                sk.regs.set(j, rnd.nextInt(0x10));
            }

            byte[] data = sk.toBytes();
            assertEquals(Sketch.VERSION, data[0]);
            assertEquals((byte)0x04, data[1]); // p
            assertEquals((byte)0x00, data[2]); // b
            assertEquals((byte)0x00, data[3]); // sparse

            Sketch res = Sketch.fromBytes(data);
            assertSketch(sk, res);
        }
    }
}
