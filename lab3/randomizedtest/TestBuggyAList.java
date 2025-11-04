package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import net.sf.saxon.om.Item;
import org.junit.Test;
import timingtest.AList;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> a = new AListNoResizing<>();
        BuggyAList<Integer> b = new BuggyAList<>();
        for (int x = 4; x <= 6; x += 1) {
            a.addLast(x);
            b.addLast(x);
        }
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i += 1){
            assertEquals(a.removeLast(),b.removeLast());
        }
    }
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> b = new BuggyAList<>();
        int N = 50000;
        for (int i = 0; i < N; i += 1) {
            int operation = StdRandom.uniform(0, 3);
            if (operation == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                b.addLast(randVal);
            } else if (operation == 1) {
                // size
                int size = L.size();
                int b_size = b.size();
                assertEquals(size, b_size);
            } else if (operation == 2) {
                int size = L.size();
                if (size > 0) {
                    assertEquals(L.getLast(), b.getLast());
                    assertEquals(L.removeLast(), b.removeLast());
                }
            }
        }
    }
}
