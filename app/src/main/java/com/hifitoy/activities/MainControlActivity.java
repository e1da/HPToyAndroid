/*
 *   MainControlActivity.java
 *
 *   Created by Artem Khlyupin on 28/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.activities;

import android.Manifest;
import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.compressor.CompressorActivity;
import com.hifitoy.activities.filters.FiltersActivity;
import com.hifitoy.activities.options.OptionsActivity;
import com.hifitoy.activities.options.presetmanager.PresetManagerActivity;
import com.hifitoy.ble.Ble;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.dialogsystem.DiscoveryDialog;
import com.hifitoy.dialogsystem.KeyboardNumber;
import com.hifitoy.dialogsystem.KeyboardNumber.NumberType;
import com.hifitoy.dialogsystem.KeyboardDialog;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.AudioSource;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Loudness;
import com.hifitoy.hifitoyobjects.Volume;
import com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel;
import com.hifitoy.widgets.AudioSourceWidget;
import java.util.Locale;


public class MainControlActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
                                                                View.OnClickListener,
                                                                AudioSourceWidget.OnCheckedListener,
                                                                KeyboardDialog.OnResultListener {
    private static final String TAG = "HiFiToy";

    private HiFiToyDevice hifiToyDevice;

    TextView discoveryBtn;

    LinearLayout audioSourceGroup_outl;
    AppCompatImageView audioSourceInfo_outl;
    AudioSourceWidget audioSource_outl;

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

        ApplicationContext.getInstance().setContext(this);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else {
            checkBleEnabled();
        }

        initActionBar();
        initOutlets();

        checkImportPreset(getIntent());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){

        if(requestCode == 1) {
            boolean blePermission = true;

            for (int i = 0; i < permissions.length; i++) {


                switch (permissions[i]) {
                    case Manifest.permission.BLUETOOTH:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.BLUETOOTH is not granted.");
                        }
                        break;
                    case Manifest.permission.BLUETOOTH_ADMIN:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.BLUETOOTH_ADMIN is not granted.");
                        }
                        break;
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.ACCESS_FINE_LOCATION is not granted.");
                        }
                        break;
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.ACCESS_COARSE_LOCATION is not granted.");
                        }
                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "permission.READ_EXTERNAL_STORAGE is not granted.");
                        }
                        break;

                }
            }

            if (blePermission) {
                checkBleEnabled();

            } else {
                finish();
            }
        }
    }

    private void checkBleEnabled() {
        if (Ble.getInstance().isSupported()) {
            if (!Ble.getInstance().isEnabled()) {
                //show ble enable request dialog
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            } else {
                //HiFiToyControl.getInstance().startDiscovery(this);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { // ble enabled dialog result
            if (Ble.getInstance().isEnabled()) {
                //HiFiToyControl.getInstance().startDiscovery(this);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.showPresetManager_outl:
                intent = new Intent(this, PresetManagerActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hifiToyDevice = HiFiToyControl.getInstance().getActiveDevice();
        setupOutlets();
    }

    private void initActionBar() {
        //show back button
        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);


        LayoutInflater mInflater = LayoutInflater.from(this);
        View actionBarView = mInflater.inflate(R.layout.action_bar, null);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayShowCustomEnabled(true);

        discoveryBtn = actionBarView.findViewById(R.id.discoveryBtn_outl);
        discoveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscoveryDialog dd = new DiscoveryDialog(ApplicationContext.getInstance().getContext());
                dd.show();
            }
        });
    }


    private void initOutlets() {
        audioSourceGroup_outl   = findViewById(R.id.audio_source_group);
        audioSourceInfo_outl    = findViewById(R.id.audio_source_info);
        audioSource_outl        = findViewById(R.id.audio_source1_outl);

        if (getPackageName().equals("com.hptoy")) {
            audioSourceGroup_outl.setVisibility(View.GONE);
        } else {
            audioSourceGroup_outl.setVisibility(View.VISIBLE);
        }

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

        filtersInfo_outl        = findViewById(R.id.filters_control_info);
        filtersActivity_outl    = findViewById(R.id.filtersActivity_outl);
        compressorInfo_outl     = findViewById(R.id.compressor_control_info);
        compressorActivity_outl = findViewById(R.id.compressorActivity_outl);
        optionsActivity_outl    = findViewById(R.id.optionsActivity_outl);

        audioSourceInfo_outl.setOnClickListener(this);
        audioSource_outl.setOnCheckedListener(this);
        //volumeInfo_outl.setOnClickListener(this);
        volumeLabel_outl.setOnClickListener(this);
        bassTrebleInfo_outl.setOnClickListener(this);
        bassLabel_outl.setOnClickListener(this);
        trebleLabel_outl.setOnClickListener(this);
        loudnessInfo_outl.setOnClickListener(this);
        loudnessLabel_outl.setOnClickListener(this);
        loudnessFreqLabel_outl.setOnClickListener(this);
        filtersInfo_outl.setOnClickListener(this);
        filtersActivity_outl.setOnClickListener(this);
        compressorInfo_outl.setOnClickListener(this);
        compressorActivity_outl.setOnClickListener(this);
        optionsActivity_outl.setOnClickListener(this);

        volumeSeekBar_outl.setOnSeekBarChangeListener(this);
        bassSeekBar_outl.setOnSeekBarChangeListener(this);
        trebleSeekBar_outl.setOnSeekBarChangeListener(this);
        loudnessSeekBar_outl.setOnSeekBarChangeListener(this);
        loudnessFreqSeekBar_outl.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setupOutlets() {
        Drawable drawable;
        if (HiFiToyControl.getInstance().isConnected()) {
            drawable = getResources().getDrawable(R.drawable.active_discovery_btn, getTheme());
        } else {
            drawable = getResources().getDrawable(R.drawable.discovery_btn, getTheme());
        }
        discoveryBtn.setBackground(drawable);

        HiFiToyPreset preset = hifiToyDevice.getActivePreset();

        audioSource_outl.setState(hifiToyDevice.getAudioSource().getSource());

        volumeLabel_outl.setText(preset.getVolume().getInfo());
        setSeekBar(volumeSeekBar_outl, preset.getVolume().getDbPercent());

        BassTrebleChannel bassTreble = preset.getBassTreble().getBassTreble127();
        bassLabel_outl.setText(String.format(Locale.getDefault(),"%ddB", bassTreble.getBassDb()));
        setSeekBar(bassSeekBar_outl, bassTreble.getBassDbPercent());

        trebleLabel_outl.setText(String.format(Locale.getDefault(),"%ddB", bassTreble.getTrebleDb()));
        setSeekBar(trebleSeekBar_outl, bassTreble.getTrebleDbPercent());

        loudnessLabel_outl.setText(preset.getLoudness().getInfo());
        setSeekBar(loudnessSeekBar_outl, preset.getLoudness().getGain() / 2);
        loudnessFreqLabel_outl.setText(preset.getLoudness().getFreqInfo());
        setSeekBar(loudnessFreqSeekBar_outl, preset.getLoudness().getBiquad().getParams().getFreqPercent());

    }

    public void onClick(View v) {
        Intent intent;
        KeyboardNumber n;
        BassTrebleChannel bassTreble;
        HiFiToyPreset preset = hifiToyDevice.getActivePreset();

        switch (v.getId()) {
            case R.id.audio_source_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.audio_source_info), "Close");
                break;

            /*case R.id.volume_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.volume_info), "Close");
                break;*/

            case R.id.volumeLabel_outl:
                n = new KeyboardNumber(NumberType.FLOAT, preset.getVolume().getDb());
                new KeyboardDialog(this, this, n, "volume").show();
                break;

            case R.id.bass_treble_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.bass_treble_info), "Close");
                break;

            case R.id.bassLabel_outl:
                bassTreble = preset.getBassTreble().getBassTreble127();
                n = new KeyboardNumber(NumberType.INTEGER, bassTreble.getBassDb());
                new KeyboardDialog(this, this, n, "bass").show();
                break;

            case R.id.trebleLabel_outl:
                bassTreble = preset.getBassTreble().getBassTreble127();
                n = new KeyboardNumber(NumberType.INTEGER, bassTreble.getTrebleDb());
                new KeyboardDialog(this, this, n, "treble").show();
                break;

            case R.id.loudness_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.loudness_info), "Close");
                break;

            case R.id.loudnessLabel_outl:
                int perc = (int)(preset.getLoudness().getGain() * 100);
                n = new KeyboardNumber(NumberType.POSITIVE_INTEGER, perc);
                new KeyboardDialog(this, this, n, "loudness").show();
                break;

            case R.id.loudnessFreqLabel_outl:
                n = new KeyboardNumber(NumberType.POSITIVE_INTEGER,
                        preset.getLoudness().getBiquad().getParams().getFreq());
                new KeyboardDialog(this, this, n, "loudnessFreq").show();
                break;

            case R.id.filters_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.filters_info), "Close");
                break;

            case R.id.filtersActivity_outl:
                intent = new Intent(this, FiltersActivity.class);
                startActivity(intent);
                break;

            case R.id.compressor_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.compressor_info), "Close");
                break;

            case R.id.compressorActivity_outl:
                intent = new Intent(this, CompressorActivity.class);
                startActivity(intent);
                break;

            case R.id.optionsActivity_outl:
                Intent intentActivity = new Intent(this, OptionsActivity.class);
                startActivity(intentActivity);
                break;

        }
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        HiFiToyPreset preset = hifiToyDevice.getActivePreset();

        try {
            if (tag.equals("volume")) {
                float f = Float.parseFloat(result.getValue());
                Volume v = preset.getVolume();

                v.setDb(f);
                v.sendToPeripheral(true);

                setupOutlets();
            }
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
            if (tag.equals("loudness")) {
                int r = Integer.parseInt(result.getValue());

                preset.getLoudness().setGain((float)r / 100);
                preset.getLoudness().sendToPeripheral(true);

                setupOutlets();
            }
            if (tag.equals("loudnessFreq")) {
                int r = Integer.parseInt(result.getValue());
                if (r > 32767) r = 32767; // TODO: fix bad solution

                Biquad b = preset.getLoudness().getBiquad();
                b.getParams().setFreq((short)r);
                b.sendToPeripheral(true);

                setupOutlets();
            }

        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }
    }

    //audio source change hadler
    public void onCheckedChanged(byte state) {
        AudioSource audioSource = hifiToyDevice.getAudioSource();
        audioSource.setSourceWithWriteToDsp(state);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;

        HiFiToyPreset p = hifiToyDevice.getActivePreset();

        if (seekBar.equals(volumeSeekBar_outl)){
            Volume v = p.getVolume();

            v.setDbPercent(getSeekBarPercent(volumeSeekBar_outl));
            volumeLabel_outl.setText(v.getInfo());

            v.sendToPeripheral(false);
        }

        if (seekBar.equals(bassSeekBar_outl)){
            BassTrebleChannel bass = p.getBassTreble().getBassTreble127();

            bass.setBassDbPercent(getSeekBarPercent(bassSeekBar_outl));
            bassLabel_outl.setText(String.format(Locale.getDefault(),"%ddB", bass.getBassDb()));

            p.getBassTreble().sendToPeripheral(false);
        }

        if (seekBar.equals(trebleSeekBar_outl)){
            BassTrebleChannel treble = p.getBassTreble().getBassTreble127();

            treble.setTrebleDbPercent(getSeekBarPercent(trebleSeekBar_outl));
            trebleLabel_outl.setText(String.format(Locale.getDefault(),"%ddB", treble.getTrebleDb()));

            p.getBassTreble().sendToPeripheral(false);
        }

        if (seekBar.equals(loudnessSeekBar_outl)){
            Loudness l = p.getLoudness();

            l.setGain(getSeekBarPercent(loudnessSeekBar_outl) * 2);
            loudnessLabel_outl.setText(l.getInfo());

            l.sendToPeripheral(false);
        }

        if (seekBar.equals(loudnessFreqSeekBar_outl)){
            Biquad.BiquadParam bp = p.getLoudness().getBiquad().getParams();

            bp.setFreqPercent(getSeekBarPercent(loudnessFreqSeekBar_outl));
            //round freq
            bp.setFreq(freqRound(bp.getFreq()));

            loudnessFreqLabel_outl.setText(p.getLoudness().getFreqInfo());
            p.getLoudness().getBiquad().sendToPeripheral(false);
        }
    }
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setSeekBar(SeekBar seekBar, float percent){//percent=[0.0 .. 1.0]
        int percentValue = (int)(percent * seekBar.getMax());
        seekBar.setProgress(percentValue);
    }
    private float getSeekBarPercent(SeekBar seekBar){//percent=[0.0 .. 1.0]
        return (float)seekBar.getProgress() / seekBar.getMax();
    }

    private short freqRound(short freq) {
        if (freq > 1000) {
            return (short)(freq / 100 * 100);
        } else if (freq > 100) {
            return (short)(freq / 10 * 10);
        }
        return freq;
    }

    //import xml preset
    private void checkImportPreset(Intent intent) {
        if ((intent == null) || (intent.getAction() == null)) return;

        if (intent.getAction().equals(Intent.ACTION_VIEW)){
            Uri uri = intent.getData();
            if (uri == null) return;


            HiFiToyPreset importPreset = new HiFiToyPreset();
            if (importPreset.importFromXml(uri)){
                //check duplicate name
                String name = importPreset.getName();
                int count = 0;
                while (HiFiToyPresetManager.getInstance().isPresetExist(name)){
                    count++;
                    name = importPreset.getName() + "_" + count;
                }
                importPreset.setName(name);
                HiFiToyPresetManager.getInstance().setPreset(importPreset);

                DialogSystem.getInstance().showDialog("Info",
                        "Add " + importPreset.getName() + " preset", "Ok");
            } else {
                DialogSystem.getInstance().showDialog("Warning", "Import preset is not success.", "Ok");
            }

        }
    }

}
