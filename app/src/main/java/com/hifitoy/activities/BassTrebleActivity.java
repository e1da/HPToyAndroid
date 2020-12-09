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

public class BassTrebleActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
                                                                View.OnClickListener,
                                                                KeyboardDialog.OnResultListener {
    private static final String TAG = "HiFiToy";

    private TextView    bassLabel;
    private Slider      bassSlider;
    private TextView    trebleLabel;
    private Slider      trebleSlider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        trebleLabel = findViewById(R.id.trebleLabel_outl);
        trebleSlider = findViewById(R.id.trebleSlider_outl);

        bassLabel.setOnClickListener(this);
        trebleLabel.setOnClickListener(this);

        bassSlider.setOnSeekBarChangeListener(this);
        trebleSlider.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setupOutlets() {
        HiFiToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

        BassTrebleChannel bassTreble = preset.getBassTreble().getBassTreble127();
        bassLabel.setText(String.format(Locale.getDefault(),"%ddB", bassTreble.getBassDb()));
        bassSlider.setPercent(bassTreble.getBassDbPercent());

        trebleLabel.setText(String.format(Locale.getDefault(),"%ddB", bassTreble.getTrebleDb()));
        trebleSlider.setPercent(bassTreble.getTrebleDbPercent());
    }

    @Override
    public void onClick(View v) {
        KeyboardNumber n;
        BassTrebleChannel bassTreble;
        HiFiToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

        switch (v.getId()) {
            case R.id.bassLabel_outl:
                bassTreble = preset.getBassTreble().getBassTreble127();
                n = new KeyboardNumber(KeyboardNumber.NumberType.INTEGER, bassTreble.getBassDb());
                new KeyboardDialog(this, this, n, "bass").show();
                break;

            case R.id.trebleLabel_outl:
                bassTreble = preset.getBassTreble().getBassTreble127();
                n = new KeyboardNumber(KeyboardNumber.NumberType.INTEGER, bassTreble.getTrebleDb());
                new KeyboardDialog(this, this, n, "treble").show();
                break;
        }
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;

        HiFiToyPreset p = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

        if (seekBar.equals(bassSlider)){
            BassTrebleChannel bass = p.getBassTreble().getBassTreble127();

            bass.setBassDbPercent(bassSlider.getPercent());
            bassLabel.setText(String.format(Locale.getDefault(),"%ddB", bass.getBassDb()));

            p.getBassTreble().sendToPeripheral(false);
        }

        if (seekBar.equals(trebleSlider)){
            BassTrebleChannel treble = p.getBassTreble().getBassTreble127();

            treble.setTrebleDbPercent(trebleSlider.getPercent());
            trebleLabel.setText(String.format(Locale.getDefault(),"%ddB", treble.getTrebleDb()));

            p.getBassTreble().sendToPeripheral(false);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }




}
