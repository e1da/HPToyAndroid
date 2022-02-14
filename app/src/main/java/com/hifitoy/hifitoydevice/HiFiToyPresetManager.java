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

    public static synchronized HiFiToyPresetManager getInstance(){
        if (instance == null){
            instance = new HiFiToyPresetManager();
        }
        return instance;
    }

    private HiFiToyPresetManager(){
        printOfficialPresets();
        printUserPresets();
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

    public File getUserPresetFile(String presetName) {
        File dir = getUserDir();
        if ((dir == null) || (presetName == null)) {
            return null;
        }

        File f = new File(dir, presetName + ".tpr");
        if (f.exists()) {
            return f;
        }

        return null;
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
        try {
            return getOfficialPreset(presetName);

        } catch (IOException e) {

            if (e.getMessage().equals("Official preset not found.")) {
                return getUserPreset(presetName);
            }
        }

        throw new IOException("Preset not found.");
    }

    public int getPresetIndex(String name) {
        List<String> presetNameList = getPresetNameList();
        for (int i = 0; i < presetNameList.size(); i++) {
            if (presetNameList.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public HiFiToyPreset getPreset(int position) throws IOException, XmlPullParserException {
        String presetName = getPresetNameList().get(position);
        return getPreset(presetName);
    }

    public boolean isPresetExist(String name) {
        for (String presetName : getPresetNameList()) {
            if (presetName.equals(name)) {
                return true;
            }
        }
        return false;
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

    public boolean deletePreset(String presetName) {
        File f = getUserPresetFile(presetName);
        if ( (f != null) && (f.delete()) ) {
            return true;
        }

        Log.d(TAG, "Delete preset is unsuccessful");
        return false;
    }

    public boolean renamePreset(String oldName, String newName) {
        //check if preset with newName exist
        for (String presetName : getPresetNameList()) {
            if (presetName.equals(newName)) {
                return false;
            }
        }

        try {
            HiFiToyPreset p = getPreset(oldName);
            p.setName(newName);
            p.save(true);

            deletePreset(oldName);
            return true;

        } catch (IOException | XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }

        return false;
    }

    public void importPreset(Uri uri) {
        if (uri == null) {
            DialogSystem.getInstance().showDialog("Error", "Uri is null.", "Ok");
            return;
        }

        try {
            HiFiToyPreset importPreset = new HiFiToyPreset(uri);
            importPreset.save(false);

            DialogSystem.getInstance().showDialog("Completed",
                    "Preset '" + importPreset.getName() + "' imported successfully.", "Ok");

        } catch (IOException | XmlPullParserException e) {
            DialogSystem.getInstance().showDialog("Error", e.getMessage(), "Ok");
        }
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
