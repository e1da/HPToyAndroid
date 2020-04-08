/*
 *   BiquadGuiFragment.java
 *
 *   Created by Artem Khlyupin on 04/04/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.config_fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hifitoy.activities.filters.ViewUpdater;
import com.hifitoy.dialogsystem.KeyboardDialog;
import com.hifitoy.dialogsystem.KeyboardNumber;
import com.hifitoy.dialogsystem.KeyboardNumber.NumberType;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.Biquad.BiquadParam;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.hifitoyobjects.PassFilter;
import com.hifitoy.widgets.ValueWidget;
import com.hifitoy.R;

import java.util.Locale;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;

public class GuiConfigFragment extends Fragment implements View.OnClickListener,
                                                            KeyboardDialog.OnResultListener,
                                                            ViewUpdater.IFilterUpdateView {
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

        ViewUpdater.getInstance().addUpdateView(this);
        ViewUpdater.getInstance().update();

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        ViewUpdater.getInstance().removeUpdateView(this);
    }

    @Override
    public void updateView() {
        BiquadParam bp = filters.getActiveBiquad().getParams();

        if (bp.getTypeValue() == BIQUAD_PARAMETRIC) {
            freqWidget.setVisibility(VISIBLE);
            qFacWidget.setVisibility(VISIBLE);
            volumeWidget.setVisibility(VISIBLE);

            freqWidget.setText("Freq:", String.format(Locale.getDefault(), "%d", bp.getFreq()), "Hz");
            qFacWidget.setText("Q:", String.format(Locale.getDefault(), "%.2f", bp.getQFac()), "");
            volumeWidget.setText("Vol:", String.format(Locale.getDefault(), "%.1f", bp.getDbVolume()), "Db");

        } else {
            freqWidget.setVisibility(VISIBLE);
            qFacWidget.setVisibility(INVISIBLE);
            volumeWidget.setVisibility(INVISIBLE);

            freqWidget.setText("Freq:", String.format(Locale.getDefault(), "%d", bp.getFreq()), "Hz");
        }
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
            Biquad b = filters.getActiveBiquad();
            BiquadParam bp = b.getParams();

            if (tag.equals("freq")) {
                int f = Integer.parseInt(result.getValue());

                if (bp.getTypeValue() == BIQUAD_LOWPASS) {
                    PassFilter lp = filters.getLowpass();
                    lp.setFreq((short)f);
                    lp.sendToPeripheral(true);

                } else if (bp.getTypeValue() == BIQUAD_HIGHPASS) {
                    PassFilter hp = filters.getHighpass();
                    hp.setFreq((short)f);
                    hp.sendToPeripheral(true);

                } else {
                    bp.setFreq((short) f);
                    b.sendToPeripheral(true);
                }

            } else if (tag.equals("q")) {
                float q = Float.parseFloat(result.getValue());
                bp.setQFac(q);
                b.sendToPeripheral(true);

            } else if (tag.equals("db")) {
                float db = Float.parseFloat(result.getValue());
                bp.setDbVolume(db);
                b.sendToPeripheral(true);

            }

            ViewUpdater.getInstance().update();

        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }
    }
}
