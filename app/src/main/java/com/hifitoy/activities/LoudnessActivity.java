/*
 *   LoudnessActivity.java
 *
 *   Created by Artem Khlyupin on 10/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hifitoy.R;
import com.hifitoy.dialogsystem.KeyboardDialog;
import com.hifitoy.dialogsystem.KeyboardNumber;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Loudness;
import com.hifitoy.widgets.Slider;

public class LoudnessActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
                                                                View.OnClickListener,
                                                                KeyboardDialog.OnResultListener {
    private static final String TAG = "HiFiToy";

    private TextView    loudnessLabel;
    private Slider      loudnessSlider;
    private TextView    loudnessFreqLabel;
    private Slider      loudnessFreqSlider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loudness);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupOutlets();
    }

    private void initOutlets() {
        loudnessLabel = findViewById(R.id.loudnessLabel_outl);
        loudnessSlider = findViewById(R.id.loudnessSlider_outl);
        loudnessFreqLabel = findViewById(R.id.loudnessFreqLabel_outl);
        loudnessFreqSlider = findViewById(R.id.loudnessFreqSlider_outl);

        loudnessLabel.setOnClickListener(this);
        loudnessFreqLabel.setOnClickListener(this);

        loudnessSlider.setOnSeekBarChangeListener(this);
        loudnessFreqSlider.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setupOutlets() {
        Loudness l = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getLoudness();

        loudnessLabel.setText(l.getInfo());
        loudnessSlider.setPercent(l.getGain() / 2);
        loudnessFreqLabel.setText(l.getFreqInfo());
        loudnessFreqSlider.setPercent(l.getBiquad().getParams().getFreqPercent());
    }

    @Override
    public void onClick(View v) {
        KeyboardNumber n;
        Loudness l = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getLoudness();

        switch (v.getId()) {
            case R.id.loudnessLabel_outl:
                int perc = (int)(l.getGain() * 100);
                n = new KeyboardNumber(KeyboardNumber.NumberType.POSITIVE_INTEGER, perc);
                new KeyboardDialog(this, this, n, "loudness").show();
                break;

            case R.id.loudnessFreqLabel_outl:
                n = new KeyboardNumber(KeyboardNumber.NumberType.POSITIVE_INTEGER,
                        l.getBiquad().getParams().getFreq());
                new KeyboardDialog(this, this, n, "loudnessFreq").show();
                break;
        }
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        Loudness l = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getLoudness();

        try {
            if (tag.equals("loudness")) {
                int r = Integer.parseInt(result.getValue());

                l.setGain((float)r / 100);
                l.sendToPeripheral(true);

                setupOutlets();
            }
            if (tag.equals("loudnessFreq")) {
                int r = Integer.parseInt(result.getValue());
                if (r > 32767) r = 32767; // TODO: fix bad solution

                Biquad b = l.getBiquad();
                b.getParams().setFreq((short)r);
                b.sendToPeripheral(true);

                setupOutlets();
            }

        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;

        Loudness l = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getLoudness();

        if (seekBar.equals(loudnessSlider)){
            l.setGain(loudnessSlider.getPercent() * 2);

            loudnessLabel.setText(l.getInfo());

            l.sendToPeripheral(false);
        }

        if (seekBar.equals(loudnessFreqSlider)){
            Biquad.BiquadParam bp = l.getBiquad().getParams();

            bp.setFreqPercent(loudnessFreqSlider.getPercent());
            //round freq
            bp.setFreq(freqRound(bp.getFreq()));

            loudnessFreqLabel.setText(l.getFreqInfo());
            l.getBiquad().sendToPeripheral(false);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private short freqRound(short freq) {
        if (freq > 1000) {
            return (short)(freq / 100 * 100);
        } else if (freq > 100) {
            return (short)(freq / 10 * 10);
        }
        return freq;
    }
}
