/*
 *   PresetManagerActivity.java
 *
 *   Created by Artem Khlyupin on 04/09/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.options.presetmanager;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.options.presetmanager.linkimporttool.LinkImportActivity;
import com.hifitoy.activities.options.presetmanager.mergetool.MergeToolActivity;
import com.hifitoy.activities.options.presetmanager.textimporttool.PresetTextImportActivity;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;

import java.util.Calendar;
import java.util.Date;

public class PresetManagerActivity extends ListActivity {
    final static String TAG = "HiFiToy";

    private PresetListAdapter mPresetListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initializes list view adapter.
        mPresetListAdapter = new PresetListAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

        // Set list view adapter.
        setListAdapter(mPresetListAdapter);
        mPresetListAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.presets_menu, menu);
        return true;
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.mergeToolActivity_outl:
                Intent intent = new Intent(this, MergeToolActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (position == mPresetListAdapter.getCount() - 1) {
            Intent intent = new Intent(this, PresetTextImportActivity.class);
            startActivity(intent);

        } else if (position == mPresetListAdapter.getCount() - 2) {
            Intent intent = new Intent(this, LinkImportActivity.class);
            startActivity(intent);

        } else {
            Intent intent = new Intent(this, PresetDetailActivity.class);
            intent.putExtra("presetPosition", position);
            startActivity(intent);
        }
    }

    // Adapter for holding devices found through scanning.
    private class PresetListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return HiFiToyPresetManager.getInstance().size() + 2;
        }

        @Override
        public Object getItem(int i) {
            if (i < HiFiToyPresetManager.getInstance().size()) {
                return HiFiToyPresetManager.getInstance().getPreset(i);
            }
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView presetName_outl;

            if (i == getCount() - 1) {
                view = getLayoutInflater().inflate(R.layout.text_import_item, null);

            } else if (i == getCount() - 2) {
                view = getLayoutInflater().inflate(R.layout.direct_link_import_item, null);

            } else {
                view = getLayoutInflater().inflate(R.layout.preset_list_item, null);
                presetName_outl = view.findViewById(R.id.preset_name);

                String presetName = HiFiToyPresetManager.getInstance().getPreset(i).getName();
                String activeName = HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset();

                //set color
                if (presetName.equals(activeName)) {
                    presetName_outl.setTextColor(0xFFFFFFFF);
                } else {
                    presetName_outl.setTextColor(0xFFA0A0A0);
                }

                //set text
                presetName_outl.setText(presetName);

            }


            return view;
        }

    }

}
