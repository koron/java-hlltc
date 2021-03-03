package example;

import net.kaoriya.hlltc.MinHashSketch;

import java.util.HashSet;
import java.util.Random;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        cardinality();
    }

    static double estimateError(long got, long exp) {
        long delta = Math.abs(got - exp);
        return (double)delta / (double)exp;
    }

    final static byte[] letterBytes = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").getBytes();

    static byte[] randBytes(Random rnd, int n) {
        byte[] d = new byte[n];
        for (int i = 0; i < n; i++) {
            d[i] = letterBytes[rnd.nextInt(letterBytes.length)];
        }
        return d;
    }

    public static void cardinality() {
        MinHashSketch sk = new MinHashSketch();
        int step = 10000;
        HashSet<String> unique = new HashSet<>();
        Random rnd = new Random();
        for (int i = 0; unique.size() <= 1000000; i++) {
            byte[] d = randBytes(rnd, rnd.nextInt(32));
            sk.add(d);
            unique.add(new String(d));
            if (unique.size() % step == 0) {
                long exact = (long)unique.size();
                long res = sk.cardinality();
                double ratio = 100 * estimateError(res, exact);
                System.out.println(String.format("exact %d, got %d which is %.2f%% error", exact, res, ratio));
                step *= 10;
            }
        }
    }
}
