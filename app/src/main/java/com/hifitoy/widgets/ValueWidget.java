/*
 *   ValueWidget.java
 *
 *   Created by Artem Khlyupin on 4/4/2020.
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.widgets;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hifitoy.R;

public class ValueWidget extends LinearLayout {
    private TextView name;
    private TextView value;
    private TextView unit;

    public ValueWidget(Context context) {
        super(context);
        init(context);
    }

    public ValueWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.value_layout, this);

        name = v.findViewById(R.id.ValueWidget_NameLabel_outl);
        value = v.findViewById(R.id.ValueWidget_ValueLabel_outl);
        unit = v.findViewById(R.id.ValueWidget_UnitLabel_outl);

    }

    public void setName(String s) {
        name.setText(s);
    }
    public void setValue(String s) {
        value.setText(s);
    }
    public void setUnit(String s) {
        unit.setText(s);
    }
    public void setText(String nameStr, String valueStr, String unitStr) {
        setName(nameStr);
        setValue(valueStr);
        setUnit(unitStr);
    }
}
