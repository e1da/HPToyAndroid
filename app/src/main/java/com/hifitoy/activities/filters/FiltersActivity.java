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
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.filters.config_fragment.BiquadConfigFragment;
import com.hifitoy.activities.filters.filter_fragment.BackConfigFragment;
import com.hifitoy.activities.filters.filter_fragment.FiltersBackground;
import com.hifitoy.activities.filters.filter_fragment.FiltersFragment;
import com.hifitoy.activities.filters.import_fragment.FilterImportFragment;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyPreset;
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

    private Filters filters;

    MenuItem enabledParam_outl;
    MenuItem typeScale_outl;

    FrameLayout fl;
    LinearLayout ll;

    FiltersFragment         filtersFragment;
    BiquadConfigFragment    biquadConfigFragment;
    BackConfigFragment      backConfigFragment;
    FilterImportFragment    filterImportFragment;

    private DisplayState state = new DisplayState();

    class DisplayState {
        private boolean filterVisible           = true;
        private boolean prevBiquadConfigVisible = false;
        private boolean biquadConfigVisible     = false;
        private boolean backConfigVisible       = false;
        private boolean filterImportVisible     = false;

        //getters/setters
        public boolean isBiquadConfigVisible() {
            return biquadConfigVisible;
        }
        public void setBiquadConfigVisible(boolean visible) {
            biquadConfigVisible = visible;
            update();
        }
        public void toggleBiquadConfigVisible() {
            setBiquadConfigVisible(!biquadConfigVisible);
        }

        public boolean isBackConfigVisible() {
            return backConfigVisible;
        }
        public void setBackConfigVisible(boolean visible) {
            backConfigVisible = visible;

            if (backConfigVisible) {
                filterVisible = false;
                prevBiquadConfigVisible = biquadConfigVisible;
                biquadConfigVisible = false;
            } else {
                filterVisible = true;
                biquadConfigVisible = prevBiquadConfigVisible;
            }

            update();
        }

        public boolean isFilterImportVisible() {
            return filterImportVisible;
        }
        public void setFilterImportVisible(boolean visible) {
            filterImportVisible = visible;

            getActionBar().setDisplayHomeAsUpEnabled(!visible);
            getActionBar().setHomeButtonEnabled(!visible);
            filtersFragment.setEnabled(!visible);

            update();
        }

        //update: show or hide fragments and then set layout params
        public void update() {
            //show/hide fragments
            FragmentTransaction fTrans = getFragmentManager().beginTransaction();

            if (filterVisible) {
                fTrans.show(filtersFragment);
            } else {
                fTrans.hide(filtersFragment);
            }

            if (biquadConfigVisible) {
                fTrans.show(biquadConfigFragment);
            } else {
                fTrans.hide(biquadConfigFragment);
            }

            if (backConfigVisible) {
                fTrans.show(backConfigFragment);
            } else {
                fTrans.hide(backConfigFragment);
            }

            if (filterImportVisible) {
                fTrans.show(filterImportFragment);
            } else {
                fTrans.hide(filterImportFragment);
            }

            fTrans.commit();

            //set layout params or fragments
            ViewGroup.LayoutParams lp0, lp1, lp2;

            lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 0.5f);

            lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

            lp2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            filtersFragment.getView().setLayoutParams(lp0);
            biquadConfigFragment.getView().setLayoutParams(lp1);
            backConfigFragment.getView().setLayoutParams(lp2);
            filterImportFragment.getView().setLayoutParams(lp2);

            invalidateOptionsMenu();
            ViewUpdater.getInstance().update();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        fl = new FrameLayout(this);
        fl.setId(3456);

        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setId(1234);

        fl.addView(ll);

        filtersFragment = new FiltersFragment();
        filtersFragment.setBackgroundListener(this);

        biquadConfigFragment    = new BiquadConfigFragment();
        backConfigFragment      = new BackConfigFragment();
        filterImportFragment = new FilterImportFragment();

        FragmentTransaction fTrans = getFragmentManager().beginTransaction();
        fTrans.add(ll.getId(), filtersFragment,         "filtersFragment");
        fTrans.add(ll.getId(), biquadConfigFragment,    "biquadConfigFragment");
        fTrans.add(fl.getId(), backConfigFragment,      "backConfigFragment");
        fTrans.add(fl.getId(), filterImportFragment,    "filterImportFragment").commit();

        setContentView(fl);
    }

    @Override
    public void onBackPressed() {
        if (!state.isFilterImportVisible()) {
            super.onBackPressed();
        }
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

        state.update();
        ViewUpdater.getInstance().addUpdateView(this);
        ViewUpdater.getInstance().update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (state.isBackConfigVisible()) {
            getMenuInflater().inflate(R.menu.background_menu, menu);
            typeScale_outl = menu.findItem(R.id.type_scale);
            typeScale_outl.setTitle(FiltersBackground.getInstance().getScaleTypeString());

        } else if (state.isFilterImportVisible()) {
            getMenuInflater().inflate(R.menu.filter_import_menu, menu);

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
                state.toggleBiquadConfigVisible();
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
                state.setBackConfigVisible(false);
                break;

            case R.id.accept_import_filter:
                updateFilter();
                state.setFilterImportVisible(false);
                break;

            case R.id.cancel_import_filter:
                filterImportFragment.cancelUpdateFilters();

                updateFilter();
                state.setFilterImportVisible(false);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateFilter() {
        filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();
        filtersFragment.setFilters(filters);
    }

    @Override
    public void updateView() {
        setTitleInfo();
        fl.invalidate();
    }

    @Override
    public void onSetBackground() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    public void onFilterImport() {
        state.setFilterImportVisible(true);
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

                    state.setBackConfigVisible(true);
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
        if (state.isBackConfigVisible()) {
            setTitle("Filters menu");
            return;

        } else if (state.isFilterImportVisible()) {
            setTitle("Import PEQs from preset list");
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
