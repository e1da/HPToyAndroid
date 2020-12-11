/*
 *   BassTrebleActivity.java
 *
 *   Created by Artem Khlyupin on 09/12/20
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
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel;
import com.hifitoy.widgets.Slider;

import java.util.Locale;

public class BassActivity extends BaseActivity implements KeyboardDialog.OnResultListener {
    private static final String TAG = "HiFiToy";

    private TextView    bassLabel;
    private Slider      bassSlider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Bass");
        setContentView(R.layout.activity_bass);

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
        bassLabel = findViewById(R.id.bassLabel_outl);
        bassSlider = findViewById(R.id.bassSlider_outl);

        bassLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HiFiToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
                BassTrebleChannel bt = preset.getBassTreble().getBassTreble127();

                KeyboardNumber n = new KeyboardNumber(KeyboardNumber.NumberType.INTEGER, bt.getBassDb());
                new KeyboardDialog(BassActivity.this, BassActivity.this, n, "bass").show();
            }
        });

        bassSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                HiFiToyPreset p = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
                BassTrebleChannel bass = p.getBassTreble().getBassTreble127();

                bass.setBassDbPercent(bassSlider.getPercent());
                bassLabel.setText(String.format(Locale.getDefault(),"%ddB", bass.getBassDb()));

                p.getBassTreble().sendToPeripheral(false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void setupOutlets() {
        HiFiToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
        BassTrebleChannel bassTreble = preset.getBassTreble().getBassTreble127();

        bassLabel.setText(String.format(Locale.getDefault(),"%ddB", bassTreble.getBassDb()));
        bassSlider.setPercent(bassTreble.getBassDbPercent());
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        HiFiToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

        try {
            if (tag.equals("bass")) {
                int r = Integer.parseInt(result.getValue());
                if (r > 127) r = 127; // TODO: fix bad solution
                if (r < -128) r = -128;

                preset.getBassTreble().getBassTreble127().setBassDb((byte)r);
                preset.getBassTreble().sendToPeripheral(true);

                setupOutlets();
            }
            if (tag.equals("treble")) {
                int r = Integer.parseInt(result.getValue());
                if (r > 127) r = 127; // TODO: fix bad solution
                if (r < -128) r = -128;

                preset.getBassTreble().getBassTreble127().setTrebleDb((byte)r);
                preset.getBassTreble().sendToPeripheral(true);

                setupOutlets();
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }
    }
}
