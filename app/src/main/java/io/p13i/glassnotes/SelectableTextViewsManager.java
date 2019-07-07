package io.p13i.glassnotes;

import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class SelectableTextViewsManager {
    private ViewGroup mParentViewGroup;

    private List<TextView> mTextViews = new ArrayList<>();

    SelectableTextViewsManager(ViewGroup parentViewGroup) {
        mParentViewGroup = parentViewGroup;
    }

    void addChild(TextView textView) {

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (mTextViews.size() > 0) {
            layoutParams.addRule(RelativeLayout.BELOW, mTextViews.get(mTextViews.size() - 1).getId());
        }

        mTextViews.add(textView);
        mParentViewGroup.addView(textView);
    }


    private void handleKeypadDpadDown() {
        // Set the next element to underlined
        TextView nextTextView = (TextView) getNextChild(mParentViewGroup, getSelectedTextView());
        if (nextTextView != null) {
            setSelectedTextView(nextTextView);
        }
    }

    private void handleKeypadDpadUp() {
        // Set the next element to underlined
        TextView previousTextView = (TextView) getPreviousChild(mParentViewGroup, getSelectedTextView());
        if (previousTextView != null) {
            setSelectedTextView(previousTextView);
        }
    }

    boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                handleKeypadDpadDown();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                handleKeypadDpadUp();
                return true;
            default:
                return false;
        }
    }

    private View getNextChild(ViewGroup inViewGroup, View afterView) {
        for (int i = 0; i < inViewGroup.getChildCount() - 1; i++) {
            if (inViewGroup.getChildAt(i).getId() == afterView.getId()) {
                return inViewGroup.getChildAt(i + 1);
            }
        }
        return null;
    }

    private View getPreviousChild(ViewGroup inViewGroup, View afterView) {
        for (int i = 1; i < inViewGroup.getChildCount(); i++) {
            if (inViewGroup.getChildAt(i).getId() == afterView.getId()) {
                return inViewGroup.getChildAt(i - 1);
            }
        }
        return null;
    }

    private void addUnderline(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void removeUnderline(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() & ~(Paint.UNDERLINE_TEXT_FLAG));
    }


    private void setSelectedTextView(TextView newSelectedTextView) {
        if (mSelectedTextView != null) {
            removeUnderline(mSelectedTextView);
        }
        addUnderline(mSelectedTextView = newSelectedTextView);
    }

    private TextView getSelectedTextView() {
        return mSelectedTextView;
    }

    private TextView mSelectedTextView;

    void init() {
        setSelectedTextView(mTextViews.get(0));
        for (int i = 1; i < mTextViews.size(); i++) {
            removeUnderline(mTextViews.get(i));
        }
    }
}
