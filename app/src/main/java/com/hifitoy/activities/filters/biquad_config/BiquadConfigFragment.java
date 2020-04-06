/*
 *   BiquadFragment.java
 *
 *   Created by Artem Khlyupin on 04/04/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.biquad_config;

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
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Biquad.BiquadParam;
import com.hifitoy.hifitoyobjects.PassFilter;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.widgets.SegmentedControlWidget;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Order.BIQUAD_ORDER_2;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_USER;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;

public class BiquadConfigFragment extends Fragment {
    private final String TAG = "HiFiToy";

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
                updateOutlets();
            }
        });

        nextBiquadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters.incActiveBiquadIndex();
                updateOutlets();
            }
        });

        biquadInputTypeWidget.setOnCheckedChangeListener(new SegmentedControlWidget.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SegmentedControlWidget segmentedControl, int checkedIndex) {
                Log.d(TAG, "onCheckedChanged");

                Biquad b = filters.getActiveBiquad();
                BiquadParam bp = b.getParams();

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

                updateOutlets();
            }
        });
        updateOutlets();

        return v;
    }

    public void updateOutlets() {
        biquadLabel.setText("BIQUAD #" + Integer.toString(filters.getActiveBiquadIndex()));

        int index = (filters.getActiveBiquad().getParams().getTypeValue() == BIQUAD_USER) ? 1 : 0;
        biquadInputTypeWidget.check(index);

        FragmentTransaction fTrans = getFragmentManager().beginTransaction();

        if (index == 1) {
            fTrans.remove(guiFragment);
            if (getFragmentManager().findFragmentByTag("textFragment") == null) {
                fTrans.add(biquadData.getId(), textFragment, "textFragment");
            }

        } else {
            fTrans.remove(textFragment);
            if (getFragmentManager().findFragmentByTag("guiFragment") == null) {
                fTrans.add(biquadData.getId(), guiFragment, "guiFragment");
            }

        }
        fTrans.commit();

    }
}
