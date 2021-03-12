/*
 *   BassTrebleChannel.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.basstreble;

import java.io.Serializable;
import java.util.Objects;

public class BassTrebleChannel implements Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

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
    public BassTrebleChannel(byte channel) {
        this(channel, BassFreq.BASS_FREQ_NONE, (byte)0, TrebleFreq.TREBLE_FREQ_NONE, (byte)0,
                HW_BASSTREBLE_MAX_DB, HW_BASSTREBLE_MIN_DB, HW_BASSTREBLE_MAX_DB, HW_BASSTREBLE_MIN_DB);
    }
    public BassTrebleChannel() {
        this(BassTrebleCh.BASS_TREBLE_CH_127, BassFreq.BASS_FREQ_NONE, (byte)0, TrebleFreq.TREBLE_FREQ_NONE, (byte)0,
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
    public byte getChannel() {
        return channel;
    }

    public void setBassFreq(byte bassFreq) {
        if (bassFreq > BassFreq.BASS_FREQ_500) bassFreq = BassFreq.BASS_FREQ_500;
        if (bassFreq < BassFreq.BASS_FREQ_NONE) bassFreq = BassFreq.BASS_FREQ_NONE;
        this.bassFreq = bassFreq;
    }
    public byte getBassFreq() {
        return bassFreq;
    }
    public void setTrebleFreq(byte trebleFreq) {
        if (trebleFreq > TrebleFreq.TREBLE_FREQ_13000) trebleFreq = TrebleFreq.TREBLE_FREQ_13000;
        if (trebleFreq < TrebleFreq.TREBLE_FREQ_NONE) trebleFreq = TrebleFreq.TREBLE_FREQ_NONE;
        this.trebleFreq = trebleFreq;
    }
    public byte getTrebleFreq() {
        return trebleFreq;
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

    /*public XmlData toXmlData() {
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
    public void importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
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
            String msg = "BassTrebleChannel=" + Integer.toString(channel) +
                    ". Import from xml is not success.";
            Log.d(TAG, msg);
            throw new IOException(msg);
        }
        //Log.d(TAG, getInfo());
    }*/

    public class BassFreq {
        //freq for FS = 96kHz
        public final static byte BASS_FREQ_NONE = 0;
        public final static byte BASS_FREQ_125  = 1;
        public final static byte BASS_FREQ_250  = 2;
        public final static byte BASS_FREQ_375  = 3;
        public final static byte BASS_FREQ_438  = 4;
        public final static byte BASS_FREQ_500  = 5;
    }
    public class TrebleFreq {
        //freq for FS = 96kHz
        public final static byte TREBLE_FREQ_NONE   = 0;
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
