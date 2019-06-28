/*
 *   HiFiToyDevice.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.hifitoy.ApplicationContext;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.CommonCommand;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.Checksummer;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.Biquad;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;

import java.io.Externalizable;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;

public class HiFiToyDevice implements PeripheralData.PeripheralDataDelegate, Serializable {
    private static final String TAG = "HiFiToy";

    private String  mac;
    private String  name;
    private String  activeKeyPreset;
    private int     pairingCode;

    private AudioSource       audioSource;
    private AdvertiseMode     advertiseMode;
    private EnergyConfig      energyConfig;

    private transient boolean clipFlag = false;

    public HiFiToyDevice() {
        setDefault();
    }

    public void setDefault() {
        mac = "demo";
        name = "Demo";
        activeKeyPreset = "DefaultPreset";

        pairingCode = 0;
        audioSource = new AudioSource();
        advertiseMode = new AdvertiseMode();
        energyConfig = new EnergyConfig();

        clipFlag = false;
    }

    //setters/getters
    public String   getMac(){
        return mac;
    }
    public void     setMac(String mac){
        this.mac = mac;
    }
    public String   getName(){
        return name;
    }
    public void     setName(String name){
        this.name = name;
    }
    public int      getPairingCode(){
        return pairingCode;
    }
    public void     setPairingCode(int pairingCode){
        this.pairingCode = pairingCode;
    }
    public short getVersion() {
        return PeripheralData.VERSION;
    }
    public String   getActiveKeyPreset(){
        return activeKeyPreset;
    }
    public boolean  setActiveKeyPreset(String key){
        if (HiFiToyPresetManager.getInstance().getPreset(key) != null) {
            activeKeyPreset = key;
            HiFiToyDeviceManager.getInstance().store();
            return true;
        }
        return false;
    }
    public HiFiToyPreset getActivePreset() {
        return HiFiToyPresetManager.getInstance().getPreset(activeKeyPreset);
    }

    public AudioSource      getAudioSource() {
        return audioSource;
    }
    public void             setAudioSource(AudioSource audioSource) {
        this.audioSource = audioSource;
    }
    public EnergyConfig     getEnergyConfig() {
        return energyConfig;
    }
    public void             setEnergyConfig(EnergyConfig energyConfig) {
        this.energyConfig = energyConfig;
    }
    public AdvertiseMode    getAdvertiseMode() {
        return advertiseMode;
    }
    public void             setAdvertiseMode(AdvertiseMode advertiseMode) {
        this.advertiseMode = advertiseMode;
    }

    public boolean getClipFlag() {
        return clipFlag;
    }
    public void setClipFlag(boolean clipFlag) {
        this.clipFlag = clipFlag;
    }

    public void restoreFactorySettings() {
        pairingCode = 0;
        audioSource = new AudioSource();
        advertiseMode = new AdvertiseMode();
        energyConfig = new EnergyConfig();
        setActiveKeyPreset("DefaultPreset");

        PeripheralData peripheralData = new PeripheralData(this);
        peripheralData.exportWithDialog("Restore factory...");
    }

    public void importPreset() {
        PeripheralData d = new PeripheralData();
        d.setDelegate(this);
        d.importWithDialog("Import Preset...");
    }

    public void description(){
        Log.d(TAG, "mac=" + mac + "|name=" + name + "|pair=" + pairingCode);
    }

    @Override
    public void didImportData(PeripheralData peripheralData) {
        HiFiToyPreset importPreset = new HiFiToyPreset();
        importPreset.setName(Calendar.getInstance().getTime().toString());

        if (importPreset.importFromDataBufs(peripheralData.getDataBufs())) {
            HiFiToyPresetManager.getInstance().setPreset(importPreset);
            setActiveKeyPreset(importPreset.getName());

            Log.d(TAG, "Preset import success.");
        } else {
            Log.d(TAG, "Preset import unsuccess.");
        }

    }
}

