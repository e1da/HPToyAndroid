/*
 *   MainControlActivity.java
 *
 *   Created by Artem Khlyupin on 28/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.options.OptionsActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;


public class MainControlActivity extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private static final String TAG = "HiFiToy";

    private HiFiToyDevice hifiToyDevice;

    AppCompatImageView audioSourceInfo_outl;

    AppCompatImageView volumeInfo_outl;
    TextView volumeLabel_outl;
    SeekBar volumeSeekBar_outl;

    AppCompatImageView bassTrebleInfo_outl;
    TextView bassLabel_outl;
    SeekBar bassSeekBar_outl;
    TextView trebleLabel_outl;
    SeekBar trebleSeekBar_outl;

    AppCompatImageView loudnessInfo_outl;
    TextView loudnessLabel_outl;
    SeekBar loudnessSeekBar_outl;
    TextView loudnessFreqLabel_outl;
    SeekBar loudnessFreqSeekBar_outl;

    AppCompatImageView filtersInfo_outl;
    TextView filtersActivity_outl;
    AppCompatImageView compressorInfo_outl;
    TextView compressorActivity_outl;
    TextView optionsActivity_outl;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_control);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_control_menu, menu);
        return true;
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.showPresetManager_outl:
                /*intent = new Intent(this,
                        PresetManagerActivity.class);
                startActivity(intent);*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

        hifiToyDevice = HiFiToyControl.getInstance().getActiveDevice();

        setupOutlets();

    }

    private void initOutlets() {
        audioSourceInfo_outl     = findViewById(R.id.audio_source_info);

        //volumeInfo_outl     = findViewById(R.id.volume_control_info);
        volumeLabel_outl    = findViewById(R.id.volumeLabel_outl);
        volumeSeekBar_outl  = findViewById(R.id.volumeSeekBar_outl);

        bassTrebleInfo_outl = findViewById(R.id.bass_treble_info);
        bassLabel_outl      = findViewById(R.id.bassLabel_outl);
        bassSeekBar_outl    = findViewById(R.id.bassSeekBar_outl);
        trebleLabel_outl    = findViewById(R.id.trebleLabel_outl);
        trebleSeekBar_outl  = findViewById(R.id.trebleSeekBar_outl);

        loudnessInfo_outl           = findViewById(R.id.loudness_info);
        loudnessLabel_outl          = findViewById(R.id.loudnessLabel_outl);
        loudnessSeekBar_outl        = findViewById(R.id.loudnessSeekBar_outl);
        loudnessFreqLabel_outl      = findViewById(R.id.loudnessFreqLabel_outl);
        loudnessFreqSeekBar_outl    = findViewById(R.id.loudnessFreqSeekBar_outl);

        filtersInfo_outl = findViewById(R.id.filters_control_info);
        filtersActivity_outl = findViewById(R.id.filtersActivity_outl);
        compressorInfo_outl     = findViewById(R.id.compressor_control_info);
        compressorActivity_outl = findViewById(R.id.compressorActivity_outl);
        optionsActivity_outl    = findViewById(R.id.optionsActivity_outl);

        audioSourceInfo_outl.setOnClickListener(this);
        //volumeInfo_outl.setOnClickListener(this);
        bassTrebleInfo_outl.setOnClickListener(this);
        loudnessInfo_outl.setOnClickListener(this);
        filtersInfo_outl.setOnClickListener(this);
        filtersActivity_outl.setOnClickListener(this);
        compressorInfo_outl.setOnClickListener(this);
        compressorActivity_outl.setOnClickListener(this);
        optionsActivity_outl.setOnClickListener(this);
    }

    private void setupOutlets() {

    }

    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.audio_source_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.audio_source_info), "Close");
                break;
            /*case R.id.volume_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.volume_info), "Close");
                break;*/
            case R.id.bass_treble_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.bass_treble_info), "Close");
                break;
            case R.id.loudness_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.loudness_info), "Close");
                break;
            case R.id.filters_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.filters_info), "Close");
                break;
            case R.id.filtersActivity_outl:
                break;
            case R.id.compressor_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.compressor_info), "Close");
                break;
            case R.id.compressorActivity_outl:
                break;
            case R.id.optionsActivity_outl:
                Intent intentActivity = new Intent(this, OptionsActivity.class);
                startActivity(intentActivity);
                break;

        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;

        if (seekBar.equals(volumeSeekBar_outl)){
            //dspPreset.gainSlider.setPercent((double)progress / 100);
            /*dspPreset.gainSlider.setPercent(getSeekBarPercent(seekBar));
            gainLabel_outl.setText(dspPreset.gainSlider.getInfo());

            dspPreset.gainSlider.sendToDsp(false);//false = without response*/
        }
        if (seekBar.equals(bassSeekBar_outl)){

        }
        if (seekBar.equals(trebleSeekBar_outl)){

        }
        if (seekBar.equals(loudnessSeekBar_outl)){

        }
        if (seekBar.equals(loudnessFreqSeekBar_outl)){

        }
    }
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setSeekBar(SeekBar seekBar, double percent){//percent=[0.0 .. 1.0]
        int percentValue = (int)(percent * seekBar.getMax());
        seekBar.setProgress(percentValue);
    }
    private double getSeekBarPercent(SeekBar seekBar){//percent=[0.0 .. 1.0]
        return (double)seekBar.getProgress() / seekBar.getMax();
    }



}
