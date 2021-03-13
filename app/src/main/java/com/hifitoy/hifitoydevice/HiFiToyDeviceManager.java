/*
 *   HiFiToyDeviceManager.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.hifitoy.ApplicationContext;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HiFiToyDeviceManager {
    private static final String TAG = "HiFiToy";
    private static HiFiToyDeviceManager instance;

    private Set<HiFiToyDevice> deviceSet = new HashSet<>();

    public static synchronized HiFiToyDeviceManager getInstance(){
        if (instance == null){
            instance = new HiFiToyDeviceManager();
        }
        return instance;
    }

    private HiFiToyDeviceManager(){
        restore();
        if (getDevice("demo") == null) {
            add(new HiFiToyDevice());
        }
    }

    public void add(HiFiToyDevice device){
        deviceSet.add(device);
        store();
    }

    public HiFiToyDevice getDevice(String mac){
        for (HiFiToyDevice d: deviceSet) {
            if (d.getMac().equals(mac)) {
                return d;
            }
        }
        return null;
    }

    public Collection<HiFiToyDevice> getDevices() {
        return deviceSet;
    }

    //store/restore
    private void restore(){
        Log.d(TAG, "Restore HiFiToyDeviceSet.");

        Context context = ApplicationContext.getInstance().getContext();
        if (context == null) return;

        try {
            FileInputStream fis = context.openFileInput("HiFiToyDeviceSet.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            Object o = is.readObject();
            deviceSet = (HashSet<HiFiToyDevice>)o;

            is.close();
        } catch(FileNotFoundException f) {
            Log.d(TAG, "HiFiToyDeviceSet.dat is not found.");

            //create default preset
            deviceSet.clear();
            add(new HiFiToyDevice());

        } catch (IOException | ClassNotFoundException e) {
            Log.d(TAG, e.toString());
        }

    }

    public void store(){
        Log.d(TAG, "Store HiFiToyDeviceSet.");

        Context context = ApplicationContext.getInstance().getContext();
        if (context == null) return;

        try {
            FileOutputStream fos = context.openFileOutput("HiFiToyDeviceSet.dat",
                    Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(deviceSet);
            os.close();

            Log.d(TAG, toString());

        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    @NonNull
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder(" \n");
        s.append("=============== <Devices> ======================\n");
        for (HiFiToyDevice d: deviceSet) {
            s.append(d.toString()).append("\n");
        }
        s.append("================</Devices>======================\n");
        return s.toString();
    }

}
