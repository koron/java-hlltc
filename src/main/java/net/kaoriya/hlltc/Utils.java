package net.kaoriya.hlltc;

import java.util.Arrays;

import util.hash.MetroHash;

class Utils {
    static long bextr(long v, int start, int len) {
        return (v >>> start) & ((1L << len) - 1);
    }

    static int bextr32(int v, int start, int len) {
        return (v >>> start) & ((1 << len) - 1);
    }

    static long hash(byte[] d) {
        return MetroHash.hash64(1337, d).get();
    }

    static double linearCount(int m, int v) {
        return m * Math.log((double)m / v);
    }

    static double beta14(double ez) {
        double zl = Math.log1p(ez);
        return -0.370393911 * ez +
            0.070471823 * zl +
            0.17393686 * Math.pow(zl, 2) +
            0.16339839 * Math.pow(zl, 3) +
            -0.09237745 * Math.pow(zl, 4) +
            0.03738027 * Math.pow(zl, 5) +
            -0.005384159 * Math.pow(zl, 6) +
            0.00042419 * Math.pow(zl, 7);
    }

    static double beta16(double ez) {
        double zl = Math.log1p(ez);
        return -0.37331876643753059 * ez +
            -1.41704077448122989 * zl +
            0.40729184796612533 * Math.pow(zl, 2) +
            1.56152033906584164 * Math.pow(zl, 3) +
            -0.99242233534286128 * Math.pow(zl, 4) +
            0.26064681399483092 * Math.pow(zl, 5) +
            -0.03053811369682807 * Math.pow(zl, 6) +
            0.00155770210179105 * Math.pow(zl, 7);
    }

    static double alpha(int m) {
        switch (m) {
            case 16:
                return 0.673;
            case 32:
                return 0.697;
            case 64:
                return 0.709;
        }
	return 0.7213 / (1 + 1.079/(double)m);
    }

    static int[] sortUnsigned(int[] a) {
        final int end = a.length;
        Arrays.sort(a);
        int n = 0;
        for (; n < end; ++n) {
            if (a[n] >= 0) {
                break;
            }
        }
        if (n == 0 || n == end) {
            return a;
        }
        final int m = end - n;
        if (n <  end / 2) {
            int[] tmp = Arrays.copyOfRange(a, 0, n);
            for (int i = 0; i < m; ++i) {
                a[i] = a[i + n];
            }
            for (int i = 0; i < n; ++i) {
                a[i + m] = tmp[i];
            }
        } else {
            int[] tmp = Arrays.copyOfRange(a, n, end);
            for (int i = n - 1; i >= 0; --i) {
                a[i + m] = a[i];
            }
            for (int i = 0; i < m; ++i) {
                a[i] = tmp[i];
            }
        }
        return a;
    }
}
