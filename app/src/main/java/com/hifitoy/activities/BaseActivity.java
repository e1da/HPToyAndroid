/*
 *   BaseActivity.java
 *
 *   Created by Artem Khlyupin on 26/11/2020
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities;

import android.app.Activity;

import com.hifitoy.ApplicationContext;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

    }

    public void setupOutlets() {

    }

}
