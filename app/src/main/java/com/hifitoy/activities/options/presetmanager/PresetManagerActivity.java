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
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

        // Initializes list view adapter.
        if (mPresetListAdapter == null){
            mPresetListAdapter = new PresetListAdapter();
        }
        setListAdapter(mPresetListAdapter);

        mPresetListAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preset_manager_menu, menu);
        return true;
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.addPreset_outl:
                addNewPreset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, PresetDetailActivity.class);
        intent.putExtra("presetPosition", position);
        startActivity(intent);
    }


    private void addNewPreset() {
        //get active preset
        HiFiToyDevice device = HiFiToyControl.getInstance().getActiveDevice();
        HiFiToyPreset activePreset = device.getActivePreset();
        HiFiToyPreset preset;

        try {
            //copy active preset, rename and update checksum
            Date date = Calendar.getInstance().getTime();
            preset = activePreset.clone();
            preset.setName(date.toString());
            preset.updateChecksum();

            //restore current preset
            HiFiToyPresetManager.getInstance().restore();
            //add new preset to list and store
            HiFiToyPresetManager.getInstance().setPreset(preset.getName(), preset);

        } catch (CloneNotSupportedException e){
            Log.d(TAG, "DspPreset.clone() exception.");
            Toast.makeText(getApplicationContext(),
                    "DspPreset.clone() exception.", Toast.LENGTH_SHORT).show();
            return;
        }


        //set preset active in device
        device.setActiveKeyPreset(preset.getName());

        //save new preset to cc2540
        preset.storeToPeripheral();

        //update view
        mPresetListAdapter.notifyDataSetChanged();
    }

    // Adapter for holding devices found through scanning.
    private class PresetListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return HiFiToyPresetManager.getInstance().size();
        }

        @Override
        public Object getItem(int i) {
            return HiFiToyPresetManager.getInstance().getPreset(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView presetName_outl;

            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.preset_list_item, null);
                presetName_outl = view.findViewById(R.id.preset_name);
                view.setTag(presetName_outl);
            } else {
                presetName_outl = (TextView) view.getTag();
            }

            HiFiToyPreset preset = HiFiToyPresetManager.getInstance().getPreset(i);
            String key = HiFiToyPresetManager.getInstance().getKey(i);
            String activeKey = HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset();

            //set color
            if (key.equals(activeKey)){
                presetName_outl.setTextColor(0xFFFFFFFF);
            } else {
                presetName_outl.setTextColor(0xFFA0A0A0);
            }

            //set text
            String presetName;
            if (preset.getName().length() > 23){
                presetName = preset.getName().substring(0, 20) + "...";
            } else {
                presetName = preset.getName();
            }
            presetName_outl.setText(presetName);



            return view;
        }

    }

}
