package com.hifitoy.activities;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class SystemBarInsetsHelper {
    private static final int EDGE_TO_EDGE_ENFORCEMENT_API = 35;

    private SystemBarInsetsHelper() {
    }

    public static void applyTopInsetPadding(Activity activity) {
        if (Build.VERSION.SDK_INT < EDGE_TO_EDGE_ENFORCEMENT_API) {
            return;
        }

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }

        View root = content.getChildAt(0);
        final int initialPaddingLeft = root.getPaddingLeft();
        final int initialPaddingTop = root.getPaddingTop();
        final int initialPaddingRight = root.getPaddingRight();
        final int initialPaddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialPaddingLeft,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight,
                    initialPaddingBottom
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
