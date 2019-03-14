/*
 *   Volume.java
 *
 *   Created by Artem Khlyupin on 11/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import android.util.Log;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.xml.XmlData;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Volume implements HiFiToyObject, Cloneable {
    private static final String TAG = "HiFiToy";

    private final static float HW_MAX_DB    = 18.0f;
    private final static float HW_MIN_DB    = -127.0f;
    public final static float HW_MUTE_DB    = -81.0f;

    private byte    address;
    private float   db;

    private float   maxDb;
    private float   minDb;

    public Volume(byte address, float db, float maxDb, float minDb) {
        this.address = address;

        if (maxDb > HW_MAX_DB) maxDb = HW_MAX_DB;
        if (minDb < HW_MIN_DB) minDb = HW_MIN_DB;
        this.maxDb = maxDb;
        this.minDb = minDb;

        setDb(db);
    }
    public Volume(byte address, float db) {
        this(address, db, HW_MAX_DB, HW_MIN_DB);
    }
    public Volume(byte address) {
        this(address, 0.0f, HW_MAX_DB, HW_MIN_DB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Volume volume = (Volume) o;
        return address == volume.address &&
                Float.compare(volume.db, db) == 0 &&
                Float.compare(volume.maxDb, maxDb) == 0 &&
                Float.compare(volume.minDb, minDb) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, db, maxDb, minDb);
    }

    @Override
    public Volume clone() throws CloneNotSupportedException{
        return (Volume) super.clone();
    }

    //setters / getters
    public void setDb(float db) {
        if (db > maxDb) db = maxDb;
        if (db < minDb) db = minDb;
        this.db = db;
    }
    public float getDb() {
        return db;
    }
    public void setDbPercent(float percent) {
        if (percent > 1.0f) percent = 1.0f;
        if (percent < 0.0f) percent = 0.0f;

        setDb(percent * (maxDb - minDb) + minDb);
    }
    public float getDbPercent() {
        return (db - minDb) / (maxDb - minDb);
    }

    public float dbToAmpl(float db) {
        return (float)Math.pow(10, (db / 20));
    }
    public float amplToDb(float ampl) {
        return (float)(20 * Math.log10(ampl));
    }

    @Override
    public byte getAddress() {
        return address;
    }

    @Override
    public String getInfo() {
        if (db > HW_MUTE_DB) {
            return String.format(Locale.getDefault(),"%.1fdb", db);
        }
        return "Mute";
    }

    @Override
    public void sendToPeripheral(boolean response) {
        HiFiToyControl.getInstance().sendDataToDsp(getBinary(), response);
    }

    @Override
    public byte[] getBinary() {
        int v = 0x245;
        if (db > HW_MUTE_DB) {
            v = (int) ((18.0f - db) / 0.25);
            if (v < 1) v = 1;
            if (v > 0x245) v = 0x245;
        }

        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        b.putInt(v);

        HiFiToyDataBuf data = new HiFiToyDataBuf(address, b);
        return data.getBinary().array();
    }

    @Override
    public boolean importData(byte[] data) {
        return false;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();

        xmlData.addXmlElement("MaxDb", maxDb);
        xmlData.addXmlElement("MinDb", minDb);
        xmlData.addXmlElement("Db", db);

        XmlData gainXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Address", Integer.toString(address));

        gainXmlData.addXmlElement("Volume", xmlData, attrib);
        return gainXmlData;
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
                if (xmlParser.getName().equals("Volume")) break;

                elementName = null;
            }

            if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){
                String elementValue = xmlParser.getText();
                if (elementValue == null) continue;

                if (elementName.equals("MaxDb")){
                    maxDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("MinDb")){
                    minDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("Db")){
                    setDb(Float.parseFloat(elementValue));
                    count++;
                }
            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        //check import result
        if (count != 3){
            Log.d(TAG, "Volume=" + Integer.toString(address) +
                    ". Import from xml is not success.");
            return false;
        }
        Log.d(TAG, getInfo());

        return true;
    }
}
