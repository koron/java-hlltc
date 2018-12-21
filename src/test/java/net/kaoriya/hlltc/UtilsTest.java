package net.kaoriya.hlltc;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

public class UtilsTest {

    void hashCheck(String k, long exp) throws Exception {
        assertEquals(exp, Utils.hash(k.getBytes()));
    }

    @Test
    public void hash() throws Exception {
        hashCheck("flow-0", 0xa98bb970027f249cL);
        hashCheck("flow-1", 0xfab9d75072031069L);
        hashCheck("flow-2", 0x71a315436bafa2d2L);
        hashCheck("flow-3", 0xa6e68aeb3b159d3bL);
        hashCheck("flow-4", 0x05221b6f207396ddL);
        hashCheck("flow-5", 0x5781ff74050e9f42L);
        hashCheck("flow-6", 0x57a892a8c1873490L);
        hashCheck("flow-7", 0x842e865e1e91cdd1L);
        hashCheck("flow-8", 0x7126026e12357900L);
        hashCheck("flow-9", 0x6d78fb88c1de8b08L);
    }

    @Test
    public void sortUnsigned() {
        int[] a1 = new int[]{ 3, 5, 1, 4, 2 };
        assertArrayEquals(
                new int[]{ 1, 2, 3, 4, 5 },
                Utils.sortUnsigned(a1));

        int[] a2 = new int[]{ 0, 2, -2, 1, -1 };
        assertArrayEquals(
                new int[]{ 0, 1, 2, -2, -1 },
                Utils.sortUnsigned(a2));

        int[] a3 = new int[]{ -1, 1, -3, 0, -2 };
        assertArrayEquals(
                new int[]{ 0, 1, -3, -2, -1 },
                Utils.sortUnsigned(a3));
    }
}
