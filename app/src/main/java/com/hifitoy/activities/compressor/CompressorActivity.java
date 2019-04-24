/*
 *   CompressorActivity.java
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
import android.view.Menu;
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

public class CompressorActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    private CompressorView  compressorView;
    private TextView        compressorLabel_outl;
    private SeekBar         compressorSeekBar_outl;

    private HiFiToyPreset preset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compressor);

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

        preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();
        setupOutlets();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compressor_menu, menu);
        return true;
    }


    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.time_const_outl:
                Intent intentActivity = new Intent(this, TimeConstActivity.class);
                startActivity(intentActivity);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    private void initOutlets() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        compressorView = findViewById(R.id.compressor_view_outl);
        compressorView.setLayoutParams(new LinearLayout.LayoutParams(size.x, size.x));

        compressorLabel_outl = findViewById(R.id.compressorLabel_outl);
        compressorSeekBar_outl = findViewById(R.id.compressorSeekBar_outl);

        compressorSeekBar_outl.setOnSeekBarChangeListener(this);
    }

    private void setupOutlets() {
        Drc drc = preset.getDrc();

        compressorLabel_outl.setText(String.format(Locale.getDefault(), "%d%%",
                                    (int)(drc.getEnabledChannel((byte)0) * 100)));
        setSeekBar(compressorSeekBar_outl, drc.getEnabledChannel((byte)0));

        compressorView.invalidate();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(compressorSeekBar_outl)){
            Drc drc = preset.getDrc();
            drc.setEnabled(getSeekBarPercent(seekBar), (byte)0);
            drc.setEnabled(getSeekBarPercent(seekBar), (byte)1);

            compressorLabel_outl.setText(String.format(Locale.getDefault(), "%d%%",
                                        (int)(drc.getEnabledChannel((byte)0) * 100)));

            drc.sendEnabledToPeripheral((byte)0, false);
            drc.sendEnabledToPeripheral((byte)1, false);
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
