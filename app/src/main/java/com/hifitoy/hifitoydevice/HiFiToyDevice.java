/*
 *   HiFiToyDevice.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.util.Log;
import com.hifitoy.hifitoyobjects.AMMode;
import com.hifitoy.hifitoyobjects.PostProcess;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

public class HiFiToyDevice implements Serializable {
    private static final String TAG = "HiFiToy";

    private String                  mac;
    private String                  name;
    private String                  activeKeyPreset;
    private transient ToyPreset activePreset = null;
    private int                     pairingCode;

    private AudioSource     audioSource;
    private AdvertiseMode   advertiseMode;
    private EnergyConfig    energyConfig;
    private OutputMode      outputMode;
    private AMMode          amMode;

    private transient boolean newPDV21Hw = false;
    private transient boolean clipFlag = false;


    public HiFiToyDevice() {
        setDefault();
    }

    public void setDefault() {
        mac = "demo";
        name = "Demo";
        activeKeyPreset = "No processing";

        pairingCode = 0;
        audioSource = new AudioSource();
        advertiseMode = new AdvertiseMode();
        energyConfig = new EnergyConfig();
        outputMode = new OutputMode();
        amMode = new AMMode();

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
    public short    getVersion() {
        return PeripheralData.VERSION;
    }
    public String   getActiveKeyPreset(){
        return activeKeyPreset;
    }
    public boolean  setActiveKeyPreset(String key){
        try {
            ToyPreset p = HiFiToyPresetManager.getInstance().getPreset(key);

            activeKeyPreset = key;
            activePreset = p;
            HiFiToyDeviceManager.getInstance().store();
            return true;

        } catch (IOException | XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }
        return false;
    }
    public void forceSetActiveKeyPreset(String key){
        activeKeyPreset = key;
        HiFiToyDeviceManager.getInstance().store();
    }
    public ToyPreset getActivePreset() {
        if (activePreset == null) {
            try {
                activePreset = HiFiToyPresetManager.getInstance().getPreset(activeKeyPreset);

            } catch (IOException | XmlPullParserException e) {
                setActiveKeyPreset("No processing");
            }
        }
        return activePreset;
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
    public OutputMode       getOutputMode() {
        return outputMode;
    }
    public AMMode           getAmMode() { return amMode; }

    public boolean isNewPDV21Hw() {
        return newPDV21Hw;
    }
    public void setNewPDV21Hw(boolean newPDV21Hw) {
        this.newPDV21Hw = newPDV21Hw;
    }

    public boolean getClipFlag() {
        return clipFlag;
    }
    public void setClipFlag(boolean clipFlag) {
        this.clipFlag = clipFlag;
    }

    public void restoreFactorySettings(final PostProcess postProcess) {
        pairingCode = 0;
        audioSource = new AudioSource();
        advertiseMode = new AdvertiseMode();
        energyConfig = new EnergyConfig();
        outputMode = new OutputMode();
        amMode = new AMMode();
        setActiveKeyPreset("No processing");

        PeripheralData peripheralData = new PeripheralData(this);
        peripheralData.exportWithDialog("Restore factory...", postProcess);
    }

    public void importPreset(final PostProcess postProcess) {
        final PeripheralData pd = new PeripheralData();

        pd.importWithDialog("Import Preset...", new PostProcess() {

            @Override
            public void onPostProcess() {
                try {
                    ToyPreset importPreset = new ToyPreset(
                            Calendar.getInstance().getTime().toString(),
                            pd.getDataBufs(),
                            pd.getBiquadTypes());

                    importPreset.save(true);
                    setActiveKeyPreset(importPreset.getName());

                    Log.d(TAG, "Preset import success.");

                    if (postProcess != null) {
                        postProcess.onPostProcess();
                    }

                } catch (IOException e) {
                    Log.d(TAG, "Preset import unsuccess.");
                    Log.d(TAG, e.toString());
                }
            }
        });
    }

    public void description(){
        Log.d(TAG, "mac=" + mac + "|name=" + name + "|pair=" + pairingCode);
    }
}

