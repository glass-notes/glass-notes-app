package io.p13i.glassnotes.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Date;

import io.p13i.glassnotes.utilities.DateUtilities;

@SuppressLint("AppCompatCustomView")
public class StatusTextView extends TextView {

    String mPageTitle = "Page Title";
    int mCurrentPageTitleStartingIndex = 0;
    String mStatus = "";
    private Date mPreviousPageTitleUpdateDate = DateUtilities.now()
            ;

    public StatusTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public StatusTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // EEE, MMM dd, yyyy @
        String now = DateUtilities.nowAs("KK:mm:ss a");

        setText(getShortPageTitle(mCurrentPageTitleStartingIndex) + " | " + now + " | " + mStatus);

        if (DateUtilities.now().getSeconds() > mPreviousPageTitleUpdateDate.getSeconds()) {
            mCurrentPageTitleStartingIndex = (mCurrentPageTitleStartingIndex + 1) % mPageTitle.length();
            mPreviousPageTitleUpdateDate = DateUtilities.now();
        }
    }

    public String getShortPageTitle(int startingAtIndex) {
        final int maximumLength = 12;

        StringBuilder sb = new StringBuilder();
        sb.append(mPageTitle.substring(startingAtIndex, Math.min(startingAtIndex + maximumLength, mPageTitle.length())));

        while (sb.length() < maximumLength) {
            sb.append(" ");
        }

        return sb.toString();
    }

    public void setPageTitle(String pageTitle) {
        mPageTitle = pageTitle;
    }

    public void setStatus(String status) {
        mStatus = status;
    }
}
