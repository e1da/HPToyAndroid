/*
 *   AutoOffActivity.java
 *
 *   Created by Artem Khlyupin on 27/02/2019.
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.EnergyConfig;

public class AutoOffActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    final static String TAG = "HiFiToy";
    private static final int SYNC_MENU_ID = 1;

    LinearLayout    autoOffGroup_outl;
    TextView        autoOffLabel_outl;
    SeekBar         autoOffSeekBar_outl;
    TextView        clipLabel_outl;
    SeekBar         clipSeekBar_outl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_off);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, SYNC_MENU_ID, Menu.NONE, "Sync");
        MenuItem menuItem = menu.findItem(SYNC_MENU_ID);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case SYNC_MENU_ID:
                showEnergySyncDialog();
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

        EnergyConfig energy = HiFiToyControl.getInstance().getActiveDevice().getEnergyConfig();
        energy.readFromDsp();

        setupOutlets();
    }

    private void initOutlets() {
        autoOffGroup_outl    = findViewById(R.id.auto_off_group_outl);
        autoOffLabel_outl    = findViewById(R.id.autoOffLabel_outl);
        autoOffSeekBar_outl  = findViewById(R.id.autoOffSeekBar_outl);

        if (getPackageName().equals("com.hptoy")) {
            autoOffGroup_outl.setVisibility(View.GONE);
        } else {
            autoOffGroup_outl.setVisibility(View.VISIBLE);
        }

        clipLabel_outl    = findViewById(R.id.clipLabel_outl);
        clipSeekBar_outl  = findViewById(R.id.clipSeekBar_outl);

        autoOffSeekBar_outl.setOnSeekBarChangeListener(this);
        clipSeekBar_outl.setOnSeekBarChangeListener(this);
    }

    private void setupOutlets() {
        EnergyConfig energy = HiFiToyControl.getInstance().getActiveDevice().getEnergyConfig();

        setSeekBar(autoOffSeekBar_outl, energy.getLowThresholdDbPercent());
        autoOffLabel_outl.setText(String.format("%ddB", (int)energy.getLowThresholdDb()));
        setSeekBar(clipSeekBar_outl, energy.getHighThresholdDbPercent());
        clipLabel_outl.setText(String.format("%ddB", (int)energy.getHighThresholdDb()));
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;

        EnergyConfig energy = HiFiToyControl.getInstance().getActiveDevice().getEnergyConfig();

        if (seekBar.equals(autoOffSeekBar_outl)){
            energy.setLowThresholdDbPercent( getSeekBarPercent(autoOffSeekBar_outl) );
            autoOffLabel_outl.setText(String.format("%ddB", (int)energy.getLowThresholdDb()));

        } else if (seekBar.equals(clipSeekBar_outl)){
            energy.setHighThresholdDbPercent( getSeekBarPercent(clipSeekBar_outl) );
            clipLabel_outl.setText(String.format("%ddB", (int)energy.getHighThresholdDb()));
        }
    }
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setSeekBar(SeekBar seekBar, double percent){ //percent=[0.0 .. 1.0]
        int percentValue = (int)(percent * seekBar.getMax());
        seekBar.setProgress(percentValue);
    }
    private float getSeekBarPercent(SeekBar seekBar){ //percent=[0.0 .. 1.0]
        return (float)seekBar.getProgress() / seekBar.getMax();
    }


    private void showEnergySyncDialog() {
        DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
            public void onPositiveClick(){
                EnergyConfig energy = HiFiToyControl.getInstance().getActiveDevice().getEnergyConfig();
                energy.sendToDsp();

                Toast.makeText(ApplicationContext.getInstance().getContext(),
                        R.string.energy_sync, Toast.LENGTH_SHORT).show();
                setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
            public void onNegativeClick(){
                setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        };

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        DialogSystem.getInstance().showDialog(dialogListener, "Warning", "Are you sure want to sync energy manager?", "Sync", "Cancel");

    }

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HiFiToyControl.ENERGY_UPDATE);
        intentFilter.addAction(HiFiToyControl.CLIP_UPDATE);

        return intentFilter;
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.ENERGY_UPDATE.equals(action)) {
                setupOutlets();
            }
            if (HiFiToyControl.CLIP_UPDATE.equals(action)) {
                ApplicationContext.getInstance().updateClipView();
            }
        }
    };
}
