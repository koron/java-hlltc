package net.kaoriya.hlltc;

import org.junit.Test;
import java.util.Iterator;

import static org.junit.Assert.*;

public class CompressedListTest {

    @Test
    public void iterator() {
        CompressedList list = new CompressedList(0);
        list.add(1);
        list.add(2);
        list.add(3);

        assertEquals(3, list.size());

        Iterator<Integer> iter = list.iterator();
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(1), iter.next());
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(2), iter.next());
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(3), iter.next());
        assertFalse(iter.hasNext());

    }
}
