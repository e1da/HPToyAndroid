/*
 *   Filter.java
 *
 *   Created by Artem Khlyupin on 08/03/2019
 *   Copyright © 2019-2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.filter;

import android.support.annotation.NonNull;
import android.util.Log;


import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.HiFiToyObject;
import com.hifitoy.hifitoyobjects.biquad.AllpassBiquad;
import com.hifitoy.hifitoyobjects.biquad.BandpassBiquad;
import com.hifitoy.hifitoyobjects.biquad.Biquad;
import com.hifitoy.hifitoyobjects.biquad.HighpassBiquad;
import com.hifitoy.hifitoyobjects.biquad.IFreq;
import com.hifitoy.hifitoyobjects.biquad.LowpassBiquad;
import com.hifitoy.hifitoyobjects.biquad.ParamBiquad;
import com.hifitoy.hifitoyobjects.biquad.TextBiquad;
import com.hifitoy.hifitoyobjects.biquad.Type;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_BANDPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_OFF;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_PARAMETRIC;
import static com.hifitoy.hifitoyobjects.biquad.Type.BIQUAD_USER;
import static com.hifitoy.hifitoyobjects.filter.Order.FILTER_ORDER_0;

public class Filter implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";
    private static final int SIZE = 7;

    protected final Biquad[] biquads = new Biquad[SIZE];

    protected byte    activeBiquadIndex;
    protected boolean activeNullLP;
    protected boolean activeNullHP;

    public Filter(Filter f) throws CloneNotSupportedException {
        for (int i = 0; i < SIZE; i++) {
            biquads[i] = f.biquads[i].clone();
        }

        activeBiquadIndex = f.activeBiquadIndex;
        activeNullLP = f.activeNullLP;
        activeNullHP = f.activeNullHP;
    }

    public Filter(byte addr, byte bindAddr) {

        for (byte i = 0; i < SIZE; i++) {
            ParamBiquad pb = new ParamBiquad((byte)(addr + i),
                    (bindAddr == 0) ? 0 : (byte)(bindAddr + i));
            pb.setFreq((short)(100 * (i + 1)));

            biquads[i] = pb;
        }

        activeBiquadIndex = 0;
    }

    public Filter(byte addr) {
        this(addr, (byte)0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filter filters = (Filter) o;
        return Arrays.equals(biquads, filters.biquads);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(biquads);
    }

    public boolean dataEquals(Filter f) {
        for (int i = 0; i < SIZE; i++) {
            if (!biquads[i].dataEquals(f.biquads[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Filter clone() throws CloneNotSupportedException {
        return new Filter(this);
    }

    //getters / setters
    public boolean isActiveNullHP() {
        return activeNullHP;
    }
    public void setActiveNullHP(boolean active) {
        activeNullHP = active;
        if (active) activeNullLP = false;
    }
    public boolean isActiveNullLP() {
        return activeNullLP;
    }
    public void setActiveNullLP(boolean active) {
        activeNullLP = active;
        if (active) activeNullHP = false;
    }

    public void setActiveBiquadIndex(byte index) {
        if ((index >= 0) && (index < biquads.length) ) {
            activeBiquadIndex = index;
        }
    }
    public byte getActiveBiquadIndex() {
        return activeBiquadIndex;
    }

    public int getBiquadLength() {
        return SIZE;
    }

    public void setBiquad(byte index, Biquad b) {
        if ((index >= 0) && (index < biquads.length) ) {
            biquads[index] = b;
        }
    }

    public Biquad getBiquad(byte index) {
        if ((index >= 0) && (index < biquads.length) ) {
            return biquads[index];
        }
        return null;
    }
    public Biquad getActiveBiquad() {
        return getBiquad(activeBiquadIndex);
    }

    Biquad[] getBiquads() {
        return biquads;
    }

    public byte getBiquadIndex(Biquad b) {
        for (byte i = 0; i < biquads.length; i++) {
            if (biquads[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public byte[] getBiquadTypes() {
        byte[] types = new byte[biquads.length];
        for (byte i = 0; i < biquads.length; i++) {
            types[i] = Type.getType(biquads[i]);
        }
        return types;
    }

    //logic
    public void incActiveBiquadIndex() {
        if (++activeBiquadIndex > 6) activeBiquadIndex = 0;
        activeNullHP = false;
        activeNullLP = false;
    }
    public void decActiveBiquadIndex() {
        if (activeBiquadIndex == 0) {
            activeBiquadIndex = 6;
        } else {
            activeBiquadIndex--;
        }
        activeNullHP = false;
        activeNullLP = false;
    }

    public void nextActiveBiquadIndex() {
        byte type = Type.getType(getActiveBiquad());
        byte nextType;
        Biquad b;
        int counter = 0;

        do {
            if (++counter > 7) break;

            activeBiquadIndex++;
            if (activeBiquadIndex > 6) activeBiquadIndex = 0;

            b = getActiveBiquad();
            nextType = Type.getType(b);

        } while (((type == BIQUAD_LOWPASS) && (nextType == BIQUAD_LOWPASS)) ||
                ((type == BIQUAD_HIGHPASS) && (nextType == BIQUAD_HIGHPASS)) ||
                (!b.isEnabled()));

    }
    public void prevActiveBiquadIndex() {
        byte type = Type.getType(getActiveBiquad());
        byte nextType;
        Biquad b;
        int counter = 0;

        do {
            if (++counter > 7) break;

            activeBiquadIndex--;
            if (activeBiquadIndex < 0) activeBiquadIndex = 6;

            b = getActiveBiquad();
            nextType = Type.getType(b);

        } while (((type == BIQUAD_LOWPASS) && (nextType == BIQUAD_LOWPASS)) ||
                ((type == BIQUAD_HIGHPASS) && (nextType == BIQUAD_HIGHPASS)) ||
                (!b.isEnabled()));

    }


    public List<Biquad> getBiquads(byte typeValue) {
        List<Biquad> biquads = new ArrayList<>();

        for (byte i = 0 ; i < 7; i++) {
            Biquad b = getBiquad(i);

            if (Type.getType(b) == typeValue) {
                biquads.add(b);
            }
        }
        return biquads;
    }

    protected Biquad getFreeBiquad() {
        List<Biquad> offBiquads = getBiquads(BIQUAD_OFF);
        if (offBiquads.size() > 0) {
            return offBiquads.get(0);
        }

        List<Biquad> paramBiquads = getBiquads(BIQUAD_PARAMETRIC);
        if (paramBiquads.size() > 0) {

            //try find parametric with db == 0
            for (int i = 0; i < paramBiquads.size(); i++) {
                ParamBiquad pb = (ParamBiquad) paramBiquads.get(i);

                if (FloatUtility.isFloatNull(pb.getDbVolume())) {
                    return pb;
                }
            }

            //find parametric with min abs db
            ParamBiquad pb = (ParamBiquad) paramBiquads.get(0);
            for (int i = 1; i < paramBiquads.size(); i++) {
                ParamBiquad current = (ParamBiquad) paramBiquads.get(i);
                if ( (Math.abs(pb.getDbVolume())) > (Math.abs(current.getDbVolume())) ) {
                    pb = current;
                }
            }
            return pb;
        }

        List<Biquad> allpassBiquads = getBiquads(BIQUAD_ALLPASS);
        if (allpassBiquads.size() > 0) {
            return allpassBiquads.get(0);
        }

        return null;
    }

    /*public LPFilter getLowpass() {
        return (LPFilter) this;
    }

    public HPFilter getHighpass() {
        return (HPFilter) this;
    }

    private boolean isLowpassFull() {
        List<Biquad> lpBiquads = getBiquads(BIQUAD_LOWPASS);
        return lpBiquads.size() >= 2;
    }
    private boolean isHighpassFull() {
        List<Biquad> hpBiquads = getBiquads(BIQUAD_HIGHPASS);
        return hpBiquads.size() >= 2;
    }*/

    /*public void upOrderFor(byte type) {
        short freq;

        //check type and get freq
        if (type == BIQUAD_LOWPASS) {
            if (isLowpassFull()) return;
            PassFilter lp = getLowpass();
            freq = (lp != null) ? lp.getFreq() : 20000;
        } else if (type == BIQUAD_HIGHPASS) {
            if (isHighpassFull()) return;
            PassFilter hp = getHighpass();
            freq = (hp != null) ? hp.getFreq() : 20;
        } else {
            return;
        }

        List<Biquad> biquads = getBiquads(type);

        if (biquads.size() < 2) {//need 1 biquad
            Biquad b = getFreeBiquad();
            if (b != null) {
                b.setEnabled(true);
                b.getParams().setTypeValue(type);
                b.getParams().setFreq(freq);
            }
        } else {

            return;
        }

        //get biquads
        biquads = getBiquads(type);
        //set active biquad index
        byte index = getBiquadIndex(biquads.get(0));
        if (index != -1) {
            activeBiquadIndex = index;
            if ((type == BIQUAD_LOWPASS) && (activeNullLP)) activeNullLP = false;
            if ((type == BIQUAD_HIGHPASS) && (activeNullHP)) activeNullHP = false;
        }

        //update lp biquads with order
        PassFilter p = new PassFilter(biquads, type);
        p.sendToPeripheral(true);

    }

    public void downOrderFor(byte type) {
        if ((type != BIQUAD_LOWPASS) && (type != BIQUAD_HIGHPASS)) return;

        List<Biquad> biquads = getBiquads(type);
        if (biquads.size() == 0) return;

        int s;

        if (biquads.size() > 4) {
            s = 4;
        } else if (biquads.size() > 2) {
            s = 2;
        } else if (biquads.size() > 1) {
            s = 1;
        } else {
            s = 0;
        }

        //free excess biquads from LP and set to Parametric
        for (int i = s; i < biquads.size(); i++) {
            Biquad b = biquads.get(i);

            b.setEnabled(isPEQEnabled());
            b.getParams().setTypeValue(BIQUAD_PARAMETRIC);

            short freq = getBetterNewFreqForBiquad(b);
            b.getParams().setFreq((freq != -1) ? freq : 100);

            b.getParams().setQFac(1.41f);
            b.getParams().setDbVolume(0.0f);

            b.sendToPeripheral(true);
        }

        //get lp biquads
        biquads = getBiquads(type);
        if (biquads.size() == 0) {
            if (type == BIQUAD_LOWPASS) activeNullLP = true;
            if (type == BIQUAD_HIGHPASS) activeNullHP = true;
        }

        //update lp biquads with order
        PassFilter p = new PassFilter(biquads, type);
        p.sendToPeripheral(true);
    }*/

    public boolean isBiquadEnabled(byte biquadType) {
        boolean result = true;

        List<Biquad> biquads = getBiquads(biquadType);
        if (biquads.size() == 0) return false;

        for (int i = 0; i < biquads.size(); i++) {
            Biquad b = biquads.get(i);
            if (!b.isEnabled()) {
                result = false;
                break;
            }
        }

        if (!result) {
            for (int i = 0; i < biquads.size(); i++) {
                Biquad b = biquads.get(i);
                if (b.isEnabled()) {
                    b.setEnabled(false);
                    b.sendToPeripheral(true);
                }
            }
        }
        return result;
    }

    public void setBiquadEnabled(byte biquadType, boolean enabled) {
        List<Biquad> biquads = getBiquads(biquadType);

        for (int i = 0; i < biquads.size(); i++) {
            Biquad b = biquads.get(i);
            if (b.isEnabled() != enabled) {
                b.setEnabled(enabled);
                b.sendToPeripheral(true);
            }
        }

        if (!getActiveBiquad().isEnabled()) nextActiveBiquadIndex();
    }

    public short getBetterNewFreqForBiquad(Biquad b) {
        ArrayList<Short> freqs = new ArrayList<>();
        short freq = -1;

        //get freqs from all params, hp, lp
        for (int i = 0; i < 7; i++) {
            if ((b != null) && (biquads[i] == b)) continue;

            if (IFreq.class.isAssignableFrom(biquads[i].getClass())) {
                short f = ((IFreq)biquads[i]).getFreq();
                freqs.add(f);
            }
        }
        LowpassFilter lpf = new LowpassFilter(this);
        HighpassFilter hpf = new HighpassFilter(this);
        if (lpf.getOrder() == FILTER_ORDER_0) freqs.add((short)20000);
        if (hpf.getOrder() == FILTER_ORDER_0) freqs.add((short)20);

        if (freqs.size() < 2) return freq;
        Collections.sort(freqs);

        //get max delta freq
        float maxDLogFreq = 0;
        for (int i = 0; i < freqs.size() - 1; i++){
            short f0 = freqs.get(i);
            short f1 = freqs.get(i + 1);

            if (Math.log10(f1) - Math.log10(f0) > maxDLogFreq){
                maxDLogFreq = (float)(Math.log10(f1) - Math.log10(f0));
                //freq calculate
                double log_freq = Math.log10(f0) + maxDLogFreq / 2;
                freq = (short)Math.pow(10, log_freq);
            }
        }
        return freq;
    }


    //get AMPL FREQ response
    public float getAFR(float freqX) {
        float resultAFR = 1.0f;

        for (int i = 0; i < 7; i++) {
            resultAFR *= biquads[i].getAFR(freqX);
        }
        return resultAFR;
    }

    /*============================= HiFiToyObject protocol implements ===================================*/
    @Override
    public byte getAddress() {
        return biquads[0].getAddress();
    }

    @NonNull
    @Override
    public String toString() {
        return "Filters is 7 biquads";
    }

    @Override
    public void sendToPeripheral(boolean response) {
        for (int i = 0; i < SIZE; i++) {
            biquads[i].sendToPeripheral(true);
        }
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();

        for (int i = 0; i < SIZE; i++) {
            l.addAll(biquads[i].getDataBufs());
        }
        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        for (int i = 0; i < SIZE; i++) {
            Biquad b = new Biquad(biquads[i]);
            if (b.importFromDataBufs(dataBufs)) {
                byte type = Type.getType(b);

                if (type == BIQUAD_LOWPASS) {
                    biquads[i] = new LowpassBiquad(b);
                } else if (type == BIQUAD_HIGHPASS) {
                    biquads[i] = new HighpassBiquad(b);
                } else if (type == BIQUAD_PARAMETRIC) {
                    biquads[i] = new ParamBiquad(b);
                } else if (type == BIQUAD_ALLPASS) {
                    biquads[i] = new AllpassBiquad(b);
                } else if (type == BIQUAD_BANDPASS) {
                    biquads[i] = new BandpassBiquad(b);
                } else if (type == BIQUAD_USER) {
                    biquads[i] = new TextBiquad(b);
                }

            } else {
                return false;
            }
        }

        Log.d(TAG, "Filters import success.");
        return true;
    }

    /*@Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();

        for (int i = 0; i < 7; i++) {
            xmlData.addXmlData(biquads[i].toXmlData());
        }

        XmlData filtersXmlData = new XmlData();
        /*Map<String, String> attrib = new HashMap<>();
        attrib.put("Address", ByteUtility.toString(address0));
        attrib.put("Address1", ByteUtility.toString(address1));*/

    /*    filtersXmlData.addXmlElement("Filters", xmlData, null);
        return filtersXmlData;
    }

    @Override
    public void importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        String elementName = null;
        int count = 0;

        do {
            xmlParser.next();

            if (xmlParser.getEventType() == XmlPullParser.START_TAG){
                elementName = xmlParser.getName();

                if (elementName.equals("Biquad")) {
                    String addrStr = xmlParser.getAttributeValue(null, "Address");
                    if (addrStr == null) continue;
                    byte addr = ByteUtility.parse(addrStr);

                    for (int i = 0; i < 7; i++) {
                        if (biquads[i].getAddress() == addr) {
                            biquads[i].importFromXml(xmlParser);
                            count++;
                        }
                    }
                }
            }
            if (xmlParser.getEventType() == XmlPullParser.END_TAG){
                if (xmlParser.getName().equals("Filters")) break;

                elementName = null;
            }

            if ((xmlParser.getEventType() == XmlPullParser.TEXT) && (elementName != null)){

            }
        } while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT);

        //check import result
        if (count != 7){
            String msg = "Filters=" + getAddress() +
                    ". Import from xml is not success.";
            Log.d(TAG, msg);
            throw new IOException(msg);
        }
        Log.d(TAG, toString());
    }*/
}
