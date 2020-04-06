/*
 *   BiquadTextFragment.java
 *
 *   Created by Artem Khlyupin on 04/04/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.biquad_config;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hifitoy.dialogsystem.KeyboardDialog;
import com.hifitoy.dialogsystem.KeyboardNumber;
import com.hifitoy.dialogsystem.KeyboardNumber.NumberType;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.Biquad.BiquadParam;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.widgets.ValueWidget;
import com.hifitoy.R;

import java.util.Locale;

public class TextConfigFragment extends Fragment implements View.OnClickListener, KeyboardDialog.OnResultListener {
    private final String TAG = "HiFiToy";

    private ValueWidget b0Widget;
    private ValueWidget b1Widget;
    private ValueWidget b2Widget;
    private ValueWidget a1Widget;
    private ValueWidget a2Widget;

    private Filters filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.biquad_text_layout, container, false);
        b0Widget = v.findViewById(R.id.b0Widget_outl);
        b1Widget = v.findViewById(R.id.b1Widget_outl);
        b2Widget = v.findViewById(R.id.b2Widget_outl);
        a1Widget = v.findViewById(R.id.a1Widget_outl);
        a2Widget = v.findViewById(R.id.a2Widget_outl);

        b0Widget.setOnClickListener(this);
        b1Widget.setOnClickListener(this);
        b2Widget.setOnClickListener(this);
        a1Widget.setOnClickListener(this);
        a2Widget.setOnClickListener(this);

        updateOutlets();

        return v;
    }

    public void updateOutlets() {
        BiquadParam bp = filters.getActiveBiquad().getParams();

        b0Widget.setText("B0:", String.format(Locale.getDefault(), "%.6f", bp.getB0()), "");
        b1Widget.setText("B1:", String.format(Locale.getDefault(), "%.6f", bp.getB1()), "");
        b2Widget.setText("B2:", String.format(Locale.getDefault(), "%.6f", bp.getB2()), "");
        a1Widget.setText("A1:", String.format(Locale.getDefault(), "%.6f", bp.getA1()), "");
        a2Widget.setText("A2:", String.format(Locale.getDefault(), "%.6f", bp.getA2()), "");
    }

    @Override
    public void onClick(View v) {
        BiquadParam bp = filters.getActiveBiquad().getParams();
        KeyboardNumber n;
        String tag;

        if (v == b0Widget) {
            tag = "b0";
            n = new KeyboardNumber(NumberType.MAX_REAL, Float.toString(bp.getB0()));

        } else if (v == b1Widget) {
            tag = "b1";
            n = new KeyboardNumber(NumberType.MAX_REAL, Float.toString(bp.getB1()));

        } else if (v == b2Widget) {
            tag = "b2";
            n = new KeyboardNumber(NumberType.MAX_REAL, Float.toString(bp.getB2()));

        } else if (v == a1Widget) {
            tag = "a1";
            n = new KeyboardNumber(NumberType.MAX_REAL, Float.toString(bp.getA1()));

        } else if (v == a2Widget) {
            tag = "a2";
            n = new KeyboardNumber(NumberType.MAX_REAL, Float.toString(bp.getA2()));

        } else {
            return;
        }

        new KeyboardDialog(getActivity(), TextConfigFragment.this, n, tag).show();
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        try {
            BiquadParam bp = filters.getActiveBiquad().getParams();
            float rs = Float.parseFloat(result.getValue());

            if (tag.equals("b0")) {
                bp.setB0(rs);

            } else if (tag.equals("b1")) {
                bp.setB1(rs);

            } else if (tag.equals("b2")) {
                bp.setB2(rs);

            } else if (tag.equals("a1")) {
                bp.setA1(rs);

            } else if (tag.equals("a2")) {
                bp.setA2(rs);

            }
            updateOutlets();

        } catch (NumberFormatException e) {
                Log.d(TAG, e.toString());
        }
    }

}
