/*
 *   BiquadFragment.java
 *
 *   Created by Artem Khlyupin on 04/04/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.config_fragment;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hifitoy.R;
import com.hifitoy.activities.filters.ViewUpdater;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.biquad.ParamBiquad;
import com.hifitoy.hifitoyobjects.biquad.TextBiquad;
import com.hifitoy.hifitoyobjects.biquad.Type;
import com.hifitoy.hifitoyobjects.filter.Filter;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.filter.HighpassFilter;
import com.hifitoy.hifitoyobjects.filter.LowpassFilter;
import com.hifitoy.widgets.SegmentedControlWidget;

import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_PARAMETRIC;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_USER;

public class BiquadConfigFragment extends Fragment implements ViewUpdater.IFilterUpdateView {
    private final String TAG = "HiFiToy";

    ViewGroup.LayoutParams          lp;

    private Button                  prevBiquadButton;
    private TextView                biquadLabel;
    private Button                  nextBiquadButton;
    private SegmentedControlWidget  biquadInputTypeWidget;
    private LinearLayout            biquadData;

    private Filter filter = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getActiveFilter();

    TextConfigFragment textFragment   = new TextConfigFragment();
    GuiConfigFragment guiFragment    = new GuiConfigFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.biquad_config_layout, container, false);
        prevBiquadButton        = v.findViewById(R.id.prevBiquadButton);
        biquadLabel             = v.findViewById(R.id.biquadLabel);
        nextBiquadButton        = v.findViewById(R.id.nextBiquadButton);
        biquadInputTypeWidget   = v.findViewById(R.id.biquadInputTypeWidget);
        biquadData              = v.findViewById(R.id.biquadData_outl);

        prevBiquadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter.decActiveBiquadIndex();
                ViewUpdater.getInstance().update();
            }
        });

        nextBiquadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter.incActiveBiquadIndex();
                ViewUpdater.getInstance().update();
            }
        });

        biquadInputTypeWidget.setOnCheckedChangeListener(new SegmentedControlWidget.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SegmentedControlWidget segmentedControl, int checkedIndex) {
                Log.d(TAG, "onCheckedChanged");

                Biquad b = filter.getActiveBiquad();
                byte type = Type.getType(b);

                if (checkedIndex == 1) { // set text

                    if (type == BIQUAD_USER) return;

                    b = new TextBiquad(b);
                    b.setEnabled(true);
                    b.sendToPeripheral(true);

                    //update pass biquads
                    if (type == BIQUAD_HIGHPASS) {
                        HighpassFilter hp = new HighpassFilter(filter);
                        hp.sendToPeripheral(true);

                    } else if (type == BIQUAD_LOWPASS) {
                        LowpassFilter lp = new LowpassFilter(filter);
                        lp.sendToPeripheral(true);
                    }

                } else { // set gui

                    if (type != BIQUAD_USER) return;

                    b = new ParamBiquad(b);
                    b.setEnabled(filter.isBiquadEnabled(BIQUAD_PARAMETRIC));

                    short freq = filter.getBetterNewFreqForBiquad(b);
                    ((ParamBiquad)b).setFreq((freq != -1) ? freq : 100);

                    ((ParamBiquad)b).setQ(1.41f);
                    ((ParamBiquad)b).setDbVolume(0.0f);

                    ((ParamBiquad)b).sendToPeripheral(true);
                }

                ViewUpdater.getInstance().update();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (lp != null) {
            getView().setLayoutParams(lp);
        }

        FragmentTransaction fTrans = getFragmentManager().beginTransaction();
        fTrans.add(biquadData.getId(), guiFragment, "guiFragment");
        fTrans.add(biquadData.getId(), textFragment, "textFragment");
        fTrans.commit();

        ViewUpdater.getInstance().addUpdateView(this);
        ViewUpdater.getInstance().update();

    }

    @Override
    public void onPause() {
        super.onPause();

        FragmentTransaction fTrans = getFragmentManager().beginTransaction();
        fTrans.remove(guiFragment);
        fTrans.remove(textFragment);
        fTrans.commit();

        ViewUpdater.getInstance().removeUpdateView(this);

    }

    public void setFilter(Filter f) {
        filter = f;
        guiFragment.setFilter(f);
        textFragment.setFilter(f);
    }

    @Override
    public void updateView() {
        biquadLabel.setText("BIQUAD #" + Integer.toString(filter.getActiveBiquadIndex() + 1));

        byte type = Type.getType(filter.getActiveBiquad());
        int index = (type == BIQUAD_USER) ? 1 : 0;
        biquadInputTypeWidget.check(index);

        FragmentTransaction fTrans = getFragmentManager().beginTransaction();

        if (index == 1) {
            fTrans.hide(guiFragment);
            fTrans.show(textFragment);
            if (textFragment.isResumed()) {
                textFragment.updateView();
            }

        } else {
            fTrans.hide(textFragment);
            fTrans.show(guiFragment);
            if (guiFragment.isResumed()) {
                guiFragment.updateView();
            }
        }
        fTrans.commit();

    }

    public void setLayoutParams(ViewGroup.LayoutParams lp) {
        this.lp = lp;

        if (getView() != null) {
            getView().setLayoutParams(lp);
        }
    }
}
