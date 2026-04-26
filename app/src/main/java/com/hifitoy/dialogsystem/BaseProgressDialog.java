/*
 *   BaseProgressDialog.java
 *
 *   Created by Artem Khlyupin on 11/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.dialogsystem;

import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hifitoy.R;

public class BaseProgressDialog extends BaseDialog {
    private final ProgressBar progressBar;

    public BaseProgressDialog(Context context) {
        super(context);

        LinearLayout container = new LinearLayout(context);
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, context.getResources().getDisplayMetrics());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, padding);

        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setId(R.id.progress_dialog_bar);
        progressBar.setIndeterminate(false);
        progressBar.setProgressDrawable(context.getDrawable(R.drawable.progress_bar));
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        container.addView(progressBar);
        setView(container);
    }

    public void setMax(int max) {
        progressBar.setMax(max);
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    public void setSecondaryProgress(int progress) {
        progressBar.setSecondaryProgress(progress);
    }

    public void incrementProgressBy(int diff) {
        progressBar.incrementProgressBy(diff);
    }
}
