/*
 *   BiquadFragment.java
 *
 *   Created by Artem Khlyupin on 04/04/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.config_fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hifitoy.R;
import com.hifitoy.activities.filters.ViewUpdater;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.PassFilter;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.widgets.SegmentedControlWidget;

import static com.hifitoy.hifitoyobjects.biquad.Order.BIQUAD_ORDER_2;
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

    private Filters filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();

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
                filters.decActiveBiquadIndex();
                ViewUpdater.getInstance().update();
            }
        });

        nextBiquadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.incActiveBiquadIndex();
                ViewUpdater.getInstance().update();
            }
        });

        biquadInputTypeWidget.setOnCheckedChangeListener(new SegmentedControlWidget.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SegmentedControlWidget segmentedControl, int checkedIndex) {
                Log.d(TAG, "onCheckedChanged");

                Biquad b = filters.getActiveBiquad();
                Biquad.BiquadParam bp = b.getParams();

                if (checkedIndex == 1) { // set text

                    if (bp.getTypeValue() == BIQUAD_USER) return;

                    b.setEnabled(true);
                    byte prevType = bp.getTypeValue();
                    bp.setTypeValue(BIQUAD_USER);

                    b.sendToPeripheral(true);

                    //update pass biquads
                    if (prevType == BIQUAD_HIGHPASS) {
                        PassFilter pass = filters.getHighpass();
                        if (pass != null) pass.sendToPeripheral(true);

                    } else if (prevType == BIQUAD_LOWPASS) {
                        PassFilter pass = filters.getLowpass();
                        if (pass != null) pass.sendToPeripheral(true);
                    }

                } else { // set gui

                    if (bp.getTypeValue() != BIQUAD_USER) return;

                    b.setEnabled(filters.isPEQEnabled());
                    bp.setOrderValue(BIQUAD_ORDER_2);
                    bp.setTypeValue(BIQUAD_PARAMETRIC);

                    short freq = filters.getBetterNewFreqForBiquad(b);
                    bp.setFreq((freq != -1) ? freq : 100);

                    bp.setQFac(1.41f);
                    bp.setDbVolume(0.0f);

                    b.sendToPeripheral(true);
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

    @Override
    public void updateView() {
        biquadLabel.setText("BIQUAD #" + Integer.toString(filters.getActiveBiquadIndex() + 1));

        int index = (filters.getActiveBiquad().getParams().getTypeValue() == BIQUAD_USER) ? 1 : 0;
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
