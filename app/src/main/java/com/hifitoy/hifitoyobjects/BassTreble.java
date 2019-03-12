/*
 *   BassTreble.java
 *
 *   Created by Artem Khlyupin on 11/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import android.util.Log;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.tas5558.TAS5558;
import com.hifitoy.xml.XmlData;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.hifitoy.hifitoyobjects.BassTreble.BassTrebleChannel.BassFreq.BASS_FREQ_125;
import static com.hifitoy.hifitoyobjects.BassTreble.BassTrebleChannel.BassTrebleCh.BASS_TREBLE_CH_127;
import static com.hifitoy.hifitoyobjects.BassTreble.BassTrebleChannel.TrebleFreq.TREBLE_FREQ_11000;

public class BassTreble implements HiFiToyObject, Cloneable {
    private static final String TAG = "HiFiToy";

    private float[] enabledCh = new float[8]; // 0.0 .. 1.0, 8 channels

    private BassTrebleChannel bassTreble127;
    private BassTrebleChannel bassTreble34;
    private BassTrebleChannel bassTreble56;
    private BassTrebleChannel bassTreble8;

    public BassTreble(BassTrebleChannel bassTreble127, BassTrebleChannel bassTreble34,
                      BassTrebleChannel bassTreble56, BassTrebleChannel bassTreble8) {
        for (byte i = 0; i < 8; i++) {
            setEnabledChannel(i, 0.0f);
        }
        this.bassTreble127 = bassTreble127;
        this.bassTreble34 = bassTreble34;
        this.bassTreble56 = bassTreble56;
        this.bassTreble8 = bassTreble8;
    }
    public BassTreble(BassTrebleChannel bassTreble127) {
        this(bassTreble127, null, null, null);
    }
    public BassTreble() {
        this(null, null, null, null);
        bassTreble127 = new BassTrebleChannel(BASS_TREBLE_CH_127, BASS_FREQ_125, (byte)0, TREBLE_FREQ_11000, (byte)0);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BassTreble that = (BassTreble) o;
        return Arrays.equals(enabledCh, that.enabledCh) &&
                Objects.equals(bassTreble127, that.bassTreble127) &&
                Objects.equals(bassTreble34, that.bassTreble34) &&
                Objects.equals(bassTreble56, that.bassTreble56) &&
                Objects.equals(bassTreble8, that.bassTreble8);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(bassTreble127, bassTreble34, bassTreble56, bassTreble8);
        result = 31 * result + Arrays.hashCode(enabledCh);
        return result;
    }

    @Override
    public BassTreble clone() throws CloneNotSupportedException{
        BassTreble bt = (BassTreble) super.clone();

        bt.enabledCh = new float[8];
        for (int i = 0; i < 8; i++) {
            bt.enabledCh[i] = enabledCh[i];
        }

        bt.bassTreble127 = null;
        bt.bassTreble34 = null;
        bt.bassTreble56 = null;
        bt.bassTreble8 = null;

        if (bassTreble127 != null) bt.bassTreble127 = bassTreble127.clone();
        if (bassTreble34 != null) bt.bassTreble34 = bassTreble34.clone();
        if (bassTreble56 != null) bt.bassTreble56 = bassTreble56.clone();
        if (bassTreble8 != null) bt.bassTreble8 = bassTreble8.clone();

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
        HiFiToyControl.getInstance().sendDataToDsp(getFreqDbBinary(), response);
    }

    public void sendEnabledToPeripheral(byte channel, boolean response) {
        HiFiToyControl.getInstance().sendDataToDsp(getEnabledBinary(channel), response);
    }

    private byte[] getFreqDbBinary() {
        ByteBuffer b = ByteBuffer.allocate(16);
        //bass
        b.put(bassTreble8.bassFreq);
        b.put(bassTreble56.bassFreq);
        b.put(bassTreble34.bassFreq);
        b.put(bassTreble127.bassFreq);
        b.put(dbToTAS5558Format(bassTreble8.bassDb));
        b.put(dbToTAS5558Format(bassTreble56.bassDb));
        b.put(dbToTAS5558Format(bassTreble34.bassDb));
        b.put(dbToTAS5558Format(bassTreble127.bassDb));
        //treble
        b.put(bassTreble8.trebleFreq);
        b.put(bassTreble56.trebleFreq);
        b.put(bassTreble34.trebleFreq);
        b.put(bassTreble127.trebleFreq);
        b.put(dbToTAS5558Format(bassTreble8.trebleDb));
        b.put(dbToTAS5558Format(bassTreble56.trebleDb));
        b.put(dbToTAS5558Format(bassTreble34.trebleDb));
        b.put(dbToTAS5558Format(bassTreble127.trebleDb));

        HiFiToyDataBuf dataBuf = new HiFiToyDataBuf(getAddress(), b);
        return dataBuf.getBinary().array();
    }

    private byte[] getEnabledBinary(byte channel) {
        if (channel > 7) channel = 7;

        int val = (int)(0x800000 * enabledCh[channel]);
        int ival = (int)(0x800000 - 0x800000 * enabledCh[channel]);
        ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        b.putInt(val);
        b.putInt(ival);

        HiFiToyDataBuf dataBuf = new HiFiToyDataBuf(TAS5558.BASS_TREBLE_REG, b);
        return dataBuf.getBinary().array();
    }

    @Override
    public byte[] getBinary() {
        byte[] data = new byte[0];

        for (byte i = 0; i < 8; i++) {
            data = BinaryOperation.concatData(data, getEnabledBinary(i));
        }
        data = BinaryOperation.concatData(data, getFreqDbBinary());

        return data;
    }

    @Override
    public boolean importData(byte[] data) {
        return false;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();

        for (int i = 0; i < 8; i++){
            xmlData.addXmlElement(String.format(Locale.getDefault(), "enabledCh%d", i), enabledCh[i]);
        }

        if (bassTreble127 != null)  xmlData.addXmlData(bassTreble127.toXmlData());
        if (bassTreble34 != null)   xmlData.addXmlData(bassTreble34.toXmlData());
        if (bassTreble56 != null)   xmlData.addXmlData(bassTreble56.toXmlData());
        if (bassTreble8 != null)    xmlData.addXmlData(bassTreble8.toXmlData());

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

                if (elementName.equals("Channel")){
                    String channelStr = xmlParser.getAttributeValue("Channel", null);
                    if (channelStr == null) continue;
                    byte channel = Byte.parseByte(channelStr);

                    if ((bassTreble127 != null) && (bassTreble127.channel == channel)){
                        bassTreble127.importFromXml(xmlParser);
                        count++;
                    }
                    if ((bassTreble34 != null) && (bassTreble34.channel == channel)){
                        bassTreble34.importFromXml(xmlParser);
                        count++;
                    }
                    if ((bassTreble56 != null) && (bassTreble56.channel == channel)){
                        bassTreble56.importFromXml(xmlParser);
                        count++;
                    }
                    if ((bassTreble8 != null) && (bassTreble8.channel == channel)){
                        bassTreble8.importFromXml(xmlParser);
                        count++;
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
        int cmp_count = 8;
        if (bassTreble127 != null)  cmp_count++;
        if (bassTreble34 != null)   cmp_count++;
        if (bassTreble56 != null)   cmp_count++;
        if (bassTreble8 != null)    cmp_count++;

        if (count != cmp_count){
            Log.d(TAG, "BassTreble. Import from xml is not success.");
            return false;
        }
        Log.d(TAG, getInfo());

        return true;
    }

    public class BassTrebleChannel implements Cloneable{
        private final static byte HW_BASSTREBLE_MAX_DB = 18;
        private final static byte HW_BASSTREBLE_MIN_DB = -18;

        private byte    channel;

        private byte    bassFreq;
        private byte    bassDb;
        private byte    trebleFreq;
        private byte    trebleDb;

        private byte    maxBassDb;
        private byte    minBassDb;
        private byte    maxTrebleDb;
        private byte    minTrebleDb;


        public BassTrebleChannel(byte channel, byte bassFreq, byte bassDb, byte trebleFreq, byte trebleDb,
                                 byte maxBassDb, byte minBassDb, byte maxTrebleDb, byte minTrebleDb) {
            setChannel(channel);
            setBassFreq(bassFreq);
            setTrebleFreq(trebleFreq);

            if (maxBassDb > HW_BASSTREBLE_MAX_DB)      maxBassDb = HW_BASSTREBLE_MAX_DB;
            if (maxTrebleDb > HW_BASSTREBLE_MAX_DB)    maxTrebleDb = HW_BASSTREBLE_MAX_DB;
            if (minBassDb < HW_BASSTREBLE_MIN_DB)      minBassDb = HW_BASSTREBLE_MIN_DB;
            if (minTrebleDb < HW_BASSTREBLE_MIN_DB)    minTrebleDb = HW_BASSTREBLE_MIN_DB;
            this.maxBassDb = maxBassDb;
            this.minBassDb = minBassDb;
            this.maxTrebleDb = maxTrebleDb;
            this.minTrebleDb = minTrebleDb;

            setBassDb(bassDb);
            setTrebleDb(trebleDb);
        }
        public BassTrebleChannel(byte channel, byte bassFreq, byte bassDb, byte trebleFreq, byte trebleDb) {
            this(channel, bassFreq, bassDb, trebleFreq, trebleDb,
                    HW_BASSTREBLE_MAX_DB, HW_BASSTREBLE_MIN_DB, HW_BASSTREBLE_MAX_DB, HW_BASSTREBLE_MIN_DB);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BassTrebleChannel that = (BassTrebleChannel) o;
            return channel == that.channel &&
                    bassFreq == that.bassFreq &&
                    bassDb == that.bassDb &&
                    trebleFreq == that.trebleFreq &&
                    trebleDb == that.trebleDb &&
                    maxBassDb == that.maxBassDb &&
                    minBassDb == that.minBassDb &&
                    maxTrebleDb == that.maxTrebleDb &&
                    minTrebleDb == that.minTrebleDb;
        }

        @Override
        public int hashCode() {
            return Objects.hash(channel, bassFreq, bassDb, trebleFreq, trebleDb, maxBassDb, minBassDb, maxTrebleDb, minTrebleDb);
        }

        @Override
        public BassTrebleChannel clone() throws CloneNotSupportedException{
            return (BassTrebleChannel) super.clone();
        }

        //setters/getters
        private void setChannel(byte channel) {
            if (channel > BassTrebleCh.BASS_TREBLE_CH_8) channel = BassTrebleCh.BASS_TREBLE_CH_8;
            if (channel < BassTrebleCh.BASS_TREBLE_CH_127) channel = BassTrebleCh.BASS_TREBLE_CH_127;
            this.channel = channel;
        }

        private void setBassFreq(byte bassFreq) {
            if (bassFreq > BassFreq.BASS_FREQ_500) bassFreq = BassFreq.BASS_FREQ_500;
            if (bassFreq < BassFreq.BASS_FREQ_125) bassFreq = BassFreq.BASS_FREQ_125;
            this.bassFreq = bassFreq;
        }
        private void setTrebleFreq(byte trebleFreq) {
            if (trebleFreq > TrebleFreq.TREBLE_FREQ_13000) trebleFreq = TrebleFreq.TREBLE_FREQ_13000;
            if (trebleFreq < TrebleFreq.TREBLE_FREQ_2750) trebleFreq = TrebleFreq.TREBLE_FREQ_2750;
            this.trebleFreq = trebleFreq;
        }

        public void setBassDb(byte db) {
            if (db < minBassDb) db = minBassDb;
            if (db > maxBassDb) db = maxBassDb;
            bassDb = db;
        }
        public byte getBassDb() {
            return bassDb;
        }
        public void setTrebleDb(byte db) {
            if (db < minTrebleDb) db = minTrebleDb;
            if (db > maxTrebleDb) db = maxTrebleDb;
            trebleDb = db;
        }
        public byte getTrebleDb() {
            return trebleDb;
        }

        public void setBassDbPercent(float percent) {
            if (percent > 1.0f) percent = 1.0f;
            if (percent < 0.0f) percent = 0.0f;
            setBassDb((byte)(percent * (maxBassDb - minBassDb) + minBassDb));
        }
        public float getBassDbPercent() {
            return (float)(bassDb - minBassDb) / (maxBassDb - minBassDb);
        }
        public void setTrebleDbPercent(float percent) {
            if (percent > 1.0f) percent = 1.0f;
            if (percent < 0.0f) percent = 0.0f;

            setTrebleDb((byte)(percent * (maxTrebleDb - minTrebleDb) + minTrebleDb));
        }
        public float getTrebleDbPercent() {
            return (float)(trebleDb - minTrebleDb) / (maxTrebleDb - minTrebleDb);
        }

        public XmlData toXmlData() {
            XmlData xmlData = new XmlData();

            xmlData.addXmlElement("BassFreq", bassFreq);
            xmlData.addXmlElement("BassDb", bassDb);
            xmlData.addXmlElement("TrebleFreq", trebleFreq);
            xmlData.addXmlElement("TrebleDb", trebleDb);

            xmlData.addXmlElement("maxBassDb", maxBassDb);
            xmlData.addXmlElement("minBassDb", minBassDb);
            xmlData.addXmlElement("maxTrebleDb", maxTrebleDb);
            xmlData.addXmlElement("minTrebleDb", minTrebleDb);

            XmlData bassTrebleXmlData = new XmlData();
            Map<String, String> attrib = new HashMap<>();
            attrib.put("Channel", Integer.toString(channel));

            bassTrebleXmlData.addXmlElement("BassTrebleChannel", xmlData, attrib);
            return bassTrebleXmlData;
        }
        public boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
            String elementName = null;
            int count = 0;

            do {
                xmlParser.next();

                if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                    elementName = xmlParser.getName();
                }
                if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                    if (xmlParser.getName().equals("BassTrebleChannel")) break;

                    elementName = null;
                }

                if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){
                    String elementValue = xmlParser.getText();
                    if (elementValue == null) continue;

                    if (elementName.equals("BassFreq")){
                        bassFreq = Byte.parseByte(elementValue);
                        count++;
                    }
                    if (elementName.equals("BassDb")){
                        bassDb = Byte.parseByte(elementValue);
                        count++;
                    }
                    if (elementName.equals("TrebleFreq")){
                        trebleFreq = Byte.parseByte(elementValue);
                        count++;
                    }
                    if (elementName.equals("TrebleDb")){
                        trebleDb = Byte.parseByte(elementValue);
                        count++;
                    }
                    if (elementName.equals("maxBassDb")){
                        maxBassDb = Byte.parseByte(elementValue);
                        count++;
                    }
                    if (elementName.equals("minBassDb")){
                        minBassDb = Byte.parseByte(elementValue);
                        count++;
                    }
                    if (elementName.equals("maxTrebleDb")){
                        maxTrebleDb = Byte.parseByte(elementValue);
                        count++;
                    }
                    if (elementName.equals("minTrebleDb")){
                        minTrebleDb = Byte.parseByte(elementValue);
                        count++;
                    }
                }
            } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

            //check import result
            if (count != 8){
                Log.d(TAG, "BassTrebleChannel=" + Integer.toString(channel) +
                        ". Import from xml is not success.");
                return false;
            }
            //Log.d(TAG, getInfo());

            return true;
        }

        public class BassFreq {
            //freq for FS = 96kHz
            public final static byte BASS_FREQ_125 = 1;
            public final static byte BASS_FREQ_250 = 2;
            public final static byte BASS_FREQ_375 = 3;
            public final static byte BASS_FREQ_438 = 4;
            public final static byte BASS_FREQ_500 = 5;
        }
        public class TrebleFreq {
            //freq for FS = 96kHz
            public final static byte TREBLE_FREQ_2750   = 1;
            public final static byte TREBLE_FREQ_5500   = 2;
            public final static byte TREBLE_FREQ_9000   = 3;
            public final static byte TREBLE_FREQ_11000  = 4;
            public final static byte TREBLE_FREQ_13000  = 5;
        }
        public class BassTrebleCh {
            public final static byte BASS_TREBLE_CH_127 = 0;
            public final static byte BASS_TREBLE_CH_34  = 1;
            public final static byte BASS_TREBLE_CH_56  = 2;
            public final static byte BASS_TREBLE_CH_8   = 3;
        }

    }
}
