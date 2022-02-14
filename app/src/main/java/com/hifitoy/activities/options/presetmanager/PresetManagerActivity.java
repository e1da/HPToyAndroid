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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.options.presetmanager.linkimporttool.LinkImportActivity;
import com.hifitoy.activities.options.presetmanager.mergetool.MergeToolActivity;
import com.hifitoy.activities.options.presetmanager.textimporttool.PresetTextImportActivity;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;

public class PresetManagerActivity extends ListActivity {
    final static String TAG = "HiFiToy";

    private PresetListAdapter mPresetListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater mInflater = LayoutInflater.from(this);
        View actionBarView = mInflater.inflate(R.layout.action_bar_default, null);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayShowCustomEnabled(true);

        TextView title = actionBarView.findViewById(R.id.title_outl);
        title.setText("Preset manager");
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.importPreset_outl:
                intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Preset"), 1);
                return true;

            case R.id.mergeToolActivity_outl:
                intent = new Intent(this, MergeToolActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( (resultCode == RESULT_OK) && (requestCode == 1) ) {
            Uri uri = data.getData();
            HiFiToyPresetManager.getInstance().importPreset(uri);

        } else {
            Log.d(TAG, "Not get result");
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, PresetDetailActivity.class);
        intent.putExtra("presetPosition", position);
        startActivity(intent);
    }

    // Adapter for holding devices found through scanning.
    private class PresetListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return HiFiToyPresetManager.getInstance().size();
        }

        @Override
        public String getItem(int i) {
            if (i < HiFiToyPresetManager.getInstance().size()) {
                return HiFiToyPresetManager.getInstance().getPresetNameList().get(i);
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

            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.preset_list_item, null);
            }
            presetName_outl = view.findViewById(R.id.preset_name);

            String presetName = getItem(i);
            String activeName = HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset();

            //set color
            if (presetName.equals(activeName)) {
                presetName_outl.setTextColor(0xFFFFFFFF);
            } else {
                presetName_outl.setTextColor(0xFFA0A0A0);
            }

            //set text
            presetName_outl.setText(presetName);
            return view;
        }

    }

}
