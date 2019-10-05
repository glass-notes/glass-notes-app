package io.p13i.glassnotes.utilities;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class LimitedViewItemManagerTest extends TestCase {

    private LimitedViewItemManager<Character> mLimitedViewItemManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mLimitedViewItemManager = new LimitedViewItemManager<Character>(new ArrayList<Character>() {{
            add('a');
            add('b');
            add('c');
            add('d');
            add('e');
            add('f');
            add('g');
        }}, 3);
    }

    public void testGetVisibleItems() {
        assertEquals(Arrays.asList('a', 'b', 'c'), mLimitedViewItemManager.getVisibleItems());
    }

    public void testScrollUp() {
        mLimitedViewItemManager.scrollUp();
        assertEquals(Arrays.asList('a', 'b', 'c'), mLimitedViewItemManager.getVisibleItems());
    }

    public void testScrollDown() {
        mLimitedViewItemManager.scrollDown();
        assertEquals(Arrays.asList('b', 'c', 'd'), mLimitedViewItemManager.getVisibleItems());
    }

    public void testNextPage() {
        mLimitedViewItemManager.nextPage();
        assertEquals(Arrays.asList('d', 'e', 'f'), mLimitedViewItemManager.getVisibleItems());

        mLimitedViewItemManager.priorPage();
        mLimitedViewItemManager.scrollDown();
        mLimitedViewItemManager.nextPage();
        assertEquals(Arrays.asList('e', 'f', 'g'), mLimitedViewItemManager.getVisibleItems());
    }

    public void testPriorPage() {
        mLimitedViewItemManager.priorPage();
        assertEquals(Arrays.asList('a', 'b', 'c'), mLimitedViewItemManager.getVisibleItems());
    }
}