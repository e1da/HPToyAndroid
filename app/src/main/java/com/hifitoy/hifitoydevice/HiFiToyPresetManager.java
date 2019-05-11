/*
 *   HiFiToyPresetManager.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class HiFiToyPresetManager {
    private static final String TAG = "HiFiToy";
    private static HiFiToyPresetManager instance;

    private Map<String, HiFiToyPreset> presetMap = new HashMap<String, HiFiToyPreset>();

    public static synchronized HiFiToyPresetManager getInstance(){
        if (instance == null){
            instance = new HiFiToyPresetManager();
        }
        return instance;
    }

    private HiFiToyPresetManager(){
        restore();
        if (getPreset("DefaultPreset") == null) {
            setPreset("DefaultPreset", new HiFiToyPreset());
        }
    }

    //store/restore
    public void restore(){
        Context context = ApplicationContext.getInstance().getContext();

        try {
            FileInputStream fis = context.openFileInput("HiFiToyPresetMap.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            presetMap = (HashMap<String, HiFiToyPreset>)is.readObject();

            is.close();
            Log.d(TAG, "Restore HiFiToyPresetMap.");

        } catch(FileNotFoundException f) {
            Log.d(TAG, "HiFiToyPresetMap.dat is not found.");

            //create default preset
            presetMap.clear();
            HiFiToyPreset preset = new HiFiToyPreset();
            setPreset("DefaultPreset", preset);

            Log.d(TAG, "Create HiFiToyPresetMap.");

        } catch (IOException w) {
            w.printStackTrace();
        } catch (ClassNotFoundException w) {
            w.printStackTrace();
        }

    }

    public void store(){
        Context context = ApplicationContext.getInstance().getContext();
        try {
            FileOutputStream fos = context.openFileOutput("HiFiToyPresetMap.dat",
                    Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(presetMap);
            os.close();
            Log.d(TAG, "Store HiFiToyPresetMap.");

        } catch (NotSerializableException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    //map methods: count/remove/get/set
    public int size() {
        return presetMap.size();
    }

    public void removePreset(String key) {
        presetMap.remove(key);

        store();
        description();
    }

    public void setPreset(String key, HiFiToyPreset preset){
        presetMap.put(key, preset);//must be clone!!!

        store();
        description();
    }

    public boolean isPresetExist(String key) {
        return getPreset(key) != null;
    }

    public HiFiToyPreset getPreset(String key){
        return presetMap.get(key);
    }

    public HiFiToyPreset getPreset(int position){
        if (position < size()){
            Object[] presetArray = presetMap.values().toArray();
            return (HiFiToyPreset)presetArray[position];
        }
        return null;
    }

    public HiFiToyPreset getDefaultDspPreset(){
        HiFiToyPreset preset = getPreset("DefaultPreset");
        if (preset == null) {
            preset = new HiFiToyPreset();
            setPreset("DefaultPreset", preset);
        }

        return preset;
    }

    public String getKey(int position){
        if (position < size()){
            Object[] keys = presetMap.keySet().toArray();
            return (String)keys[position];
        }
        return null;
    }

    public void description() {
        Log.d(TAG, "=============== <PresetMap> ======================");
        for (Map.Entry<String, HiFiToyPreset> entry: presetMap.entrySet()) {
            Log.d(TAG, "key=" + entry.getKey() +
                            " name=" + entry.getValue().getInfo());
        }
        Log.d(TAG, "================</PresetMap>======================");
    }
}
