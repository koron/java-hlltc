package net.kaoriya.hlltc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

class CompressedList implements Iterable<Integer> {
    int count; // uint32
    int last; // uint32
    ByteArrayOutputStream b;

    CompressedList(int size) {
        this.b = new ByteArrayOutputStream(size);
    }

    protected CompressedList clone() {
        CompressedList other = new CompressedList(this.b.size());
        other.count = this.count;
        other.last = this.last;
        try {
            this.b.writeTo(other.b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return other;
    }

    int size() {
        return this.b.size();
    }

    static class Iter implements Iterator<Integer> {
        byte[] b;
        int i;
        int last;

        Iter(byte[] b) {
            this.b = b;
        }

        public boolean hasNext() {
            return this.i < this.b.length;
        }

        public Integer next() {
            int x = 0;
            int j = this.i;
            for (; (this.b[j] & 0x80) != 0; ++j) {
                x |= (int)(this.b[j] & 0x7f) << ((j - i) * 7);
            }
            x |= (int)this.b[j] << ((j - i) * 7);
            this.last = x + this.last;
            return this.last;
        }
    }

    public Iterator<Integer> iterator() {
        return new Iter(this.b.toByteArray());
    }

    void add(int n) {
        ++this.count;
        this.append(n - this.last);
        this.last = n;
    }

    private void append(int x) {
        while ((x & 0xffffff80) != 0) {
            this.b.write((x & 0x7f) | 0x80);
            x >>>= 7;
        }
        this.b.write(x & 0x7f);
    }

    void marshalTo(DataOutputStream out) throws IOException {
        out.writeInt(this.b.size());
        this.b.writeTo(out);

        out.writeInt(this.count);
        out.writeInt(this.last);
    }

    static CompressedList unmarshalFrom(DataInputStream in) throws IOException {
        int size = in.readInt();

        byte[] b = new byte[size];
        in.read(b);
        CompressedList v = new CompressedList(size);
        v.b.write(b);

        v.count = in.readInt();
        v.last = in.readInt();
        return v;
    }
}
