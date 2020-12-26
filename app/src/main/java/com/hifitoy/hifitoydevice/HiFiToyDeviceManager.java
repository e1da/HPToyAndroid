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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
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
        if (getDevice("demo") == null) {
            setDevice("demo", new HiFiToyDevice());
        }
    }

    public void setDevice(String key, HiFiToyDevice device){
        deviceMap.put(key, device);

        store();
    }

    public HiFiToyDevice getDevice(String key){
        if (deviceMap != null){
            return deviceMap.get(key);
        }
        return null;
    }

    public Collection<HiFiToyDevice> getDevices() {
        return deviceMap.values();
    }

    public void description(){
        Log.d(TAG, "=============== <DeviceMap> ======================");
        for (Map.Entry<String,HiFiToyDevice> entry: deviceMap.entrySet()) {
            entry.getValue().description();
        }
        Log.d(TAG, "================</DeviceMap>======================");
    }

    //store/restore
    public void restore(){
        Log.d(TAG, "Restore HiFiToyDeviceMap.");

        Context context = ApplicationContext.getInstance().getContext();
        if (context == null) return;

        try {
            FileInputStream fis = context.openFileInput("HiFiToyDeviceMap.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            Object o = is.readObject();
            deviceMap = (HashMap<String, HiFiToyDevice>)o;

            is.close();
        } catch(FileNotFoundException f) {
            Log.d(TAG, "HiFiToyDeviceMap.dat is not found.");

            //create default preset
            deviceMap.clear();
            setDevice("demo", new HiFiToyDevice());

        } catch (IOException e) {
            Log.d(TAG, e.toString());
        } catch (ClassNotFoundException e) {
            Log.d(TAG, e.toString());
        }

    }

    public void store(){
        Log.d(TAG, "Store HiFiToyDeviceMap.");

        Context context = ApplicationContext.getInstance().getContext();
        if (context == null) return;

        try {
            FileOutputStream fos = context.openFileOutput("HiFiToyDeviceMap.dat",
                    Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(deviceMap);
            os.close();

            description();
        } catch (NotSerializableException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

}
