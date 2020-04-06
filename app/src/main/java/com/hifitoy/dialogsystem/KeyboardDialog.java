/*
 *   KeyboardDialog.java
 *
 *   Created by Artem Khlyupin on 28/12/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.dialogsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.hifitoy.R;

public class KeyboardDialog extends AlertDialog implements View.OnClickListener {
    private final String TAG = "AUE";
    private OnResultListener listener;

    private Button btn[], btn_back, btn_enter, btn_minus, btn_point;
    private TextView input;
    private KeyboardNumber number;
    private String tag;

    public interface OnResultListener {
        void onKeyboardResult(String tag, KeyboardNumber result);
    }

    public KeyboardDialog(Context context, OnResultListener listener, KeyboardNumber number, String tag) {
        super(context);
        this.listener = listener;
        this.number = number;
        this.tag = tag;

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        setView(inflater.inflate(R.layout.keyboard_layout, null));
    }

    @Override
    protected void onStart() {
        super.onStart();

        btn = new Button[10];
        btn[0] = findViewById(R.id.keyboard_btn_0);
        btn[1] = findViewById(R.id.keyboard_btn_1);
        btn[2] = findViewById(R.id.keyboard_btn_2);
        btn[3] = findViewById(R.id.keyboard_btn_3);
        btn[4] = findViewById(R.id.keyboard_btn_4);
        btn[5] = findViewById(R.id.keyboard_btn_5);
        btn[6] = findViewById(R.id.keyboard_btn_6);
        btn[7] = findViewById(R.id.keyboard_btn_7);
        btn[8] = findViewById(R.id.keyboard_btn_8);
        btn[9] = findViewById(R.id.keyboard_btn_9);
        btn_back    = findViewById(R.id.keyboard_btn_back);
        btn_enter   = findViewById(R.id.keyboard_btn_enter);
        btn_minus   = findViewById(R.id.keyboard_btn_minus);
        btn_point   = findViewById(R.id.keyboard_btn_point);

        input = findViewById(R.id.keyboard_input);
        input.setText(number.getValue());

        btn_point.setVisibility(number.typeIsInteger() ? View.INVISIBLE : View.VISIBLE);
        btn_minus.setVisibility(number.typeIsPositive() ? View.INVISIBLE : View.VISIBLE);

        for (int i = 0; i < 10; i++) {
            btn[i].setOnClickListener(this);
        }
        btn_back.setOnClickListener(this);
        btn_enter.setOnClickListener(this);
        btn_minus.setOnClickListener(this);
        btn_point.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String s = ((Button)v).getText().toString();

        if (s.equals("Enter")) {
            if (listener != null) listener.onKeyboardResult(tag, number);
            dismiss();
        } else {
            number.putChar(s.charAt(0));
            input.setText(number.getValue());
        }
    }

}
