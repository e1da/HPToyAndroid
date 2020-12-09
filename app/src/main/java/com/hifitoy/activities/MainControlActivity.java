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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.compressor.CompressorActivity;
import com.hifitoy.activities.filters.FiltersActivity;
import com.hifitoy.activities.options.OptionsActivity;
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
import com.hifitoy.hifitoyobjects.Volume;
import com.hifitoy.widgets.AudioSourceWidget;
import com.hifitoy.widgets.Slider;


public class MainControlActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
                                                                View.OnClickListener,
                                                                AudioSourceWidget.OnCheckedListener,
                                                                KeyboardDialog.OnResultListener {
    private static final String TAG = "HiFiToy";

    private HiFiToyDevice hifiToyDevice;

    TextView discoveryBtn;

    LinearLayout        audioSourceGroup;
    AppCompatImageView  audioSourceInfo;
    AudioSourceWidget   audioSource;

    TextView    volumeLabel;
    Slider      volumeSlider;

    TextView    bassTrebleActivity;
    ImageView   bassTrebleInfo;

    TextView    loudnessActivity;
    ImageView   loudnessInfo;

    TextView    filtersActivity;
    ImageView   filtersInfo;

    TextView    compressorActivity;
    ImageView   compressorInfo;

    TextView    optionsActivity;


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

    @Override
    protected void onResume() {
        super.onResume();

        hifiToyDevice = HiFiToyControl.getInstance().getActiveDevice();
        setupOutlets();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();

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
        audioSourceGroup = findViewById(R.id.audio_source_group);
        audioSourceInfo = findViewById(R.id.audio_source_info);
        audioSource = findViewById(R.id.audio_source1_outl);

        if (getPackageName().equals("com.hptoy")) {
            audioSourceGroup.setVisibility(View.GONE);
        } else {
            audioSourceGroup.setVisibility(View.VISIBLE);
        }

        volumeLabel = findViewById(R.id.volumeLabel_outl);
        volumeSlider = findViewById(R.id.volumeSeekBar_outl);

        bassTrebleActivity = findViewById(R.id.bass_treble_outl);
        bassTrebleInfo = findViewById(R.id.bass_treble_info);

        loudnessActivity    = findViewById(R.id.loudness_outl);
        loudnessInfo = findViewById(R.id.loudness_info);

        filtersInfo = findViewById(R.id.filters_control_info);
        filtersActivity = findViewById(R.id.filtersActivity_outl);
        compressorInfo = findViewById(R.id.compressor_control_info);
        compressorActivity = findViewById(R.id.compressorActivity_outl);
        optionsActivity = findViewById(R.id.optionsActivity_outl);

        audioSourceInfo.setOnClickListener(this);
        audioSource.setOnCheckedListener(this);
        volumeLabel.setOnClickListener(this);
        bassTrebleActivity.setOnClickListener(this);
        bassTrebleInfo.setOnClickListener(this);
        loudnessActivity.setOnClickListener(this);
        loudnessInfo.setOnClickListener(this);
        filtersInfo.setOnClickListener(this);
        filtersActivity.setOnClickListener(this);
        compressorInfo.setOnClickListener(this);
        compressorActivity.setOnClickListener(this);
        optionsActivity.setOnClickListener(this);

        volumeSlider.setOnSeekBarChangeListener(this);
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

        audioSource.setState(hifiToyDevice.getAudioSource().getSource());

        volumeLabel.setText(preset.getVolume().getInfo());
        volumeSlider.setPercent(preset.getVolume().getDbPercent());

    }

    public void onClick(View v) {
        Intent intent;
        KeyboardNumber n;
        HiFiToyPreset preset = hifiToyDevice.getActivePreset();

        switch (v.getId()) {
            case R.id.audio_source_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.audio_source_info), "Close");
                break;

            case R.id.volumeLabel_outl:
                n = new KeyboardNumber(NumberType.FLOAT, preset.getVolume().getDb());
                new KeyboardDialog(this, this, n, "volume").show();
                break;

            case R.id.bass_treble_outl:
                intent = new Intent(this, BassTrebleActivity.class);
                startActivity(intent);
                break;

            case R.id.bass_treble_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.bass_treble_info), "Close");
                break;

            case R.id.loudness_outl:
                intent = new Intent(this, LoudnessActivity.class);
                startActivity(intent);
                break;

            case R.id.loudness_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.loudness_info), "Close");
                break;

            case R.id.filtersActivity_outl:
                intent = new Intent(this, FiltersActivity.class);
                startActivity(intent);
                break;

            case R.id.filters_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.filters_info), "Close");
                break;

            case R.id.compressorActivity_outl:
                intent = new Intent(this, CompressorActivity.class);
                startActivity(intent);
                break;

            case R.id.compressor_control_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.compressor_info), "Close");
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

        if (seekBar.equals(volumeSlider)){
            Volume v = p.getVolume();

            v.setDbPercent(volumeSlider.getPercent());
            volumeLabel.setText(v.getInfo());

            v.sendToPeripheral(false);
        }
    }
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void onStopTrackingTouch(SeekBar seekBar) {

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
