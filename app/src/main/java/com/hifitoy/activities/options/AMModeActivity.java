/*
 *   AMModeActivity.java
 *
 *   Created by Artem Khlyupin on 17/05/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.options;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.dialogsystem.KeyboardDialog;
import com.hifitoy.dialogsystem.KeyboardNumber;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.AdvertiseMode;
import com.hifitoy.hifitoynumbers.ByteUtility;
import com.hifitoy.hifitoyobjects.AMMode;

public class AMModeActivity extends BaseActivity implements View.OnClickListener,
                                                    KeyboardDialog.OnResultListener {
    final static String TAG = "HiFiToy";

    private TextView[] amModeData = new TextView[4];
    private TextView[] amModeHexData = new TextView[4];
    private TextView[] amModeBinData = new TextView[4];

    private LinearLayout[] amModeDataInput = new LinearLayout[4];

    private TextView amModeSendButton;
    private TextView amModeStoreButton;

    private AMMode amMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Am mode");
        setContentView(R.layout.activity_am_mode);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        amMode = HiFiToyControl.getInstance().getActiveDevice().getAmMode();
        amMode.readFromDsp();

        initOutlets();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AdvertiseMode advertiseMode = HiFiToyControl.getInstance().getActiveDevice().getAdvertiseMode();
        advertiseMode.readFromDsp();

        setupOutlets();
    }

    private void initOutlets() {
        amModeData[0] = findViewById(R.id.amModeD0_outl);
        amModeData[1] = findViewById(R.id.amModeD1_outl);
        amModeData[2] = findViewById(R.id.amModeD2_outl);
        amModeData[3] = findViewById(R.id.amModeD3_outl);

        amModeHexData[0] = findViewById(R.id.amModeHexD0_outl);
        amModeHexData[1] = findViewById(R.id.amModeHexD1_outl);
        amModeHexData[2] = findViewById(R.id.amModeHexD2_outl);
        amModeHexData[3] = findViewById(R.id.amModeHexD3_outl);

        amModeBinData[0] = findViewById(R.id.amModeBinD0_outl);
        amModeBinData[1] = findViewById(R.id.amModeBinD1_outl);
        amModeBinData[2] = findViewById(R.id.amModeBinD2_outl);
        amModeBinData[3] = findViewById(R.id.amModeBinD3_outl);

        amModeDataInput[0] = findViewById(R.id.amModeD0Input_outl);
        amModeDataInput[1] = findViewById(R.id.amModeD1Input_outl);
        amModeDataInput[2] = findViewById(R.id.amModeD2Input_outl);
        amModeDataInput[3] = findViewById(R.id.amModeD3Input_outl);

        amModeSendButton = findViewById(R.id.amModeSendButton_outl);
        amModeStoreButton = findViewById(R.id.amModeStoreButton_outl);

        amModeDataInput[0].setOnClickListener(this);
        amModeDataInput[1].setOnClickListener(this);
        amModeDataInput[2].setOnClickListener(this);
        amModeDataInput[3].setOnClickListener(this);

        amModeSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amMode.sendToPeripheral(true);
                ApplicationContext.getInstance().showToast("Data sent to RAM!");
            }
        });
        amModeStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amMode.storeToPeripheral();
                ApplicationContext.getInstance().showToast("Data store to hardware!");
            }
        });
    }

    @Override
    public void setupOutlets() {
        byte d0 = amMode.getData(0);
        byte d1 = amMode.getData(1);
        byte d2 = amMode.getData(2);
        byte d3 = amMode.getData(3);

        amModeData[0].setText(ByteUtility.toString(d0));
        amModeData[1].setText(ByteUtility.toString(d1));
        amModeData[2].setText(ByteUtility.toString(d2));
        amModeData[3].setText(ByteUtility.toString(d3));

        amModeHexData[0].setText("0x" + ByteUtility.toHexString(d0));
        amModeHexData[1].setText("0x" + ByteUtility.toHexString(d1));
        amModeHexData[2].setText("0x" + ByteUtility.toHexString(d2));
        amModeHexData[3].setText("0x" + ByteUtility.toHexString(d3));

        amModeBinData[0].setText(ByteUtility.toBinString(d0));
        amModeBinData[1].setText(ByteUtility.toBinString(d1));
        amModeBinData[2].setText(ByteUtility.toBinString(d2));
        amModeBinData[3].setText(ByteUtility.toBinString(d3));
    }

    @Override
    public void onClick(View v) {
        byte val = 0;
        String tag = "";

        switch (v.getId()) {
            case R.id.amModeD0Input_outl:
                val = amMode.getData(0);
                tag = "d0";
                break;
            case R.id.amModeD1Input_outl:
                val = amMode.getData(1);
                tag = "d1";
                break;
            case R.id.amModeD2Input_outl:
                val = amMode.getData(2);
                tag = "d2";
                break;
            case R.id.amModeD3Input_outl:
                val= amMode.getData(3);
                tag = "d3";
                break;
            default:
                return;
        }

        KeyboardNumber n = new KeyboardNumber(KeyboardNumber.NumberType.POSITIVE_INTEGER, ByteUtility.toString(val));
        new KeyboardDialog(this, this, n, tag).show();
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        try {
            int valInt = Integer.parseInt(result.getValue());
            byte val = ByteUtility.getIntLSB(valInt);

            if (tag.equals("d0")) {
                amMode.setData(0, val);
            } else if (tag.equals("d1")) {
                amMode.setData(1, val);
            } else if (tag.equals("d2")) {
                amMode.setData(2, val);
            } else if (tag.equals("d3")) {
                amMode.setData(3, val);
            }

            setupOutlets();

        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }
    }
}
