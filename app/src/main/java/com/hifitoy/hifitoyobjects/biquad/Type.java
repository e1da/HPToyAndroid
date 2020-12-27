/*
 *   Type.java
 *
 *   Created by Artem Khlyupin on 16/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;


import android.util.Log;
import android.widget.ListView;

import com.hifitoy.hifitoynumbers.FloatUtility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Type {
    public final static byte BIQUAD_LOWPASS       = 2;
    public final static byte BIQUAD_HIGHPASS      = 1;
    public final static byte BIQUAD_OFF           = 0;
    public final static byte BIQUAD_PARAMETRIC    = 3;
    public final static byte BIQUAD_ALLPASS       = 4;
    public final static byte BIQUAD_BANDPASS      = 5;
    public final static byte BIQUAD_USER          = 6;
    public final static byte BIQUAD_DEFAULT       = BIQUAD_PARAMETRIC;

    static class TypeDiff implements Comparable {
        byte type;
        float diff;

        public TypeDiff(byte type, float diff) {
            this.type = type;
            this.diff = diff;
        }

        @Override
        public int compareTo(Object o) {
            if (diff > ((TypeDiff)o).diff) {
                return 1;
            } else if (diff < ((TypeDiff)o).diff) {
                return -1;
            }
            return 0;
        }
    }
    public static byte getType(Biquad b) {
        List<TypeDiff> l = new ArrayList<>();

        float bp_l = FloatUtility.diff(b.getB1(), -b.getA1());
        l.add(new TypeDiff(BIQUAD_PARAMETRIC, bp_l));

        float bbp_l0 = FloatUtility.diff(b.getB0(), -b.getB2());
        float bbp_l1 = FloatUtility.diff(b.getB1(), 0.0f);
        float bbp_l = (float)Math.sqrt(bbp_l0 * bbp_l0 + bbp_l1 * bbp_l1);
        l.add(new TypeDiff(BIQUAD_BANDPASS, bbp_l));

        float bap_l0 = FloatUtility.diff(b.getB0(), -b.getA1());
        float bap_l1 = FloatUtility.diff(b.getB1(), 1.0f);
        float bap_l = (float)Math.sqrt(bap_l0 * bap_l0 + bap_l1 * bap_l1);
        l.add(new TypeDiff(BIQUAD_ALLPASS, bap_l));

        float blp_l0 = FloatUtility.diff(b.getB0(), b.getB2());
        float blp_l1 = FloatUtility.diff(b.getB1(), 2 * b.getB0());
        float blp_l = (float)Math.sqrt(blp_l0 * blp_l0 + blp_l1 * blp_l1);
        l.add(new TypeDiff(BIQUAD_LOWPASS, blp_l));

        float bhp_l0 = FloatUtility.diff(b.getB0(), b.getB2());
        float bhp_l1 = FloatUtility.diff(b.getB1(), -2 * b.getB0());
        float bhp_l = (float)Math.sqrt(bhp_l0 * bhp_l0 + bhp_l1 * bhp_l1);
        l.add(new TypeDiff(BIQUAD_HIGHPASS, bhp_l));

        Collections.sort(l);

        TypeDiff res = l.get(0);
        if (res.diff > 2048.0) return BIQUAD_USER;
        return res.type;


        /*if (FloatUtility.isFloatEqualWithAccuracy(b.getB1(), -b.getA1(), 16)) {
            return BIQUAD_PARAMETRIC;
        }
        if (FloatUtility.isFloatEqualWithAccuracy(b.getB0(), -b.getB2(), 16) &&
                FloatUtility.isFloatEqualWithAccuracy(b.getB1(), 0.0f, 16)) {
            return BIQUAD_BANDPASS;
        }

        //apf 1st order
        if (FloatUtility.isFloatEqualWithAccuracy(b.getB0(), -b.getA1(), 16) &&
                FloatUtility.isFloatEqualWithAccuracy(b.getB1(), 1.0f, 16)) {
            return BIQUAD_ALLPASS;
        }

        if (FloatUtility.isFloatEqualWithAccuracy(b.getB0(), b.getB2(), 16) &&
                FloatUtility.isFloatEqualWithAccuracy(b.getB1(), 2 * b.getB0(), 16)) {
            return BIQUAD_LOWPASS;
        }
        if (FloatUtility.isFloatEqualWithAccuracy(b.getB0(), b.getB2(), 16) &&
                FloatUtility.isFloatEqualWithAccuracy(b.getB1(), -2 * b.getB0(), 16)) {
            return BIQUAD_HIGHPASS;
        }

        return BIQUAD_USER;*/
    }

    /*private byte value;

    public Type() {
        value = BIQUAD_DEFAULT;
    }*/

    /*public static byte getType(Biquad b) {
        if (b.getClass().equals(ParamBiquad.class)) {
            return BIQUAD_PARAMETRIC;
        } else if (b.getClass().equals(LowpassBiquad.class)) {
            return BIQUAD_LOWPASS;
        } else if (b.getClass().equals(HighpassBiquad.class)) {
            return BIQUAD_HIGHPASS;
        } else if (b.getClass().equals(AllpassBiquad.class)) {
            return BIQUAD_ALLPASS;
        } else if (b.getClass().equals(BandpassBiquad.class)) {
            return BIQUAD_BANDPASS;
        } else if (b.getClass().equals(TextBiquad.class)) {
            return BIQUAD_USER;
        } else {
            return BIQUAD_OFF;
        }

    }

    /*public void setValue(byte value) {
        if (value > BIQUAD_USER) value = BIQUAD_USER;
        if (value < BIQUAD_OFF) value = BIQUAD_OFF;

        this.value = value;
    }
    public byte getValue() {
        return value;
    }

    @Override
    public Type clone() throws CloneNotSupportedException{
        return (Type) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return value == type.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }*/


}
