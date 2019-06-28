package com.hifitoy.activities.options.presetmanager.textimporttool;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.filters.FiltersActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;

public class PresetTextImportActivity extends Activity {
    final static String TAG = "HiFiToy";

    private TextView presetTextData_outl;
    private GestureDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_import);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);
        //registerReceiver(broadcastReceiver, makeIntentFilter());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preset_text_menu, menu);
        return true;
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pastePresetText_outl:
                pastePresetData();
                return true;
            case R.id.importPresetText_outl:
                importPresetFromText();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initOutlets() {
        presetTextData_outl = findViewById(R.id.presetTextDataView_outl);

    }

    private void pastePresetData() {
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Activity.CLIPBOARD_SERVICE);

        try {
            ClipData pData = clipboardManager.getPrimaryClip();
            ClipData.Item item = pData.getItemAt(0);
            String txtpaste = item.getText().toString();
            presetTextData_outl.setText(txtpaste);
            presetTextData_outl.setGravity(Gravity.LEFT);
            //presetTextData_outl.setVerticalScrollBarEnabled(true);
        } catch (NullPointerException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void importPresetFromText() {
        if (presetTextData_outl.getText().toString().equals("")) return;

        DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
            public void onPositiveClick(String name){
                if (name.length() > 0) {
                    HiFiToyPreset importPreset = new HiFiToyPreset();
                    if (importPreset.importFromXml(presetTextData_outl.getText().toString(), name)){

                        HiFiToyPresetManager.getInstance().setPreset(importPreset);

                        DialogSystem.getInstance().showDialog("Info",
                                "Add " + importPreset.getName() + " preset", "Ok");
                    } else {
                        DialogSystem.getInstance().showDialog("Warning", "Import preset is not success.", "Ok");
                    }

                    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Name field is empty.", Toast.LENGTH_SHORT).show();
                    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
            public void onNegativeClick(String text){
                setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        };

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        DialogSystem.getInstance().showTextDialog(dialogListener, "Enter preset name", "Import", "Cancel");

    }


    /*class TapGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            pastePresetData();
            return true;
        }
    }*/
}
