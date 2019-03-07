/*
 *   Biquad.java
 *
 *   Created by Artem Khlyupin on 06/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.hifitoynumbers.Number523;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;

public class Biquad implements HiFiToyObject {
    private final int FS = 96000;

    private boolean hiddenGui;
    private boolean enabled;

    private byte address0;
    private byte address1; //if == 0 then off else stereo (2nd channel)

    private BiquadParam params;

    public Biquad(byte address0, byte address1) {
        hiddenGui = false;
        enabled = true;

        this.address0 = address0;
        this.address1 = address1;

        params = new BiquadParam();
    }
    public Biquad(byte address0) {
        this(address0, (byte)0);
    }
    public Biquad() {
        this((byte)0, (byte)0);
    }

    //setters getters
    @Override
    public int getAddress() {
        return address0;
    }

    public BiquadParam getParams() {
        return params;
    }

    @Override
    public void sendToPeripheral(boolean response) {

        if (params.type.getValue() == BiquadParam.Type.BIQUAD_USER) {
            ByteBuffer b = ByteBuffer.allocate(22).order(ByteOrder.LITTLE_ENDIAN);
            b.put(address0);
            b.put(address1);

            b.putFloat(params.b0);
            b.putFloat(params.b1);
            b.putFloat(params.b2);
            b.putFloat(params.a1);
            b.putFloat(params.a2);

            HiFiToyControl.getInstance().sendDataToDsp(b.array(), true);

        } else {

            ByteBuffer b = ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN);
            b.put(address0);
            b.put(address1);

            if (enabled) {
                b.put(params.order.getValue());
                b.put(params.type.getValue());
            } else {
                b.put(BiquadParam.Order.BIQUAD_ORDER_2);
                b.put(BiquadParam.Type.BIQUAD_OFF);
            }
            b.putShort(params.freq);
            b.putFloat(params.qFac);
            b.putFloat(params.dbVolume);

            HiFiToyControl.getInstance().sendDataToDsp(b.array(), response);
        }
    }

    @Override
    public byte[] getBinary() {

        ByteBuffer data = params.getBinary();
        HiFiToyDataBuf dataBuf = new HiFiToyDataBuf(address0, data);

        ByteBuffer b;

        if (address1 != 0) {
            b = ByteBuffer.allocate(2 * dataBuf.getBinary().capacity());
            b.put(dataBuf.getBinary());
            b.put(new HiFiToyDataBuf(address1, data).getBinary());
        } else {
            b = dataBuf.getBinary();
        }

        return b.array();
    }

    //we get data with length==20
    @Override
    public boolean importData(byte[] data) {
        return params.importData(data);
    }
    public boolean importData1(ArrayList<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);

            if ((buf.getAddr() == address0) && (buf.getLength() == 20)) {
                return params.importData(buf.getData().array());
            }
        }
        return false;
    }

    public float getAFR(float freqX) {
        if ( (!hiddenGui) && (enabled) ) {
            return params.getAFR(freqX);
        }
        return 1.0f;
    }
    public String getInfo() {
        if (enabled) {
            return params.getInfo();
        }
        return "Biquad disabled.";
    }

    public class BiquadParam implements Cloneable, Serializable {
        private Order order;
        private Type type;

        //biquad coefs
        private float b0;
        private float b1;
        private float b2;
        private float a1;
        private float a2;

        //macro param
        private short freq;
        private float qFac;
        private float dbVolume;

        //border property
        private short maxFreq;
        private short minFreq;
        private float maxQ;
        private float minQ;
        private float maxDbVolume;
        private float minDbVolume;

        public BiquadParam() {
            order = new Order();
            type = new Type();

            setBorderFreq((short)20000, (short)20);
            setBorderQ(10.0f, 0.1f);
            setBorderDbVolume(12.0f, -36.0f);

            freq = 100;
            qFac = 1.41f;
            dbVolume = 0.0f;
            update(freq, qFac, dbVolume);
        }

        public BiquadParam(byte orderValue, byte typeValue, short freq, float qFac, float dbVolume) {
            this();

            order.setValue(orderValue);
            type.setValue(typeValue);

            this.freq = freq;
            this.qFac = qFac;
            this.dbVolume = dbVolume;
            checkBorders();

            update(this.freq, this.qFac, this.dbVolume);
        }

        public BiquadParam(byte orderValue, byte typeValue, float b0, float b1, float b2, float a1, float a2) {
            this();

            order.setValue(orderValue);
            type.setValue(typeValue);

            setCoefs(b0, b1, b2, a1, a2);
        }

        @Override
        public BiquadParam clone() throws CloneNotSupportedException{
            return (BiquadParam) super.clone();
        }

        //setters getters
        public void setOrderValue(byte value) {
            order.setValue(value);
            update(freq, qFac, dbVolume);
        }
        public byte getOrderValue() {
            return order.getValue();
        }
        public void setTypeValue(byte value) {
            type.setValue(value);
            update(freq, qFac, dbVolume);
        }
        public byte getTypeValue() {
            return type.getValue();
        }

        public void setCoefs(float b0, float b1, float b2, float a1, float a2) {
            this.b0 = b0;
            this.b1 = b1;
            this.b2 = b2;
            this.a1 = a1;
            this.a2 = a2;

            update(b0, b1, b2, a1, a2);
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

        public void setFreq(short freq) {
            if (freq < minFreq) freq = minFreq;
            if (freq > maxFreq) freq = maxFreq;

            this.freq = freq;
            update(freq, qFac, dbVolume);
        }
        public short getFreq() {
            return freq;
        }
        public void setQFac(float qFac) {
            if (qFac < minQ) qFac = minQ;
            if (qFac > maxQ) qFac = maxQ;

            this.qFac = qFac;
            update(freq, qFac, dbVolume);
        }
        public float getQFac() {
            return qFac;
        }
        public void setDbVolume(float dbVolume) {
            if (dbVolume < minDbVolume) dbVolume = minDbVolume;
            if (dbVolume > maxDbVolume) dbVolume = maxDbVolume;

            this.dbVolume = dbVolume;
            update(freq, qFac, dbVolume);
        }
        public float getDbVolume() {
            return dbVolume;
        }


        //border setter function
        public void setBorderFreq(short maxFreq, short minFreq) {
            this.maxFreq = maxFreq;
            this.minFreq = minFreq;
        }
        public void setBorderQ(float maxQ, float minQ) {
            this.maxQ = maxQ;
            this.minQ = minQ;
        }
        public void setBorderDbVolume(float maxDbVolume, float minDbVolume) {
            this.maxDbVolume = maxDbVolume;
            this.minDbVolume = minDbVolume;
        }
        public void checkBorders() {
            if (freq < minFreq) freq = minFreq;
            if (freq > maxFreq) freq = maxFreq;
            if (qFac < minQ) qFac = minQ;
            if (qFac > maxQ) qFac = maxQ;
            if (dbVolume < minDbVolume) dbVolume = minDbVolume;
            if (dbVolume > maxDbVolume) dbVolume = maxDbVolume;
        }

        private void update(float b0, float b1, float b2, float a1, float a2) {
            float arg, w0;

            if (order.value == Order.BIQUAD_ORDER_2){
                switch (type.value){
                    case Type.BIQUAD_LOWPASS:
                        arg = 2 * b1 / a1 + 1;
                        if ((arg < 1.0f) && (arg > -1.0f)) break;

                        w0 = (float)Math.acos(1.0f / arg);
                        freq = (short)Math.round(w0 * FS / (2 * Math.PI));
                        qFac = (float)(Math.sin(w0) * a1 / (2 * (2 * Math.cos(w0) - a1)));
                        break;

                    case Type.BIQUAD_HIGHPASS:
                        arg = 2 * b1 / a1 + 1;
                        if ((arg < 1.0f) && (arg > -1.0f)) break;

                        w0 = (float)Math.acos(-1.0f / arg);
                        freq = (short)Math.round(w0 * FS / (2 * Math.PI));
                        qFac = (float)(Math.sin(w0) * a1 / (2 * (2 * Math.cos(w0) - a1)));
                        break;

                    case Type.BIQUAD_PARAMETRIC:
                        arg = a1 / (b0 + b2);
                        if ((arg > 1.0f) || (arg < -1.0f)) break;

                        w0 = (float)Math.acos(arg);
                        freq = (short)Math.round(w0 * FS / (2 * Math.PI));

                        arg = (float)((b0 * 2 * Math.cos(w0) - a1) / (2 * Math.cos(w0) - a1));
                        if (arg < 0.0) break;

                        double ampl = Math.sqrt(arg);
                        dbVolume = (float)(40 * Math.log10(ampl));

                        double alpha = (2 * Math.cos(w0) / a1 - 1) * ampl;
                        qFac = (float)(Math.sin(w0) / (2 * alpha));
                        break;

                    case Type.BIQUAD_ALLPASS:
                        arg = a1 / (b0 + 1);
                        if ((arg > 1.0f) || (arg < -1.0f)) break;

                        w0 = (float)Math.acos(arg);
                        freq = (short)Math.round(w0 * FS / (2 * Math.PI));
                        qFac = (float)(Math.sin(w0) * a1 / (2 * (2 * Math.cos(w0) - a1)));
                        break;

                    case Type.BIQUAD_BANDPASS:
                        w0 = (float)(Math.acos(a1 / 2 * (1 + b0 / (1 - b0))));
                        freq = (short)(w0 * (float)FS / (2 * Math.PI));
                        //TODO set import bandwidth
                        break;

                    case Type.BIQUAD_OFF:
                    case Type.BIQUAD_USER:
                    default:
                        break;
                }
            } else {//BIQUAD_ORDER_1

                if (a1 > 0) {
                    w0 = (float)(-Math.log10(a1) / Math.log10(2.7));
                    freq = (short)Math.round(w0 * FS / (2 * Math.PI));
                }
            }
        }
        private void update(short freq, float qFac, float dbVolume) {
            float w0 = 2 * (float)Math.PI * freq / FS;
            float ampl;
            float bandwidth = 1.41f;
            float alpha, a0;

            if (type.value == Type.BIQUAD_USER) return;

            float s = (float)Math.sin(w0), c = (float)Math.cos(w0);

            if (order.value == Order.BIQUAD_ORDER_2){
                switch (type.value){
                    case Type.BIQUAD_LOWPASS:
                        alpha = s / (2 * qFac);
                        a0 =  1 + alpha;
                        a1 =  2 * c / (a0);
                        a2 =  (1 - alpha) / (-a0);
                        b0 =  (1 - c) / (2 * a0);
                        b1 =  (1 - c) / a0;
                        b2 =  (1 - c) / (2 * a0);
                        break;
                    case Type.BIQUAD_HIGHPASS:
                        alpha = s / (2 * qFac);
                        a0 =  1 + alpha;
                        a1 =  2 * c / (a0);
                        a2 =  (1 - alpha) / (-a0);
                        b0 =  (1 + c) / (2 * a0);
                        b1 =  (1 + c) / (-a0);
                        b2 =  (1 + c) / (2 * a0);
                        break;
                    case Type.BIQUAD_PARAMETRIC:
                        ampl = (float)Math.pow(10, dbVolume / 40);
                        alpha = s / (2 * qFac);
                        a0 =  1 + alpha / ampl;
                        a1 =  2 * c / a0;
                        a2 =  (1 - alpha / ampl) / (-a0);
                        b0 =  (1 + alpha * ampl) / a0;
                        b1 =  (2 * c) / (-a0);
                        b2 =  (1 - alpha * ampl) / a0;
                        break;
                    case Type.BIQUAD_ALLPASS:
                        alpha = s / (2 * qFac);
                        a0 =   1 + alpha;
                        a1 =  2 * c / (a0);
                        a2 =  (1 - alpha) / (-a0);
                        b0 =  (1 - alpha) / a0;
                        b1 =  2 * c / (-a0);
                        b2 =  (1 + alpha) / a0;
                        break;
                    case Type.BIQUAD_BANDPASS:
                        //ln(2) / 2 = 0.3465735902
                        alpha = (float)( s * Math.sinh( 0.3465735902 * bandwidth * w0 / s) );

                        a0 =   1 + alpha;
                        a1 =   2 * c / a0;
                        a2 =   (1 - alpha) / (-a0);
                        b0 =   alpha / a0;
                        b1 =   0;
                        b2 =  -alpha / a0;
                        break;
                    case Type.BIQUAD_OFF:
                        b0 =  1.0f;
                        b1 =  0.0f;
                        b2 =  0.0f;
                        a1 =  0.0f;
                        a2 =  0.0f;
                        break;
                    default:
                        break;
                }
            } else {//order == BIQUAD_ORDER_1
                a2 = 0;
                b2 = 0;

                switch (type.value){
                    case Type.BIQUAD_LOWPASS:
                        a1 = (float)Math.pow(2.7, -w0);
                        b0 = 1.0f - a1;
                        //WARNING b1 = ???
                        break;
                    case Type.BIQUAD_HIGHPASS:
                        a1 = (float)Math.pow(2.7, -w0);
                        b0 = a1;
                        b1 = -a1;
                        break;
                    case Type.BIQUAD_ALLPASS:
                        a1 = (float)Math.pow(2.7, -w0);
                        b0 = -a1;
                        b1 = 1.0f;
                        break;
                    case Type.BIQUAD_OFF:
                        b0 =  1.0f;
                        b1 =  0.0f;
                        b2 =  0.0f;
                        a1 =  0.0f;
                        a2 =  0.0f;
                        break;
                    default:
                        break;
                }

            }
        }
        private void updateOrder() {

            FloatUtility.isFloatNull(b2);

            if ( (FloatUtility.isFloatNull(b2)) && (FloatUtility.isFloatNull(a2)) &&
                    (!FloatUtility.isFloatNull(b0)) && (!FloatUtility.isFloatNull(b1)) &&
                    (!FloatUtility.isFloatNull(a1)) ) {
                order.setValue(Order.BIQUAD_ORDER_1);
            } else {
                order.setValue(Order.BIQUAD_ORDER_2);
            }
        }

        //get binary
        public ByteBuffer getBinary() {
            ByteBuffer b = ByteBuffer.allocate(20);
            b.put(Number523.get523BigEnd(params.b0));
            b.put(Number523.get523BigEnd(params.b1));
            b.put(Number523.get523BigEnd(params.b2));
            b.put(Number523.get523BigEnd(params.a1));
            b.put(Number523.get523BigEnd(params.a2));

            return b;
        }
        //import from binary
        public boolean importData(byte[] data) {
            if (data.length == 20) {
                ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
                b0 = Number523.toFloat(b.put(data, 0, 4));
                b1 = Number523.toFloat(b.put(data, 4, 4));
                b2 = Number523.toFloat(b.put(data, 8, 4));
                a1 = Number523.toFloat(b.put(data, 12, 4));
                a2 = Number523.toFloat(b.put(data, 16, 4));

                updateOrder();
                update(b0, b1, b2, a1, a2);
                return true;
            }

            return false;
        }


        //math calculations
        private float getLPF(float freqX) {
            return (float)Math.sqrt(1.0f/(  Math.pow(1 - Math.pow(freqX / freq, 2), 2) + Math.pow(freqX / (freq * qFac), 2)    ));
        }
        private float getHPF(float freqX) {
            return (float)(     Math.sqrt(
                                    Math.pow( Math.pow(freqX / freq , 4) - Math.pow(freqX / freq, 2) , 2) +
                                    Math.pow(freqX / freq, 6) / Math.pow(qFac, 2)) /
                                (Math.pow(1 - Math.pow(freqX / freq, 2), 2) + Math.pow(freqX / (qFac * freq), 2)) );
        }
        private float getPEQ(float freqX) {
            double Ampl = Math.pow(10, dbVolume / 40);
            double A1 = Math.pow(1 - Math.pow(freqX / freq, 2), 2) + Math.pow(freqX / (qFac * freq), 2);
            double A2 =     (1 - Math.pow(freqX / freq, 2)) *
                            (freqX * Ampl / (qFac * freq) - freqX / (Ampl * qFac * freq));

            double B = Math.pow(1 - Math.pow(freqX / freq, 2), 2) + Math.pow(freqX / (Ampl * qFac * freq), 2);


            return (float)( Math.sqrt(Math.pow(A1, 2) + Math.pow(A2, 2)) / B );
        }
        public float getAFR(float freqX) {
            if (order.value == Order.BIQUAD_ORDER_2) {

                switch (type.value) {
                    case Type.BIQUAD_LOWPASS:
                        return getLPF(freqX);
                    case Type.BIQUAD_HIGHPASS:
                        return getHPF(freqX);
                    case Type.BIQUAD_PARAMETRIC:
                        return getPEQ(freqX);
                    case Type.BIQUAD_OFF:
                        return 1.0f;
                    default:
                        return 1.0f;
                }
            }
            return 1.0f;
        }

        //get info
        public String getInfo() {
            switch (type.value) {
                case Type.BIQUAD_LOWPASS:
                case Type.BIQUAD_HIGHPASS:
                case Type.BIQUAD_BANDPASS:
                case Type.BIQUAD_ALLPASS:
                    return Short.toString(freq) + "Hz";

                case Type.BIQUAD_PARAMETRIC:
                    return String.format(Locale.getDefault(),
                            "%dHz Q:%.2f dB:%.1f", freq, qFac, dbVolume);
                case Type.BIQUAD_USER:
                    return String.format(Locale.getDefault(),
                            "b0:%f b1:%f b2:%f a1:%f a2:%f", b0, b1, b2, a1, a2);
                case Type.BIQUAD_OFF:
                        return "Biquad off.";
            }
            return "";
        }

        //utility classes
        public class Type implements Cloneable, Serializable{
            public final static byte BIQUAD_LOWPASS       = 2;
            public final static byte BIQUAD_HIGHPASS      = 1;
            public final static byte BIQUAD_OFF           = 0;
            public final static byte BIQUAD_PARAMETRIC    = 3;
            public final static byte BIQUAD_ALLPASS       = 4;
            public final static byte BIQUAD_BANDPASS      = 5;
            public final static byte BIQUAD_USER          = 6;
            public final static byte BIQUAD_DEFAULT       = BIQUAD_PARAMETRIC;

            private byte value;

            public Type() {
                value = BIQUAD_DEFAULT;
            }
            public void setValue(byte value) {
                if (value > BIQUAD_USER) value = BIQUAD_USER;
                if (value < BIQUAD_OFF) value = BIQUAD_OFF;

                this.value = value;
            }
            public byte getValue() {
                return value;
            }
        }

        public class Order implements Cloneable, Serializable{
            public final static byte BIQUAD_ORDER_1 = 0;
            public final static byte BIQUAD_ORDER_2 = 1;

            private byte value;

            public Order() {
                value = BIQUAD_ORDER_2;
            }
            public void setValue(byte value) {
                if (value > BIQUAD_ORDER_2) value = BIQUAD_ORDER_2;
                if (value < BIQUAD_ORDER_1) value = BIQUAD_ORDER_1;

                this.value = value;
            }
            public byte getValue() {
                return value;
            }
        }
    }


}

