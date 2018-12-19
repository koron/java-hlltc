package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class InterOperabilityTest {

    static Sketch loadResource(String name) throws Exception {
        InputStream ires = InterOperabilityTest.class.getClassLoader().getResourceAsStream(name);
        byte[] b = IOUtils.toByteArray(ires);
        return Sketch.fromBytes(b);
    }

    @Test
    public void loadSparse() throws Exception {
        Sketch sk = loadResource("by_go/sparse-14-3400.hlltc");
        SketchTest.assertErrorRatio(sk, 3400L, 2);
    }

    @Test
    public void loadDense() throws Exception {
        Sketch sk = loadResource("by_go/dense-14-1M.hlltc");
        SketchTest.assertErrorRatio(sk, 1000000, 2);
    }

    @Test
    public void mergeSparse() throws Exception {
        Sketch sk0 = loadResource("by_go/sparse-14-3400.hlltc");
        Sketch sk1 = loadResource("by_go/sparse-14-3400.hlltc");

        Sketch sk = new Sketch(14);
        for (int i = 1; i <= 3400; i += 2) {
            sk.insert(String.format("flow-%d", i).getBytes());
        }
        SketchTest.assertErrorRatio(sk, 1700, 2);

        sk0.merge(sk);
        SketchTest.assertErrorRatio(sk0, 3400, 2);

        sk.merge(sk1);
        SketchTest.assertErrorRatio(sk, 3400, 2);
    }

    @Test
    public void mergeDense() throws Exception {
        Sketch sk0 = loadResource("by_go/dense-14-1M.hlltc");
        Sketch sk1 = loadResource("by_go/dense-14-1M.hlltc");

        Sketch sk = new Sketch(14);
        for (int i = 1; i <= 1000000; i += 2) {
            sk.insert(String.format("flow-%d", i).getBytes());
        }
        SketchTest.assertErrorRatio(sk, 500000, 2);

        sk0.merge(sk);
        SketchTest.assertErrorRatio(sk0, 1000000, 2);

        sk.merge(sk1);
        SketchTest.assertErrorRatio(sk, 1000000, 2);
    }

    @Test
    public void mergeDenseAdd25per() throws Exception {
        Sketch sk0 = loadResource("by_go/dense-14-1M.hlltc");
        Sketch sk1 = loadResource("by_go/dense-14-1M.hlltc");

        Sketch sk = new Sketch(14);
        for (int i = 1; i <= 1000000; i++) {
            sk.insert(String.format("flow-%d", i + 250000).getBytes());
        }
        SketchTest.assertErrorRatio(sk, 1000000, 2);

        sk0.merge(sk);
        SketchTest.assertErrorRatio(sk0, 1250000, 2);

        sk.merge(sk1);
        SketchTest.assertErrorRatio(sk, 1250000, 2);
    }

    @Test
    public void mergeDenseAdd50per() throws Exception {
        Sketch sk0 = loadResource("by_go/dense-14-1M.hlltc");
        Sketch sk1 = loadResource("by_go/dense-14-1M.hlltc");

        Sketch sk = new Sketch(14);
        for (int i = 1; i <= 1000000; i++) {
            sk.insert(String.format("flow-%d", i + 500000).getBytes());
        }
        SketchTest.assertErrorRatio(sk, 1000000, 2);

        sk0.merge(sk);
        SketchTest.assertErrorRatio(sk0, 1500000, 2);

        sk.merge(sk1);
        SketchTest.assertErrorRatio(sk, 1500000, 2);
    }

    @Test
    public void mergeDenseAdd75per() throws Exception {
        Sketch sk0 = loadResource("by_go/dense-14-1M.hlltc");
        Sketch sk1 = loadResource("by_go/dense-14-1M.hlltc");

        Sketch sk = new Sketch(14);
        for (int i = 1; i <= 1000000; i++) {
            sk.insert(String.format("flow-%d", i + 750000).getBytes());
        }
        SketchTest.assertErrorRatio(sk, 1000000, 2);

        sk0.merge(sk);
        SketchTest.assertErrorRatio(sk0, 1750000, 2);

        sk.merge(sk1);
        SketchTest.assertErrorRatio(sk, 1750000, 2);
    }

    @Test
    public void mergeDenseAdd99per() throws Exception {
        Sketch sk0 = loadResource("by_go/dense-14-1M.hlltc");
        Sketch sk1 = loadResource("by_go/dense-14-1M.hlltc");

        Sketch sk = new Sketch(14);
        for (int i = 1; i <= 1000000; i++) {
            sk.insert(String.format("flow-%d", i + 990000).getBytes());
        }
        SketchTest.assertErrorRatio(sk, 1000000, 2);

        sk0.merge(sk);
        SketchTest.assertErrorRatio(sk0, 1990000, 2);

        sk.merge(sk1);
        SketchTest.assertErrorRatio(sk, 1990000, 2);
    }
}
