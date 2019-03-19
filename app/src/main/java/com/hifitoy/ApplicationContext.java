/*
 *   ApplicationContext.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy;

import android.content.Context;
import android.content.Intent;

public class ApplicationContext {
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
    }

    public Context getContext(){
        return context;
    }

    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }
    public void broadcastUpdate(final String action, byte[] data) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, data);
        context.sendBroadcast(intent);
    }

}
