/*
 *   Drc.java
 *
 *   Created by Artem Khlyupin on 12/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.drc;

import android.util.Log;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Drc implements HiFiToyObject, Cloneable {
    private static final String TAG = "HiFiToy";

    private float[]         enabledCh; // 0.0 .. 1.0, 8 channels
    private byte[]          evaluationCh;

    private DrcCoef         coef17;
    private DrcCoef         coef8;
    private DrcTimeConst    timeConst17;
    private DrcTimeConst    timeConst8;

    public Drc (DrcCoef coef17, DrcCoef coef8, DrcTimeConst timeConst17, DrcTimeConst timeConst8) {
        this.coef17 = coef17;
        this.coef8 = coef8;
        this.timeConst17 = timeConst17;
        this.timeConst8 = timeConst8;

        enabledCh = new float[8];
        evaluationCh = new byte[8];
        for (int i = 0; i < 8; i++) {
            enabledCh[i] = 0.0f;
            evaluationCh[i] = DrcEvaluation.DISABLED_EVAL;
        }
    }
    public Drc (DrcCoef coef17, DrcTimeConst timeConst17) {
        this(coef17, null, timeConst17, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drc drc = (Drc) o;
        return Arrays.equals(enabledCh, drc.enabledCh) &&
                Arrays.equals(evaluationCh, drc.evaluationCh) &&
                Objects.equals(coef17, drc.coef17) &&
                Objects.equals(coef8, drc.coef8) &&
                Objects.equals(timeConst17, drc.timeConst17) &&
                Objects.equals(timeConst8, drc.timeConst8);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(coef17, coef8, timeConst17, timeConst8);
        result = 31 * result + Arrays.hashCode(enabledCh);
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

        drc.coef17      = null;
        drc.coef8       = null;
        drc.timeConst17 = null;
        drc.timeConst8  = null;

        if (coef17 != null)         drc.coef17 = coef17.clone();
        if (coef8 != null)          drc.coef8 = coef8.clone();
        if (timeConst17 != null)    drc.timeConst17 = timeConst17.clone();
        if (timeConst8 != null)     drc.timeConst8 = timeConst8.clone();

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

    @Override
    public byte getAddress() {
        return TAS5558.DRC1_CONTROL_REG;
    }

    @Override
    public String getInfo() {
        return "Drc info";
    }

    public void sendEvaluationToPeripheral(boolean response) {
        HiFiToyControl.getInstance().sendDataToDsp(getEvaluationDataBuf().getBinary(), response);
    }

    public void sendEnabledToPeripheral(byte channel, boolean response) {
        HiFiToyDataBuf d = getEnabledDataBuf(channel);

        if (d != null) HiFiToyControl.getInstance().sendDataToDsp(d.getBinary(), response);
    }

    @Override
    public void sendToPeripheral(boolean response) {
        if (coef17 != null)         coef17.sendToPeripheral(response);
        if (coef8 != null)          coef8.sendToPeripheral(response);
        if (timeConst17 != null)    timeConst17.sendToPeripheral(response);
        if (timeConst8 != null)     timeConst8.sendToPeripheral(response);
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

        return new HiFiToyDataBuf((byte)(TAS5558.DRC_BYPASS1_REG + channel), b);
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();

        if (coef17 != null)         l.addAll(coef17.getDataBufs());
        if (coef8 != null)          l.addAll(coef8.getDataBufs());
        if (timeConst17 != null)    l.addAll(timeConst17.getDataBufs());
        if (timeConst8 != null)     l.addAll(timeConst8.getDataBufs());

        return l;
    }

    @Override
    public boolean importData(byte[] data) {
        return false;
    }

    @Override
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
        attrib.put("Address", Integer.toString(getAddress()));

        drcXmlData.addXmlElement("Drc", xmlData, attrib);
        return drcXmlData;
    }

    @Override
    public boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
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
                        if (coef17.importFromXml(xmlParser)) count++;
                    }
                    if ((coef8 != null) && (coef8.getChannel() == channel)){
                        if (coef8.importFromXml(xmlParser)) count++;
                    }
                }
                if (elementName.equals("DrcTimeConst")){
                    String channelStr = xmlParser.getAttributeValue(null, "Channel");
                    if (channelStr == null) continue;
                    byte channel = Byte.parseByte(channelStr);

                    if ((timeConst17 != null) && (timeConst17.getChannel() == channel)){
                        if (timeConst17.importFromXml(xmlParser)) count++;
                    }
                    if ((timeConst8 != null) && (timeConst8.getChannel() == channel)){
                        if (timeConst8.importFromXml(xmlParser)) count++;
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
            Log.d(TAG, "Drc. Import from xml is not success.");
            return false;
        }
        Log.d(TAG, getInfo());

        return true;
    }

    public class DrcEvaluation {
        public final static byte DISABLED_EVAL      = 0;
        public final static byte PRE_VOLUME_EVAL    = 1;
        public final static byte POST_VOLUME_EVAL   = 2;
    }

}
