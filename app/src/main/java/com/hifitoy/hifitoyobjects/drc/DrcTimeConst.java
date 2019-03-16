/*
 *   DrcTimeConst.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.drc;

import android.util.Log;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.tas5558.TAS5558;
import com.hifitoy.xml.XmlData;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DrcTimeConst implements HiFiToyObject, Cloneable {
    private static final String TAG = "HiFiToy";

    private byte    channel;
    private float   energyMS;
    private float   attackMS;
    private float   decayMS;

    public DrcTimeConst(byte channel, float energyMS, float attackMS, float decayMS) {
        setChannel(channel);
        this.energyMS = energyMS;
        this.attackMS = attackMS;
        this.decayMS = decayMS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrcTimeConst that = (DrcTimeConst) o;
        return channel == that.channel &&
                Float.compare(that.energyMS, energyMS) == 0 &&
                Float.compare(that.attackMS, attackMS) == 0 &&
                Float.compare(that.decayMS, decayMS) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, energyMS, attackMS, decayMS);
    }

    @Override
    public DrcTimeConst clone() throws CloneNotSupportedException{
        return (DrcTimeConst) super.clone();
    }

    //setters / getters
    public void setChannel(byte channel) {
        if (channel > DrcChannel.DRC_CH_8) channel = DrcChannel.DRC_CH_8;
        if (channel < DrcChannel.DRC_CH_1_7) channel = DrcChannel.DRC_CH_1_7;

        this.channel = channel;
    }
    public byte getChannel() {
        return channel;
    }

    public void setEnergyMS(float energyMS) {
        if (energyMS < 0.05f) {
            energyMS = 0.05f;
        } else if (energyMS < 0.1f) {
            energyMS = (int)((int)(energyMS / 0.05f) * 0.05f);
        } else if (energyMS < 1.0f) {
            energyMS = (int)((int)(energyMS / 0.1f) * 0.1f);
        } else if (energyMS < 10.0f){
            energyMS = (int)energyMS;
        } else {
            energyMS = (int)((int)(energyMS / 10.0f) * 10.0f);
        }

        this.energyMS = energyMS;
    }

    @Override
    public byte getAddress() {
        if (channel == DrcChannel.DRC_CH_8) {
            return TAS5558.DRC2_ENERGY_REG;
        }
        return TAS5558.DRC1_ENERGY_REG;
    }

    @Override
    public String getInfo() {
        return String.format(Locale.getDefault(),
                "Energy=%.1f Attack=%.1f Decay=%.1f", energyMS, attackMS, decayMS);
    }

    public String getEnergyDescription() {
        if (energyMS < 0.1f) {
            return String.format(Locale.getDefault(), "%dus", (int)(energyMS * 1000));
        } else if (energyMS < 1.0f) {
            return String.format(Locale.getDefault(), "%.1fms", energyMS);
        }
        return String.format(Locale.getDefault(), "%dms", (int)energyMS);
    }

    public void sendEnergyToPeripheral(boolean response) {
        HiFiToyControl.getInstance().sendDataToDsp(getEnergyDataBuf().getBinary(), response);
    }

    public void sendAttackDecayToPeripheral(boolean response) {
        HiFiToyControl.getInstance().sendDataToDsp(getAttackDecayDataBuf().getBinary(), response);
    }

    @Override
    public void sendToPeripheral(boolean response) {
        sendEnergyToPeripheral(response);
        sendAttackDecayToPeripheral(response);
    }

    private int timeToInt(float time_ms) {
        return (int)(Math.pow(Math.E, -2000.0f / time_ms / TAS5558.TAS5558_FS) * 0x800000) & 0x007FFFFF;
    }

    private HiFiToyDataBuf getEnergyDataBuf() {
        ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        b.putInt(0x800000 - timeToInt(energyMS));
        b.putInt(timeToInt(energyMS));

        return new HiFiToyDataBuf(getAddress(), b);
    }

    private HiFiToyDataBuf getAttackDecayDataBuf() {
        ByteBuffer b = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        b.putInt(0x800000 - timeToInt(attackMS));
        b.putInt(timeToInt(attackMS));
        b.putInt(0x800000 - timeToInt(decayMS));
        b.putInt(timeToInt(decayMS));

        return new HiFiToyDataBuf((byte)(getAddress() + 4), b);
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        return new ArrayList<>(Arrays.asList(getEnergyDataBuf(), getAttackDecayDataBuf()));
    }

    @Override
    public boolean importData(byte[] data) {
        return false;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();
        xmlData.addXmlElement("Energy", energyMS);
        xmlData.addXmlElement("Attack", attackMS);
        xmlData.addXmlElement("Decay", decayMS);

        XmlData drcTimeConstXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Channel", Integer.toString(channel));

        drcTimeConstXmlData.addXmlElement("DrcTimeConst", xmlData, attrib);
        return drcTimeConstXmlData;
    }

    @Override
    public boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String elementName = null;
        int count = 0;

        do {
            xmlParser.next();

            if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                elementName = xmlParser.getName();
            }
            if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                if (xmlParser.getName().equals("DrcTimeConst")) break;

                elementName = null;
            }

            if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){
                String elementValue = xmlParser.getText();
                if (elementValue == null) continue;

                if (elementName.equals("Energy")){
                    energyMS = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("Attack")){
                    attackMS = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("Decay")){
                    decayMS = Float.parseFloat(elementValue);
                    count++;
                }
            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        //check import result
        if (count != 3){
            Log.d(TAG, "DrcTimeConst=" + Integer.toString(channel) +
                    ". Import from xml is not success.");
            return false;
        }
        Log.d(TAG, getInfo());

        return true;
    }
}
