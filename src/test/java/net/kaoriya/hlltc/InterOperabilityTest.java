package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
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

    //static class StreamGobbler extends Thread {
    //    InputStream is;

    //    StreamGobbler(InputStream is) {
    //        this.is = is;
    //    }

    //    public void run() {
    //        try (InputStreamReader r = new InputStreamReader(is);
    //            BufferedReader br = new BufferedReader(r)) {
    //            String line = null;
    //            while ((line = br.readLine()) != null) {
    //                System.out.println(line);
    //            }
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //}

    //@Test
    //public void golang() throws Exception {
    //    // marshal sparse Sketch and generate files to read by golang.
    //    Sketch sk1 = new Sketch(14, true);
    //    for (int i = 1; i <= 3400; i++) {
    //        sk1.insert(String.format("flow-%d", i).getBytes());
    //    }
    //    FileUtils.writeByteArrayToFile(
    //            new File("src/test/go/testdata/sparse-14-3400.hlltc"),
    //            sk1.toBytes());

    //    // marshal dense Sketche and generate files to read by golang.
    //    Sketch sk2 = new Sketch(14, false);
    //    for (int i = 1; i <= 1000000; i++) {
    //        sk2.insert(String.format("flow-%d", i).getBytes());
    //    }
    //    FileUtils.writeByteArrayToFile(
    //            new File("src/test/go/testdata/dense-14-1M.hlltc"),
    //            sk2.toBytes());

    //    // run golang tests
    //    ProcessBuilder pb = new ProcessBuilder("go", "test", "-v", "-count=1", "./src/test/go");
    //    pb.redirectErrorStream(true);
    //    Process proc = pb.start();
    //    StreamGobbler out = new StreamGobbler(proc.getInputStream());
    //    out.start();
    //    proc.waitFor(30, TimeUnit.SECONDS);
    //    int ret = proc.exitValue();
    //    assertEquals(0, ret);
    //}

    static byte[] genSparseBytes() throws Exception {
        Sketch sk = new Sketch(14, true);
        for (int i = 1; i <= 3400; i++) {
            sk.insert(String.format("flow-%d", i).getBytes());
        }
        return sk.toBytes();
    }

    static byte[] genDenseBytes() throws Exception {
        Sketch sk = new Sketch(14, false);
        for (int i = 1; i <= 1000000; i++) {
            sk.insert(String.format("flow-%d", i).getBytes());
        }
        return sk.toBytes();
    }

    @Test
    public void reparseSparse() throws Exception {
        byte[] b = genSparseBytes();
        Sketch sk0 = Sketch.fromBytes(b);
        Sketch sk1 = Sketch.fromBytes(b);
        SketchTest.assertErrorRatio(sk0, 3400, 2);
        SketchTest.assertErrorRatio(sk1, 3400, 2);

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
    public void reparseDense() throws Exception {
        byte[] b = genDenseBytes();
        Sketch sk0 = Sketch.fromBytes(b);
        Sketch sk1 = Sketch.fromBytes(b);
        SketchTest.assertErrorRatio(sk0, 1000000, 2);
        SketchTest.assertErrorRatio(sk1, 1000000, 2);

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
    public void reparseDense25per() throws Exception {
        byte[] b = genDenseBytes();
        Sketch sk0 = Sketch.fromBytes(b);
        Sketch sk1 = Sketch.fromBytes(b);
        SketchTest.assertErrorRatio(sk0, 1000000, 2);
        SketchTest.assertErrorRatio(sk1, 1000000, 2);

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
    public void reparseDense50per() throws Exception {
        byte[] b = genDenseBytes();
        Sketch sk0 = Sketch.fromBytes(b);
        Sketch sk1 = Sketch.fromBytes(b);
        SketchTest.assertErrorRatio(sk0, 1000000, 2);
        SketchTest.assertErrorRatio(sk1, 1000000, 2);

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
    public void reparseDense75per() throws Exception {
        byte[] b = genDenseBytes();
        Sketch sk0 = Sketch.fromBytes(b);
        Sketch sk1 = Sketch.fromBytes(b);
        SketchTest.assertErrorRatio(sk0, 1000000, 2);
        SketchTest.assertErrorRatio(sk1, 1000000, 2);

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
    public void reparseDense99per() throws Exception {
        byte[] b = genDenseBytes();
        Sketch sk0 = Sketch.fromBytes(b);
        Sketch sk1 = Sketch.fromBytes(b);
        SketchTest.assertErrorRatio(sk0, 1000000, 2);
        SketchTest.assertErrorRatio(sk1, 1000000, 2);

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
