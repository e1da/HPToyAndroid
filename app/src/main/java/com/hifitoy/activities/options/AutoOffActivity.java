/*
 *   AutoOffActivity.java
 *
 *   Created by Artem Khlyupin on 27/02/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.activities.options;

import android.app.ActionBar;
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
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.EnergyConfig;
import com.hifitoy.widgets.Slider;

public class AutoOffActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    final static String TAG = "HiFiToy";
    private static final int SYNC_MENU_ID = 1;

    LinearLayout    autoOffGroup_outl;
    TextView        autoOffLabel_outl;
    Slider          autoOffSeekBar_outl;
    TextView        clipLabel_outl;
    Slider          clipSeekBar_outl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Clip threshold");
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
        switch (item.getItemId()) {
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

    @Override
    public void setupOutlets() {
        EnergyConfig energy = HiFiToyControl.getInstance().getActiveDevice().getEnergyConfig();

        autoOffSeekBar_outl.setPercent(energy.getLowThresholdDbPercent());
        autoOffLabel_outl.setText(String.format("%ddB", (int)energy.getLowThresholdDb()));
        clipSeekBar_outl.setPercent(energy.getHighThresholdDbPercent());
        clipLabel_outl.setText(String.format("%ddB", (int)energy.getHighThresholdDb()));
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;

        EnergyConfig energy = HiFiToyControl.getInstance().getActiveDevice().getEnergyConfig();

        if (seekBar.equals(autoOffSeekBar_outl)){
            energy.setLowThresholdDbPercent( autoOffSeekBar_outl.getPercent() );
            autoOffLabel_outl.setText(String.format("%ddB", (int)energy.getLowThresholdDb()));

        } else if (seekBar.equals(clipSeekBar_outl)){
            energy.setHighThresholdDbPercent( clipSeekBar_outl.getPercent() );
            clipLabel_outl.setText(String.format("%ddB", (int)energy.getHighThresholdDb()));
        }
    }
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    public void onStopTrackingTouch(SeekBar seekBar) {

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
}
