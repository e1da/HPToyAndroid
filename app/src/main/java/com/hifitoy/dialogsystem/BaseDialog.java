/*
 *   BaseDialog.java
 *
 *   Created by Artem Khlyupin on 09/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.dialogsystem;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;

public class BaseDialog extends AlertDialog {
    protected BaseDialog(Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();
        setColor();
    }

    private void setColor() {
        Context c = ApplicationContext.getInstance().getContext();
        int colorTitle = c.getResources().getColor(R.color.colorWhite);
        int colorDivider = c.getResources().getColor(R.color.colorAlphaWhite);

        setColor(colorTitle, colorDivider);
    }

    private void setColor(int colorTitle, int colorDivider) {
        Context c = ApplicationContext.getInstance().getContext();

        int titleDividerId = c.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = findViewById(titleDividerId);
        if (titleDivider != null) {

            titleDivider.setBackgroundColor(colorDivider);
        }
        int textViewId = getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = findViewById(textViewId);
        tv.setTextColor(colorTitle);
    }
}
