/*
 *   Biquad.java
 *
 *   Created by Artem Khlyupin on 06/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import android.util.Log;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.Complex;
import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.hifitoynumbers.Number523;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.HiFiToyObject;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Biquad implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private boolean enabled;

    protected byte addr;
    protected byte bindAddr;

    protected float b0;
    protected float b1;
    protected float b2;
    protected float a1;
    protected float a2;

    public Biquad(Biquad b) {

        enabled = b.enabled;

        addr = b.addr;
        bindAddr = b.bindAddr;

        setCoefs(b.b0, b.b1, b.b2, b.a1, b.a2);
    }

    public Biquad(byte addr, byte bindAddr) {
        enabled = true;

        this.addr = addr;
        this.bindAddr = bindAddr;

        setCoefs(1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public Biquad(byte addr) {
        this(addr, (byte)0);
    }
    public Biquad() {
        this((byte)0, (byte)0);
    }

    @Override
    public Biquad clone() throws CloneNotSupportedException{
        return (Biquad) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biquad biquad = (Biquad) o;
        return addr == biquad.addr &&
                bindAddr == biquad.bindAddr &&
                enabled == biquad.enabled &&
                FloatUtility.isFloatDiffLessThan(biquad.b0, b0, 0.01f) &&
                FloatUtility.isFloatDiffLessThan(biquad.b1, b1, 0.01f) &&
                FloatUtility.isFloatDiffLessThan(biquad.b2, b2, 0.01f) &&
                FloatUtility.isFloatDiffLessThan(biquad.a1, a1, 0.01f) &&
                FloatUtility.isFloatDiffLessThan(biquad.a2, a2, 0.01f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addr, bindAddr);
    }

    //setters getters
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isEnabled() {
        return enabled;
    }

    public void setBindAddr(byte bindAddr) {
        this.bindAddr = bindAddr;
    }
    public byte getBindAddr() {
        return bindAddr;
    }
    public void moveBindAddr() {
        addr = bindAddr;
        bindAddr = 0;
    }

    public void setCoefs(float b0, float b1, float b2, float a1, float a2) {
        this.b0 = b0;
        this.b1 = b1;
        this.b2 = b2;
        this.a1 = a1;
        this.a2 = a2;
    }

    public float getB0(){
        return b0;
    }
    public float getB1(){
        return b1;
    }
    public float getB2(){
        return b2;
    }
    public float getA1(){
        return a1;
    }
    public float getA2(){
        return a2;
    }

    @Override
    public byte getAddress() {
        return addr;
    }


    @Override
    public void sendToPeripheral(boolean response) {

        //if (params.type.getValue() == Type.BIQUAD_USER) {
            ByteBuffer b = ByteBuffer.allocate(22).order(ByteOrder.LITTLE_ENDIAN);
            b.put(addr);
            b.put(bindAddr);

            b.putFloat(b0);
            b.putFloat(b1);
            b.putFloat(b2);
            b.putFloat(a1);
            b.putFloat(a2);

            HiFiToyControl.getInstance().sendDataToDsp(b, true);

        /*} else {

            ByteBuffer b = ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN);
            b.put(address0);
            b.put(address1);

            if (enabled) {
                b.put(params.order.getValue());
                b.put(params.type.getValue());
            } else {
                b.put(Order.BIQUAD_ORDER_2);
                b.put(Type.BIQUAD_OFF);
            }
            b.putShort(params.freq);
            b.putFloat(params.qFac);
            b.putFloat(params.dbVolume);

            HiFiToyControl.getInstance().sendDataToDsp(b, response);
        }*/
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        ByteBuffer data = ByteBuffer.allocate(20);
        data.put(Number523.get523BigEnd(b0));
        data.put(Number523.get523BigEnd(b1));
        data.put(Number523.get523BigEnd(b2));
        data.put(Number523.get523BigEnd(a1));
        data.put(Number523.get523BigEnd(a2));

        if (bindAddr != 0) {
            return new ArrayList<>(Arrays.asList(   new HiFiToyDataBuf(addr, data),
                                                    new HiFiToyDataBuf(bindAddr, data) ));
        }

        return new ArrayList<>(Collections.singletonList(new HiFiToyDataBuf(addr, data)));
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);

            if ((buf.getAddr() == addr) && (buf.getLength() == 20) ) {
                ByteBuffer bb = buf.getData();
                if (bb.capacity() == 20) {
                    float b0 = Number523.toFloat(BinaryOperation.copyOfRange(bb, 0, 4));
                    float b1 = Number523.toFloat(BinaryOperation.copyOfRange(bb, 4, 8));
                    float b2 = Number523.toFloat(BinaryOperation.copyOfRange(bb, 8, 12));
                    float a1 = Number523.toFloat(BinaryOperation.copyOfRange(bb, 12, 16));
                    float a2 = Number523.toFloat(BinaryOperation.copyOfRange(bb, 16, 20));

                    setCoefs(b0, b1, b2, a1, a2);

                    Log.d(TAG, String.format(Locale.getDefault(), "Biquad=0x%x import success", getAddress()));
                    return true;
                }
            }
        }
        return false;
    }

    public float getAFR(float freqX) {
        if (enabled) {
            double w = 2.0 * Math.PI * freqX / 96000;
            Complex Z1 = Complex.trigonometricForm(1, w).reciprocal();
            Complex Z2 = Z1.mul(Z1);

            /* Z(f) = e^(i * w(f))
             *   H(f) = (b0 + b1 * Z^-1 + b2 * Z^-2) / (1 + a1 * Z^-1 + a2 * Z^-2)
             */
            Complex num = new Complex(b0, 0).add(new Complex(b1, 0).mul(Z1)).add(new Complex(b2, 0).mul(Z2));
            Complex denom = new Complex(1, 0).add(new Complex(-a1, 0).mul(Z1)).add(new Complex(-a2, 0).mul(Z2));
            Complex H = num.div(denom);

            return (float)H.mod(); //ampl
        }
        return 1.0f;
    }

    @Override
    public String toString() {
        if (enabled) {
            return String.format(Locale.getDefault(), "b0:%f b1:%f b2:%f a1:%f a2:%f", b0, b1, b2, a1, a2);
        }
        return "Biquad disabled.";
    }

    /*@Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();

        xmlData.addXmlElement("addr0", addr0);
        xmlData.addXmlElement("addr1", addr1);

        xmlData.addXmlElement("b0", b0);
        xmlData.addXmlElement("b1", b1);
        xmlData.addXmlElement("b2", b2);
        xmlData.addXmlElement("a1", a1);
        xmlData.addXmlElement("a2", a2);

        XmlData biquadXmlData = new XmlData();
        biquadXmlData.addXmlElement("Biquad", xmlData);

        return biquadXmlData;
    }

    @Override
    public void importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String elementName = null;
        int count = 0;

        float b0 = 1.0f, b1 = 0, b2 = 0, a1 = 0, a2 = 0;

        do {
            xmlParser.next();

            if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                elementName = xmlParser.getName();
            }
            if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                if (xmlParser.getName().equals("Biquad")) break;

                elementName = null;
            }

            if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){
                String elementValue = xmlParser.getText();
                if (elementValue == null) continue;

                if (elementName.equals("addr0")){
                    addr0 = Byte.parseByte(elementValue);
                    count++;
                }
                if (elementName.equals("addr1")){
                    addr1 = Byte.parseByte(elementValue);
                    count++;
                }

                if (elementName.equals("b0")){
                    b0 = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("b1")){
                    b1 = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("b2")){
                    b2 = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("a1")){
                    a1 = Float.parseFloat(elementValue);
                    count++;
                }
                if (elementName.equals("a2")){
                    a2 = Float.parseFloat(elementValue);
                    count++;
                }
            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        //check import result
        if (count != 7){
            String msg = "Biquad=" + Integer.toString(addr0) + ". Import from xml is not success.";
            Log.d(TAG, msg);
            throw new IOException(msg);
        }
        setCoefs(b0, b1, b2 ,a1, a2);
        Log.d(TAG, toString());

    }*/


}

