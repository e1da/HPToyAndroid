/*
 *   AdvertiseActivity.java
 *
 *   Created by Artem Khlyupin on 28/02/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.options;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.AdvertiseMode;

public class AdvertiseActivity extends Activity implements View.OnClickListener{
    RadioButton alwaysAdvertButton_outl;
    RadioButton after1MinAdvertButton_outl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise_mode);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);
        registerReceiver(broadcastReceiver, makeIntentFilter());

        AdvertiseMode advertiseMode = HiFiToyControl.getInstance().getActiveDevice().getAdvertiseMode();
        advertiseMode.readFromDsp();

        setupOutlets();
    }

    private void initOutlets() {
        alwaysAdvertButton_outl     = findViewById(R.id.alwaysAdvertButton_outl);
        after1MinAdvertButton_outl  = findViewById(R.id.after1MinAdvertButton_outl);

        alwaysAdvertButton_outl.setOnClickListener(this);
        after1MinAdvertButton_outl.setOnClickListener(this);
    }

    private void setupOutlets() {
        AdvertiseMode advertiseMode = HiFiToyControl.getInstance().getActiveDevice().getAdvertiseMode();
        if (advertiseMode.getMode() == AdvertiseMode.ALWAYS_ENABLED) {
            alwaysAdvertButton_outl.setChecked(true);
        } else {
            after1MinAdvertButton_outl.setChecked(true);
        }
    }

    public void onClick(View v) {
        AdvertiseMode advertiseMode = HiFiToyControl.getInstance().getActiveDevice().getAdvertiseMode();

        switch (v.getId()) {
            case R.id.alwaysAdvertButton_outl:
                advertiseMode.setMode(AdvertiseMode.ALWAYS_ENABLED);
                advertiseMode.sendToDsp();
                break;

            case R.id.after1MinAdvertButton_outl:
                advertiseMode.setMode(AdvertiseMode.AFTER_1MIN_DISABLED);
                advertiseMode.sendToDsp();
                break;
        }

    }


    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HiFiToyControl.ADVERTISE_MODE_UPDATE);
        intentFilter.addAction(HiFiToyControl.CLIP_UPDATE);

        return intentFilter;
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.ADVERTISE_MODE_UPDATE.equals(action)) {
                setupOutlets();
            }
            if (HiFiToyControl.CLIP_UPDATE.equals(action)) {
                ApplicationContext.getInstance().updateClipView();
            }
        }
    };
}
