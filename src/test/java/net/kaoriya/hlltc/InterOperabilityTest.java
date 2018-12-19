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
        Sketch sk = loadResource("by_go/sparse-3400.hlltc");
        SketchTest.assertErrorRatio(sk, 3400L, 2, true);
    }

    @Test
    public void loadDense() throws Exception {
        Sketch sk = loadResource("by_go/dense-1M.hlltc");
        SketchTest.assertErrorRatio(sk, 1000000, 2, true);
    }
}
