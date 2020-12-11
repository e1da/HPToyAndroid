/*
 *   TimeConstActivity.java
 *
 *   Created by Artem Khlyupin on 04/19/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.compressor;

import android.app.ActionBar;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hifitoy.R;
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.drc.Drc;
import com.hifitoy.widgets.Slider;

public class TimeConstActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private TextView    energyLabel_outl;
    private Slider      energySeekBar_outl;
    private TextView    attackLabel_outl;
    private Slider      attackSeekBar_outl;
    private TextView    decayLabel_outl;
    private Slider      decaySeekBar_outl;

    private Drc         drc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Compressor time const");
        setContentView(R.layout.activity_timeconst);

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

    private void enabledSeekBarListener(boolean enabled) {
        energySeekBar_outl.setOnSeekBarChangeListener(enabled ? this : null);
        attackSeekBar_outl.setOnSeekBarChangeListener(enabled ? this : null);
        decaySeekBar_outl.setOnSeekBarChangeListener(enabled ? this : null);
    }

    private void initOutlets() {
        drc = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getDrc();

        energyLabel_outl = findViewById(R.id.energyLabel_outl);
        energySeekBar_outl = findViewById(R.id.energySeekBar_outl);
        attackLabel_outl = findViewById(R.id.attackLabel_outl);
        attackSeekBar_outl = findViewById(R.id.attackSeekBar_outl);
        decayLabel_outl = findViewById(R.id.decayLabel_outl);
        decaySeekBar_outl = findViewById(R.id.decaySeekBar_outl);

        enabledSeekBarListener(true);
    }

    @Override
    public void setupOutlets() {
        enabledSeekBarListener(false);

        energyLabel_outl.setText(drc.getTimeConst17().getEnergyDescription());
        energySeekBar_outl.setPercent(drc.getTimeConst17().getEnergyPercent());

        attackLabel_outl.setText(drc.getTimeConst17().getAttackDescription());
        attackSeekBar_outl.setPercent(drc.getTimeConst17().getAttackPercent());

        decayLabel_outl.setText(drc.getTimeConst17().getDecayDescription());
        decaySeekBar_outl.setPercent(drc.getTimeConst17().getDecayPercent());

        enabledSeekBarListener(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(energySeekBar_outl)){
            drc.getTimeConst17().setEnergyPercent(energySeekBar_outl.getPercent());
            energyLabel_outl.setText(drc.getTimeConst17().getEnergyDescription());

            drc.getTimeConst17().sendEnergyToPeripheral(false);
        }

        if (seekBar.equals(attackSeekBar_outl)){
            drc.getTimeConst17().setAttackPercent(attackSeekBar_outl.getPercent());
            attackLabel_outl.setText(drc.getTimeConst17().getAttackDescription());

            drc.getTimeConst17().sendAttackDecayToPeripheral(false);
        }

        if (seekBar.equals(decaySeekBar_outl)){
            drc.getTimeConst17().setDecayPercent(decaySeekBar_outl.getPercent());
            decayLabel_outl.setText(drc.getTimeConst17().getDecayDescription());

            drc.getTimeConst17().sendAttackDecayToPeripheral(false);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
