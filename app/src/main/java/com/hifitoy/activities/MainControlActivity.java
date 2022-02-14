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
import com.hifitoy.hifitoyobjects.Volume;
import com.hifitoy.widgets.AudioSourceWidget;
import com.hifitoy.widgets.Slider;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


public class MainControlActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
                                                                View.OnClickListener,
                                                                AudioSourceWidget.OnCheckedListener,
                                                                KeyboardDialog.OnResultListener {
    private static final String TAG = "HiFiToy";

    TextView discoveryBtn;

    LinearLayout        audioSourceGroup;
    AppCompatImageView  audioSourceInfo;
    AudioSourceWidget   audioSource;

    TextView    volumeLabel;
    Slider      volumeSlider;

    TextView    bassActivity;
    TextView    trebleActivity;
    TextView    loudnessActivity;

    TextView    filtersActivity;
    TextView    compressorActivity;

    TextView    presetsActivity;
    TextView    savePresetBtn;
    TextView    optionsActivity;

    ImageView mainInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        setupOutlets();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        setOutletsEnabled(hasFocus);
    }

    private void setOutletsEnabled(boolean enabled) {
        volumeLabel.setEnabled(enabled);

        bassActivity.setEnabled(enabled);
        trebleActivity.setEnabled(enabled);
        loudnessActivity.setEnabled(enabled);

        filtersActivity.setEnabled(enabled);
        compressorActivity.setEnabled(enabled);

        presetsActivity.setEnabled(enabled);
        savePresetBtn.setEnabled(enabled);
        optionsActivity.setEnabled(enabled);

        mainInfo.setEnabled(enabled);
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
        audioSourceGroup    = findViewById(R.id.audio_source_group);
        audioSourceInfo     = findViewById(R.id.audio_source_info);
        audioSource         = findViewById(R.id.audio_source1_outl);

        if (getPackageName().equals("com.hptoy")) {
            audioSourceGroup.setVisibility(View.GONE);
        } else {
            audioSourceGroup.setVisibility(View.VISIBLE);
        }

        volumeLabel         = findViewById(R.id.volumeLabel_outl);
        volumeSlider        = findViewById(R.id.volumeSeekBar_outl);

        bassActivity        = findViewById(R.id.bassActivity_outl);
        trebleActivity      = findViewById(R.id.trebleActivity_outl);
        loudnessActivity    = findViewById(R.id.loudnessActivity_outl);

        filtersActivity     = findViewById(R.id.filtersActivity_outl);
        compressorActivity  = findViewById(R.id.compressorActivity_outl);

        presetsActivity     = findViewById(R.id.presetsActivity_outl);
        savePresetBtn       = findViewById(R.id.savePresetBtn_outl);
        optionsActivity     = findViewById(R.id.settingsActivity_outl);
        mainInfo            = findViewById(R.id.mainInfo_outl);

        audioSourceInfo.setOnClickListener(this);
        audioSource.setOnCheckedListener(this);
        volumeLabel.setOnClickListener(this);
        bassActivity.setOnClickListener(this);
        trebleActivity.setOnClickListener(this);
        loudnessActivity.setOnClickListener(this);
        filtersActivity.setOnClickListener(this);
        compressorActivity.setOnClickListener(this);
        presetsActivity.setOnClickListener(this);
        savePresetBtn.setOnClickListener(this);
        optionsActivity.setOnClickListener(this);
        mainInfo.setOnClickListener(this);

        volumeSlider.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setupOutlets() {
        Drawable drawable;
        if (HiFiToyControl.getInstance().isConnectionReady()) {
            drawable = getResources().getDrawable(R.drawable.active_discovery_btn, getTheme());
        } else {
            drawable = getResources().getDrawable(R.drawable.discovery_btn, getTheme());
        }
        discoveryBtn.setBackground(drawable);

        HiFiToyDevice dev = HiFiToyControl.getInstance().getActiveDevice();
        HiFiToyPreset preset = dev.getActivePreset();

        audioSource.setState(dev.getAudioSource().getSource());

        volumeLabel.setText(preset.getVolume().getInfo());
        volumeSlider.setPercent(preset.getVolume().getDbPercent());

    }

    public void onClick(View v) {
        Intent intent;
        KeyboardNumber n;
        HiFiToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

        switch (v.getId()) {
            case R.id.audio_source_info:
                DialogSystem.getInstance().showDialog("Info", getString(R.string.audio_source_info), "Close");
                break;

            case R.id.volumeLabel_outl:
                n = new KeyboardNumber(NumberType.FLOAT, preset.getVolume().getDb());
                new KeyboardDialog(this, this, n, "volume").show();
                break;

            case R.id.bassActivity_outl:
                intent = new Intent(this, BassActivity.class);
                startActivity(intent);
                break;

            case R.id.trebleActivity_outl:
                intent = new Intent(this, TrebleActivity.class);
                startActivity(intent);
                break;

            case R.id.loudnessActivity_outl:
                intent = new Intent(this, LoudnessActivity.class);
                startActivity(intent);
                break;

            case R.id.filtersActivity_outl:
                intent = new Intent(this, FiltersActivity.class);
                startActivity(intent);
                break;

            case R.id.compressorActivity_outl:
                intent = new Intent(this, CompressorActivity.class);
                startActivity(intent);
                break;

            case R.id.presetsActivity_outl:
                intent = new Intent(this, PresetManagerActivity.class);
                startActivity(intent);
                break;

            case R.id.savePresetBtn_outl:
                savePresetBtn.setEnabled(false);
                DialogSystem.getInstance().showTextDialog(savePresetHandler,
                        "Please input preset name", "Ok", "Cancel");
                break;

            case R.id.settingsActivity_outl:
                intent = new Intent(this, OptionsActivity.class);
                startActivity(intent);
                break;

            case R.id.mainInfo_outl:
                DialogSystem.getInstance().showDialog("Info",
                        getLayoutInflater().inflate(R.layout.info_main, null), "Ok");
                break;

        }
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        HiFiToyPreset preset = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

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
        AudioSource audioSource = HiFiToyControl.getInstance().getActiveDevice().getAudioSource();
        audioSource.setSourceWithWriteToDsp(state);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;

        HiFiToyPreset p = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

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

    DialogSystem.OnClickTextDialog savePresetHandler = new DialogSystem.OnClickTextDialog() {
        HiFiToyDevice device;
        HiFiToyPreset preset;


        @Override
        public void onPositiveClick(String name) {
            device = HiFiToyControl.getInstance().getActiveDevice();

            try {
                preset = device.getActivePreset().clone();
                preset.setName(name);
                preset.updateChecksum();

                //save new preset
                preset.save(false);

                //set preset active in device
                device.setActiveKeyPreset(preset.getName());
                //save new preset to cc2540
                preset.storeToPeripheral();

            } catch (Exception e) {

                if (e.getMessage().equals("Preset with this name already exist!")) {
                    DialogSystem.getInstance().showDialog(new DialogSystem.OnClickDialog() {
                                                              @Override
                                                              public void onPositiveClick() {
                                                                  try {
                                                                      //save preset with rewrite
                                                                      preset.save(true);

                                                                      //set preset active in device
                                                                      device.setActiveKeyPreset(preset.getName());
                                                                      //save new preset to cc2540
                                                                      preset.storeToPeripheral();
                                                                  } catch (Exception e) {
                                                                      DialogSystem.getInstance().showDialog("Error",
                                                                              "IOException. Preset was not saved successfully.", "Ok");
                                                                  }

                                                              }

                                                              @Override
                                                              public void onNegativeClick() {

                                                              }

                                                          }, "Warning", "Preset with this name already exists!",
                            "Rewrite", "Cancel");

                } else {
                    DialogSystem.getInstance().showDialog("Error",
                            "Exception. Preset was not saved successfully.", "Ok");
                }
            }
        }

        @Override
        public void onNegativeClick(String text) {
        }
    };


    //import xml preset
    private void checkImportPreset(Intent intent) {
        if ((intent == null) || (intent.getAction() == null)) return;

        if (intent.getAction().equals(Intent.ACTION_VIEW)){
            Uri uri = intent.getData();
            HiFiToyPresetManager.getInstance().importPreset(uri);
        }
    }

}
