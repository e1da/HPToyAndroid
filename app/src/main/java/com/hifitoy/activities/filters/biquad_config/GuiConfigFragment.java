/*
 *   BiquadGuiFragment.java
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

public class GuiConfigFragment extends Fragment implements View.OnClickListener, KeyboardDialog.OnResultListener {
    private final String TAG = "HiFiToy";

    private ValueWidget freqWidget;
    private ValueWidget qFacWidget;
    private ValueWidget volumeWidget;

    private Filters filters = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getFilters();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.biquad_gui_layout, container, false);
        freqWidget = v.findViewById(R.id.freqWidget_outl);
        qFacWidget = v.findViewById(R.id.qFacWidget_outl);
        volumeWidget = v.findViewById(R.id.volumeWidget_outl);

        freqWidget.setOnClickListener(this);
        qFacWidget.setOnClickListener(this);
        volumeWidget.setOnClickListener(this);

        updateOutlets();

        return v;
    }

    public void updateOutlets() {
        BiquadParam bp = filters.getActiveBiquad().getParams();

        freqWidget.setText("Freq:", String.format(Locale.getDefault(), "%d", bp.getFreq()), "Hz");
        qFacWidget.setText("Q:", String.format(Locale.getDefault(), "%.2f", bp.getQFac()), "");
        volumeWidget.setText("Vol:", String.format(Locale.getDefault(), "%.1f", bp.getDbVolume()), "Db");
    }

    @Override
    public void onClick(View v) {
        BiquadParam bp = filters.getActiveBiquad().getParams();
        KeyboardNumber n;
        String tag;

        if (v == freqWidget) {
            tag = "freq";
            n = new KeyboardNumber(NumberType.POSITIVE_INTEGER, Integer.toString(bp.getFreq()));

        } else if (v == qFacWidget) {
            tag = "q";
            n = new KeyboardNumber(NumberType.POSITIVE_DOUBLE, Float.toString(bp.getQFac()));

        } else if (v == volumeWidget) {
            tag = "db";
            n = new KeyboardNumber(NumberType.FLOAT, Float.toString(bp.getDbVolume()));

        } else {
            return;
        }

        new KeyboardDialog(getActivity(), GuiConfigFragment.this, n, tag).show();
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        try {
            BiquadParam bp = filters.getActiveBiquad().getParams();

            if (tag.equals("freq")) {
                int f = Integer.parseInt(result.getValue());
                bp.setFreq((short)f);

            } else if (tag.equals("q")) {
                float q = Float.parseFloat(result.getValue());
                bp.setQFac(q);

            } else if (tag.equals("db")) {
                float db = Float.parseFloat(result.getValue());
                bp.setDbVolume(db);

            }

            updateOutlets();

        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }
    }
}
