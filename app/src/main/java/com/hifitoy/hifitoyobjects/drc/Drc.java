/*
 *   Drc.java
 *
 *   Created by Artem Khlyupin on 12/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.drc;

import android.util.Log;

import com.hifitoy.ble.BlePacket;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.ByteUtility;
import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.hifitoynumbers.Number523;
import com.hifitoy.hifitoynumbers.Number88;
import com.hifitoy.hifitoynumbers.Number923;
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

import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_1_7;
import static com.hifitoy.hifitoyobjects.drc.DrcChannel.DRC_CH_8;
import static com.hifitoy.tas5558.TAS5558.DRC_BYPASS1_REG;

public class Drc implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private float[]         enabledCh; // 0.0 .. 1.0, 8 channels
    private byte[]          evaluationCh;

    private DrcCoef         coef17;
    private DrcCoef         coef8;
    private DrcTimeConst    timeConst17;
    private DrcTimeConst    timeConst8;

    public Drc() {
        enabledCh = new float[8];
        evaluationCh = new byte[8];
        for (int i = 0; i < 8; i++) {
            enabledCh[i] = 0.0f;
            evaluationCh[i] = DrcEvaluation.DISABLED_EVAL;
        }

        this.coef17         = new DrcCoef(DRC_CH_1_7);
        this.coef8          = new DrcCoef(DRC_CH_8);
        this.timeConst17    = new DrcTimeConst(DRC_CH_1_7);
        this.timeConst8     = new DrcTimeConst(DRC_CH_8);
    }
    public Drc (DrcCoef coef17, DrcTimeConst timeConst17) {
        this();

        if (coef17 != null) this.coef17 = coef17;
        if (timeConst17 != null) this.timeConst17 = timeConst17;
    }
    public Drc (DrcCoef coef17, DrcCoef coef8, DrcTimeConst timeConst17, DrcTimeConst timeConst8) {
        this();

        if (coef17 != null) this.coef17 = coef17;
        if (coef8 != null) this.coef8 = coef8;
        if (timeConst17 != null) this.timeConst17 = timeConst17;
        if (timeConst8 != null) this.timeConst8 = timeConst8;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drc drc = (Drc) o;

        for (int i = 0; i < 8; i++) {
            if (!FloatUtility.isFloatDiffLessThan(enabledCh[i], drc.enabledCh[i], 0.01f)) {
                return false;
            }
        }
        return Arrays.equals(evaluationCh, drc.evaluationCh) &&
                Objects.equals(coef17, drc.coef17) &&
                Objects.equals(coef8, drc.coef8) &&
                Objects.equals(timeConst17, drc.timeConst17) &&
                Objects.equals(timeConst8, drc.timeConst8);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(coef17, coef8, timeConst17, timeConst8);
        result = 31 * result + Arrays.hashCode(evaluationCh);
        return result;
    }

    @Override
    public Drc clone() throws CloneNotSupportedException{
        Drc drc = (Drc) super.clone();

        drc.enabledCh = new float[8];
        drc.evaluationCh = new byte[8];
        for (int i = 0; i < 8; i++) {
            drc.enabledCh[i] = enabledCh[i];
            drc.evaluationCh[i] = evaluationCh[i];
        }

        drc.coef17 = coef17.clone();
        drc.coef8 = coef8.clone();
        drc.timeConst17 = timeConst17.clone();
        drc.timeConst8 = timeConst8.clone();

        return drc;
    }

    //setters / getters
    public void setEnabled(float enabled, byte channel) {
        if ((channel >= 0) && (channel < 8)) {
            enabledCh[channel] = enabled;
        }
    }
    public float getEnabledChannel(byte channel) {
        if ((channel >= 0) && (channel < 8)) {
            return enabledCh[channel];
        }
        return 0.0f;
    }
    public void setEvaluation(byte evaluation, byte channel) {
        if ((channel >= 0) && (channel < 8)) {
            if (evaluation < DrcEvaluation.DISABLED_EVAL) evaluation = DrcEvaluation.DISABLED_EVAL;
            if (evaluation > DrcEvaluation.POST_VOLUME_EVAL) evaluation = DrcEvaluation.POST_VOLUME_EVAL;

            evaluationCh[channel] = evaluation;
        }
    }
    public byte getEvaluation(byte channel) {
        if ((channel >= 0) && (channel < 8)) {
            return evaluationCh[channel];
        }
        return DrcEvaluation.DISABLED_EVAL;
    }

    public DrcTimeConst getTimeConst17(){
        return timeConst17;
    }

    public DrcCoef getCoef17() {
        return coef17;
    }

    @Override
    public byte getAddress() {
        return TAS5558.DRC1_CONTROL_REG;
    }

    @Override
    public String toString() {
        return "Drc info";
    }

    public void sendEvaluationToPeripheral(boolean response) {
        BlePacket p = new BlePacket(getEvaluationDataBuf().getBinary(), 20, response);
        HiFiToyControl.getInstance().sendDataToDsp(p);
    }

    public void sendEnabledToPeripheral(byte channel, boolean response) {
        HiFiToyDataBuf d = getEnabledDataBuf(channel);

        if (d != null) {
            BlePacket p = new BlePacket(d.getBinary(), 20, response);
            HiFiToyControl.getInstance().sendDataToDsp(p);
        }
    }

    @Override
    public void sendToPeripheral(boolean response) {
        coef17.sendToPeripheral(response);
        coef8.sendToPeripheral(response);
        timeConst17.sendToPeripheral(response);
        timeConst8.sendToPeripheral(response);
        sendEvaluationToPeripheral(response);

        for (byte i = 0; i < 8; i++) {
            sendEnabledToPeripheral(i, response);
        }
    }

    private HiFiToyDataBuf getEvaluationDataBuf() {
        int d = 0;
        for (int i = 7; i >= 0; i--){
            d <<= 2;
            d |= evaluationCh[i] & 0x03;
        }

        ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        b.putInt(d);
        b.putInt((evaluationCh[7] & 0x03));

        return new HiFiToyDataBuf(getAddress(), b);
    }

    private HiFiToyDataBuf getEnabledDataBuf(byte channel) {
        if ( (channel < 0) || (channel > 7) ) return null;

        ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        b.putInt(0x800000 - (int)(0x800000 * enabledCh[channel]));
        b.putInt((int)(0x800000 * enabledCh[channel]));

        return new HiFiToyDataBuf((byte)(DRC_BYPASS1_REG + channel), b);
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();

        l.addAll(coef17.getDataBufs());
        l.addAll(coef8.getDataBufs());
        l.addAll(timeConst17.getDataBufs());
        l.addAll(timeConst8.getDataBufs());

        l.add(getEvaluationDataBuf());

        for (byte i = 0; i < 8; i++){
            l.add(getEnabledDataBuf(i));
        }

        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        if (!coef17.importFromDataBufs(dataBufs)) return false;
        if (!coef17.importFromDataBufs(dataBufs)) return false;
        if (!timeConst17.importFromDataBufs(dataBufs)) return false;
        if (!timeConst8.importFromDataBufs(dataBufs)) return false;

        int importCounter = 0;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);

            if ((buf.getAddr() == getAddress()) && (buf.getLength() == 8)) {
                int d = buf.getData().getInt(0);

                for (int u = 0; u < 7; u++){
                    evaluationCh[u] = (byte)(d & 0x03);
                    d >>>= 2;
                }
                evaluationCh[7] = (byte)(buf.getData().getInt(1) & 0x03);

                importCounter++;
            }

            if ((buf.getAddr() >= DRC_BYPASS1_REG) &&
                    (buf.getAddr() < DRC_BYPASS1_REG + 8) && (buf.getLength() == 8)) {

                float val = Number523.toFloat(BinaryOperation.copyOfRange(buf.getData(), 4, 8));
                enabledCh[buf.getAddr() - DRC_BYPASS1_REG] = val;

                importCounter++;
            }

            if (importCounter >= 9) {
                Log.d(TAG, "Drc import success.");
                return true;
            }

        }

        return false;
    }

    /*@Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();

        for (int i = 0; i < 8; i++){
            String keyStr = String.format(Locale.getDefault(), "enabledCh%d", i);
            xmlData.addXmlElement(keyStr, enabledCh[i]);
        }
        for (int i = 0; i < 8; i++){
            String keyStr = String.format(Locale.getDefault(), "evaluationCh%d", i);
            xmlData.addXmlElement(keyStr, evaluationCh[i]);
        }
        xmlData.addXmlData(coef17.toXmlData());
        xmlData.addXmlData(coef8.toXmlData());
        xmlData.addXmlData(timeConst17.toXmlData());
        xmlData.addXmlData(timeConst8.toXmlData());

        XmlData drcXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Address", ByteUtility.toString(getAddress()));

        drcXmlData.addXmlElement("Drc", xmlData, attrib);
        return drcXmlData;
    }

    @Override
    public void importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String elementName = null;
        int count = 0;

        do {
            xmlParser.next();

            if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                elementName = xmlParser.getName();

                if (elementName.equals("DrcCoef")){
                    String channelStr = xmlParser.getAttributeValue(null, "Channel");
                    if (channelStr == null) continue;
                    byte channel = Byte.parseByte(channelStr);

                    if ((coef17 != null) && (coef17.getChannel() == channel)){
                        coef17.importFromXml(xmlParser);
                        count++;
                    }
                    if ((coef8 != null) && (coef8.getChannel() == channel)){
                        coef8.importFromXml(xmlParser);
                        count++;
                    }
                }
                if (elementName.equals("DrcTimeConst")){
                    String channelStr = xmlParser.getAttributeValue(null, "Channel");
                    if (channelStr == null) continue;
                    byte channel = Byte.parseByte(channelStr);

                    if ((timeConst17 != null) && (timeConst17.getChannel() == channel)){
                        timeConst17.importFromXml(xmlParser);
                        count++;
                    }
                    if ((timeConst8 != null) && (timeConst8.getChannel() == channel)){
                        timeConst8.importFromXml(xmlParser);
                        count++;
                    }
                }
            }
            if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                if (xmlParser.getName().equals("Drc")) break;

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
                for (int i = 0; i < 8; i++){
                    String keyStr = String.format(Locale.getDefault(), "evaluationCh%d", i);
                    if (elementName.equals(keyStr)){
                        evaluationCh[i] = Byte.parseByte(elementValue);
                        count++;
                    }
                }
            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        //check import result
        if (count != 20){
            String msg = "Drc. Import from xml is not success.";
            Log.d(TAG, msg);
            throw new IOException(msg);
        }
        Log.d(TAG, toString());
    }*/

    public class DrcEvaluation {
        public final static byte DISABLED_EVAL      = 0;
        public final static byte PRE_VOLUME_EVAL    = 1;
        public final static byte POST_VOLUME_EVAL   = 2;
    }

}
