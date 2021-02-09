/*
 *   BiquadGuiFragment.java
 *
 *   Created by Artem Khlyupin on 04/04/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities.filters.config_fragment;

import android.support.v4.app.Fragment;
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
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.biquad.IFreq;
import com.hifitoy.hifitoyobjects.biquad.ParamBiquad;
import com.hifitoy.hifitoyobjects.biquad.Type;
import com.hifitoy.hifitoyobjects.filter.Filter;
import com.hifitoy.hifitoyobjects.filter.HighpassFilter;
import com.hifitoy.hifitoyobjects.filter.LowpassFilter;
import com.hifitoy.widgets.ValueWidget;
import com.hifitoy.R;

import java.util.Locale;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_PARAMETRIC;

public class GuiConfigFragment extends Fragment implements View.OnClickListener,
                                                            KeyboardDialog.OnResultListener {
    private final String TAG = "HiFiToy";

    private ValueWidget freqWidget;
    private ValueWidget qFacWidget;
    private ValueWidget volumeWidget;

    private Filter filter = HiFiToyControl.getInstance().getActiveDevice().getActivePreset().getActiveFilter();

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

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setFilter(Filter f) {
        filter = f;
    }

    public void updateView() {
        Biquad b = filter.getActiveBiquad();

        if (Type.getType(b) == BIQUAD_PARAMETRIC) {
            freqWidget.setVisibility(VISIBLE);
            qFacWidget.setVisibility(VISIBLE);
            volumeWidget.setVisibility(VISIBLE);

            ParamBiquad pb = (ParamBiquad)b;
            freqWidget.setText("Freq:", String.format(Locale.getDefault(), "%d", pb.getFreq()), "Hz");
            qFacWidget.setText("Q:", String.format(Locale.getDefault(), "%.2f", pb.getQ()), "");
            volumeWidget.setText("Vol:", String.format(Locale.getDefault(), "%.1f", pb.getDbVolume()), "Db");

        } else {
            freqWidget.setVisibility(VISIBLE);
            qFacWidget.setVisibility(INVISIBLE);
            volumeWidget.setVisibility(INVISIBLE);

            short freq = ((IFreq)b).getFreq();
            freqWidget.setText("Freq:", String.format(Locale.getDefault(), "%d", freq), "Hz");
        }
    }

    @Override
    public void onClick(View v) {
        Biquad b = filter.getActiveBiquad();
        KeyboardNumber n;
        String tag;

        if ( (v == freqWidget) && (IFreq.class.isAssignableFrom(b.getClass())) ) {
            tag = "freq";
            n = new KeyboardNumber(NumberType.POSITIVE_INTEGER, ((IFreq)b).getFreq());

        } else if (v == qFacWidget) {
            tag = "q";
            n = new KeyboardNumber(NumberType.POSITIVE_DOUBLE, ((ParamBiquad)b).getQ());

        } else if (v == volumeWidget) {
            tag = "db";
            n = new KeyboardNumber(NumberType.FLOAT, ((ParamBiquad)b).getDbVolume());

        } else {
            return;
        }

        new KeyboardDialog(getActivity(), GuiConfigFragment.this, n, tag).show();
    }

    @Override
    public void onKeyboardResult(String tag, KeyboardNumber result) {
        try {
            Biquad b = filter.getActiveBiquad();

            if (tag.equals("freq")) {
                int f = Integer.parseInt(result.getValue());

                if (Type.getType(b) == BIQUAD_LOWPASS) {
                    LowpassFilter lp = new LowpassFilter(filter);
                    lp.setFreq((short)f);
                    lp.sendToPeripheral(true);

                } else if (Type.getType(b) == BIQUAD_HIGHPASS) {
                    HighpassFilter hp = new HighpassFilter(filter);
                    hp.setFreq((short)f);
                    hp.sendToPeripheral(true);

                } else {
                    ((ParamBiquad)b).setFreq((short) f);
                    ((ParamBiquad)b).sendToPeripheral(true);
                }

            } else if (tag.equals("q")) {
                float q = Float.parseFloat(result.getValue());
                ((ParamBiquad)b).setQ(q);
                ((ParamBiquad)b).sendToPeripheral(true);

            } else if (tag.equals("db")) {
                float db = Float.parseFloat(result.getValue());
                ((ParamBiquad)b).setDbVolume(db);
                ((ParamBiquad)b).sendToPeripheral(true);

            }

            ViewUpdater.getInstance().update();

        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }
    }
}
