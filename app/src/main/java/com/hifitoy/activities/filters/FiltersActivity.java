/*
 *   FiltersActivity.java
 *
 *   Created by Artem Khlyupin on 04/15/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.filters.config_fragment.BiquadConfigFragment;
import com.hifitoy.activities.filters.filter_fragment.BackConfigFragment;
import com.hifitoy.activities.filters.filter_fragment.FiltersBackground;
import com.hifitoy.activities.filters.filter_fragment.FiltersFragment;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.hifitoyobjects.PassFilter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;

public class FiltersActivity extends Activity implements ViewUpdater.IFilterUpdateView, FiltersFragment.OnSetBackgroundListener {
    private static String TAG = "HiFiToy";

    private Filters filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();

    MenuItem enabledParam_outl;
    MenuItem typeScale_outl;

    LinearLayout ll;

    FiltersFragment         filtersFragment;
    BiquadConfigFragment    biquadConfigFragment;
    BackConfigFragment      backConfigFragment;

    private DisplayState state;

    class DisplayState {
        static final int FILTERS_DISPLAY_STATE             = 0;
        static final int FILTERS_AND_CONFIG_DISPLAY_STATE  = 1;
        static final int BACKGROUND_CONFIG_DISPLAY_STATE   = 2;

        private int value;
        private int prevValue;

        public DisplayState() {
            value = FILTERS_DISPLAY_STATE;
            prevValue = FILTERS_DISPLAY_STATE;
            prepareFragments();
        }
        public void setValue(int value) {
            if ((value >= FILTERS_DISPLAY_STATE) &&
                    (value <= BACKGROUND_CONFIG_DISPLAY_STATE) &&
                    (value != this.value)) {

                prevValue = this.value;
                this.value = value;
                prepareFragments();
                updateLayoutFragments();
            }
        }

        public int getValue() {
            return value;
        }

        void setPreviousValue() {
            setValue(prevValue);
        }

        private void prepareFragments() {
            FragmentTransaction fTrans = getFragmentManager().beginTransaction();

            if (value == FILTERS_DISPLAY_STATE) {

                fTrans.remove(biquadConfigFragment);
                fTrans.remove(backConfigFragment);
                if (getFragmentManager().findFragmentByTag("filtersFragment") == null) {
                    fTrans.add(ll.getId(), filtersFragment, "filtersFragment");
                }
                fTrans.commit();

                if (prevValue == BACKGROUND_CONFIG_DISPLAY_STATE) {
                    invalidateOptionsMenu();
                }

            } else if (value == FILTERS_AND_CONFIG_DISPLAY_STATE) {

                fTrans.remove(backConfigFragment);
                if (getFragmentManager().findFragmentByTag("filtersFragment") == null) {
                    fTrans.add(ll.getId(), filtersFragment, "filtersFragment");
                }
                if (getFragmentManager().findFragmentByTag("biquadConfigFragment") == null) {
                    fTrans.add(ll.getId(), biquadConfigFragment, "biquadConfigFragment");
                }
                fTrans.commit();

                if (prevValue == BACKGROUND_CONFIG_DISPLAY_STATE) {
                    invalidateOptionsMenu();
                }

            } else if (value == BACKGROUND_CONFIG_DISPLAY_STATE) {

                fTrans.remove(biquadConfigFragment);
                fTrans.remove(filtersFragment);
                if (getFragmentManager().findFragmentByTag("backConfigFragment") == null) {
                    fTrans.add(ll.getId(), backConfigFragment, "backConfigFragment");
                }
                fTrans.commit();

                if (prevValue != BACKGROUND_CONFIG_DISPLAY_STATE) {
                    invalidateOptionsMenu();
                }

            }
        }

        private void updateLayoutFragments() {
            ViewGroup.LayoutParams lp0, lp1;

            if (value == FILTERS_DISPLAY_STATE) {
                lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                filtersFragment.setLayoutParams(lp0);

            } else if (value == FILTERS_AND_CONFIG_DISPLAY_STATE) {
                lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 0.5f);

                lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

                filtersFragment.setLayoutParams(lp0);
                biquadConfigFragment.setLayoutParams(lp1);

            } else if (value == BACKGROUND_CONFIG_DISPLAY_STATE) {

                lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                backConfigFragment.setLayoutParams(lp0);
            }

            ViewUpdater.getInstance().update();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setId(1234);

        filtersFragment         = new FiltersFragment();
        biquadConfigFragment    = new BiquadConfigFragment();
        backConfigFragment      = new BackConfigFragment();

        filtersFragment.setBackgroundListener(this);
        state = new DisplayState();

        setContentView(ll);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        ViewUpdater.getInstance().removeUpdateView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);
        registerReceiver(broadcastReceiver, makeIntentFilter());

        filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();

        ViewUpdater.getInstance().addUpdateView(this);
        ViewUpdater.getInstance().update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (state.getValue() == DisplayState.BACKGROUND_CONFIG_DISPLAY_STATE) {
            getMenuInflater().inflate(R.menu.background_menu, menu);
            typeScale_outl = menu.findItem(R.id.type_scale);
            typeScale_outl.setTitle(FiltersBackground.getInstance().getScaleTypeString());

        } else {
            getMenuInflater().inflate(R.menu.filters_menu, menu);
            enabledParam_outl = menu.findItem(R.id.enabled_parametrics);
            enabledParam_outl.setTitle(filters.isPEQEnabled() ? "PEQ On" : "PEQ Off");
        }
        return true;
    }


    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.enabled_parametrics:
                filters.setPEQEnabled(!filters.isPEQEnabled());
                enabledParam_outl.setTitle(filters.isPEQEnabled() ? "PEQ On" : "PEQ Off");
                ViewUpdater.getInstance().update();
                break;

            case R.id.filters_info:
                DialogSystem.getInstance().showDialog("Info",
                        "To select a filter please double tap on it or tap and hold > 1sec. " +
                                "Horizontal slide changes a frequency, vertical one controls PEQ's gain or LPF/HPF's order. " +
                                "Zoomin-zoomout to control Q of PEQ.", "Close");
                break;

            case R.id.show_coef:
                if (state.getValue() == DisplayState.FILTERS_AND_CONFIG_DISPLAY_STATE) {
                    state.setValue(DisplayState.FILTERS_DISPLAY_STATE);

                } else if (state.getValue() == DisplayState.FILTERS_DISPLAY_STATE) {
                    state.setValue(DisplayState.FILTERS_AND_CONFIG_DISPLAY_STATE);

                }
                break;

            case R.id.mirror_bitmap:
                FiltersBackground.getInstance().mirrorX();
                ViewUpdater.getInstance().update();
                break;

            case R.id.type_scale:
                FiltersBackground.getInstance().invertScaleType();
                typeScale_outl.setTitle(FiltersBackground.getInstance().getScaleTypeString());
                break;

            case R.id.done_bitmap:
                state.setPreviousValue();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateView() {
        setTitleInfo();
        ll.invalidate();
    }

    @Override
    public void onSetBackground() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( (resultCode == RESULT_OK) && (requestCode == 1) ) {

            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {

                String selectedImagePath = selectedImageUri.getPath();
                Log.d(TAG, selectedImagePath);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    FiltersBackground.getInstance().setBitmap(bitmap);

                    state.setValue(DisplayState.BACKGROUND_CONFIG_DISPLAY_STATE);
                    ViewUpdater.getInstance().update();

                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found exception");
                } catch (IOException e) {
                    Log.d(TAG, "IO exception");
                }

            } else {
                Log.d(TAG, "Not select image.");
            }

        } else {
            Log.d(TAG, "Not get result");
        }
    }

    public void setTitleInfo() {
        if (state.getValue() == DisplayState.BACKGROUND_CONFIG_DISPLAY_STATE) {
            setTitle("Filters menu");
            return;
        }

        Biquad b = filters.getActiveBiquad();
        byte type = b.getParams().getTypeValue();

        if (filters.isActiveNullLP()) {
            setTitle("LP: Off");

        } else if (filters.isActiveNullHP()){
            setTitle("HP: Off");

        } else if (type == BIQUAD_LOWPASS) {
            PassFilter lp = filters.getLowpass();
            setTitle("LP:" + lp.getInfo());

        } else if (type == BIQUAD_HIGHPASS) {
            PassFilter hp = filters.getHighpass();
            setTitle("HP:" + hp.getInfo());

        } else if (type == BIQUAD_PARAMETRIC) {
            setTitle(String.format(Locale.getDefault(), "PEQ%d: %s",
                                    filters.getActiveBiquadIndex() + 1, b.getInfo()));

        } else if (type == BIQUAD_ALLPASS) {
            setTitle(String.format(Locale.getDefault(), "APF%d: %s",
                    filters.getActiveBiquadIndex() + 1, b.getInfo()));

        } else {
            setTitle("Filters menu");
        }
    }

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
