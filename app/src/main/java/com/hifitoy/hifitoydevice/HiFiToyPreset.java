/*
 *   HiFiToyPreset.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Xml;

import com.hifitoy.ApplicationContext;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.Checksummer;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.basstreble.BassTreble;
import com.hifitoy.hifitoyobjects.drc.Drc;
import com.hifitoy.hifitoyobjects.filter.DFilter;
import com.hifitoy.hifitoyobjects.filter.Filter;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.hifitoyobjects.Loudness;
import com.hifitoy.hifitoyobjects.Volume;
import com.hifitoy.hifitoyobjects.drc.DrcCoef;
import com.hifitoy.hifitoyobjects.drc.DrcTimeConst;
import com.hifitoy.tas5558.TAS5558;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.hifitoy.hifitoyobjects.drc.Drc.DrcEvaluation.POST_VOLUME_EVAL;
import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_1_7;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT0_INPUT_DB;
import static com.hifitoy.hifitoyobjects.drc.DrcCoef.POINT3_INPUT_DB;

public class HiFiToyPreset implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private String  name;
    private short   checkSum;

    public DFilter      dFilter;
    public Volume       masterVolume;
    public BassTreble   bassTreble;
    public Loudness     loudness;
    public Drc          drc;

    public HiFiToyPreset() {
        setDefault();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HiFiToyPreset that = (HiFiToyPreset) o;

        boolean b0 = true;//checkSum == that.checkSum;//!!!Warning

        return b0 &&
                Objects.equals(dFilter, that.dFilter) &&
                Objects.equals(masterVolume, that.masterVolume) &&
                Objects.equals(bassTreble, that.bassTreble) &&
                Objects.equals(loudness, that.loudness) &&
                Objects.equals(drc, that.drc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkSum, dFilter, masterVolume, bassTreble, loudness, drc);
    }

    @Override
    public HiFiToyPreset clone() throws CloneNotSupportedException{
        HiFiToyPreset preset = (HiFiToyPreset) super.clone();

        preset.dFilter = dFilter.clone();
        preset.masterVolume = masterVolume.clone();
        preset.bassTreble = bassTreble.clone();
        preset.loudness = loudness.clone();
        preset.drc = drc.clone();

        return preset;
    }


    public List<HiFiToyObject> getToyObjects() {
        List<HiFiToyObject> o = new ArrayList<>();
        o.add(dFilter);
        o.add(masterVolume);
        o.add(bassTreble);
        o.add(loudness);
        o.add(drc);

        return o;
    }

    public void setDefault() {
        name = "No processing";

        //Filters
        dFilter = new DFilter();

        //MasterVolume
        masterVolume = new Volume(TAS5558.MASTER_VOLUME_REG, 0.0f, 0.0f, Volume.HW_MUTE_DB);

        //Bass Treble
        bassTreble = new BassTreble();
        bassTreble.setEnabledChannel((byte)0, 1.0f);
        bassTreble.setEnabledChannel((byte)1, 1.0f);

        //Loudness
        loudness = new Loudness();

        //Drc
        DrcCoef drcCoef17 = new DrcCoef(DRC_CH_1_7,
                                        new DrcCoef.DrcPoint(POINT0_INPUT_DB, -120.0f),
                                        new DrcCoef.DrcPoint(-72.0f, -72.0f),
                                        new DrcCoef.DrcPoint(-24.0f, -24.0f),
                                        new DrcCoef.DrcPoint(POINT3_INPUT_DB, -24.0f));
        DrcTimeConst drcTimeConst17 = new DrcTimeConst(DRC_CH_1_7, 0.1f, 10.0f, 100.0f);

        drc = new Drc(drcCoef17, drcTimeConst17);
        drc.setEvaluation(POST_VOLUME_EVAL, (byte)0);
        drc.setEvaluation(POST_VOLUME_EVAL, (byte)1);

        updateChecksum();
    }

    //setters / getters
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public short getChecksum() {
        return checkSum;
    }

    public DFilter getDFilter() {
        return dFilter;
    }

    public Filter getActiveFilter() {
        return dFilter.getActiveFilter();
    }
    public void setFilters(Filter f) {
        dFilter.setFilterCh0(f);
    } // TODO:update for stereo filter

    public Volume getVolume() {
        return masterVolume;
    }
    public BassTreble getBassTreble() {
        return bassTreble;
    }
    public Loudness getLoudness() {
        return loudness;
    }
    public Drc getDrc() {
        return drc;
    }

    public void updateChecksum() {
        updateChecksum(getDataBufs());
    }

    private void updateChecksum(List<HiFiToyDataBuf> dataBufs) {
        checkSum = Checksummer.calc(BinaryOperation.getBinary(dataBufs));
        Log.d(TAG, String.format(Locale.getDefault(), "Update checksum = 0x%x", checkSum));
    }

    public void storeToPeripheral() {
        PeripheralData peripheralData = new PeripheralData();
        peripheralData.setBiquadTypes(getActiveFilter().getBiquadTypes());
        peripheralData.setDataBufs(getDataBufs());
        peripheralData.exportPresetWithDialog("Sending Preset...");
    }

    @Override
    public byte getAddress() {
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void sendToPeripheral(boolean response) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        List<HiFiToyObject> toyObjects = getToyObjects();

        for (int i = 0; i < toyObjects.size(); i++){
            toyObjects.get(i).sendToPeripheral(true);
        }
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();
        List<HiFiToyObject> toyObjects = getToyObjects();

        for (int i = 0; i < toyObjects.size(); i++){
            l.addAll(toyObjects.get(i).getDataBufs());
        }

        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        List<HiFiToyObject> toyObjects = getToyObjects();

        for (int i = 0; i < toyObjects.size(); i++){
            if (!toyObjects.get(i).importFromDataBufs(dataBufs)) {
                return false;
            }
        }

        updateChecksum(dataBufs);
        return true;
    }

    private String getPresetName(String filename) {
        if (filename == null) return null;

        int end = filename.indexOf(".tpr");
        if (end != -1) {
            return filename.substring(0, end);
        }

        return filename;
    }

    private String getPresetName(Uri uri) {
        if (uri == null) return null;

        if ( (uri.getScheme() != null) && (uri.getScheme().equals("content")) ) {
            Context c = ApplicationContext.getInstance().getContext();
            Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    return getPresetName(filename);
                }
                cursor.close();
            }

        }

        if (uri.getLastPathSegment() != null) {
            return getPresetName(uri.getLastPathSegment());
        }

        return null;
    }

    private String checkPresetName(String name) throws IOException{
        if (name == null) throw new IOException("Preset name is not correct");

        if (HiFiToyControl.getInstance().getActiveDevice().getActiveKeyPreset().equals(name)) {
            String msg = String.format("Preset %s is exists and active. Import is not success.", name);
            throw new IOException(msg);
        }

        int index = 1;
        String modifyName = name;

        while (HiFiToyPresetManager.getInstance().isPresetExist(modifyName)) {
            modifyName = name + String.format(Locale.getDefault(),"_%d", index++);
        }

        return modifyName;
    }
}
