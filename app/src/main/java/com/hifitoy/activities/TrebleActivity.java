/*
 *   TrebleActivity.java
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
import com.hifitoy.hifitoydevice.ToyPreset;
import com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel;
import com.hifitoy.widgets.Slider;

import java.util.Locale;

public class TrebleActivity extends BaseActivity implements KeyboardDialog.OnResultListener {
    private static final String TAG = "HiFiToy";

    private TextView    trebleLabel;
    private Slider      trebleSlider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Treble");
        setContentView(R.layout.activity_treble);

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
        trebleLabel = findViewById(R.id.trebleLabel_outl);
        trebleSlider = findViewById(R.id.trebleSlider_outl);

        trebleLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
                BassTrebleChannel bt = preset.getBassTreble().getBassTreble127();

                KeyboardNumber n = new KeyboardNumber(KeyboardNumber.NumberType.INTEGER, bt.getTrebleDb());
                new KeyboardDialog(TrebleActivity.this, TrebleActivity.this, n, "treble").show();
            }
        });

        trebleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                ToyPreset p = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
                BassTrebleChannel treble = p.getBassTreble().getBassTreble127();

                treble.setTrebleDbPercent(trebleSlider.getPercent());
                trebleLabel.setText(String.format(Locale.getDefault(),"%ddB", treble.getTrebleDb()));

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
        ToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
        BassTrebleChannel bassTreble = preset.getBassTreble().getBassTreble127();

        trebleLabel.setText(String.format(Locale.getDefault(),"%ddB", bassTreble.getTrebleDb()));
        trebleSlider.setPercent(bassTreble.getTrebleDbPercent());
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        ToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

        try {
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
