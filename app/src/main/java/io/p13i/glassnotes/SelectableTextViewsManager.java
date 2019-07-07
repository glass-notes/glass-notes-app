package io.p13i.glassnotes;

import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

class SelectableTextViewsManager {
    private ViewGroup mParentViewGroup;
    private OnTextViewSelectedListener mOnTextViewSelectedListener;

    private List<TextView> mTextViews = new ArrayList<>();

    SelectableTextViewsManager(ViewGroup parentViewGroup, SelectableTextViewsManager.OnTextViewSelectedListener onTextViewSelectedListener) {
        mParentViewGroup = parentViewGroup;
        mOnTextViewSelectedListener = onTextViewSelectedListener;
    }

    void addManagedTextViewChild(TextView textView) {

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (mTextViews.size() > 0) {
            layoutParams.addRule(RelativeLayout.BELOW, mTextViews.get(mTextViews.size() - 1).getId());
        }

        mTextViews.add(textView);
        mParentViewGroup.addView(textView);
    }

    void addViewChild(View view) {
        mParentViewGroup.addView(view);
    }


    public void handleKeypadDpadDown() {
        // Set the next element to underlined
        TextView nextTextView = (TextView) getNextChild(getSelectedTextView());
        if (nextTextView != null) {
            setSelectedTextView(nextTextView);
        }
    }

    public void handleKeypadDpadUp() {
        // Set the next element to underlined
        TextView previousTextView = (TextView) getPreviousChild(getSelectedTextView());
        if (previousTextView != null) {
            setSelectedTextView(previousTextView);
        }
    }

    public void handleEnter() {
        mOnTextViewSelectedListener.onTextViewSelected(getSelectedTextView());
    }

    private boolean managingTextView(View textView) {
        for (TextView tv : mTextViews) {
            if (tv.getId() == textView.getId()) {
                return true;
            }
        }
        return false;
    }

    private View getNextChild(View afterView) {
        for (int i = 0; i < mTextViews.size() - 1; i++) {
            if (mTextViews.get(i).getId() == afterView.getId()) {
                return mTextViews.get(i + 1);
            }
        }
        return null;
    }

    private View getPreviousChild(View afterView) {
        for (int i = 1; i < mTextViews.size(); i++) {
            if (mTextViews.get(i).getId() == afterView.getId()) {
                return mTextViews.get(i - 1);
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

    interface OnTextViewSelectedListener {
        void onTextViewSelected(TextView textView);
    }
}
