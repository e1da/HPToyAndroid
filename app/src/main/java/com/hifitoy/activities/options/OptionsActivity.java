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
import com.hifitoy.activities.options.presetmanager.PresetManagerActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;

public class OptionsActivity extends Activity implements View.OnClickListener {
    final static String TAG = "HiFiToy";

    LinearLayout deviceNameLayout_outl;
    TextView deviceNameLabel_outl;
    TextView deviceMacLabel_outl;
    TextView restoreFactorySettings_outl;
    TextView changePairingCode_outl;
    TextView presetManager_outl;
    LinearLayout autoOff_outl;
    LinearLayout advertiseMode_outl;

    HiFiToyDevice hifiToyDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

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

        hifiToyDevice = HiFiToyControl.getInstance().getActiveDevice();

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initOutlets() {
        deviceNameLayout_outl       = findViewById(R.id.deviceNameLayout_outl);
        deviceNameLabel_outl        = findViewById(R.id.deviceNameLabel_outl);
        deviceMacLabel_outl         = findViewById(R.id.deviceMacLabel_outl);
        restoreFactorySettings_outl = findViewById(R.id.restoreFactorySettings_outl);
        changePairingCode_outl      = findViewById(R.id.changePairingCode_outl);
        presetManager_outl          = findViewById(R.id.presetManager_outl);
        autoOff_outl                = findViewById(R.id.autoOff_outl);
        advertiseMode_outl          = findViewById(R.id.advertiseMode_outl);


        deviceNameLayout_outl.setOnClickListener(this);
        restoreFactorySettings_outl.setOnClickListener(this);
        changePairingCode_outl.setOnClickListener(this);
        presetManager_outl.setOnClickListener(this);
        autoOff_outl.setOnClickListener(this);
        advertiseMode_outl.setOnClickListener(this);
    }

    private void setupOutlets() {
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
                DialogSystem.getInstance().showFactoryResetDialog();
                break;
            case R.id.changePairingCode_outl:
                showChangePairingCodeDialog();
                break;
            case R.id.presetManager_outl:
                intent = new Intent(OptionsActivity.this, PresetManagerActivity.class);
                startActivity(intent);
                break;
            case R.id.autoOff_outl:
                intent = new Intent(OptionsActivity.this, AutoOffActivity.class);
                startActivity(intent);
                break;
            case R.id.advertiseMode_outl:
                intent = new Intent(OptionsActivity.this, AdvertiseActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*--------------------------- Dialogs implementation ------------------------------*/
    public void showChangePairingCodeDialog(){
        final Context context = ApplicationContext.getInstance().getContext();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Enter new pairing code");

        LinearLayout view = (LinearLayout)getLayoutInflater().inflate(R.layout.change_paircode, null);
        final EditText oldPairInput = view.findViewById(R.id.oldPairInput);
        final EditText newPairInput = view.findViewById(R.id.newPairInput);
        final EditText confirmPairInput = view.findViewById(R.id.confirmPairInput);

        alertDialog.setView(view);

        alertDialog.setPositiveButton("Change",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
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
                        } finally {
                            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        }
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                });

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        Dialog dialog = alertDialog.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
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

    /*--------------------------- Broadcast receiver implementation ------------------------------*/
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HiFiToyControl.DID_CONNECT);
        intentFilter.addAction(HiFiToyControl.DID_DISCONNECT);

        return intentFilter;
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.DID_CONNECT.equals(action)){
                setupOutlets();
            }
            if (HiFiToyControl.DID_DISCONNECT.equals(action)){
                setupOutlets();
            }
        }
    };

}
