package io.p13i.glassnotes.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.p13i.glassnotes.utilities.DateUtilities;

@SuppressLint("AppCompatCustomView")
public class StatusTextView extends TextView {

    String mPageTitle = "Page Title";
    int mCurrentPageTitleStartingIndex = 0;
    String mStatus = "";
    private Timer mTimer;

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
        setTypeface(Typeface.MONOSPACE);
        setTextSize(14f);

        setText(MessageFormat.format("{0} | {1} | {2}", getShortPageTitle(mCurrentPageTitleStartingIndex), now, mStatus));

        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mCurrentPageTitleStartingIndex = mPageTitle.length() > 12 ? (mCurrentPageTitleStartingIndex + 1) % mPageTitle.length() : 0;
//                    setText(getShortPageTitle(mCurrentPageTitleStartingIndex));
                }
            }, 0, 1000);
        }
    }

    public String getShortPageTitle(int startingAtIndex) {
        final int maximumLength = 12;

        StringBuilder sb = new StringBuilder();
        sb.append(mPageTitle, startingAtIndex, Math.min(startingAtIndex + maximumLength, mPageTitle.length()));

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
