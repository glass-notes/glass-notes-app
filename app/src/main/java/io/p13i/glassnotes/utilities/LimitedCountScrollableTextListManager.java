package io.p13i.glassnotes.utilities;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class LimitedCountScrollableTextListManager<T> {
    public final static String TAG = LimitedCountScrollableTextListManager.class.getName();
    List<T> mAllStrings;
    int mMaximumCount;
    int mCurrentStartIndex = 0;

    public LimitedCountScrollableTextListManager(List<T> allStrings, int maximumCount) {
        mAllStrings = allStrings;
        mMaximumCount = maximumCount;
    }

    public List<T> getVisibleStrings() {
        List<T> visibleStrings = new LinkedList<>();
        int currentStringIndex = mCurrentStartIndex;
        while (currentStringIndex < mAllStrings.size() && visibleStrings.size() < mMaximumCount) {
            visibleStrings.add(mAllStrings.get(currentStringIndex));
            currentStringIndex++;
        }
        return visibleStrings;
    }

    public void scrollUp() {
        mCurrentStartIndex = Math.max(mCurrentStartIndex - 1, 0);
    }

    public void scrollDown() {
        mCurrentStartIndex = Math.min(mCurrentStartIndex + 1, mAllStrings.size() - 1);
    }
}
