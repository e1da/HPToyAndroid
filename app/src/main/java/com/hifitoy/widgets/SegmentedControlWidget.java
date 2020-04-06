/*
 *   SegmentedControlWidget.java
 *
 *   Created by Artem Khlyupin on 04/04/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import com.hifitoy.R;

public class SegmentedControlWidget extends LinearLayout implements View.OnClickListener {
    private SegmentedButton[] buttons;
    private int checkedIndex = 0;
    private OnCheckedChangeListener onCheckedChangeListener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(SegmentedControlWidget segmentedControl, int checkedIndex);
    }

    public SegmentedControlWidget(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
    }

    public SegmentedControlWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        TypedArray bb = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SegmentedControlWidget, 0, 0);
        CharSequence[] entries;

        try {
            entries = bb.getTextArray(R.styleable.SegmentedControlWidget_android_entries);
        } finally {
            bb.recycle();
        }

        init(context, entries);
    }

    private void init(Context context, CharSequence[] entries) {
        setOrientation(HORIZONTAL);

        if (entries != null) {
            buttons = new SegmentedButton[entries.length];
            for (int i = 0; i < entries.length; i++) {
                buttons[i] = new SegmentedButton(context, entries[i]);
                if (i == checkedIndex) buttons[i].setChecked(true);

                buttons[i].setOnClickListener(this);
                addView(buttons[i]);
            }
        }
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < buttons.length; i++) {
            if ( (buttons[i] == v) && (check(i)) ) {

                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(this, checkedIndex);
                }

                break;
            }
        }
    }

    public int getCheckedIndex() {
        return checkedIndex;
    }

    public SegmentedButton getButton(int index) {
        if ((index < 0) || (index >= buttons.length)) return null;
        return buttons[index];

    }
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    public boolean check(int index) {
        if ((index == checkedIndex) || (getButton(index) == null)) return false;

        SegmentedButton oldChecked = getButton(checkedIndex);
        if (oldChecked != null) oldChecked.setChecked(false);

        getButton(index).setChecked(true);
        checkedIndex = index;

        return true;
    }

    @SuppressLint("AppCompatCustomView")
    class SegmentedButton extends RadioButton {

        public SegmentedButton(Context context) {
            super(context);

            setTextAlignment(TEXT_ALIGNMENT_CENTER);
            setButtonDrawable(null);
            setChecked(false);

            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
            lp.setMargins(0, 0 , 0, 0);
            setLayoutParams(lp);

            setTextSize(12);
        }

        public SegmentedButton(Context context, CharSequence text) {
            this(context);
            setText(text);
        }

        @Override
        public void setChecked(boolean checked) {
            super.setChecked(checked);

            if (checked) {
                setBackgroundResource(R.drawable.segmented_checked);
            } else {
                setBackgroundResource(R.drawable.segmented_unchecked);
            }
        }

    }
}
