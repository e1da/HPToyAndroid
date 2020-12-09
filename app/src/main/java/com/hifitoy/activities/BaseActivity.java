/*
 *   BaseActivity.java
 *
 *   Created by Artem Khlyupin on 26/11/2020
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.activities;

import android.app.Activity;
import android.view.MenuItem;

import com.hifitoy.ApplicationContext;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationContext.getInstance().setContext(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void setupOutlets() {

    }

}
