/*
 *   HiFiToyPreset.java
 *
 *   Created by Artem Khlyupin on 24/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoydevice;

import android.net.Uri;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.basstreble.BassTreble;
import com.hifitoy.hifitoyobjects.drc.Drc;
import com.hifitoy.hifitoyobjects.Filters;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.hifitoyobjects.Loudness;
import com.hifitoy.hifitoyobjects.Volume;
import com.hifitoy.xml.XmlData;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

//TODO: WARNING! It is old preset module. Deprecated.
public class HiFiToyPreset implements HiFiToyObject, Cloneable, Serializable {

    private String  name;
    private short   checkSum;

    // HiFiToy CHARACTERISTICS, pointer to all characteristics
    private List<HiFiToyObject> characteristics;

    public Filters     filters;
    public Volume      masterVolume;
    public BassTreble  bassTreble;
    public Loudness    loudness;
    public Drc         drc;

    public HiFiToyPreset() {
    }

    public HiFiToyPreset(Uri uri) {
        this();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public HiFiToyPreset clone() {
        return null;
    }

    public void updateCharacteristics() {
    }

    public void setDefault() {
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
    public Filters getFilters() {
        return filters;
    }
    public void setFilters(Filters f) {
        filters = f;
    }

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
    }

    public void storeToPeripheral() {
    }

    @Override
    public byte getAddress() {
        return 0;
    }

    @Override
    public String getInfo() {
        return name;
    }

    @Override
    public void sendToPeripheral(boolean response) {
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        return null;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        return true;
    }


    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs, byte[] biquadTypes) {
        return false;
    }

    @Override
    public XmlData toXmlData() {
        return null;
    }

    @Override
    public void importFromXml(XmlPullParser xmlParser) {
    }

    public void importFromXml(InputStream in, String filename) {
    }

    public void importFromXml(Uri uri) {
    }

    public void importFromXml(String xmlData, String presetName) {
    }

}
