/*
 *   TimeConstActivity.java
 *
 *   Created by Artem Khlyupin on 04/19/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.compressor;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoyobjects.drc.Drc;

import java.util.Locale;

public class TimeConstActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    private TextView    energyLabel_outl;
    private SeekBar     energySeekBar_outl;
    private TextView    attackLabel_outl;
    private SeekBar     attackSeekBar_outl;
    private TextView    decayLabel_outl;
    private SeekBar     decaySeekBar_outl;

    private Drc         drc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeconst);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);
        registerReceiver(broadcastReceiver, makeIntentFilter());

        setupOutlets();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void setupOutlets() {
        enabledSeekBarListener(false);

        energyLabel_outl.setText(drc.getTimeConst17().getEnergyDescription());
        setSeekBar(energySeekBar_outl, drc.getTimeConst17().getEnergyPercent());

        attackLabel_outl.setText(drc.getTimeConst17().getAttackDescription());
        setSeekBar(attackSeekBar_outl, drc.getTimeConst17().getAttackPercent());

        decayLabel_outl.setText(drc.getTimeConst17().getDecayDescription());
        setSeekBar(decaySeekBar_outl, drc.getTimeConst17().getDecayPercent());

        enabledSeekBarListener(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(energySeekBar_outl)){
            drc.getTimeConst17().setEnergyPercent(getSeekBarPercent(energySeekBar_outl));
            energyLabel_outl.setText(drc.getTimeConst17().getEnergyDescription());

            drc.getTimeConst17().sendEnergyToPeripheral(false);
        }

        if (seekBar.equals(attackSeekBar_outl)){
            drc.getTimeConst17().setAttackPercent(getSeekBarPercent(attackSeekBar_outl));
            attackLabel_outl.setText(drc.getTimeConst17().getAttackDescription());

            drc.getTimeConst17().sendAttackDecayToPeripheral(false);
        }

        if (seekBar.equals(decaySeekBar_outl)){
            drc.getTimeConst17().setDecayPercent(getSeekBarPercent(decaySeekBar_outl));
            decayLabel_outl.setText(drc.getTimeConst17().getDecayDescription());

            drc.getTimeConst17().sendAttackDecayToPeripheral(false);
        }
    }

    private float getSeekBarPercent(SeekBar seekBar){//percent=[0.0 .. 1.0]
        return (float)seekBar.getProgress() / seekBar.getMax();
    }
    private void setSeekBar(SeekBar seekBar, float percent){//percent=[0.0 .. 1.0]
        int percentValue = (int)(percent * seekBar.getMax());
        seekBar.setProgress(percentValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    /*--------------------------- Broadcast receiver implementation ------------------------------*/
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HiFiToyControl.CLIP_UPDATE);

        return intentFilter;
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.CLIP_UPDATE.equals(action)) {
                ApplicationContext.getInstance().updateClipView();
            }
        }
    };
}
