package net.kaoriya.hlltc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

class Registers {
    byte[] tailcuts;
    int nz;

    // TODO: check consistency of "nz" member.

    static class Sum {
        double sum;
        int ez;
    }

    Registers(int size) {
        this.tailcuts = new byte[size];
        this.nz = size;
    }

    protected Registers clone() {
        Registers r = new Registers(this.nz);
        for (int i = 0; i <  r.tailcuts.length; ++i) {
            r.tailcuts[i] = this.tailcuts[i];
        }
        return r;
    }

    void rebase(int delta) {
        int nz = this.tailcuts.length;
        for (int i = 0; i < this.tailcuts.length; ++i) {
            int v = this.get(i);
            if (v >= delta) {
                v -= delta;
                this.set(i, v);
                if (v > 0) {
                    --nz;
                }
            }
        }
        this.nz = nz;
    }

    int get(int off) {
        return this.tailcuts[off];
    }

    void set(int off, int val) {
        val &= 0x0f;
        if (this.tailcuts[off] == 0 && val != 0) {
            //--this.nz;
        }
        this.tailcuts[off] = (byte)val;
    }

    Sum sum(int base) {
        Sum res = new Sum();
        for (int r : this.tailcuts) {
            int v = base + r;
            if (v == 0) {
                ++res.ez;
            }
            res.sum += 1.0 / Math.pow(2.0, v);
        }
        return res;
    }

    int min() {
        if (this.nz > 0) {
            return 0;
        }
        int min = 16;
        for (int r : this.tailcuts) {
            if (r == 0) {
                return 0;
            }
            if (r < min) {
                min = r;
            }
        }
        return min;
    }

    int size() {
        return this.tailcuts.length;
    }

    void marshalTo(DataOutputStream out) throws IOException {
        out.writeInt(this.tailcuts.length / 2);
        for (int i = 0; i < this.tailcuts.length; i += 2) {
            out.writeByte((this.tailcuts[i] << 4) | this.tailcuts[i+1]);
        }
    }

    static Registers unmarshalFrom(DataInputStream in) throws IOException {
        int sz = in.readInt();
        byte[] data = new byte[sz];
        in.read(data);

        Registers regs = new Registers(sz * 2);
        for (int i = 0; i < sz; i++) {
            byte b = data[i];
            regs.set(i, b >>> 4);
            regs.set(i+1, b & 0x0f);
        }
        return regs;
    }
}
