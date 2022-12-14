/*
 *   ApplicationContext.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.activities.BaseActivity;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;

public class ApplicationContext {
    private static final String TAG = "HiFiToy";

    private static ApplicationContext instance;
    private Context context;

    public final static String EXTRA_DATA = "com.hifitoy.EXTRA_DATA";

    public static ApplicationContext getInstance(){
        if (instance == null){
            instance = new ApplicationContext();
        }
        return instance;
    }

    public void setContext(Context context){
        this.context = context;
        updateActionBar();
        updateClipView();
    }

    public Context getContext(){
        return context;
    }

    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }
    public void broadcastUpdate(final String action, int value) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, value);
        context.sendBroadcast(intent);
    }
    public void broadcastUpdate(final String action, byte[] data) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, data);
        context.sendBroadcast(intent);
    }

    public void setupOutlets() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Activity a = (Activity) context;
                Class superclass = a.getClass().getSuperclass();

                if ((superclass != null) && (superclass.equals(BaseActivity.class))) {
                    ((BaseActivity)a).setupOutlets();
                }
            }
        };
        mainHandler.post(myRunnable);
    }

    public void showToast(final CharSequence msg) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        };
        mainHandler.post(myRunnable);
    }

    public void updateActionBar() {


    }

    public void updateClipView() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {

                Activity a = (Activity) context;
                ActionBar actionBar = a.getActionBar();

                if (actionBar != null) {
                    HiFiToyDevice d = HiFiToyControl.getInstance().getActiveDevice();
                    if ( (d != null) && (d.getClipFlag()) ) {
                        actionBar.setBackgroundDrawable(context.getResources().
                                getDrawable(R.drawable.clip_on_shape, a.getTheme()));
                    } else {
                        actionBar.setBackgroundDrawable(context.getResources().
                                getDrawable(R.drawable.clip_off_shape, a.getTheme()));
                    }
                }

            }
        };
        mainHandler.post(myRunnable);


    }

}
