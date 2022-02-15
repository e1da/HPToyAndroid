/*
 *   PresetManagerActivity.java
 *
 *   Created by Artem Khlyupin on 04/09/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.options.presetmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.options.presetmanager.mergetool.MergeToolActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.ToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;
import com.hifitoy.widgets.SegmentedControlWidget;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class PresetManagerActivity extends Activity {
    final static String TAG = "HiFiToy";

    private PresetListAdapter       userPresetListAdapter;
    private PresetListAdapter       officialPresetListAdapter;

    private ListView                presetListView;
    private SegmentedControlWidget  presetTypeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

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

        userPresetListAdapter = new PresetListAdapter(PresetListAdapter.Type.USER);
        officialPresetListAdapter = new PresetListAdapter(PresetListAdapter.Type.OFFICIAL);
        initOutlets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

        setupOutlets();
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

    private void initOutlets() {
        presetTypeSwitch = findViewById(R.id.presetTypeWidget_outl);

        presetListView = findViewById(R.id.presetListView_outl);
        presetListView.setAdapter(userPresetListAdapter);

        //init switch and active adapter
        String activePresetName = HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset();
        if (HiFiToyPresetManager.getInstance().isUserPresetExist(activePresetName)) {
            presetTypeSwitch.check(0);
            presetListView.setAdapter(userPresetListAdapter);
        } else {
            presetTypeSwitch.check(1);
            presetListView.setAdapter(officialPresetListAdapter);
        }


        presetTypeSwitch.setOnCheckedChangeListener(new SegmentedControlWidget.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SegmentedControlWidget segmentedControl, int checkedIndex) {
                if (checkedIndex == 1) {
                    presetListView.setAdapter(officialPresetListAdapter);
                } else {
                    presetListView.setAdapter(userPresetListAdapter);
                }
                setupOutlets();
            }
        });

        presetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (presetTypeSwitch.getCheckedIndex() == 1) { // official
                    String presetName
                            = HiFiToyPresetManager.getInstance().getOfficialPresetNameList().get(position);
                    setActivePreset(presetName);

                } else { // user

                    String presetName
                            = HiFiToyPresetManager.getInstance().getUserPresetNameList().get(position);

                    Intent intent = new Intent(PresetManagerActivity.this, PresetDetailActivity.class);
                    intent.putExtra("presetName", presetName);
                    startActivity(intent);
                }
            }
        });

    }

    public void setupOutlets() {
        userPresetListAdapter.notifyDataSetChanged();
        officialPresetListAdapter.notifyDataSetChanged();
    }

    private void setActivePreset(final String presetName){
        final HiFiToyDevice device = HiFiToyControl.getInstance().getActiveDevice();

        if (presetName.equals(device.getActiveKeyPreset())){
            return;
        }

        DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
            public void onPositiveClick(){
                try {
                    ToyPreset preset = HiFiToyPresetManager.getInstance().getPreset(presetName);
                    preset.storeToPeripheral();

                    device.setActiveKeyPreset(presetName);

                    setupOutlets();
                } catch (IOException | XmlPullParserException e) {
                    Log.d(TAG, e.toString());
                }
            }
            public void onNegativeClick(){
                //
            }
        };

        DialogSystem.getInstance().showDialog(dialogListener,
                "Warning",
                "Are you sure you want to load '" + presetName + "' preset?",
                "Ok", "Cancel");

    }

    private static class PresetListAdapter extends BaseAdapter {
        enum Type {
            USER, OFFICIAL
        }
        private final Type type;

        public PresetListAdapter(Type type) {
            this.type = type;
        }

        @Override
        public int getCount() {
            if (type.equals(Type.USER)) {
                return HiFiToyPresetManager.getInstance().getUserPresetSize();
            }
            return HiFiToyPresetManager.getInstance().getOfficialPresetSize();
        }

        @Override
        public String getItem(int i) {
            if (type.equals(Type.USER)) {
                return HiFiToyPresetManager.getInstance().getUserPresetNameList().get(i);
            }
            return HiFiToyPresetManager.getInstance().getOfficialPresetNameList().get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                Activity a = (Activity)ApplicationContext.getInstance().getContext();
                view = a.getLayoutInflater().inflate(R.layout.preset_list_item, null);
            }
            TextView presetName_outl = view.findViewById(R.id.preset_name);

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
