package net.kaoriya.hlltc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import util.hash.MetroHash;
import util.hash.MetroHash128;

public class MinHashSketch {
    final static int VERSION = 1;

    final static int P = 14;
    final static int M = 1 << P;
    final static int MAX = 64 - P;
    final static long MAXX = (1 << MAX) - 1;
    final static double ALPHA = 0.7213 / (1 + 1.079 / (double)(M));
    final static int Q = 6;
    final static int R = 10;
    final static int _2Q = 1 << Q;
    final static int _2R = 1 << R;
    final static double C = 0.169919487159739093975315012348;

    static class Reg {
        byte lz;
        short sig;

        Reg() {}
        Reg(Reg r) {
            lz = r.lz;
            sig = r.sig;
        }
        Reg(long x, long y) {
            byte lz = (byte)(Long.numberOfLeadingZeros((x << P)^MAXX) + 1);
            short sig = (short)(y & ((1 << R) - 1));
            this.lz = lz;
            this.sig = sig;
        }
        boolean lessThan(Reg r) {
            return lz < r.lz || (lz == r.lz && sig < r.sig);
        }
        void merge(Reg r) {
            if (lessThan(r)) {
                lz = r.lz;
                sig = r.sig;
            }
        }
        boolean isZero() {
            return lz == 0 && sig == 0;
        }
        boolean isEqual(Reg r) {
            return lz == r.lz && sig == r.sig;
        }
        int toShort() {
            return (int)lz << (16 - Q) | (int)sig;
        }
        void setShort(int v) {
            lz = (byte)((v >> (16 - Q)) & ((1 << Q) - 1));
            sig = (short)(v & ((1 << R) - 1));
        }
    }

    final Reg[] regs;

    public MinHashSketch() {
        regs = new Reg[M];
        for (int i = 0; i < regs.length; i++) {
            regs[i] = new Reg();
        }
    }

    MinHashSketch(MinHashSketch other) {
        regs = new Reg[M];
        for (int i = 0; i < regs.length; i++) {
            regs[i] = new Reg(other.regs[i]);
        }
    }

    public MinHashSketch clone() {
        return new MinHashSketch(this);
    }

    static double beta(double ez) {
        return Utils.beta14(ez);
    }

    static class SumAndZeros {
        double sum;
        int zeros;
    }

    SumAndZeros regSumAndZeros() {
        SumAndZeros sum = new SumAndZeros();
        for (Reg r : regs) {
            if (r.lz == 0) {
                sum.zeros++;
            }
            sum.sum += 1 / Math.pow(2f, (double)(r.lz));
        }
        return sum;
    }

    /**
     * addHash takes in a "hashed" value (bring your own hashing).
     */
    public void addHash(long x, long y) {
        int k = (int)(x >>> MAX);
        Reg r = new Reg(x, y);
        if (regs[k].lessThan(r)) {
            regs[k] = r;
        }
    }

    /** add inserts a value into the sketch. */
    public void add(byte[] value) {
        MetroHash128 h = MetroHash.hash128(1337, value);
        addHash(h.getHigh(), h.getLow());
    }

    /**
     * cardinality returns the number of unique elements added to the sketch.
     */
    public long cardinality() {
        SumAndZeros sum = regSumAndZeros();
        double m = (double)M;
	return (long)(ALPHA * m * (m - sum.zeros) / (beta(sum.zeros) + sum.sum));
    }

    /**
     * merge merges other hash, and modify this.
     * If you want keep original clone first.
     */
    public void merge(MinHashSketch other) {
        for (int i = 0; i < regs.length; i++) {
            regs[i].merge(other.regs[i]);
        }
    }

    public double similarity(MinHashSketch other) {
        double cc = 0, nn = 0;
        for (int i = 0; i < regs.length; i++) {
            if (!regs[i].isZero() && regs[i].isEqual(other.regs[i])) {
                cc++;
            }
            if (!regs[i].isZero() || !other.regs[i].isZero()) {
                nn++;
            }
        }
        if (cc == 0) {
            return 0;
        }

        double n = (double)cardinality();
        double m = (double)other.cardinality();
        double ec = approximateExpectedCollisions(n, m);

	//FIXME: must be a better way to predetect this
	if (cc < ec) {
            return 0;
	}
	return (cc - ec) / nn;
    }

    static double approximateExpectedCollisions(double n, double m) {
        if (n < m) {
            double tmp = n;
            n = m;
            m = tmp;
        }
        if (n > Math.pow(2, Math.pow(2, Q) + R)) {
            return Long.MAX_VALUE;
        } else if (n > Math.pow(2, P + 5)) {
            double d = ( 4 * n / m) / Math.pow((1 + n) / m, 2);
            return C * Math.pow(2, P - R) * d + 0.5;
        }
        return expectedCollision(n, m) / (double)P;
    }

    static double expectedCollision(double n, double m) {
        double x = 0, b1 = 0, b2 = 0;
        for (int i = 0; i <= _2Q; i++) {
            for (int j = 0; j <= _2R; j++) {
                if (i != _2Q) {
                    double den = Math.pow(2, P + R + i);
                    b1 = (_2R + j) / den;
                    b2 = (_2R + j + 1) / den;
                } else {
                    double den = Math.pow(2, P + R + i - 1);
                    b1 = j / den;
                    b2 = (j + 1) / den;
                }
                double prx = Math.pow(1 - b2, n) - Math.pow(1 - b1, n);
                double pry = Math.pow(1 - b2, m) - Math.pow(1 - b1, m);
                x += prx * pry;
            }
        }
        return (x * (double)P) + 0.5;
    }

    /** intersection returns number of intersections between sk and other. */
    public long intersection(MinHashSketch other) {
        double sim = similarity(other);
        MinHashSketch thiz = clone();
        thiz.merge(other);
        return (long)(sim * (double)(thiz.cardinality()) + 0.5);
    }

    public byte[] toBytes() throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bout))
        {
            out.writeByte(VERSION);
            out.writeByte((byte)P);
            out.writeByte((byte)Q);
            out.writeByte((byte)R);
            out.writeInt(regs.length);
            for (Reg r : regs) {
                out.writeShort(r.toShort());
            }
            out.flush();
            return bout.toByteArray();
        }
    }

    public static MinHashSketch fromBytes(byte[] b) throws IOException {
        try (ByteArrayInputStream bin = new ByteArrayInputStream(b);
             DataInputStream in = new DataInputStream(bin))
        {
            int version = in.readUnsignedByte();
            if (version != VERSION) {
                throw new IOException("unsupported version");
            }
            int p = in.readUnsignedByte();
            int q = in.readUnsignedByte();
            int r = in.readUnsignedByte();
            if (p != P || q != Q || r != R) {
                throw new IOException("unsupported P/Q/R");
            }
            int sz = in.readInt();
            if (sz != M) {
                throw new IOException("illegal register size");
            }
            MinHashSketch sk = new MinHashSketch();
            for (int i = 0; i < sk.regs.length; i++) {
                sk.regs[i].setShort(in.readShort());
            }
            return sk;
        }
    }
}
