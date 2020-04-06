/*
 *   Filters.java
 *
 *   Created by Artem Khlyupin on 08/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import android.util.Log;

import com.hifitoy.hifitoynumbers.ByteUtility;
import com.hifitoy.hifitoynumbers.FloatUtility;
import com.hifitoy.tas5558.TAS5558;
import com.hifitoy.xml.XmlData;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_ALLPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_BANDPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_HIGHPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_LOWPASS;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_OFF;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;
import static com.hifitoy.tas5558.TAS5558.BIQUAD_FILTER_REG;

public class Filters implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private Biquad[] biquads; // 7 biquads

    private byte address0;
    private byte address1; //if == 0 then off else stereo (2nd channel)

    private byte    activeBiquadIndex;
    private boolean activeNullLP;
    private boolean activeNullHP;

    public Filters(byte address0, byte address1) {
        this.address0 = address0;
        this.address1 = address1;

        biquads = new Biquad[7];

        for (byte i = 0; i < 7; i++) {
            biquads[i] = new Biquad((byte)(address0 + i), (address1 != 0) ? (byte)(address1 + i) : (byte)0);

            Biquad.BiquadParam p = biquads[i].getParams();
            p.setBorderFreq((short)20000, (short)20);

            p.setTypeValue(Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC);
            p.setFreq((short)(100 * (i + 1)));
            p.setQFac(1.41f);
            p.setDbVolume(0.0f);
        }

        activeBiquadIndex = 0;
    }
    public Filters() {
        this(BIQUAD_FILTER_REG, (byte)(TAS5558.BIQUAD_FILTER_REG + 7));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filters filters = (Filters) o;
        return address0 == filters.address0 &&
                address1 == filters.address1 &&
                Arrays.equals(biquads, filters.biquads);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(address0, address1);
        result = 31 * result + Arrays.hashCode(biquads);
        return result;
    }

    @Override
    public Filters clone() throws CloneNotSupportedException{
        Filters p = (Filters) super.clone();
        p.biquads = new Biquad[7];

        for (int i = 0; i < 7; i++) {
            p.biquads[i] = biquads[i].clone();
        }
        return p;
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
        return 7;
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

    public Biquad[] getBiquads() {
        return biquads;
    }
    byte getBiquadIndex(Biquad b) {
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
            types[i] = biquads[i].getParams().getTypeValue();
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
        byte type = getActiveBiquad().getParams().getTypeValue();
        byte nextType;
        Biquad b;
        int counter = 0;

        do {
            if (++counter > 7) break;

            activeBiquadIndex++;
            if (activeBiquadIndex > 6) activeBiquadIndex = 0;

            b = getActiveBiquad();
            nextType = b.getParams().getTypeValue();

        } while (((type == BIQUAD_LOWPASS) && (nextType == BIQUAD_LOWPASS)) ||
                ((type == BIQUAD_HIGHPASS) && (nextType == BIQUAD_HIGHPASS)) ||
                (!b.isEnabled()));

    }
    public void prevActiveBiquadIndex() {
        byte type = getActiveBiquad().getParams().getTypeValue();
        byte nextType;
        Biquad b;
        int counter = 0;

        do {
            if (++counter > 7) break;

            activeBiquadIndex--;
            if (activeBiquadIndex < 0) activeBiquadIndex = 6;

            b = getActiveBiquad();
            nextType = b.getParams().getTypeValue();

        } while (((type == BIQUAD_LOWPASS) && (nextType == BIQUAD_LOWPASS)) ||
                ((type == BIQUAD_HIGHPASS) && (nextType == BIQUAD_HIGHPASS)) ||
                (!b.isEnabled()));

    }


    public List<Biquad> getBiquads(byte typeValue) {
        List<Biquad> biquads = new ArrayList<>();

        for (byte i = 0 ; i < 7; i++) {
            Biquad b = getBiquad(i);

            if (b.getParams().getTypeValue() == typeValue) {
                biquads.add(b);
            }
        }
        return biquads;
    }

    private Biquad getFreeBiquad() {
        List<Biquad> offBiquads = getBiquads(BIQUAD_OFF);
        if (offBiquads.size() > 0) {
            return offBiquads.get(0);
        }

        List<Biquad> paramBiquads = getBiquads(BIQUAD_PARAMETRIC);
        if (paramBiquads.size() > 0) {

            //try find parametric with db == 0
            for (int i = 0; i < paramBiquads.size(); i++) {
                Biquad p = paramBiquads.get(i);

                if (FloatUtility.isFloatNull(p.getParams().getDbVolume())) {
                    return p;
                }
            }

            //find parametric with min abs db
            Biquad p = paramBiquads.get(0);
            for (int i = 1; i < paramBiquads.size(); i++) {
                Biquad current = paramBiquads.get(i);
                if ( (Math.abs(p.getParams().getDbVolume())) > (Math.abs(current.getParams().getDbVolume())) ) {
                    p = current;
                }
            }
            return p;
        }

        List<Biquad> allpassBiquads = getBiquads(BIQUAD_ALLPASS);
        if (allpassBiquads.size() > 0) {
            return allpassBiquads.get(0);
        }

        return null;
    }

    public PassFilter getLowpass() {
        List<Biquad> lpBiquads = getBiquads(BIQUAD_LOWPASS);
        if (lpBiquads.size() == 0) return null;

        return new PassFilter(lpBiquads, BIQUAD_LOWPASS);
    }

    public PassFilter getHighpass() {
        List<Biquad> hpBiquads = getBiquads(BIQUAD_HIGHPASS);
        if (hpBiquads.size() == 0) return null;

        return new PassFilter(hpBiquads, BIQUAD_HIGHPASS);
    }

    private boolean isLowpassFull() {
        List<Biquad> lpBiquads = getBiquads(BIQUAD_LOWPASS);
        return lpBiquads.size() >= 2;
    }
    private boolean isHighpassFull() {
        List<Biquad> hpBiquads = getBiquads(BIQUAD_HIGHPASS);
        return hpBiquads.size() >= 2;
    }

    public void upOrderFor(byte type) {
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
    }

    public boolean isPEQEnabled() {
        boolean result = true;

        List<Biquad> biquads = getBiquads(BIQUAD_PARAMETRIC);
        biquads.addAll(getBiquads(BIQUAD_ALLPASS));
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

    //set enablend and send to dsp
    public void setPEQEnabled(boolean enabled) {
        List<Biquad> biquads = getBiquads(BIQUAD_PARAMETRIC);
        biquads.addAll(getBiquads(BIQUAD_ALLPASS));

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
            byte bType = biquads[i].getParams().getTypeValue();
            boolean enabled = biquads[i].isEnabled();
            boolean typeCondition = (bType == BIQUAD_HIGHPASS) || (bType == BIQUAD_LOWPASS) ||
                                    (bType == BIQUAD_PARAMETRIC) || (bType == BIQUAD_BANDPASS);
            boolean biquadCondition = (b == null) || (biquads[i] != b);

            if ( (enabled) && (typeCondition) && (biquadCondition)) {
                freqs.add(biquads[i].getParams().getFreq());
            }
        }
        if (getLowpass() == null) freqs.add((short)20000);
        if (getHighpass() == null) freqs.add((short)20);

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
        return address0;
    }

    @Override
    public String getInfo() {
        return "Filters is 7 biquads";
    }

    @Override
    public void sendToPeripheral(boolean response) {
        for (int i = 0; i < 7; i++) {
            biquads[i].sendToPeripheral(true);
        }
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        List<HiFiToyDataBuf> l = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            l.addAll(biquads[i].getDataBufs());
        }
        return l;
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) return false;

        for (int i = 0; i < 7; i++) {
            if (!biquads[i].importFromDataBufs(dataBufs)) {
                return false;
            }
        }

        Log.d(TAG, "Filters import success.");
        return true;
    }

    @Override
    public XmlData toXmlData() {
        XmlData xmlData = new XmlData();

        for (int i = 0; i < 7; i++) {
            xmlData.addXmlData(biquads[i].toXmlData());
        }

        XmlData filtersXmlData = new XmlData();
        Map<String, String> attrib = new HashMap<>();
        attrib.put("Address", ByteUtility.toString(address0));
        attrib.put("Address1", ByteUtility.toString(address1));

        filtersXmlData.addXmlElement("Filters", xmlData, attrib);
        return filtersXmlData;
    }

    @Override
    public boolean importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
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
                        if ( (biquads[i].getAddress() == addr) && (biquads[i].importFromXml(xmlParser)) ) {
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
            Log.d(TAG, "Filters=" + Integer.toString(address0) +
                    ". Import from xml is not success.");
            return false;
        }
        Log.d(TAG, getInfo());
        return true;
    }
}
