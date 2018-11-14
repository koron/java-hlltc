package net.kaoriya.hlltc;

class Sparse {
    int i; // uint32
    int r; // uint8

    public Sparse() {
        this(0, 0);
    }

    private Sparse(int i, int r) {
        this.i = i;
        this.r = r;
    }

    static int encodeHash(long x, int p, int pp) {
        long idx = Utils.bextr(x, 64-pp, pp);
        if (Utils.bextr(x, 64-pp, pp-p) == 0) {
            long n = (Utils.bextr(x, 0, 64-pp) << pp) | ((1 << pp) - 1);
            int zeros = Long.numberOfLeadingZeros(n) + 1;
            return (((int)idx) << 7) | (zeros << 1) | 1;
        }
        return ((int)idx) << 1;
    }

    static Sparse decodeHash(int k, int p, int pp) {
        Sparse sp = new Sparse();
        sp.i = Sparse.getIndex(k, p, pp);
        if ((k & 1) != 0) {
            sp.r = (Utils.bextr32(k, 1, 6) & 0xff) + pp - p;
        } else {
            long n = (long)k << (32 - pp + p - 1);
            sp.r = Long.numberOfLeadingZeros(n) - 31;
        }
        return sp;
    }

    static Sparse getPosVal(long x, int p) {
        long i = Utils.bextr(x, 64 - p, p);
        long w = (x << p) | 1 << (p - 1);
        int rho = Long.numberOfLeadingZeros(w) + 1;
        return new Sparse((int)i, rho);
    }

    static int getIndex(int k, int p, int pp) {
        if ((k & 1) != 0) {
            return Utils.bextr32(k, 32 - p, p);
        }
        return Utils.bextr32(k, pp - p + 1, p);
    }
}
