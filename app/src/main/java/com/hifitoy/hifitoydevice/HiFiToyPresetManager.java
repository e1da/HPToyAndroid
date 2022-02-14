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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.hifitoy.dialogsystem.DialogSystem;

import org.xmlpull.v1.XmlPullParserException;

public class HiFiToyPresetManager {
    private static final String TAG = "HiFiToy";
    private static HiFiToyPresetManager instance;

    private final List<HiFiToyPreset> presetList;

    public static synchronized HiFiToyPresetManager getInstance(){
        if (instance == null){
            instance = new HiFiToyPresetManager();
        }
        return instance;
    }

    private HiFiToyPresetManager(){
        printOfficialPresets();
        printUserPresets();

        //restore preset list
        presetList = new LinkedList<>();

        for (String presetName : getOfficialPresetNameList()) {
            try {
                presetList.add(getOfficialPreset(presetName));
            } catch (IOException | XmlPullParserException e) {
                Log.d(TAG, e.toString());
            }
        }
        for (String presetName : getUserPresetNameList()) {
            try {
                presetList.add(getUserPreset(presetName));
            } catch (IOException | XmlPullParserException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    public File getUserDir() {
        Context context = ApplicationContext.getInstance().getContext();

        //get app internal directory
        File dir = new File(context.getFilesDir() + "/PresetList");

        if ((!dir.exists()) && (!dir.mkdirs())) {
            Log.d(TAG, "Error. Internal directory is not available.");
            Toast.makeText(context,
                    "Error. Internal directory is not available.", Toast.LENGTH_SHORT).show();

            return null;
        }
        return dir;
    }

    private boolean checkFormat(String filename) {
        return filename.contains(".tpr");
    }
    private boolean checkFormat(File file) {
        return file.getName().contains(".tpr");
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

    public List<String> getUserPresetNameList() {
        List<String> presetNameList = new ArrayList<>();

        File dir = getUserDir();
        if (dir == null) {
            return presetNameList;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return presetNameList;
        }

        for (File f : files) {
            if (checkFormat(f)) {
                presetNameList.add(filenameToPresetName(f.getName()));
            }
        }

        return presetNameList;
    }

    public List<String> getPresetNameList() {
        List<String> presetNameList = new ArrayList<>();
        presetNameList.addAll(getOfficialPresetNameList());
        presetNameList.addAll(getUserPresetNameList());
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
        throw new IOException("Official preset not found.");
    }

    private HiFiToyPreset getUserPreset(String presetName) throws IOException, XmlPullParserException {
        File dir = getUserDir();
        if (dir == null) {
            throw new IOException("User preset directory not found.");
        }

        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("User preset not found");
        }

        for (File f : files) {
            String n = filenameToPresetName(f.getName());
            if (presetName.equals(n)) {
                return new HiFiToyPreset(f);
            }
        }
        throw new IOException("User preset not found");
    }

    public HiFiToyPreset getPreset(String presetName) throws IOException, XmlPullParserException {
        HiFiToyPreset p = getOfficialPreset(presetName);
        if (p == null) {
            return getUserPreset(presetName);
        }
        return p;
    }

    public int getOfficialPresetSize() {
        return getOfficialPresetNameList().size();
    }
    public int getUserPresetSize() {
        return getUserPresetNameList().size();
    }
    public int size() {
        return getOfficialPresetSize() + getUserPresetSize();
    }

    public void removePreset(String name) {
        for (int i = 0; i < presetList.size(); i++) {
            HiFiToyPreset p = presetList.get(i);

            if (p.getName().equals(name)) {
                presetList.remove(i);

                //store();
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

                        //store();
                        description();
                        return;

                    } else {
                        Log.d(TAG, "Error. Preset with this name already exist!");
                        throw new Exception("Preset with this name already exist!");
                    }
                }
            }

            presetList.add(preset.clone());
            //store();
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

                //store();
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
    public void printUserPresets() {
        Log.d(TAG, "User presets:");
        printPresets(getUserPresetNameList());
    }
}
