/*
 *   OutputModeActivity.java
 *
 *   Created by Artem Khlyupin on 09/02/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.options;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.hifitoy.R;
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.OutputMode;

public class OutputModeActivity extends BaseActivity implements View.OnClickListener {
    RadioButton balanceButton;
    RadioButton unbalanceButton;
    RadioButton unbalanceBoostButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Output mode");
        setContentView(R.layout.activity_output_mode);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();
    }

    @Override
    protected void onResume() {
        super.onResume();

        OutputMode outputMode = HiFiToyControl.getInstance().getActiveDevice().getOutputMode();
        outputMode.readFromDsp();

        setupOutlets();
    }

    private void initOutlets() {
        balanceButton     = findViewById(R.id.balanceModeButton_outl);
        unbalanceButton  = findViewById(R.id.unbalanceModeButton_outl);
        unbalanceBoostButton  = findViewById(R.id.unbalanceBoostModeButton_outl);

        balanceButton.setOnClickListener(this);
        unbalanceButton.setOnClickListener(this);
        unbalanceBoostButton.setOnClickListener(this);
    }

    @Override
    public void setupOutlets() {
        OutputMode outputMode = HiFiToyControl.getInstance().getActiveDevice().getOutputMode();

        switch (outputMode.getValue()) {
            case OutputMode.BALANCE_OUT_MODE:
                balanceButton.setChecked(true);
                break;

            case OutputMode.UNBALANCE_OUT_MODE:
                unbalanceButton.setChecked(true);
                break;

            case OutputMode.UNBALANCE_BOOST_OUT_MODE:
                unbalanceBoostButton.setChecked(true);
                break;
        }
    }

    public void onClick(View v) {
        OutputMode outputMode = HiFiToyControl.getInstance().getActiveDevice().getOutputMode();

        switch (v.getId()) {
            case R.id.balanceModeButton_outl:
                String balWarningMessage = getResources().getString(R.string.balance_warning);

                DialogSystem.getInstance().showDialog(new DialogSystem.OnClickDialog() {
                    @Override
                    public void onPositiveClick() {
                        OutputMode outputMode = HiFiToyControl.getInstance().getActiveDevice().getOutputMode();
                        outputMode.setValue(OutputMode.BALANCE_OUT_MODE);
                        outputMode.sendToDsp();
                    }

                    @Override
                    public void onNegativeClick() {
                        setupOutlets();
                    }
                }, "Warning", balWarningMessage, "Set", "Cancel");

                break;

            case R.id.unbalanceModeButton_outl:
                outputMode.setValue(OutputMode.UNBALANCE_OUT_MODE);
                outputMode.sendToDsp();
                break;
            case R.id.unbalanceBoostModeButton_outl:
                outputMode.setValue(OutputMode.UNBALANCE_BOOST_OUT_MODE);
                outputMode.sendToDsp();
                break;
        }

    }
}
