/*
 *   PresetdetailActivity.java
 *
 *   Created by Artem Khlyupin on 04/09/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.options.presetmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;
import com.hifitoy.xml.XmlData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PresetDetailActivity extends Activity implements View.OnClickListener{
    final static String TAG = "HiFiToy";

    HiFiToyPreset preset;

    LinearLayout    presetNameLayout_outl;
    TextView        presetName_outl;
    LinearLayout    setActivePresetLayout_outl;
    TextView        setActivePreset_outl;
    TextView        exportPreset_outl;
    //TextView        exportPresetToFile_outl;
    LinearLayout    deletePresetLayout_outl;
    TextView        deletePreset_outl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_detail);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();

        int position = getIntent().getIntExtra("presetPosition", -1);
        if (position == -1){
            position = 0;
        }
        preset = HiFiToyPresetManager.getInstance().getPreset(position);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);
        registerReceiver(broadcastReceiver, makeIntentFilter());

        setupOutlets();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            Log.d(TAG, Integer.toString(requestCode) + " " + Integer.toString(resultCode));
        } else {
            Log.d(TAG, Integer.toString(requestCode) + " " + Integer.toString(resultCode) + " " + data.toString());
        }
    }


    private void initOutlets() {
        presetNameLayout_outl       = findViewById(R.id.presetNameLayout_outl);
        presetName_outl             = findViewById(R.id.presetNameLabel_outl);
        setActivePresetLayout_outl  = findViewById(R.id.setActivePresetLayout_outl);
        setActivePreset_outl        = findViewById(R.id.setActivePreset_outl);
        exportPreset_outl           = findViewById(R.id.exportPresetToEmail_outl);
        deletePresetLayout_outl     = findViewById(R.id.deletePresetLayout_outl);
        deletePreset_outl           = findViewById(R.id.deletePreset_outl);

        presetNameLayout_outl.setOnClickListener(this);
        setActivePreset_outl.setOnClickListener(this);
        exportPreset_outl.setOnClickListener(this);
        deletePreset_outl.setOnClickListener(this);
    }

    private void setupOutlets() {
        String presetName;
        if (preset.getName().length() > 23){
            presetName = preset.getName().substring(0, 20) + "...";
        } else {
            presetName = preset.getName();
        }
        presetName_outl.setText(presetName);

        HiFiToyDevice activeDevice = HiFiToyControl.getInstance().getActiveDevice();
        if (preset.getName().equals(activeDevice.getActiveKeyPreset())){
            setActivePresetLayout_outl.setVisibility(View.GONE);
        } else {
            setActivePresetLayout_outl.setVisibility(View.VISIBLE);
        }

        if ( (preset.getName().equals("No processing")) ||
                (preset.getName().equals(activeDevice.getActiveKeyPreset())) ) {
            deletePresetLayout_outl.setVisibility(View.INVISIBLE);
        } else {
            deletePresetLayout_outl.setVisibility(View.VISIBLE);
        }


    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.presetNameLayout_outl:
                showChangePresetNameDialog();
                break;
            case R.id.setActivePreset_outl:
                setActivePreset();
                break;
            case R.id.exportPresetToEmail_outl:
                exportPreset(preset);
                break;
            case R.id.deletePreset_outl:
                deletePreset();
                break;
        }
    }

    void showChangePresetNameDialog(){
        if (preset.getName().equals("No processing")){//rename is not available for No processing
            return;
        }

        DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
            public void onPositiveClick(String name){
                if (name.length() > 0) {
                    preset.setName(name);
                    HiFiToyPresetManager.getInstance().store();

                    presetName_outl.setText(name);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Name field is empty.", Toast.LENGTH_SHORT).show();
                }
            }
            public void onNegativeClick(String text){
            }
        };

        DialogSystem.getInstance().showTextDialog(dialogListener, "Enter new name", "Change", "Cancel");
    }

    private void setActivePreset(){
        final HiFiToyDevice device = HiFiToyControl.getInstance().getActiveDevice();

        if (preset.getName().equals(device.getActiveKeyPreset())){
            return;
        }

        DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
            public void onPositiveClick(){
                device.setActiveKeyPreset(preset.getName());

                preset.storeToPeripheral();
            }
            public void onNegativeClick(){
                //
            }
        };

        DialogSystem.getInstance().showDialog(dialogListener,
                "Warning",
                "Are you sure you want to load '" + preset.getName() + "' preset?",
                "Ok", "Cancel");

    }

    private void deletePreset(){
        HiFiToyDevice device = HiFiToyControl.getInstance().getActiveDevice();

        if ( (preset.getName().equals("No processing")) || (preset.getName().equals(device.getActiveKeyPreset())) ) {
            return;
        }

        DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
            public void onPositiveClick(){

                HiFiToyPresetManager.getInstance().removePreset(preset.getName());
                finish();

            }
            public void onNegativeClick(){
                //
            }
        };

        DialogSystem.getInstance().showDialog(dialogListener,
                "Warning",
                "Are you sure you want to delete '" + preset.getName() + "' preset?",
                "Ok", "Cancel");

    }

    // Checks if external storage is available for read and write
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    //TargetApi should be > 18
    private File getExtStorage() {

        if (isExternalStorageWritable()) {
            Toast.makeText(getApplicationContext(),
                    "Error. External storage is not writable.", Toast.LENGTH_SHORT).show();

            return null;
        }

        File extDir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                    "ToyExport");

        if ( (!extDir.exists()) && (!extDir.mkdirs()) ) {
            Toast.makeText(getApplicationContext(),
                    "Error. External Toy presets directory is not available.", Toast.LENGTH_SHORT).show();

            return null;
        }

        return extDir;
    }

    private File getIntStorage() {
        File intDir = new File(getApplicationContext().getFilesDir() + "/ToyExport");

        if ( (!intDir.exists()) && (!intDir.mkdirs()) ) {
            Toast.makeText(getApplicationContext(),
                    "Error. Internal IWoofer_Presets directory is not available.", Toast.LENGTH_SHORT).show();

            return null;
        }

        return intDir;
    }


    private File savePresetToFile(HiFiToyPreset preset) {
        //get xml string of preset
        XmlData xmlData = preset.toXmlData();
        String xmlString = xmlData.toString();

        //create file to save on internal storage
        File dir = getIntStorage();
        if (dir == null) {
            return null;
        }

        String filename = preset.getName() + ".tpr";
        File file = new File(dir, filename);

        Log.d(TAG, "Export directories = " + file.toString());

        try {
            //create file if he is not exists
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException(filename + " is not create successful.");
                }
            }

            if (file.canWrite()){
                //write to temp file
                FileWriter fw = new FileWriter(file);
                fw.write(xmlString);
                fw.close();
            } else {
                throw new IOException(filename + " is not writable.");
            }

        } catch (IOException e){
            Log.d(TAG, e.toString());

            Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT).show();
            return null;
        }

        return file;
    }

    private void exportPreset(HiFiToyPreset preset){
        //save preset to file and get pointer
        File file = savePresetToFile(preset);
        if (file == null) {
            return;
        }

        //get file Uri
        Uri fileUri = null;

        try {
            fileUri = FileProvider.getUriForFile(this,
                    getString(R.string.fileprovider_authority), file);

        } catch (IllegalArgumentException e) {
            Log.d(TAG, "The selected file can't be shared: " + file.toString());
        }

        //send Uri to chooser window (email, gmail, bluetooth, etc)
        if (fileUri != null){
            //start E-Mail export view
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_extra_subject));
            sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            sendIntent.setType("application/tpr");
            //startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.string_to)));
            startActivityForResult(Intent.createChooser(sendIntent, "String To"), 1);
        }
    }

    /*--------------------------- Broadcast receiver implementation ------------------------------*/
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HiFiToyControl.CLIP_UPDATE);

        return intentFilter;
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.CLIP_UPDATE.equals(action)) {
                ApplicationContext.getInstance().updateClipView();
            }
        }
    };
}
