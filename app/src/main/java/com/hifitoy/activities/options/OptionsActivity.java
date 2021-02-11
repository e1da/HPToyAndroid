/*
 *   OptionsActivity.java
 *
 *   Created by Artem Khlyupin on 28/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.activities.options;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.activities.options.presetmanager.PresetManagerActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;

public class OptionsActivity extends BaseActivity implements View.OnClickListener {
    final static String TAG = "HiFiToy";

    LinearLayout deviceNameLayout_outl;
    TextView deviceNameLabel_outl;
    TextView deviceMacLabel_outl;
    TextView restoreFactorySettings_outl;
    TextView changePairingCode_outl;
    LinearLayout autoOff_outl;
    LinearLayout advertiseMode_outl;
    LinearLayout outputMode_outl;

    HiFiToyDevice hifiToyDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Options");
        setContentView(R.layout.activity_options);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();
    }

    @Override
    protected void onResume() {
        super.onResume();

        hifiToyDevice = HiFiToyControl.getInstance().getActiveDevice();

        setupOutlets();
    }

    private void initOutlets() {
        deviceNameLayout_outl       = findViewById(R.id.deviceNameLayout_outl);
        deviceNameLabel_outl        = findViewById(R.id.deviceNameLabel_outl);
        deviceMacLabel_outl         = findViewById(R.id.deviceMacLabel_outl);
        restoreFactorySettings_outl = findViewById(R.id.restoreFactorySettings_outl);
        changePairingCode_outl      = findViewById(R.id.changePairingCode_outl);
        autoOff_outl                = findViewById(R.id.autoOff_outl);
        advertiseMode_outl          = findViewById(R.id.advertiseMode_outl);
        outputMode_outl             = findViewById(R.id.outputMode_outl);

        deviceNameLayout_outl.setOnClickListener(this);
        restoreFactorySettings_outl.setOnClickListener(this);
        changePairingCode_outl.setOnClickListener(this);
        autoOff_outl.setOnClickListener(this);
        advertiseMode_outl.setOnClickListener(this);
        outputMode_outl.setOnClickListener(this);
    }

    @Override
    public void setupOutlets() {
        deviceNameLabel_outl.setText(hifiToyDevice.getName());
        deviceMacLabel_outl.setText(hifiToyDevice.getMac());
    }

    public void onClick(View v) {

        Intent intent;

        switch (v.getId()) {
            case R.id.deviceNameLayout_outl:
                showChangeDeviceNameDialog();

                break;
            case R.id.restoreFactorySettings_outl:
                showRestoreFactoryDialog();
                break;

            case R.id.changePairingCode_outl:
                showChangePairingCodeDialog();
                break;
            case R.id.autoOff_outl:
                intent = new Intent(OptionsActivity.this, AutoOffActivity.class);
                startActivity(intent);
                break;
            case R.id.advertiseMode_outl:
                intent = new Intent(OptionsActivity.this, AdvertiseActivity.class);
                startActivity(intent);
                break;
            case R.id.outputMode_outl:
                intent = new Intent(OptionsActivity.this, OutputModeActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*--------------------------- Dialogs implementation ------------------------------*/
    public void showRestoreFactoryDialog(){
        DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
            public void onPositiveClick(){
                HiFiToyControl.getInstance().getActiveDevice().restoreFactorySettings();
            }
            public void onNegativeClick(){
            }
        };

        DialogSystem.getInstance().showDialog(dialogListener, "Warning",
                "Are you sure you want to reset to factory defaults?",
                "Ok", "Cancel");
    }

    public void showChangePairingCodeDialog(){
        LinearLayout view = (LinearLayout)getLayoutInflater().inflate(R.layout.change_paircode, null);
        final EditText oldPairInput = view.findViewById(R.id.oldPairInput);
        final EditText newPairInput = view.findViewById(R.id.newPairInput);
        final EditText confirmPairInput = view.findViewById(R.id.confirmPairInput);

        DialogSystem.getInstance().showDialog(new DialogSystem.OnClickDialog() {
            @Override
            public void onPositiveClick() {
                try {
                    int oldPair = Integer.parseInt(oldPairInput.getText().toString());
                    int newPair = Integer.parseInt(newPairInput.getText().toString());
                    int confirmPair = Integer.parseInt(confirmPairInput.getText().toString());

                    if (newPair == confirmPair) {
                        if (hifiToyDevice.getPairingCode() == oldPair) {
                            hifiToyDevice.setPairingCode(newPair);
                            Log.d(TAG, String.format("New pair code = %d", hifiToyDevice.getPairingCode()));

                            HiFiToyDeviceManager.getInstance().store();
                            //send pairing code to dsp
                            HiFiToyControl.getInstance().sendNewPairingCode(hifiToyDevice.getPairingCode());

                            Toast.makeText(ApplicationContext.getInstance().getContext(),
                                    "Pair code changed successfully!", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(ApplicationContext.getInstance().getContext(),
                                    "Error. Old pair code is not true.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ApplicationContext.getInstance().getContext(),
                                "Error. Confirm and New strings are not equal.", Toast.LENGTH_SHORT).show();
                    }

                } catch (NumberFormatException e) {
                    Toast.makeText(ApplicationContext.getInstance().getContext(),
                            "Error. The value of a pair code is not allowed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNegativeClick() {

            }
        }, "Enter new pairing code", view, "Change", "Cancel");
    }

    void showChangeDeviceNameDialog(){
        DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
            public void onPositiveClick(String name){
                if (name.length() > 0) {
                    hifiToyDevice.setName(name);
                    HiFiToyDeviceManager.getInstance().store();

                    deviceNameLabel_outl.setText(String.format("%s", hifiToyDevice.getName()));
                    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Name field is empty.", Toast.LENGTH_SHORT).show();
                    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
            public void onNegativeClick(String text){
                setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        };

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        DialogSystem.getInstance().showTextDialog(dialogListener, "Enter new name", "Change", "Cancel");
    }

}
