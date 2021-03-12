/*
 *   DrcCoef.java
 *
 *   Created by Artem Khlyupin on 13/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.drc;

import android.util.Log;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.hifitoynumbers.Number523;
import com.hifitoy.hifitoynumbers.Number88;
import com.hifitoy.hifitoynumbers.Number923;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.tas5558.TAS5558;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_1_7;


public class DrcCoef implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    public final static float POINT0_INPUT_DB = -120.0f;
    public final static float POINT3_INPUT_DB = 0.0f;

    private byte        channel;

    private DrcPoint    point0;
    private DrcPoint    point1;
    private DrcPoint    point2;
    private DrcPoint    point3;

    public DrcCoef(byte channel, DrcPoint p0, DrcPoint p1, DrcPoint p2, DrcPoint p3) {
        setChannel(channel);
        setPoint0(p0);
        setPoint1(p1);
        setPoint2(p2);
        setPoint3(p3);
    }
    public DrcCoef(byte channel) {
        this(channel, new DrcPoint(POINT0_INPUT_DB, -120.0f),
                        new DrcCoef.DrcPoint(-72.0f, -72.0f),
                        new DrcCoef.DrcPoint(-24.0f, -24.0f),
                        new DrcCoef.DrcPoint(POINT3_INPUT_DB, -24.0f));
    }
    public DrcCoef() {
        this(DRC_CH_1_7);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrcCoef drcCoef = (DrcCoef) o;
        return channel == drcCoef.channel &&
                Objects.equals(point0, drcCoef.point0) &&
                Objects.equals(point1, drcCoef.point1) &&
                Objects.equals(point2, drcCoef.point2) &&
                Objects.equals(point3, drcCoef.point3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }

    @Override
    public DrcCoef clone() throws CloneNotSupportedException{
        DrcCoef dc =  (DrcCoef) super.clone();
        dc.point0  = point0.clone();
        dc.point1  = point1.clone();
        dc.point2  = point2.clone();
        dc.point3  = point3.clone();
        return dc;
    }

    //setters / getters
    public void setChannel(byte channel) {
        if (channel > DrcChannel.DRC_CH_8) channel = DrcChannel.DRC_CH_8;
        if (channel < DRC_CH_1_7) channel = DRC_CH_1_7;
        this.channel = channel;
    }
    public byte getChannel() {
        return channel;
    }

    public List<DrcPoint> getPoints() {
        List<DrcPoint> points = new ArrayList<>();
        points.add(point0);
        points.add(point1);
        points.add(point2);
        points.add(point3);

        return points;
    }
    public void setPoint0(DrcPoint p) {
        if (p.inputDb > 0) p.inputDb = 0;
        if (p.outputDb > 0) p.outputDb = 0;
        point0 = p;
    }
    public DrcPoint getPoint0() {
        return point0;
    }
    public void setPoint1(DrcPoint p) {
        if (p.inputDb > 0) p.inputDb = 0;
        if (p.outputDb > 0) p.outputDb = 0;
        point1 = p;
    }
    public DrcPoint getPoint1() {
        return point1;
    }
    public void setPoint2(DrcPoint p) {
        if (p.inputDb > 0) p.inputDb = 0;
        if (p.outputDb > 0) p.outputDb = 0;
        point2 = p;
    }
    public DrcPoint getPoint2() {
        return point2;
    }
    public void setPoint3(DrcPoint p) {
        if (p.inputDb > 0) p.inputDb = 0;
        if (p.outputDb > 0) p.outputDb = 0;
        point3 = p;
    }
    public DrcPoint getPoint3() {
        return point3;
    }
    public void setPoint0WithCheck(DrcPoint point0) {
        if (point0.outputDb > point1.outputDb) {
            point0.outputDb = point1.outputDb;
        }
        this.point0.outputDb = point0.outputDb;
    }
    public void setPoint1WithCheck(DrcPoint point1) {
        if (point1.inputDb > point2.inputDb) {
            point1.inputDb = point2.inputDb;
        }
        if (point1.outputDb > point2.outputDb) {
            point1.outputDb = point2.outputDb;
        }
        if (point1.outputDb < point0.outputDb) {
            point1.outputDb = point0.outputDb;
        }
        this.point1 = point1;
    }
    public void setPoint2WithCheck(DrcPoint point2) {
        if (point2.inputDb < point1.inputDb) {
            point2.inputDb = point1.inputDb;
        }
        if (point2.outputDb > point3.outputDb) {
            point2.outputDb = point3.outputDb;
        }
        if (point2.outputDb < point1.outputDb) {
            point2.outputDb = point1.outputDb;
        }
        this.point2 = point2;
    }
    public void setPoint3WithCheck(DrcPoint point3) {
        if (point3.outputDb < point2.outputDb) {
            point3.outputDb = point2.outputDb;
        }
        this.point3.outputDb = point3.outputDb;
    }

    @Override
    public byte getAddress() {
        if (channel == DrcChannel.DRC_CH_8) {
            return TAS5558.DRC2_THRESHOLD_REG;
        }
        return TAS5558.DRC1_THRESHOLD_REG;
    }

    @Override
    public String toString() {
        return "0:" + point0.getInfo() + " 1:" + point1.getInfo() +
                " 2:" + point2.getInfo() + " 3:" + point3.getInfo();
    }

    @Override
    public void sendToPeripheral(boolean response) {
        ByteBuffer b = ByteBuffer.allocate(17);
        b.put(channel);
        b.put(point0.get88Binary());
        b.put(point1.get88Binary());
        b.put(point2.get88Binary());
        b.put(point3.get88Binary());

        //send data
        HiFiToyControl.getInstance().sendDataToDsp(b, response);
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        ByteBuffer b = new DrcParam(point0, point1, point2, point3).getBinary();
        return new ArrayList<>(Collections.singletonList(new HiFiToyDataBuf(getAddress(), b)));
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);

            if ((buf.getAddr() == getAddress()) && (buf.getLength() == 28)) {
                DrcParam param = new DrcParam();
                if (param.parseBinary(buf.getData())) {
                    DrcPoint[] p = param.getDrcPoints();

                    point0 = p[0];
                    point1 = p[1];
                    point2 = p[2];
                    point3 = p[3];

                    Log.d(TAG, "DrcCoef import success.");
                    return true;
                }
            }
        }

        return false;
    }

    /*@Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();
        xmlData.addXmlElement("InputDb0", point0.inputDb);
        xmlData.addXmlElement("OutputDb0", point0.outputDb);
        xmlData.addXmlElement("InputDb1", point1.inputDb);
        xmlData.addXmlElement("OutputDb1", point1.outputDb);
        xmlData.addXmlElement("InputDb2", point2.inputDb);
        xmlData.addXmlElement("OutputDb2", point2.outputDb);
        xmlData.addXmlElement("InputDb3", point3.inputDb);
        xmlData.addXmlElement("OutputDb3", point3.outputDb);

        XmlData drcCoefXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Channel", Integer.toString(channel));

        drcCoefXmlData.addXmlElement("DrcCoef", xmlData, attrib);
        return drcCoefXmlData;
    }

    @Override
    public void importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String elementName = null;
        int count = 0;

        do {
            xmlParser.next();

            if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                elementName = xmlParser.getName();
            }
            if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                if (xmlParser.getName().equals("DrcCoef")) break;

                elementName = null;
            }

            if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){
                String elementValue = xmlParser.getText();
                if (elementValue == null) continue;

                if (elementName.equals("InputDb0")){
                    point0.inputDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("OutputDb0")){
                    point0.outputDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("InputDb1")){
                    point1.inputDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("OutputDb1")){
                    point1.outputDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("InputDb2")){
                    point2.inputDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("OutputDb2")){
                    point2.outputDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("InputDb3")){
                    point3.inputDb = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("OutputDb3")){
                    point3.outputDb = Float.parseFloat(elementValue);
                    count++;
                }
            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        //check import result
        if (count != 8){
            String msg = "DrcCoef=" + Integer.toString(channel) +
                    ". Import from xml is not success.";
            Log.d(TAG, msg);
            throw new IOException(msg);
        }
        Log.d(TAG, toString());
    }*/


    public static class DrcPoint implements Cloneable, Serializable{
        private float inputDb;
        private float outputDb;

        public DrcPoint(float inputDb, float outputDb) {
            this.inputDb = inputDb;
            this.outputDb = outputDb;
        }
        public DrcPoint() {
            this(0, 0);
        }

        @Override
        public DrcPoint clone() throws CloneNotSupportedException{
            return (DrcPoint) super.clone();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DrcPoint drcPoint = (DrcPoint) o;
            return FloatUtility.isFloatDiffLessThan(drcPoint.inputDb, inputDb, 0.5f) &&
                    FloatUtility.isFloatDiffLessThan(drcPoint.outputDb, outputDb, 0.5f);
        }

        @Override
        public int hashCode() {
            return 0;
        }

        //settters / getters
        public void setInputDb(float inputDb) {
            this.inputDb = inputDb;
        }
        public float getInputDb() {
            return inputDb;
        }
        public void setOutputDb(float outputDb) {
            this.outputDb = outputDb;
        }
        public float getOutputDb() {
            return outputDb;
        }

        public String getInfo() {
            return String.format(Locale.getDefault(), "%.1f %.1f", inputDb, outputDb);
        }

        public ByteBuffer get88Binary() {
            ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            b.put(Number88.get88LittleEnd(inputDb));
            b.put(Number88.get88LittleEnd(outputDb));
            return (ByteBuffer) b.position(0);
        }
    }

    public static class DrcParam implements Cloneable, Serializable {
        private float threshold1_db;
        private float threshold2_db;
        private float offset1_db;
        private float offset2_db;
        private float k0;    //k>0 - expansion
        private float k1;    //-1<k<0 - compression
        private float k2;

        public DrcParam() {
            threshold1_db = 0.0f;
            threshold2_db = -1.0f;
            offset1_db = 0.0f;
            offset2_db = 0.0f;

            k0 = 1.0f;
            k1 = 1.0f;
            k2 = 1.0f;
        }
        public DrcParam(DrcPoint p0, DrcPoint p1, DrcPoint p2, DrcPoint p3) {
            threshold1_db = p1.inputDb;
            threshold2_db = p2.inputDb;
            offset1_db = p1.inputDb - p1.outputDb;
            offset2_db = p2.inputDb - p2.outputDb;

            k0 = getK(p0, p1) - 1;
            k1 = getK(p1, p2) - 1;
            k2 = getK(p2, p3) - 1;
        }
        public DrcParam(ByteBuffer data) {
            this();
            parseBinary(data);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DrcParam drcParam = (DrcParam) o;
            return FloatUtility.isFloatDiffLessThan(drcParam.threshold1_db, threshold1_db, 0.5f) &&
                    FloatUtility.isFloatDiffLessThan(drcParam.threshold2_db, threshold2_db, 0.5f) &&
                    FloatUtility.isFloatDiffLessThan(drcParam.offset1_db, offset1_db, 0.5f) &&
                    FloatUtility.isFloatDiffLessThan(drcParam.offset2_db, offset2_db, 0.5f) &&
                    FloatUtility.isFloatDiffLessThan(drcParam.k0, k0, 0.01f) &&
                    FloatUtility.isFloatDiffLessThan(drcParam.k1, k1, 0.01f) &&
                    FloatUtility.isFloatDiffLessThan(drcParam.k2, k2, 0.01f);
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public DrcParam clone() throws CloneNotSupportedException{
            return (DrcParam) super.clone();
        }

        private float getK(DrcPoint p0, DrcPoint p1) {
            if (p1.inputDb == p0.inputDb) {
                return 1;
            }
            return (p1.outputDb - p0.outputDb) / (p1.inputDb - p0.inputDb);
        }

        public DrcPoint[] getDrcPoints() {
            DrcPoint[] p = new DrcPoint[4];

            p[1] = new DrcPoint(threshold1_db, threshold1_db - offset1_db);
            p[2] = new DrcPoint(threshold2_db, threshold2_db - offset2_db);

            p[3] = new DrcPoint(POINT3_INPUT_DB, p[2].outputDb + (k2 + 1) * (POINT3_INPUT_DB - p[2].inputDb));
            p[0] = new DrcPoint(POINT0_INPUT_DB, p[1].outputDb - (k0 + 1) * (p[1].inputDb - POINT0_INPUT_DB));

            return p;
        }

        public ByteBuffer getBinary() {
            ByteBuffer b = ByteBuffer.allocate(7 * 4);
            b.put(Number923.get923BigEnd(threshold1_db / -6.0206f));
            b.put(Number923.get923BigEnd(threshold2_db / -6.0206f));
            b.put(Number523.get523BigEnd(k0));
            b.put(Number523.get523BigEnd(k1));
            b.put(Number523.get523BigEnd(k2));
            b.put(Number923.get923BigEnd((offset1_db + 24.0824f) / 6.0206f));
            b.put(Number923.get923BigEnd((offset2_db + 24.0824f) / 6.0206f));
            return b;
        }

        private boolean parseBinary(ByteBuffer data) {
            if (data.capacity() != 28) return false;

            threshold1_db = Number923.toFloat(BinaryOperation.copyOfRange(data, 0, 4)) * -6.0206f;
            threshold2_db = Number923.toFloat(BinaryOperation.copyOfRange(data, 4, 8)) * -6.0206f;
            k0 = Number523.toFloat(BinaryOperation.copyOfRange(data, 8, 12));
            k1 = Number523.toFloat(BinaryOperation.copyOfRange(data, 12, 16));
            k2 = Number523.toFloat(BinaryOperation.copyOfRange(data, 16, 20));
            offset1_db = Number923.toFloat(BinaryOperation.copyOfRange(data, 20, 24)) * 6.0206f - 24.0824f;
            offset2_db = Number923.toFloat(BinaryOperation.copyOfRange(data, 24, 28)) * 6.0206f - 24.0824f;

            return true;
        }
    }
}
