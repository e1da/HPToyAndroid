/*
 *   HiFiToyPresetManager.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import com.hifitoy.ApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.hifitoy.dialogsystem.DialogSystem;

import org.xmlpull.v1.XmlPullParserException;

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
        printOfficialPresets();

        restore();
        if (!isPresetExist("No processing")) {
            setPreset(new HiFiToyPreset());
        }
    }

    private boolean checkFormat(String filename) {
        return filename.contains(".tpr");
    }

    private String filenameToPresetName(String name) {
        int end = name.indexOf(".tpr");
        if (end != -1) {
            return name.substring(0, end);
        }
        return name;
    }

    private List<String> getOfficialPresetNameList() {
        List<String> presetNameList = new ArrayList<>();
        presetNameList.add("No processing");

        Context c = ApplicationContext.getInstance().getContext();
        AssetManager am = c.getAssets();

        try {
            String[] list = am.list("base_presets");
            if (list == null) return presetNameList;

            for (String filename : list) {
                if (checkFormat(filename)) {
                    presetNameList.add(filenameToPresetName(filename));
                }
            }

        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        return presetNameList;
    }

    private HiFiToyPreset getOfficialPreset(String presetName) throws IOException, XmlPullParserException {
        if (presetName.equals("No processing")) {
            return new HiFiToyPreset();
        }

        Context c = ApplicationContext.getInstance().getContext();
        AssetManager am = c.getAssets();

        for (String n : getOfficialPresetNameList()) {
            if (presetName.equals(n)) {
                String filename = presetName + ".tpr";
                return new HiFiToyPreset(filename, am.open("base_presets/" + filename));
            }
        }
        return null;
    }

    private void restore(){
        Context context = ApplicationContext.getInstance().getContext();

        try {
            FileInputStream fis = context.openFileInput("HiFiToyPresetMap.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            presetList = (LinkedList<HiFiToyPreset>)is.readObject();

            is.close();
            Log.d(TAG, "Restore HiFiToyPresetMap.");

        } catch(FileNotFoundException f) {
            Log.d(TAG, "HiFiToyPresetMap.dat is not found.");
            restorePresetsFromAsserts();//restoreFromBundle();

        } catch (IOException w) {
            w.printStackTrace();
            restorePresetsFromAsserts();//restoreFromBundle();

        } catch (ClassNotFoundException w) {
            w.printStackTrace();
            restorePresetsFromAsserts();//restoreFromBundle();

        }

    }

    private String parsePresetName(String filename) {
        int index = filename.lastIndexOf(".");
        if (index != -1){
            filename = filename.substring(0, index);
        }
        return filename;
    }

    private void restorePresetsFromAsserts() {
        Context c = ApplicationContext.getInstance().getContext();
        AssetManager am = c.getAssets();

        String[] list;

        presetList.clear();
        presetList.add(new HiFiToyPreset()); // add No processing

        try {
            list = am.list("base_presets");
            if ((list != null) && (list.length > 0) ) {

                for (String filename : list) {
                    HiFiToyPreset p = new HiFiToyPreset(filename, am.open("base_presets/" + filename));

                    presetList.add(p);
                    Log.d(TAG, "Import preset = '" + p.getInfo() + "'");
                }
            }
        } catch (XmlPullParserException | IOException e) {
            Log.d(TAG, e.toString());
        }

        store();

        Log.d(TAG, "Finished restore preset from asserts.");
    }

    private void store(){
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

    public void setPreset(HiFiToyPreset preset) {
        try {
            setPreset(preset, true);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void setPreset(HiFiToyPreset preset, boolean rewrite) throws Exception {

        try {

            for (int i = 0; i < presetList.size(); i++) {
                HiFiToyPreset p = presetList.get(i);

                if (p.getName().equals(preset.getName())) {
                    if (rewrite) {
                        presetList.set(i, preset.clone());

                        store();
                        description();
                        return;

                    } else {
                        Log.d(TAG, "Error. Preset with this name already exist!");
                        throw new Exception("Preset with this name already exist!");
                    }
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

    public int getPresetIndex(String name) {
        for (int i = 0; i < size(); i++) {
            if (presetList.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
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

    public void importPreset(Uri uri) {
        if (uri == null) {
            DialogSystem.getInstance().showDialog("Error", "Uri is null.", "Ok");
            return;
        }

        try {
            HiFiToyPreset importPreset = new HiFiToyPreset(uri);

            //add new preset to list and store
            HiFiToyPresetManager.getInstance().setPreset(importPreset);

            DialogSystem.getInstance().showDialog("Completed",
                    "Preset '" + importPreset.getName() + "' imported successfully.", "Ok");

        } catch (IOException | XmlPullParserException e) {
            DialogSystem.getInstance().showDialog("Error", e.getMessage(), "Ok");
        }
    }


    public void description() {
        Log.d(TAG, "=============== <PresetList> ======================");
        for (HiFiToyPreset p : presetList) {
            Log.d(TAG, "name=" + p.getInfo());
        }
        Log.d(TAG, "================</PresetList>======================");
    }

    private void printPresets(List<String> presetNameList) {
        Log.d(TAG, "Files count: "+ presetNameList.size());

        for (String name : presetNameList) {
            Log.d(TAG, "Filename: " + name);
        }
    }

    public void printOfficialPresets() {
        Log.d(TAG, "Official presets:");
        printPresets(getOfficialPresetNameList());
    }
}
