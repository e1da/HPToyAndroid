/*
 *   HiFiToyPresetManager.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

public class HiFiToyPresetManager {
    private static final String TAG = "HiFiToy";
    private static HiFiToyPresetManager instance;

    private List<HiFiToyPreset> presetList = new LinkedList<HiFiToyPreset>();

    public static synchronized HiFiToyPresetManager getInstance(){
        if (instance == null){
            instance = new HiFiToyPresetManager();
        }
        return instance;
    }

    private HiFiToyPresetManager(){
        restore();
        if (!isPresetExist("No processing")) {
            setPreset(new HiFiToyPreset());
        }
    }

    //store/restore
    public void restore(){
        Context context = ApplicationContext.getInstance().getContext();

        try {
            FileInputStream fis = context.openFileInput("HiFiToyPresetMap.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            presetList = (LinkedList<HiFiToyPreset>)is.readObject();

            is.close();
            Log.d(TAG, "Restore HiFiToyPresetMap.");

        } catch(FileNotFoundException f) {
            Log.d(TAG, "HiFiToyPresetMap.dat is not found.");
            restoreFromBundle();

        } catch (IOException w) {
            w.printStackTrace();
            restoreFromBundle();

        } catch (ClassNotFoundException w) {
            w.printStackTrace();
            restoreFromBundle();

        }

    }

    private void restoreFromBundle() {
        Context context = ApplicationContext.getInstance().getContext();

        try {
            InputStream is = context.getResources().openRawResource(R.raw.presets);
            ObjectInputStream ois = new ObjectInputStream(is);
            presetList = (LinkedList<HiFiToyPreset>)ois.readObject();

            is.close();
            Log.d(TAG, "Restore HiFiToyPresetMap from bundle.");

            store();

        } catch(Resources.NotFoundException f) {
            Log.d(TAG, "HiFiToyPresetMap.dat is not found in bundle.");

            //create default preset
            presetList.clear();
            setPreset(new HiFiToyPreset());

            Log.d(TAG, "Create null HiFiToyPresetMap.");

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
            os.writeObject(presetList);
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
        return presetList.size();
    }

    public void removePreset(String name) {
        for (int i = 0; i < presetList.size(); i++) {
            HiFiToyPreset p = presetList.get(i);

            if (p.getName().equals(name)) {
                presetList.remove(i);

                store();
                description();
                break;
            }
        }
    }

    public void setPreset(HiFiToyPreset preset){

        try {

            for (int i = 0; i < presetList.size(); i++) {
                HiFiToyPreset p = presetList.get(i);

                if (p.getName().equals(preset.getName())) {
                    presetList.set(i, preset.clone());

                    store();
                    description();
                    return;
                }
            }

            presetList.add(preset.clone());
            store();
            description();

        } catch (CloneNotSupportedException e) {
            Log.d(TAG, e.toString());
        }
    }

    public boolean renamePreset(String oldName, String newName) {
        for (int i = 0; i < presetList.size(); i++) {
            HiFiToyPreset p = presetList.get(i);

            if (p.getName().equals(oldName)) {
                p.setName(newName);

                store();
                description();
                return true;
            }
        }
        return false;
    }

    public boolean isPresetExist(String name) {
        for (HiFiToyPreset p : presetList) {
            if (p.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public HiFiToyPreset getPreset(String name){
        for (HiFiToyPreset p : presetList) {
            if (p.getName().equals(name)) {
                try {
                    return p.clone();
                } catch (CloneNotSupportedException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        return null;
    }

    public HiFiToyPreset getPreset(int position){
        if (position < size()){
            try {
                return presetList.get(position).clone();
            } catch (CloneNotSupportedException e) {
                Log.d(TAG, e.toString());
            }
        }
        return null;
    }

    public void description() {
        Log.d(TAG, "=============== <PresetList> ======================");
        for (HiFiToyPreset p : presetList) {
            Log.d(TAG, "name=" + p.getInfo());
        }
        Log.d(TAG, "================</PresetList>======================");
    }
}
