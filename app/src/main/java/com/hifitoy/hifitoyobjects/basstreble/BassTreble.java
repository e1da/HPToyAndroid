/*
 *   BassTreble.java
 *
 *   Created by Artem Khlyupin on 11/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.basstreble;

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
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.BassFreq.BASS_FREQ_125;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.BassTrebleCh.BASS_TREBLE_CH_127;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.BassTrebleCh.BASS_TREBLE_CH_34;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.BassTrebleCh.BASS_TREBLE_CH_56;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.BassTrebleCh.BASS_TREBLE_CH_8;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.TrebleFreq.TREBLE_FREQ_11000;
import static com.hifitoy.hifitoyobjects.basstreble.BassTrebleChannel.TrebleFreq.TREBLE_FREQ_9000;
import static com.hifitoy.tas5558.TAS5558.BASS_TREBLE_REG;

public class BassTreble implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private float[] enabledCh = new float[8]; // 0.0 .. 1.0, 8 channels

    private BassTrebleChannel bassTreble127;
    private BassTrebleChannel bassTreble34;
    private BassTrebleChannel bassTreble56;
    private BassTrebleChannel bassTreble8;

    public BassTreble() {
        for (byte i = 0; i < 8; i++) {
            setEnabledChannel(i, 0.0f);
        }

        bassTreble127   = new BassTrebleChannel(BASS_TREBLE_CH_127,
                                                BASS_FREQ_125, (byte)0,
                                                TREBLE_FREQ_9000, (byte)0,
                                                (byte)12, (byte)-12, (byte)12, (byte)-12);
        bassTreble34    = new BassTrebleChannel(BASS_TREBLE_CH_34);
        bassTreble56    = new BassTrebleChannel(BASS_TREBLE_CH_56);
        bassTreble8     = new BassTrebleChannel(BASS_TREBLE_CH_8);
    }

    public BassTreble(BassTrebleChannel bassTreble127, BassTrebleChannel bassTreble34,
                      BassTrebleChannel bassTreble56, BassTrebleChannel bassTreble8) {
        this();

        if (bassTreble127 != null)  this.bassTreble127 = bassTreble127;
        if (bassTreble34 != null)   this.bassTreble34 = bassTreble34;
        if (bassTreble56 != null)   this.bassTreble56 = bassTreble56;
        if (bassTreble8 != null)    this.bassTreble8 = bassTreble8;
    }
    public BassTreble(BassTrebleChannel bassTreble127) {
        this();
        if (bassTreble127 != null) this.bassTreble127 = bassTreble127;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BassTreble that = (BassTreble) o;

        for (int i = 0; i < 8; i++) {
            if (!FloatUtility.isFloatDiffLessThan(enabledCh[i], that.enabledCh[i], 0.01f)) {
                return false;
            }
        }
        return Objects.equals(bassTreble127, that.bassTreble127) &&
                Objects.equals(bassTreble34, that.bassTreble34) &&
                Objects.equals(bassTreble56, that.bassTreble56) &&
                Objects.equals(bassTreble8, that.bassTreble8);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bassTreble127, bassTreble34, bassTreble56, bassTreble8);
    }

    @Override
    public BassTreble clone() throws CloneNotSupportedException{
        BassTreble bt = (BassTreble) super.clone();

        bt.enabledCh = new float[8];
        for (int i = 0; i < 8; i++) {
            bt.enabledCh[i] = enabledCh[i];
        }

        bt.bassTreble127    = bassTreble127.clone();
        bt.bassTreble34     = bassTreble34.clone();
        bt.bassTreble56     = bassTreble56.clone();
        bt.bassTreble8      = bassTreble8.clone();

        return bt;
    }

    //setters / getters
    public BassTrebleChannel getBassTreble127() {
        return bassTreble127;
    }

    public void setEnabledChannel(byte channel, float enabled) { //enabled = 0.0 .. 1.0
        if ((channel < 0) || (channel > 7)) return;

        if (enabled < 0.0f) enabled = 0.0f;
        if (enabled > 1.0f) enabled = 1.0f;

        enabledCh[channel] = enabled;
    }
    public float getEnabledChannel(byte channel) { //return enabled = 0.0 .. 1.0
        if ((channel < 0) || (channel > 7)) return 0.0f;
        return enabledCh[channel];
    }

    //utility
    private byte dbToTAS5558Format(byte db) {
        return (byte)(18 - db);
    }
    private byte TAS5558ToDbFormat(byte tas5558_db) {
        return (byte)(18 - tas5558_db);
    }

    @Override
    public byte getAddress() {
        return TAS5558.BASS_FILTER_SET_REG;
    }

    @Override
    public String getInfo() {
        return "BassTreble";
    }

    @Override
    public void sendToPeripheral(boolean response) {
        BlePacket p = new BlePacket(getFreqDbDataBuf().getBinary(), 20, response);
        HiFiToyControl.getInstance().sendDataToDsp(p);
    }

    public void sendEnabledToPeripheral(byte channel, boolean response) {
        BlePacket p = new BlePacket(getEnabledDataBuf(channel).getBinary(), 20, response);
        HiFiToyControl.getInstance().sendDataToDsp(p);
    }

    private HiFiToyDataBuf getFreqDbDataBuf() {
        ByteBuffer b = ByteBuffer.allocate(16);
        //bass
        b.put(bassTreble8.getBassFreq());
        b.put(bassTreble56.getBassFreq());
        b.put(bassTreble34.getBassFreq());
        b.put(bassTreble127.getBassFreq());
        b.put(dbToTAS5558Format(bassTreble8.getBassDb()));
        b.put(dbToTAS5558Format(bassTreble56.getBassDb()));
        b.put(dbToTAS5558Format(bassTreble34.getBassDb()));
        b.put(dbToTAS5558Format(bassTreble127.getBassDb()));
        //treble
        b.put(bassTreble8.getTrebleFreq());
        b.put(bassTreble56.getTrebleFreq());
        b.put(bassTreble34.getTrebleFreq());
        b.put(bassTreble127.getTrebleFreq());
        b.put(dbToTAS5558Format(bassTreble8.getTrebleDb()));
        b.put(dbToTAS5558Format(bassTreble56.getTrebleDb()));
        b.put(dbToTAS5558Format(bassTreble34.getTrebleDb()));
        b.put(dbToTAS5558Format(bassTreble127.getTrebleDb()));

        return new HiFiToyDataBuf(getAddress(), b);
    }

    private HiFiToyDataBuf getEnabledDataBuf(byte channel) {
        if (channel > 7) channel = 7;

        int val = (int)(0x800000 * enabledCh[channel]);
        int ival = (int)(0x800000 - 0x800000 * enabledCh[channel]);
        ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        b.putInt(ival);
        b.putInt(val);

        return new HiFiToyDataBuf((byte)(BASS_TREBLE_REG + channel), b);
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();

        for (byte i = 0; i < 8; i++) {
            l.add(getEnabledDataBuf(i));
        }
        l.add(getFreqDbDataBuf());

        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        int importCounter = 0;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);

            if ((buf.getAddr() == getAddress()) && (buf.getLength() == 16)) {
                ByteBuffer b = buf.getData();
                b.position(0);

                bassTreble8.setBassFreq(b.get());
                bassTreble56.setBassFreq(b.get());
                bassTreble34.setBassFreq(b.get());
                bassTreble127.setBassFreq(b.get());

                bassTreble8.setBassDb(TAS5558ToDbFormat(b.get()));
                bassTreble56.setBassDb(TAS5558ToDbFormat(b.get()));
                bassTreble34.setBassDb(TAS5558ToDbFormat(b.get()));
                bassTreble127.setBassDb(TAS5558ToDbFormat(b.get()));

                bassTreble8.setTrebleFreq(b.get());
                bassTreble56.setTrebleFreq(b.get());
                bassTreble34.setTrebleFreq(b.get());
                bassTreble127.setTrebleFreq(b.get());

                bassTreble8.setTrebleDb(TAS5558ToDbFormat(b.get()));
                bassTreble56.setTrebleDb(TAS5558ToDbFormat(b.get()));
                bassTreble34.setTrebleDb(TAS5558ToDbFormat(b.get()));
                bassTreble127.setTrebleDb(TAS5558ToDbFormat(b.get()));

                importCounter++;
            }

            if ((buf.getAddr() >= BASS_TREBLE_REG) &&
                    (buf.getAddr() < BASS_TREBLE_REG + 8) && (buf.getLength() == 8)) {

                enabledCh[buf.getAddr() - BASS_TREBLE_REG] = (float)buf.getData().getInt(4) / 0x800000;
                importCounter++;
            }

            if (importCounter >= 9) {
                Log.d(TAG, "BassTreble import success.");
                return true;
            }
        }

        return false;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();

        for (int i = 0; i < 8; i++){
            xmlData.addXmlElement(String.format(Locale.getDefault(), "enabledCh%d", i), enabledCh[i]);
        }

        xmlData.addXmlData(bassTreble127.toXmlData());
        xmlData.addXmlData(bassTreble34.toXmlData());
        xmlData.addXmlData(bassTreble56.toXmlData());
        xmlData.addXmlData(bassTreble8.toXmlData());

        XmlData bassTrebleXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Address", Integer.toString(getAddress()));

        bassTrebleXmlData.addXmlElement("BassTreble", xmlData, attrib);
        return bassTrebleXmlData;
    }

    @Override
    public boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String elementName = null;
        int count = 0;

        do {
            xmlParser.next();

            if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                elementName = xmlParser.getName();

                if (elementName.equals("BassTrebleChannel")){
                    String channelStr = xmlParser.getAttributeValue(null, "Channel");
                    if (channelStr == null) continue;
                    byte channel = Byte.parseByte(channelStr);

                    if (bassTreble127.getChannel() == channel) {
                        if (bassTreble127.importFromXml(xmlParser)) count++;
                    }
                    if (bassTreble34.getChannel() == channel) {
                        if (bassTreble34.importFromXml(xmlParser)) count++;
                    }
                    if (bassTreble56.getChannel() == channel) {
                        if (bassTreble56.importFromXml(xmlParser)) count++;
                    }
                    if (bassTreble8.getChannel() == channel) {
                        if (bassTreble8.importFromXml(xmlParser)) count++;
                    }
                }
            }
            if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                if (xmlParser.getName().equals("BassTreble")) break;

                elementName = null;
            }

            if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){
                String elementValue = xmlParser.getText();
                if (elementValue == null) continue;

                for (int i = 0; i < 8; i++){
                    String keyStr = String.format(Locale.getDefault(), "enabledCh%d", i);
                    if (elementName.equals(keyStr)){
                        enabledCh[i] = Float.parseFloat(elementValue);
                        count++;
                    }
                }
            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        //check import result
        if (count != 12){
            Log.d(TAG, "BassTreble. Import from xml is not success.");
            return false;
        }
        Log.d(TAG, getInfo());

        return true;
    }

}
