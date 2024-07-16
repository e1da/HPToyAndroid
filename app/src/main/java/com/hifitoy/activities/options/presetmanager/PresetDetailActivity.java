/*
 *   PresetdetailActivity.java
 *
 *   Created by Artem Khlyupin on 04/09/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.options.presetmanager;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.activities.BaseActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.R;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;
import com.hifitoy.hifitoydevice.ToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class PresetDetailActivity extends BaseActivity implements View.OnClickListener{
    final static String TAG = "HiFiToy";

    ToyPreset preset;

    LinearLayout    presetNameLayout_outl;
    TextView        presetName_outl;
    LinearLayout    setActivePresetLayout_outl;
    TextView        setActivePreset_outl;
    TextView        exportPreset_outl;
    LinearLayout    deletePresetLayout_outl;
    TextView        deletePreset_outl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Preset detail");
        setContentView(R.layout.activity_preset_detail);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();

        try {
            String presetName = getIntent().getStringExtra("presetName");
            preset = HiFiToyPresetManager.getInstance().getPreset(presetName);

        } catch (IOException | XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupOutlets();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Request code:" + requestCode + " Result code:" + resultCode);
        if (data != null) {
            Log.d(TAG, data.toString());
        }
    }


    private void initOutlets() {
        presetNameLayout_outl       = findViewById(R.id.presetNameLayout_outl);
        presetName_outl             = findViewById(R.id.presetNameLabel_outl);
        setActivePresetLayout_outl  = findViewById(R.id.setActivePresetLayout_outl);
        setActivePreset_outl        = findViewById(R.id.setActivePreset_outl);
        exportPreset_outl           = findViewById(R.id.exportPresetToEmail_outl);
        deletePresetLayout_outl     = findViewById(R.id.deletePresetLayout_outl);
        deletePreset_outl           = findViewById(R.id.deletePreset_outl);

        presetNameLayout_outl.setOnClickListener(this);
        setActivePreset_outl.setOnClickListener(this);
        exportPreset_outl.setOnClickListener(this);
        deletePreset_outl.setOnClickListener(this);
    }

    @Override
    public void setupOutlets() {
        String presetName;
        if (preset.getName().length() > 23){
            presetName = preset.getName().substring(0, 20) + "...";
        } else {
            presetName = preset.getName();
        }
        presetName_outl.setText(presetName);

        HiFiToyDevice activeDevice = HiFiToyControl.getInstance().getActiveDevice();
        if (preset.getName().equals(activeDevice.getActiveKeyPreset())){
            setActivePresetLayout_outl.setVisibility(View.GONE);
        } else {
            setActivePresetLayout_outl.setVisibility(View.VISIBLE);
        }

        if ( (preset.getName().equals("No processing")) ||
                (preset.getName().equals(activeDevice.getActiveKeyPreset())) ) {
            deletePresetLayout_outl.setVisibility(View.INVISIBLE);
        } else {
            deletePresetLayout_outl.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.presetNameLayout_outl:
                showChangePresetNameDialog();
                break;
            case R.id.setActivePreset_outl:
                setActivePreset();
                break;
            case R.id.exportPresetToEmail_outl:
                exportPreset();
                break;
            case R.id.deletePreset_outl:
                deletePreset();
                break;
        }
    }

    void showChangePresetNameDialog(){
        if (preset.getName().equals("No processing")){//rename is not available for No processing
            return;
        }

        DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
            public void onPositiveClick(String newName){
                if (newName.length() > 0) {

                    try {
                        String oldName = preset.getName();
                        HiFiToyPresetManager.getInstance().renamePreset(oldName, newName);

                        HiFiToyDevice d = HiFiToyControl.getInstance().getActiveDevice();
                        if (d.getActiveKeyPreset().equals(oldName)) {
                            d.setActiveKeyPreset(newName);
                        }

                        preset = HiFiToyPresetManager.getInstance().getPreset(newName);
                        presetName_outl.setText(newName);

                    } catch (IOException | XmlPullParserException e) {
                        DialogSystem.getInstance().showDialog("Error", e.getMessage(), "Ok");
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Name field is empty.", Toast.LENGTH_SHORT).show();
                }
            }
            public void onNegativeClick(String text){
            }
        };

        DialogSystem.getInstance().showTextDialog(dialogListener, "Enter new name", preset.getName(), "Change", "Cancel");
    }

    private void setActivePreset(){
        final HiFiToyDevice device = HiFiToyControl.getInstance().getActiveDevice();

        if (preset.getName().equals(device.getActiveKeyPreset())){
            return;
        }

        DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
            public void onPositiveClick(){
                device.setActiveKeyPreset(preset.getName());
                preset.storeToPeripheral();
                setupOutlets();
            }
            public void onNegativeClick(){
                //
            }
        };

        DialogSystem.getInstance().showDialog(dialogListener,
                "Warning",
                "Are you sure you want to load '" + preset.getName() + "' preset?",
                "Ok", "Cancel");

    }

    private void deletePreset(){
        HiFiToyDevice device = HiFiToyControl.getInstance().getActiveDevice();

        if ( (preset.getName().equals("No processing")) || (preset.getName().equals(device.getActiveKeyPreset())) ) {
            return;
        }

        DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
            public void onPositiveClick(){

                //check if remove preset is active for anyone device
                Collection<HiFiToyDevice> devs = HiFiToyDeviceManager.getInstance().getDevices();
                for (HiFiToyDevice dev : devs) {
                    if ( dev.getActivePreset().getName().equals(preset.getName()) ) {
                        dev.setActiveKeyPreset("No processing");
                    }
                }
                HiFiToyPresetManager.getInstance().deletePreset(preset.getName());
                finish();

            }
            public void onNegativeClick(){
                //
            }
        };

        DialogSystem.getInstance().showDialog(dialogListener,
                "Warning",
                "Are you sure you want to delete '" + preset.getName() + "' preset?",
                "Ok", "Cancel");

    }

    private void exportPreset() {
        File file = HiFiToyPresetManager.getInstance().getUserPresetFile(preset.getName());

        if (file != null) {
            Uri uri = FileProvider.getUriForFile(this,
                    getString(R.string.fileprovider_authority), file);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_extra_subject));
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setType("application/tpr");
            startActivityForResult(Intent.createChooser(sendIntent, "Send To"), 1);
        }
    }

}
