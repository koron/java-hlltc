package net.kaoriya.hlltc;

import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.*;

public class RegistersTest {

    @Test
    public void getSet() {
        final int L = 16777216;
        byte[] data = new byte[L];
        Registers regs = new Registers(L);

        Random rnd = new Random();
        for (int i = 0; i < L; i++) {
            int val = rnd.nextInt(16);
            regs.set(i, val);
            data[i] = (byte)val;
        }

        for (int i = 0; i < L; i++) {
            int got = regs.get(i);
            int exp = (int)data[i] & 0xff;
            assertEquals("get() should keep value", exp, got);
        }
    }

    @Test
    public void zeros() {
        final int L = 8;
        Registers regs = new Registers(L);

        for (int i = 0; i < L; ++i) {
            regs.set(i, (i % 15) + 1);
        }
        for (int i = 0; i < L; ++i) {
            int exp = (i % 15) + 1;
            int got = regs.get(i);
            assertEquals("get() should keep value", exp, got);
        }

        regs.rebase(1);

        for (int i = 0; i < L; ++i) {
            int exp = i % 15;
            int got = regs.get(i);
            assertEquals("get() should keep value", exp, got);
        }

        assertEquals("nz doesn't match", 1, regs.nz);
    }
}
