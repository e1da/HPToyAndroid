/*
 *   DrcTimeConst.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.drc;

import android.util.Log;

import com.hifitoy.ble.BlePacket;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.tas5558.TAS5558;
import com.hifitoy.xml.XmlData;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DrcTimeConst implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private byte    channel;
    private float   energyMS;
    private float   attackMS;
    private float   decayMS;

    private static final float MAX_ENERGY    = 50.0f;
    private static final float MIN_ENERGY    = 0.05f;
    private static final float MAX_ATTACK    = 200.0f;
    private static final float MIN_ATTACK    = 1.0f;
    private static final float MAX_DECAY     = 10000.0f;
    private static final float MIN_DECAY     = 10.0f;

    public DrcTimeConst(byte channel, float energyMS, float attackMS, float decayMS) {
        setChannel(channel);
        this.energyMS = energyMS;
        this.attackMS = attackMS;
        this.decayMS = decayMS;
    }
    public DrcTimeConst(byte channel) {
        this(channel,0.1f, 10.0f, 100.0f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrcTimeConst that = (DrcTimeConst) o;
        return channel == that.channel &&
                FloatUtility.isFloatDiffLessThan(that.energyMS, energyMS, 0.5f) &&
                FloatUtility.isFloatDiffLessThan(that.attackMS, attackMS, 0.5f) &&
                FloatUtility.isFloatDiffLessThan(that.decayMS, decayMS, 0.5f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
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
        if (energyMS > MAX_ENERGY) energyMS = MAX_ENERGY;
        if (energyMS < MIN_ENERGY) energyMS = MIN_ENERGY;

        if (energyMS < 0.05f) {
            energyMS = 0.05f;
        } else if (energyMS < 0.1f) {
            energyMS = (int)(energyMS / 0.05f) * 0.05f;

        } else if (energyMS < 1.0f) {
            energyMS = (int)(energyMS / 0.1f) * 0.1f;

        } else if (energyMS < 10.0f){
            energyMS = (int)energyMS;

        } else {
            energyMS = (int)(energyMS / 10.0f) * 10.0f;
        }

        this.energyMS = energyMS;
    }
    public float getEnergyMS() {
        return energyMS;
    }

    public void setAttackMS(float attackMS) {
        if (attackMS > MAX_ATTACK) attackMS = MAX_ATTACK;
        if (attackMS < MIN_ATTACK) attackMS = MIN_ATTACK;

        this.attackMS = (int)attackMS;
    }
    public float getAttackMS() {
        return attackMS;
    }

    public void setDecayMS(float decayMS) {
        if (decayMS > MAX_DECAY) decayMS = MAX_DECAY;
        if (decayMS < MIN_DECAY) decayMS = MIN_DECAY;

        this.decayMS = (int)(decayMS / 10) * 10;
    }
    public float getDecayMS() {
        return decayMS;
    }

    public float getEnergyPercent() {
        return (float)( (Math.log10(energyMS) - Math.log10(MIN_ENERGY)) /
                        (Math.log10(MAX_ENERGY) - Math.log10(MIN_ENERGY)));
    }
    public void setEnergyPercent(float percent) {
        float e = (float)Math.pow(10, percent * (Math.log10(MAX_ENERGY) - Math.log10(MIN_ENERGY)) + Math.log10(MIN_ENERGY));
        setEnergyMS(e);
    }

    public float getAttackPercent() {
        return (float)( (Math.log10(attackMS) - Math.log10(MIN_ATTACK)) /
                (Math.log10(MAX_ATTACK) - Math.log10(MIN_ATTACK)));
    }
    public void setAttackPercent(float percent) {
        float a = (float)Math.pow(10, percent * (Math.log10(MAX_ATTACK) - Math.log10(MIN_ATTACK)) + Math.log10(MIN_ATTACK));
        setAttackMS(a);
    }

    public float getDecayPercent() {
        return (float)( (Math.log10(decayMS) - Math.log10(MIN_DECAY)) /
                (Math.log10(MAX_DECAY) - Math.log10(MIN_DECAY)));
    }
    public void setDecayPercent(float percent) {
        float d = (float)Math.pow(10, percent * (Math.log10(MAX_DECAY) - Math.log10(MIN_DECAY)) + Math.log10(MIN_DECAY));
        setDecayMS(d);
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
    public String getAttackDescription() {
        return String.format(Locale.getDefault(), "%dms", (int)attackMS);
    }
    public String getDecayDescription() {
        return String.format(Locale.getDefault(), "%dms", (int)decayMS);
    }

    public void sendEnergyToPeripheral(boolean response) {
        BlePacket p = new BlePacket(getEnergyDataBuf().getBinary(), 20, response);
        HiFiToyControl.getInstance().sendDataToDsp(p);
    }

    public void sendAttackDecayToPeripheral(boolean response) {
        BlePacket p = new BlePacket(getAttackDecayDataBuf().getBinary(), 20, response);
        HiFiToyControl.getInstance().sendDataToDsp(p);
    }

    @Override
    public void sendToPeripheral(boolean response) {
        sendEnergyToPeripheral(response);
        sendAttackDecayToPeripheral(response);
    }

    private int timeToInt(float time_ms) {
        return (int)(Math.pow(Math.E, -2000.0f / time_ms / TAS5558.TAS5558_FS) * 0x800000) & 0x007FFFFF;
    }

    private float intToTimeMS(int time) {
        float t = (float)(time & 0x007FFFFF) / 0x800000;

        return (float)(-2000.0f / TAS5558.TAS5558_FS / Math.log(t)); //log == ln
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
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        int importCounter = 0;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);

            if ((buf.getAddr() == getAddress()) && (buf.getLength() == 8)) {
                int energy = buf.getData().order(ByteOrder.BIG_ENDIAN).getInt(4);
                energyMS = intToTimeMS(energy);

                importCounter++;
            }

            if ((buf.getAddr() == getAddress() + 4) && (buf.getLength() == 16)) {
                int attack = buf.getData().order(ByteOrder.BIG_ENDIAN).getInt(4);
                attackMS = intToTimeMS(attack);
                int decay = buf.getData().order(ByteOrder.BIG_ENDIAN).getInt(12);
                decayMS = intToTimeMS(decay);

                importCounter++;
            }

            if (importCounter >= 2) {
                Log.d(TAG, "DrcTimeConst import success.");
                return true;
            }
        }

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
