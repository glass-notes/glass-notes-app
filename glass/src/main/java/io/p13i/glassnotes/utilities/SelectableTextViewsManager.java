package io.p13i.glassnotes.utilities;

import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages text view underlining for key up and down events. Kinda magical
 */
public class SelectableTextViewsManager {
    private ViewGroup mParentViewGroup;
    private OnTextViewSelectedListener mOnTextViewSelectedListener;

    private List<TextView> mTextViews = new ArrayList<TextView>();

    public SelectableTextViewsManager(ViewGroup parentViewGroup, OnTextViewSelectedListener onTextViewSelectedListener) {
        mParentViewGroup = parentViewGroup;
        mOnTextViewSelectedListener = onTextViewSelectedListener;
    }

    public void addManagedTextViewChild(TextView textView) {

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (mTextViews.size() > 0) {
            layoutParams.addRule(RelativeLayout.BELOW, mTextViews.get(mTextViews.size() - 1).getId());
        }

        mTextViews.add(textView);
        mParentViewGroup.addView(textView);
    }

    protected void addViewChild(View view) {
        mParentViewGroup.addView(view);
    }

    public void removeViewChildAtIndex(int index) {
        View view = mParentViewGroup.getChildAt(index);
        if (view instanceof TextView && isManagingView(view)) {
            mTextViews.remove(indexOfViewInManagedViews(view));
        }
        mParentViewGroup.removeViewAt(index);
    }

    private int indexOfViewInManagedViews(View view) {
        for (int i = 0; i < mTextViews.size(); i++) {
            if (view.getId() == mTextViews.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    private boolean isManagingView(View view) {
        return indexOfViewInManagedViews(view) > -1;
    }

    public boolean handleUpRequest() {
        // Set the next element to underlined
        TextView previousTextView = (TextView) getPreviousChild(getSelectedTextView());
        if (previousTextView != null) {
            setSelectedTextView(previousTextView);
            return true;
        } else {
            return false;
        }
    }

    public boolean handleDownRequest() {
        // Set the next element to underlined
        TextView nextTextView = (TextView) getNextChild(getSelectedTextView());
        if (nextTextView != null) {
            setSelectedTextView(nextTextView);
            return true;
        } else {
            return false;
        }
    }

    public boolean handleEnter() {
        return mOnTextViewSelectedListener.onTextViewSelected(getSelectedTextView());
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

        // Add the new underline
        addUnderline(newSelectedTextView);
        mSelectedTextView = newSelectedTextView;
    }

    private TextView getSelectedTextView() {
        return mSelectedTextView;
    }

    private TextView mSelectedTextView;

    protected void init() {
        setSelectedTextView(mTextViews.get(0));
        for (int i = 1; i < mTextViews.size(); i++) {
            removeUnderline(mTextViews.get(i));
        }
    }

    public interface OnTextViewSelectedListener {

        /**
         * Handles selection of a text view
         * @param textView the selected text view
         * @return whether or not the text view was handled by this method
         */
        boolean onTextViewSelected(TextView textView);
    }
}
