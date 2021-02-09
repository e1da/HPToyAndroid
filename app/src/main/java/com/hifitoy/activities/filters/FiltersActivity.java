/*
 *   FiltersActivity.java
 *
 *   Created by Artem Khlyupin on 04/15/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters;

import android.app.ActionBar;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hifitoy.R;
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.activities.filters.config_fragment.BiquadConfigFragment;
import com.hifitoy.activities.filters.filter_fragment.BackConfigFragment;
import com.hifitoy.activities.filters.filter_fragment.FiltersBackground;
import com.hifitoy.activities.filters.filter_fragment.FiltersFragment;
import com.hifitoy.activities.filters.import_fragment.FilterImportFragment;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.biquad.AllpassBiquad;
import com.hifitoy.hifitoyobjects.biquad.ParamBiquad;
import com.hifitoy.hifitoyobjects.biquad.Type;
import com.hifitoy.hifitoyobjects.filter.DFilter;
import com.hifitoy.hifitoyobjects.filter.Filter;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.filter.HighpassFilter;
import com.hifitoy.hifitoyobjects.filter.LowpassFilter;
import com.hifitoy.widgets.SegmentedControlWidget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_PARAMETRIC;

public class FiltersActivity extends BaseActivity implements
                                            ViewUpdater.IFilterUpdateView,
                                            FiltersFragment.OnSetBackgroundListener {
    private static String TAG = "HiFiToy";

    private Filter filters;

    SegmentedControlWidget channelChooseWidget;

    MenuItem enabledParam_outl;
    MenuItem typeScale_outl;

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
            FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();

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

            invalidateOptionsMenu();
            ViewUpdater.getInstance().update();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initActionBar();
        initOutlets();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();

        LayoutInflater mInflater = LayoutInflater.from(this);
        View actionBarView = mInflater.inflate(R.layout.action_bar_filter, null);

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

        channelChooseWidget = actionBarView.findViewById(R.id.channelChooseWidget);
        channelChooseWidget.setOnCheckedChangeListener(new SegmentedControlWidget.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SegmentedControlWidget segmentedControl, int checkedIndex) {
                DFilter dFilter = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getDFilter();

                if (checkedIndex == 0) { // binded ch
                    Log.d(TAG, "Channels are binded.");

                    dFilter.bindChannels(true);
                    dFilter.setActiveChannel((byte)0);

                } else if (checkedIndex == 1) { // left ch
                    Log.d(TAG, "Left channel.");

                    dFilter.bindChannels(false);
                    dFilter.setActiveChannel((byte)0);

                } else if (checkedIndex == 2) { // right ch
                    Log.d(TAG, "Right channel.");

                    dFilter.bindChannels(false);
                    dFilter.setActiveChannel((byte)1);
                }

                updateFilter();
                ViewUpdater.getInstance().update();
            }
        });
    }

    private void initOutlets() {
        FragmentManager fm = getSupportFragmentManager();

        filtersFragment = (FiltersFragment) fm.findFragmentById(R.id.filtersFragment_outl);
        filtersFragment.setBackgroundListener(this);

        biquadConfigFragment = (BiquadConfigFragment) fm.findFragmentById(R.id.biquadConfigFragment_outl);
        backConfigFragment = (BackConfigFragment) fm.findFragmentById(R.id.backConfigFragment_outl);
        filterImportFragment = (FilterImportFragment) fm.findFragmentById(R.id.filterImportFragment_outl);
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
        ViewUpdater.getInstance().removeUpdateView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateFilter();

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
            enabledParam_outl.setTitle(filters.isBiquadEnabled(BIQUAD_PARAMETRIC) ? "PEQ On" : "PEQ Off");
        }
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.enabled_parametrics:
                boolean paramEn = filters.isBiquadEnabled(BIQUAD_PARAMETRIC);
                filters.setBiquadEnabled(BIQUAD_PARAMETRIC, !paramEn);
                enabledParam_outl.setTitle(filters.isBiquadEnabled(BIQUAD_PARAMETRIC) ? "PEQ On" : "PEQ Off");
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

    @Override
    public void setupOutlets() {
        updateView();

        //update channel widget
        DFilter dFilter = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getDFilter();

        if (dFilter.isChannelsBinded()) {
            channelChooseWidget.check(0);

        } else {
            if (dFilter.getActiveChannel() == 0) { // left ch
                channelChooseWidget.check(1);
            } else {
                channelChooseWidget.check(2);
            }
        }
    }

    private void updateFilter() {
        DFilter dFilter = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getDFilter();

        filters = dFilter.getActiveFilter();
        filtersFragment.setFilter(filters);
        biquadConfigFragment.setFilter(filters);
    }

    @Override
    public void updateView() {
        setTitleInfo();

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
        byte type = Type.getType(b);

        if (filters.isActiveNullLP()) {
            setTitle("LP: Off");

        } else if (filters.isActiveNullHP()){
            setTitle("HP: Off");

        } else if (type == BIQUAD_LOWPASS) {
            LowpassFilter lp = new LowpassFilter(filters);
            setTitle("LP:" + lp.toString());

        } else if (type == BIQUAD_HIGHPASS) {
            HighpassFilter hp = new HighpassFilter(filters);
            setTitle("HP:" + hp.toString());

        } else if (type == BIQUAD_PARAMETRIC) {
            setTitle(String.format(Locale.getDefault(), "PEQ%d: %s",
                                    filters.getActiveBiquadIndex() + 1, ((ParamBiquad)b).toString()));

        } else if (type == BIQUAD_ALLPASS) {
            setTitle(String.format(Locale.getDefault(), "APF%d: %s",
                    filters.getActiveBiquadIndex() + 1, ((AllpassBiquad)b).toString()));

        } else {
            setTitle("Filters menu");
        }
    }

}
