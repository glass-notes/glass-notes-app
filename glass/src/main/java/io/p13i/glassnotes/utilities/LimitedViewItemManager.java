package io.p13i.glassnotes.utilities;

import java.util.LinkedList;
import java.util.List;

public class LimitedViewItemManager<T> {
    public final static String TAG = LimitedViewItemManager.class.getName();
    List<T> mAllStrings;
    int mMaximumCount;
    int mCurrentStartIndex = 0;

    public LimitedViewItemManager(List<T> allStrings, int maximumCount) {
        mAllStrings = allStrings;
        mMaximumCount = maximumCount;
    }

    public List<T> getVisibleItems() {
        List<T> visibleItems = new LinkedList<T>();
        int currentStringIndex = mCurrentStartIndex;
        while (currentStringIndex < mAllStrings.size() && visibleItems.size() < mMaximumCount) {
            visibleItems.add(mAllStrings.get(currentStringIndex));
            currentStringIndex++;
        }
        return visibleItems;
    }

    public void scrollUp() {
        mCurrentStartIndex = Math.max(mCurrentStartIndex - 1, 0);
    }

    public void scrollDown() {
        mCurrentStartIndex = Math.min(mCurrentStartIndex + 1, mAllStrings.size() - 1);
    }
}
