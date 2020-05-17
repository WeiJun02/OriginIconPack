package com.origin.icon.pack.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.origin.icon.pack.R;

public class SplitButtonsLayout extends LinearLayout {

    private int mButtonCount;

    public SplitButtonsLayout(Context context) {
        super(context);
        init();
    }

    public SplitButtonsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SplitButtonsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        if (isInEditMode()) {
            mButtonCount = 2;
            addButton("Website", "https://bubbble.org/");
            addButton("Github+", "https://github.com/hujincan");
        }
    }

    /**
     * Sets how many buttons the layout will have.
     */
    public void setButtonCount(int buttonCount) {
        this.mButtonCount = buttonCount;
        setWeightSum(buttonCount);
    }

    public void addButton(String text, String link) {
        if (getChildCount() == mButtonCount)
            throw new IllegalStateException(mButtonCount + " buttons have already been added.");
        final Button newButton = (Button) LayoutInflater.from(getContext())
                .inflate(R.layout.item_credits_button, this, false);
        // width can be 0 since weight is used
        final LinearLayout.LayoutParams lp = new LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        newButton.setText(text);
        newButton.setTag(link);
        addView(newButton, lp);
    }

    public boolean hasAllButtons() {
        return getChildCount() == mButtonCount;
    }
}