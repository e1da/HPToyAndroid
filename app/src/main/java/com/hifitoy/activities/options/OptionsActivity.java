/*
 *   OptionsActivity.java
 *
 *   Created by Artem Khlyupin on 28/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.activities.options;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;

public class OptionsActivity extends Activity implements View.OnClickListener {
    final static String TAG = "HiFiToy";

    LinearLayout deviceNameLayout_outl;
    TextView deviceNameLabel_outl;
    TextView deviceMacLabel_outl;
    TextView restoreFactorySettings_outl;
    TextView changePairingCode_outl;
    TextView presetManager_outl;
    TextView autoOff_outl;

    HiFiToyDevice hifiToyDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

        registerReceiver(mBleReceiver, makeGattUpdateIntentFilter());

        hifiToyDevice = HiFiToyControl.getInstance().getActiveDevice();

        setupOutlets();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBleReceiver);
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

    private void setupOutlets() {
        deviceNameLayout_outl       = findViewById(R.id.deviceNameLayout_outl);
        deviceNameLabel_outl        = findViewById(R.id.deviceNameLabel_outl);
        deviceMacLabel_outl         = findViewById(R.id.deviceMacLabel_outl);
        restoreFactorySettings_outl = findViewById(R.id.restoreFactorySettings_outl);
        changePairingCode_outl      = findViewById(R.id.changePairingCode_outl);
        presetManager_outl          = findViewById(R.id.presetManager_outl);
        autoOff_outl                = findViewById(R.id.autoOff_outl);

        deviceNameLayout_outl.setOnClickListener(this);
        restoreFactorySettings_outl.setOnClickListener(this);
        changePairingCode_outl.setOnClickListener(this);
        presetManager_outl.setOnClickListener(this);
        autoOff_outl.setOnClickListener(this);

        deviceNameLabel_outl.setText(hifiToyDevice.getName());
        deviceMacLabel_outl.setText(hifiToyDevice.getMac());
    }

    public void onClick(View v) {

        Intent intent;

        switch (v.getId()) {
            case R.id.deviceNameLayout_outl:
                //showChangeDeviceNameDialog();
                break;
            case R.id.restoreFactorySettings_outl:
                //DialogSystem.getInstance().showFactoryResetDialog();
                break;
            case R.id.changePairingCode_outl:
                //showChangePairingCodeDialog();
                break;
            case R.id.presetManager_outl:
                //intent = new Intent(OptionsActivity.this, PresetManagerActivity.class);
                //startActivity(intent);
                break;
            case R.id.autoOff_outl:
                //intent = new Intent(OptionsActivity.this, AutoOnActivity.class);
                //startActivity(intent);
                break;
        }
    }

    /*--------------------------- Dialogs implementation ------------------------------*/
    /*public void showChangePairingCodeDialog(){
        DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
            public void onPositiveClick(String text){
                try {
                    int pair_code = Integer.parseInt(text);
                    Log.d(TAG, String.format("New pair code = %d", pair_code));

                    dspDevice.setPairingCode(pair_code);
                    DspDeviceManager.getInstance().storeDspDeviceList();

                    //send pairing code to dsp
                    DspControl.getInstance().sendNewPairingCode(pair_code);
                } catch (NumberFormatException e) {
                    Toast.makeText(ApplicationContext.getInstance().getContext(),
                            "The value of a pair code is not allowed.", Toast.LENGTH_SHORT).show();
                } finally {
                    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
            public void onNegativeClick(String text){
                setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        };

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        DialogSystem.getInstance().showTextDialog(dialogListener, "Enter new pairing code", "Change", "Cancel", true);
    }*/

    /*void showChangeDeviceNameDialog(){
        DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
            public void onPositiveClick(String name){
                if (name.length() > 0) {
                    dspDevice.setName(name);
                    DspDeviceManager.getInstance().storeDspDeviceList();

                    deviceNameLabel_outl.setText(String.format("%s", dspDevice.getName()));
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
        DialogSystem.getInstance().showTextDialog(dialogListener, "Enter new name", "Change", "Cancel", false);
    }*/

    /*--------------------------- Broadcast receiver implementation ------------------------------*/
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(BleService.BLE_DID_CONNECTED);
        //intentFilter.addAction(BleService.BLE_DID_DISCONNECTED);

        return intentFilter;
    }
    public final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            /*if (BleService.BLE_DID_CONNECTED.equals(action)){
                deviceMacLabel_outl.setText("Connected");
            }
            if (BleService.BLE_DID_DISCONNECTED.equals(action)){
                deviceMacLabel_outl.setText("Disconnected");
            }*/
        }
    };

}
