/*
 *   BaseProgressDialog.java
 *
 *   Created by Artem Khlyupin on 11/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.dialogsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;

public class BaseProgressDialog extends ProgressDialog {
    private Context context;
    private int tempOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public BaseProgressDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void show() {
        Activity activity = (Activity)context;

        tempOrientation = activity.getRequestedOrientation();
        activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        setProgressDrawable(context.getDrawable(R.drawable.progress_bar));


        super.show();

        setColor();
    }

    @Override
    public void dismiss() {
        setProgress(0);
        super.dismiss();

        Activity activity = (Activity)context;
        activity.setRequestedOrientation(tempOrientation);
    }

    public String getTitle(){
        Resources res = context.getResources();
        TextView title = findViewById(res.getIdentifier("alertTitle", "id", "android"));

        if (title != null) {
            return title.getText().toString();
        }

        return null;
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
