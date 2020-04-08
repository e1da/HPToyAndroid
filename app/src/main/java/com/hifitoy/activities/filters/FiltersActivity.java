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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.activities.filters.config_fragment.BiquadConfigFragment;
import com.hifitoy.activities.filters.filter_fragment.FiltersBackground;
import com.hifitoy.activities.filters.filter_fragment.FiltersFragment;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.hifitoyobjects.PassFilter;

import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;

public class FiltersActivity extends Activity implements ViewUpdater.IFilterUpdateView {
    private static String TAG = "HiFiToy";

    private Filters filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();

    MenuItem enabledParam_outl;
    MenuItem typeScale_outl;

    LinearLayout ll;
    boolean visibleSubview = false;

    FiltersFragment filtersFragment;
    BiquadConfigFragment biquadConfigFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setId(1234);

        filtersFragment = new FiltersFragment();
        biquadConfigFragment = new BiquadConfigFragment();

        FragmentTransaction fTrans = getFragmentManager().beginTransaction();
        fTrans.add(ll.getId(), filtersFragment, "filtersFragment").commit();

        fTrans = getFragmentManager().beginTransaction();
        fTrans.add(ll.getId(), biquadConfigFragment, "biquadConfigFragment").commit();

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

        ViewUpdater.getInstance().addUpdateView(this);
        ViewUpdater.getInstance().update();

        setVisibleSubview(visibleSubview);

        filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();
        ViewUpdater.getInstance().update();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (filtersFragment.getStateValue() == FiltersFragment.State.FILTERS_STATE) {
            getMenuInflater().inflate(R.menu.filters_menu, menu);
            enabledParam_outl = menu.findItem(R.id.enabled_parametrics);
            enabledParam_outl.setTitle(filters.isPEQEnabled() ? "PEQ On" : "PEQ Off");

        } else {
            getMenuInflater().inflate(R.menu.background_menu, menu);
            typeScale_outl = menu.findItem(R.id.type_scale);
            typeScale_outl.setTitle(FiltersBackground.getInstance().getScaleTypeString());
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
                visibleSubview = !visibleSubview;
                setVisibleSubview(visibleSubview);
                ll.invalidate();
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
                filtersFragment.setStateValue(FiltersFragment.State.FILTERS_STATE);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateView() {
        setTitleInfo();
    }

    public void setTitleInfo() {
        if (filtersFragment.getStateValue() == FiltersFragment.State.BACKGROUND_STATE) {
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

    private void setVisibleSubview(boolean show) {
        ViewGroup.LayoutParams lp0;
        ViewGroup.LayoutParams lp1;

        if (show) {
            lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 0.5f);

            lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

        } else {
            lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 0.0f);

            lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        }

        filtersFragment.getView().setLayoutParams(lp0);
        biquadConfigFragment.getView().setLayoutParams(lp1);
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
