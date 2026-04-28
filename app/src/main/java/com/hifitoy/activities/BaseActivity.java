/*
 *   BaseActivity.java
 *
 *   Created by Artem Khlyupin on 26/11/2020
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities;

import android.Manifest;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.dialogsystem.DiscoveryDialog;

public abstract class BaseActivity extends ComponentActivity {
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init action bar
        ActionBar actionBar = getActionBar();

        LayoutInflater mInflater = LayoutInflater.from(this);
        View actionBarView = mInflater.inflate(R.layout.action_bar_default, null);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayShowCustomEnabled(true);

        title = actionBarView.findViewById(R.id.title_outl);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        this.title.setText(title);
    }

    public void setTitleTextSize(int sp) {
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }

    public void setupOutlets() {

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        SystemBarInsetsHelper.applyTopInsetPadding(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        SystemBarInsetsHelper.applyTopInsetPadding(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        SystemBarInsetsHelper.applyTopInsetPadding(this);
    }

}
