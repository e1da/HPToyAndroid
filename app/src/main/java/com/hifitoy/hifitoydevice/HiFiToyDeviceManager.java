/*
 *   HiFiToyDeviceManager.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.hifitoy.ApplicationContext;
import java.util.HashMap;
import java.util.Map;

public class HiFiToyDeviceManager {
    private static final String TAG = "HiFiToy";
    private static HiFiToyDeviceManager instance;

    private Map<String, HiFiToyDevice> deviceMap = new HashMap<>();

    public static synchronized HiFiToyDeviceManager getInstance(){
        if (instance == null){
            instance = new HiFiToyDeviceManager();
        }
        return instance;
    }

    private HiFiToyDeviceManager(){
        restore();
    }

    public void setDevice(String key, HiFiToyDevice device){
        deviceMap.put(key, device);

        store();
        description();
    }

    public HiFiToyDevice getDevice(String key){
        if (deviceMap != null){
            return deviceMap.get(key);
        }
        return null;
    }

    public void description(){
        Log.d(TAG, "=============== <DeviceMap> ======================");
        for (Map.Entry<String,HiFiToyDevice> entry: deviceMap.entrySet()) {
            entry.getValue().description();
        }
        Log.d(TAG, "================</DeviceMap>======================");
    }

    private boolean restore(){
        Context context = ApplicationContext.getInstance().getContext();
        SharedPreferences sharedPref = context.getSharedPreferences("HiFiToyDeviceManager", Context.MODE_PRIVATE);

        if (sharedPref == null){
            Log.d(TAG, "sharedPref == null.");
            return false;
        }

        int length = sharedPref.getInt("DeviceCount", -1);
        if (length == -1){
            Log.d(TAG, "DeviceCount == -1.");
            return false;
        }
        Log.d(TAG, "sharedPref length == " + Integer.toString(length));

        deviceMap.clear();

        for (int i = 0; i < length; i++){
            HiFiToyDevice device = new HiFiToyDevice();
            device.restore("HiFiToyDeviceManager", "HiFiToyDevice#" + Integer.toString(i));
            deviceMap.put(device.getMac(), device);
        }

        Log.d(TAG, "Restore DeviceMap.");
        return true;
    }

    public boolean store(){
        Context context = ApplicationContext.getInstance().getContext();
        SharedPreferences sharedPref = context.getSharedPreferences("HiFiToyDeviceManager", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (editor == null){
            Log.d(TAG, "sharedPref editor == null.");
            return false;
        }

        editor.clear();

        int length = deviceMap.size();
        editor.putInt("DeviceCount", length);

        editor.commit();

        int count = 0;
        for(Map.Entry<String, HiFiToyDevice> entry : deviceMap.entrySet()) {
            StoreInterface device = entry.getValue();
            device.store("HiFiToyDeviceManager", "HiFiToyDevice#" + Integer.toString(count));
            count++;
        }

        Log.d(TAG, "Store DeviceMap.");
        return true;
    }

}
