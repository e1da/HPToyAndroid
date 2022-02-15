package com.hifitoy.activities.options.presetmanager.mergetool;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.R;
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
import com.hifitoy.hifitoydevice.HiFiToyPresetManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class MergeToolActivity extends BaseActivity {
    final static String TAG = "HiFiToy";

    RadioGroup presets;
    Button prevBtn;
    Button nextBtn;
    TextView presetElementLabel;

    private class MergeTool {
        final static int VOLUME_STATE       = 0;
        final static int BASS_TREBLE_STATE  = 1;
        final static int LOUDNESS_STATE     = 2;
        final static int FILTERS_STATE      = 3;
        final static int COMPRESSOR_STATE   = 4;

        private int state;

        private HiFiToyPreset volumeSource;
        private HiFiToyPreset bassTrebleSource;
        private HiFiToyPreset loudnessSource;
        private HiFiToyPreset filtersSource;
        private HiFiToyPreset compressorSource;

        private MergeTool() {
            reset();
        }

        private int getState() {
            return state;
        }

        private void reset() {
            state = VOLUME_STATE;

            volumeSource = null;
            bassTrebleSource = null;
            loudnessSource = null;
            filtersSource = null;
            compressorSource = null;
        }

        private void nextState() {
            HiFiToyPreset p = getPresetForState();

            if (p != null) {
                if (state < COMPRESSOR_STATE) {
                    state++;
                } else if (state == COMPRESSOR_STATE) {
                    HiFiToyPreset mergePreset = merge();
                    if (mergePreset != null) {
                        //mergePreset.presetName = [[NSDate date] descriptionWithLocale:[NSLocale systemLocale]];
                        //[self showInputNameDialog:mergePreset renameFlag:NO];
                        showInputNameDialog(mergePreset, false);

                    } else {
                        reset();
                    }
                }
            }
        }

        private void prevState() {
            if (state > VOLUME_STATE) state--;
        }

        private HiFiToyPreset merge() {
            if ( (volumeSource == null) || (bassTrebleSource == null) || (loudnessSource == null) ||
                    (filtersSource == null) || (compressorSource == null) ) return null;

            HiFiToyPreset mergePreset = new HiFiToyPreset();

            try {
                mergePreset.masterVolume = volumeSource.masterVolume.clone();
                mergePreset.bassTreble = bassTrebleSource.bassTreble.clone();
                mergePreset.loudness = loudnessSource.loudness.clone();
                mergePreset.filters = filtersSource.filters.clone();
                mergePreset.drc = compressorSource.drc.clone();
                mergePreset.updateChecksum();

                Date date = Calendar.getInstance().getTime();
                mergePreset.setName(date.toString());

            } catch (CloneNotSupportedException e) {
                Log.d(TAG, e.toString());
            }

            return mergePreset;
        }

        private void setPreset(HiFiToyPreset preset) {

            switch (state) {
                case VOLUME_STATE:
                    volumeSource = preset;
                    break;
                case BASS_TREBLE_STATE:
                    bassTrebleSource = preset;
                    break;
                case LOUDNESS_STATE:
                    loudnessSource = preset;
                    break;
                case FILTERS_STATE:
                    filtersSource = preset;
                    break;
                case COMPRESSOR_STATE:
                    compressorSource = preset;
            }
        }

        private HiFiToyPreset getPresetForState() {
            switch (state) {
                case VOLUME_STATE:
                    return volumeSource;
                case BASS_TREBLE_STATE:
                    return bassTrebleSource;
                case LOUDNESS_STATE:
                    return loudnessSource;
                case FILTERS_STATE:
                    return filtersSource;
                case COMPRESSOR_STATE:
                    return compressorSource;
            }
            return null;
        }

        private String getStateString() {
            switch (state) {
                case VOLUME_STATE:
                    return "Volume";
                case BASS_TREBLE_STATE:
                    return "Bass&Treble";
                case LOUDNESS_STATE:
                    return "Loudness";
                case FILTERS_STATE:
                    return "Filters";
                case COMPRESSOR_STATE:
                    return "Compressor";
            }
            return "err";
        }

        void showInputNameDialog(final HiFiToyPreset mergePreset, boolean renameFlag){
            String title;

            if (renameFlag) {
                title = "Preset with name \"" + mergePreset.getName() + "\" already exists. Please input another name.";
            } else {
                title = "Please input name for merge preset.";
            }


            DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
                public void onPositiveClick(String name){
                    if (name.length() > 0) {
                        try {
                            mergePreset.setName(name);
                            mergePreset.save(false);
                            Toast.makeText(getApplicationContext(),
                                    "Success.", Toast.LENGTH_SHORT).show();

                            finish();

                        } catch (IOException e) {
                            Log.d(TAG, e.toString());
                            showInputNameDialog(mergePreset, true);
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
            DialogSystem.getInstance().showTextDialog(dialogListener, title, "Ok", "Cancel");
        }

    }

    private MergeTool mergeTool;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Merge tool");
        setContentView(R.layout.activity_merge_tool);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mergeTool = new MergeTool();
        initOutlets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupOutlets();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.merge_tool_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.resetMergeTool_outl:
                mergeTool.reset();
                setupOutlets();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initOutlets() {
        presets = findViewById(R.id.presetRadioGroup_outl);
        fillPresetRadioGroup();
        prevBtn = findViewById(R.id.prevBtn_outl);
        nextBtn = findViewById(R.id.nextBtn_outl);
        presetElementLabel = findViewById(R.id.presetElementLabel_outl);
        presetElementLabel.setText(mergeTool.getStateString());

        presets.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton b = group.findViewById(checkedId);
                if ((b != null) && (b.isChecked()) ) {
                    mergeTool.setPreset((HiFiToyPreset) b.getTag());
                    setupOutlets();
                }

            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mergeTool.prevState();
                setupOutlets();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mergeTool.nextState();
                setupOutlets();
            }
        });
    }

    @Override
    public void setupOutlets() {
        presetElementLabel.setText(mergeTool.getStateString());
        setChecked(mergeTool.getPresetForState());

        if (mergeTool.getState() == MergeTool.COMPRESSOR_STATE) {
            nextBtn.setText("Merge");
        } else {
            nextBtn.setText("Next");
        }

        if (mergeTool.getState() == MergeTool.VOLUME_STATE) {
            prevBtn.setEnabled(false);
        } else {
            prevBtn.setEnabled(true);
        }

        if (mergeTool.getPresetForState() != null) {
            nextBtn.setEnabled(true);
        } else {
            nextBtn.setEnabled(false);
        }
    }

    private void setChecked(HiFiToyPreset p){
        for (int i = 0; i < presets.getChildCount(); i++) {
            View b = presets.getChildAt(i);
            if ( (b instanceof RadioButton) && (b.getTag() == p) ) {
                ((RadioButton) b).setChecked(true);
                return;
            }
        }
        presets.check(-1); // clear check
    }

    private void fillPresetRadioGroup() {
        for (int i = 0 ; i < HiFiToyPresetManager.getInstance().size(); i++) {
            try {
                HiFiToyPreset preset = HiFiToyPresetManager.getInstance().getPreset(i);

                RadioButton btn = new RadioButton(this);
                btn.setText(preset.getName());

                btn.setTag(preset);
                presets.addView(btn);
            } catch (IOException | XmlPullParserException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

}
