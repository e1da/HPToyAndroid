/*
 *   Drc.java
 *
 *   Created by Artem Khlyupin on 12/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import android.util.Log;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.Number523;
import com.hifitoy.hifitoynumbers.Number88;
import com.hifitoy.hifitoynumbers.Number923;
import com.hifitoy.tas5558.TAS5558;
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

public class Drc {
    private static final String TAG = "HiFiToy";

    public class DrcCoef implements HiFiToyObject {
        private byte        channel;

        private DrcPoint    point0;
        private DrcPoint    point1;
        private DrcPoint    point2;
        private DrcPoint    point3;

        public DrcCoef(byte channel, DrcPoint p0, DrcPoint p1, DrcPoint p2, DrcPoint p3) {
            if (channel > DrcChannel.DRC_CH_8) channel = DrcChannel.DRC_CH_8;
            if (channel < DrcChannel.DRC_CH_1_7) channel = DrcChannel.DRC_CH_1_7;

            this.channel = channel;
            setPoint0(p0);
            setPoint1(p1);
            setPoint2(p2);
            setPoint3(p3);
        }

        //setters / getters
        public void setPoint0(DrcPoint p) {
            if (p.inputDb > 0) p.inputDb = 0;
            if (p.outputDb > 0) p.outputDb = 0;
            point0 = p;
        }
        public void setPoint1(DrcPoint p) {
            if (p.inputDb > 0) p.inputDb = 0;
            if (p.outputDb > 0) p.outputDb = 0;
            point1 = p;
        }
        public void setPoint2(DrcPoint p) {
            if (p.inputDb > 0) p.inputDb = 0;
            if (p.outputDb > 0) p.outputDb = 0;
            point2 = p;
        }
        public void setPoint3(DrcPoint p) {
            if (p.inputDb > 0) p.inputDb = 0;
            if (p.outputDb > 0) p.outputDb = 0;
            point3 = p;
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
        public String getInfo() {
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
            HiFiToyControl.getInstance().sendDataToDsp(b.array(), response);
        }

        @Override
        public byte[] getBinary() {
            ByteBuffer b = new DrcParam(point0, point1, point2, point3).getBinary();
            HiFiToyDataBuf data = new HiFiToyDataBuf(getAddress(), b);
            return data.getBinary().array();
        }

        @Override
        public boolean importData(byte[] data) {
            return false;
        }

        @Override
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
        public boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
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
                Log.d(TAG, "DrcCoef=" + Integer.toString(channel) +
                        ". Import from xml is not success.");
                return false;
            }
            Log.d(TAG, getInfo());
            return true;
        }


        public class DrcPoint {
            private float inputDb;
            private float outputDb;

            public String getInfo() {
                return String.format(Locale.getDefault(), "%.1f %.1f", inputDb, outputDb);
            }

            public ByteBuffer get88Binary() {
                ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
                b.put(Number88.get88BigEnd(inputDb));
                b.put(Number88.get88BigEnd(outputDb));
                return b;
            }
        }

        public class DrcParam {
            private float threshold1_db;
            private float threshold2_db;
            private float offset1_db;
            private float offset2_db;
            private float k0;    //k>0 - expansion
            private float k1;    //-1<k<0 - compression
            private float k2;

            public DrcParam(DrcPoint p0, DrcPoint p1, DrcPoint p2, DrcPoint p3) {
                threshold1_db = p1.inputDb;
                threshold2_db = p2.inputDb;
                offset1_db = p1.inputDb - p1.outputDb;
                offset2_db = p2.inputDb - p2.outputDb;

                k0 = getK(p0, p1) - 1;
                k1 = getK(p1, p2) - 1;
                k2 = getK(p2, p3) - 1;
            }

            private float getK(DrcPoint p0, DrcPoint p1) {
                if (p1.inputDb == p0.inputDb) {
                    return 1;
                }
                return (p1.outputDb - p0.outputDb) / (p1.inputDb - p0.inputDb);
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
        }
    }

    public class DrcTimeConst implements HiFiToyObject, Cloneable {
        private byte    channel;
        private float   energyMS;
        private float   attackMS;
        private float   decayMS;

        public DrcTimeConst(byte channel, float energyMS, float attackMS, float decayMS) {
            if (channel > DrcChannel.DRC_CH_8) channel = DrcChannel.DRC_CH_8;
            if (channel < DrcChannel.DRC_CH_1_7) channel = DrcChannel.DRC_CH_1_7;

            this.channel = channel;
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
            HiFiToyControl.getInstance().sendDataToDsp(getEnergyBinary(), response);
        }

        public void sendAttackDecayToPeripheral(boolean response) {
            HiFiToyControl.getInstance().sendDataToDsp(getAttackDecayBinary(), response);
        }

        @Override
        public void sendToPeripheral(boolean response) {
            sendEnergyToPeripheral(response);
            sendAttackDecayToPeripheral(response);
        }

        private int timeToInt(float time_ms) {
            return (int)(Math.pow(Math.E, -2000.0f / time_ms / TAS5558.TAS5558_FS) * 0x800000) & 0x007FFFFF;
        }

        private byte[] getEnergyBinary() {
            ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
            b.putInt(0x800000 - timeToInt(energyMS));
            b.putInt(timeToInt(energyMS));

            HiFiToyDataBuf data = new HiFiToyDataBuf(getAddress(), b);
            return data.getBinary().array();
        }

        private byte[] getAttackDecayBinary() {
            ByteBuffer b = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
            b.putInt(0x800000 - timeToInt(attackMS));
            b.putInt(timeToInt(attackMS));
            b.putInt(0x800000 - timeToInt(decayMS));
            b.putInt(timeToInt(decayMS));

            HiFiToyDataBuf data = new HiFiToyDataBuf((byte)(getAddress() + 4), b);
            return data.getBinary().array();
        }

        @Override
        public byte[] getBinary() {
            return BinaryOperation.concatData(getEnergyBinary(), getAttackDecayBinary());
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

    public class DrcChannel {
        public final static byte DRC_CH_1_7     = 0;
        public final static byte DRC_CH_8       = 1;
    }
}
