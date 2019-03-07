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
import com.hifitoy.hifitoycontrol.CommonCommand;

public class HiFiToyDevice implements StoreInterface {
    private static final String TAG = "HiFiToy";

    private String  mac;
    private String  name;
    private String  activeKeyPreset;

    //peripheral structure                                  // offset
    private final byte      i2cAddr = 0x34;                 // 0x00
    private byte            successWriteFlag;               // 0x01
    public final short      version = 11;                   // 0x02
    private int             pairingCode;                    // 0x04
    private AudioSource     audioSource;                    // 0x08
    private EnergyConfig    energyConfig;
    private AdvertiseMode   advertiseMode;

    /*PCM9211Source_t     audioSource;        // 0x08
    uint8_t             reserved[3];        // 0x09
    EnergyConfig_t      energy;             // 0x0C
    BiquadType_t        biquadTypes[7];     // 0x18
    uint8_t             reserved1;          //

    uint16_t            dataBufLength;      // 0x20
    uint16_t            dataBytesLength;    // 0x22
    DataBufHeader_t     firstDataBuf;       // 0x24*/

    public HiFiToyDevice() {
        setDefault();
    }

    public void setDefault() {
        mac = "demo";
        name = "Demo";
        pairingCode = 0;
        activeKeyPreset = "DefaultPreset";

        audioSource = new AudioSource();
        energyConfig = new EnergyConfig();
        advertiseMode = new AdvertiseMode();
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
    public String   getActiveKeyPreset(){
        return activeKeyPreset;
    }
    public boolean  setActiveKeyPreset(String key){
        //if preset with key exist
        /*if (DspPresetManager.getInstance().getDspPreset(key) != null){
            activeKeyPreset = key;
            return true;
        }*/
        return false;
    }
    public HiFiToyPreset getActivePreset() {
        return new HiFiToyPreset();
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

    public void restoreFactorySettings() {

    }

    @Override
    public boolean restore(String filename, String key) {
        setDefault();

        Context context = ApplicationContext.getInstance().getContext();
        SharedPreferences pref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        if (pref == null){
            Log.d(TAG, "sharedPref == null.");
            return false;
        }

        mac = pref.getString(key + "_mac", "");
        name = pref.getString(key + "_name", "");
        pairingCode = pref.getInt(key + "_value", -1);
        activeKeyPreset = pref.getString(key + "_activeKeyPreset", "");

        if ((mac.equals("")) || (name.equals("")) || (pairingCode == -1) || (activeKeyPreset.equals(""))){
            return false;
        }
        return true;
    }

    @Override
    public void store(String filename, String key) {
        Context context = ApplicationContext.getInstance().getContext();
        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (editor == null){
            Log.d(TAG, "sharedPref editor == null.");
            return;
        }

        editor.putString(key + "_mac", mac);
        editor.putString(key + "_name", name);
        editor.putInt(key + "_value", pairingCode);
        editor.putString(key + "_activeKeyPreset", activeKeyPreset);

        editor.commit();
    }

    public void description(){
        Log.d(TAG, "mac=" + mac + "|name=" + name + "|pair=" + pairingCode);
    }
}

