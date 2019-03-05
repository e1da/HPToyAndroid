/*
 *   AudioSourceWidget.java
 *
 *   Created by Artem Khlyupin on 03/03/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.hifitoy.hifitoydevice.AudioSource;
import com.hifitoy.R;

public class AudioSourceWidget extends RadioGroup implements RadioButton.OnCheckedChangeListener {
    private OnCheckedListener listener;
    private RadioButton[] sourceButtons;
    private byte state;

    public AudioSourceWidget(Context context) {
        super(context);
        init(context);
    }

    public AudioSourceWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);

        sourceButtons = new RadioButton[3];
        sourceButtons[0] = initButton(context, "S/PDIF");
        sourceButtons[1] = initButton(context, "USB");
        sourceButtons[2] = initButton(context, "BT");

        setState(AudioSource.USB_SOURCE);

        enabledCheckedListener(true);
    }

    private RadioButton initButton(Context context, CharSequence text) {
        RadioButton button = new RadioButton(context);
        button.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        button.setText(text);
        button.setButtonDrawable(null);
        button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));
        addView(button);

        return button;
    }

    private void clearSourceButtons() {
        sourceButtons[0].setChecked(false);
        sourceButtons[1].setChecked(false);
        sourceButtons[2].setChecked(false);

        sourceButtons[0].setBackgroundResource(R.drawable.unchecked_button);
        sourceButtons[1].setBackgroundResource(R.drawable.unchecked_button);
        sourceButtons[2].setBackgroundResource(R.drawable.unchecked_button);
    }

    private void enabledCheckedListener(boolean enabled) {
        for (int i = 0; i < 3; i++) {
            if (enabled) {
                sourceButtons[i].setOnCheckedChangeListener(this);
            } else {
                sourceButtons[i].setOnCheckedChangeListener(null);
            }
        }
    }

    public void setState(byte state) {
        if (state > AudioSource.BT_SOURCE) state = AudioSource.BT_SOURCE;
        if (state < AudioSource.SPDIF_SOURCE) state = AudioSource.SPDIF_SOURCE;

        this.state = state;

        enabledCheckedListener(false);

        clearSourceButtons();
        sourceButtons[state].setChecked(true);
        sourceButtons[state].setBackgroundResource(R.drawable.checked_button);

        enabledCheckedListener(true);

        invalidate();
    }

    public byte getState() {
        return state;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) return;

        for (byte i = 0; i < 3; i++) {
            if (buttonView == sourceButtons[i]) {
                setState(i);
                break;
            }
        }

        //send callback
        if (listener != null) listener.onCheckedChanged(state);
    }

    public void setOnCheckedListener(OnCheckedListener listener) {
        this.listener = listener;
    }

    public interface OnCheckedListener {
        void onCheckedChanged(byte state);
    }
}
