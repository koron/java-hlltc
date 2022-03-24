package net.kaoriya.hlltc_exp1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.io.BaseEncoding;

import net.kaoriya.hlltc.Sketch;

public class Example {

    private static final BaseEncoding BASE64 = BaseEncoding.base64();

    static double estimateError(long got, long exp) {
        long delta = Math.abs(got - exp);
        //long delta = got - exp;
        return (double)delta / (double)exp;
    }

    public static void main(final String[] args) throws Exception {
        final Path file = Paths.get(Example.class.getResource("example.sketch").toURI());
        final List<String> lines = Files.readAllLines(file);

        // マージする順番が変わると結果も変わる
        // Collections.shuffle(lines);

        final Sketch mergedSketch = new Sketch();
        long estimatedSummary = 0;

        final Sketch denseSketch = new Sketch();
        final Sketch sparseSketch = new Sketch();
        long denseSum = 0;
        long sparseSum = 0;
        int x = 0;
        double prevErr = 0;

        for (final String line : lines) {
            final Sketch sketch = Sketch.fromBytes(BASE64.decode(line));
            long est = sketch.estimate();
            estimatedSummary += est;
            mergedSketch.merge(sketch);
            if (sketch.getSparse()) {
                sparseSketch.merge(sketch);
                sparseSum += est;
            } else {
                denseSketch.merge(sketch);
                denseSum += est;
            }
            long curr = mergedSketch.clone().estimate();
            double err = estimateError(curr, estimatedSummary);
            ++x;
            System.out.println(String.format("#%-3d %,10d %s P:%d E:%f ED:%f", x, est, (sketch.getSparse() ? "S" : "D"), sketch.getPercision(), err, Math.abs(prevErr - err)));
            prevErr = err;
        }
        System.out.println(String.format("     estimatedSummary:%,d", estimatedSummary));
        //      estimatedSummary:2,154,967
        System.out.println(String.format("estimatedMergedSketch:%,d", mergedSketch.estimate()));
        // estimatedMergedSketch:6,293,092

        //System.out.println(String.format("sparseSum:  %,10d", sparseSum));
        //System.out.println(String.format("sparse.est: %,10d", sparseSketch.clone().estimate()));
        //System.out.println(String.format("denseSum:  %,10d", denseSum));
        //System.out.println(String.format("dense.est: %,10d", denseSketch.clone().estimate()));

        //System.out.println("sparse.dump=" + sparseSketch.dumpString());
        //System.out.println("clone(sparse).dump=" + sparseSketch.clone().dumpString());


        //long dsEst = denseSketch.clone().merge(sparseSketch).estimate();
        //System.out.println(String.format("(dense+sparse).est:  %,10d", dsEst));

        //long sdEst = sparseSketch.clone().merge(denseSketch).estimate();
        //System.out.println(String.format("(sparse+dense).est:  %,10d", sdEst));

        //System.out.println(String.format("normal(sparse).est:  %,10d", sparseSketch.convertNormal().estimate()));
    }
}
