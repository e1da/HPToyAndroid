/*
 *   ApplicationContext.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy;

import android.content.Context;

public class ApplicationContext {
    private static ApplicationContext instance;
    private Context context;

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
}
