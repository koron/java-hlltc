package net.kaoriya.hlltc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class Sketch {
    final static int VERSION = 1;

    final static int CAPACITY = 16;
    final static int C1 = CAPACITY - 1;
    final static int PP = 25;
    final static int MP = 1 << PP;

    private boolean sparse;

    private int p; //uint8
    private int b; //uint8
    private int m; //uint32
    private double alpha; // float64

    private HashSet<Integer> tmpSet;
    private CompressedList sparseList;
    private Registers regs;

    public Sketch() {
        this(14);
    }

    public Sketch(int precision) {
        this(precision, true);
    }

    Sketch(int precision, boolean sparse) {
        if (precision < 4 || precision > 18) {
            throw new IllegalArgumentException("p has to be >= 4 and <= 18");
        }
        this.p = precision;
        this.m = 1 << precision;
        this.alpha = Utils.alpha(m);
        if (sparse) {
            this.sparse = true;
            this.tmpSet = new HashSet<Integer>();
            this.sparseList = new CompressedList(m);
        } else {
            this.regs = new Registers(this.m);
        }
    }

    public Sketch clone() {
        Sketch sk = new Sketch(this.p);
        sk.b = this.b;
        sk.m = this.m;
        sk.alpha = this.alpha;
        if (this.tmpSet != null) {
            sk.tmpSet = new HashSet<Integer>(this.tmpSet);
        }
        if (this.sparseList != null) {
            sk.sparseList = this.sparseList.clone();
        }
        if (this.regs != null) {
            sk.regs = this.regs.clone();
        }
        return sk;
    }

    protected void mergeSparse() {
        if (this.tmpSet.size() == 0) {
            return;
        }

        int[] keys = new int[this.tmpSet.size()];
        int i = 0;
        for (int k : this.tmpSet) {
            keys[i] = k;
            i++;
        }
        Arrays.sort(keys);

        CompressedList list = new CompressedList(this.m);
        Iterator<Integer> iter = this.sparseList.iterator();
        i = 0;
outer:
        while (true) {
            if (!iter.hasNext()) {
                for (; i < keys.length; ++i) {
                    list.add(keys[i]);
                }
                break outer;
            }
            int x1 = iter.next();
            while (true) {
                if (i >= keys.length) {
                    list.add(x1);
                    while (iter.hasNext()) {
                        list.add(iter.next());
                    }
                    break outer;
                }
                int x2 = keys[i];
                if (x1 == x2) {
                    list.add(x1);
                    ++i;
                    break;
                } else if (x1 > x2) {
                    list.add(x2);
                    ++i;
                    continue;
                } else {
                    list.add(x1);
                    break;
                }
            }
        }

        this.sparseList = list;
        this.tmpSet.clear();
    }

    protected void toNormal() {
        if (this.tmpSet.size() > 0) {
            this.mergeSparse();
        }

        this.regs = new Registers(this.m);
        for (int k : this.sparseList) {
            Sparse sp = Sparse.decodeHash(k, this.p, PP);
            this.insert(sp);
        }

        this.sparse = false;
        this.tmpSet = null;
        this.sparseList = null;
    }

    protected void maybeToNormal() {
        if (this.tmpSet.size() * 100 > this.m) {
            this.mergeSparse();
            if (this.sparseList.size() > this.m) {
                this.toNormal();
            }
        }
    }

    // merges other sketch to this.
    public Sketch merge(Sketch other) {
        if (other == null) {
            return this;
        }
        if (this.p != other.p) {
            throw new IllegalStateException("precision must be equal");
        }

        if (this.sparse && other.sparse) {
            this.tmpSet.addAll(other.tmpSet);
            for (int n : other.sparseList) {
                this.tmpSet.add(n);
            }
            this.maybeToNormal();
            return this;
        }

        if (this.sparse) {
            this.toNormal();
        }

        if (other.sparse) {
            for (int k : other.tmpSet) {
                Sparse sp = Sparse.decodeHash(k, other.p, PP);
                this.insert(sp);
            }
            for (int k : other.sparseList) {
                Sparse sp = Sparse.decodeHash(k, other.p, PP);
                this.insert(sp);
            }
            return this;
        }

        Sketch cpOther = other.clone();
        if (this.b < cpOther.b) {
            this.regs.rebase(cpOther.b - this.b);
            this.b = cpOther.b;
        } else {
            cpOther.regs.rebase(this.b - cpOther.b);
            cpOther.b = this.b;
        }

        for (int i = 0; i < cpOther.regs.size(); ++i) {
            int v = cpOther.regs.get(i);
            if (v > this.regs.get(i)) {
                this.regs.set(i, v);
            }
        }

        return this;
    }

    void insert(Sparse sp) {
        if ((sp.r - this.b) >= CAPACITY) {
            // overflow
            int db = this.regs.min();
            if (db > 0) {
                this.b += db;
                this.regs.rebase(db);
            }
        }
        if (sp.r > this.b) {
            int val = Math.min(sp.r - this.b, C1);
            if (val > this.regs.get(sp.i)) {
                this.regs.set(sp.i, val);
            }
        }
    }

    public void insert(byte[] d) {
        long x = Utils.hash(d);
        this.insertHash(Utils.hash(d));
    }

    public void insertHash(long x) {
        if (this.sparse) {
            this.tmpSet.add(Sparse.encodeHash(x, this.p, PP));
            if (this.tmpSet.size() * 100 > this.m / 2) {
                this.mergeSparse();
                if (this.sparseList.size() > this.m / 2) {
                    this.toNormal();
                }
            }
            return;
        }
        Sparse sp = Sparse.getPosVal(x, this.p);
        this.insert(sp);
    }

    public long estimate() {
        if (this.sparse) {
            this.mergeSparse();
            return (long)Utils.linearCount(MP, MP - this.sparseList.count);
        }

        Registers.Sum sum = this.regs.sum(this.b);

        if (this.b != 0) {
            return (long)(this.alpha * this.m * this.m / sum.sum + 0.5);
        }

        double beta = this.p < 16 ? Utils.beta14(sum.ez) : Utils.beta16(sum.ez);
        return (long)(this.alpha * this.m * (this.m - sum.ez) / (sum.sum + beta) + 0.5);
    }

    public byte[] toBytes() throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bout)) {
            out.writeByte(VERSION);
            out.writeByte(this.p);
            out.writeByte(this.b);
            if (this.sparse) {
                out.writeByte(1);
                this.marshalSparse(out);
            } else {
                // marshal dense
                out.writeByte(0);
                this.regs.marshalTo(out);
            }
            out.flush();
            return bout.toByteArray();
        }
    }

    public static Sketch fromBytes(byte[] b) throws IOException {
        try (ByteArrayInputStream bin = new ByteArrayInputStream(b);
             DataInputStream in = new DataInputStream(bin)) {
            int version = in.readUnsignedByte();
            if (version != VERSION) {
                throw new RuntimeException("unsupported version");
            }
            int p = in.readUnsignedByte();
            Sketch sk = new Sketch(p);
            sk.b = in.readUnsignedByte();
            if (in.readByte() != 0) {
                sk.sparse = true;
                sk.unmarshalSparse(in);
            } else {
                sk.sparse = false;
                sk.tmpSet = null;
                sk.sparseList = null;
                sk.regs = Registers.unmarshalFrom(in);
            }
            return sk;
        }
    }

    private void marshalSparse(DataOutputStream out) throws IOException {
        // marshal this.tmpSet
        out.writeInt(this.tmpSet.size());
        for (int k : this.tmpSet) {
            out.writeInt(k);
        }

        this.sparseList.marshalTo(out);
    }

    private void unmarshalSparse(DataInputStream in) throws IOException {
        // unmarshal this.tmpSet
        int sz = in.readInt();
        for (int i = 0; i < sz; ++i) {
            this.tmpSet.add(in.readInt());
        }

        this.sparseList = CompressedList.unmarshalFrom(in);
    }
}
