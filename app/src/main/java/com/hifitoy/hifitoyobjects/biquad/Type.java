/*
 *   Type.java
 *
 *   Created by Artem Khlyupin on 16/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;


import com.hifitoy.hifitoynumbers.FloatUtility;

public class Type {
    public final static byte BIQUAD_LOWPASS       = 2;
    public final static byte BIQUAD_HIGHPASS      = 1;
    public final static byte BIQUAD_OFF           = 0;
    public final static byte BIQUAD_PARAMETRIC    = 3;
    public final static byte BIQUAD_ALLPASS       = 4;
    public final static byte BIQUAD_BANDPASS      = 5;
    public final static byte BIQUAD_USER          = 6;
    public final static byte BIQUAD_DEFAULT       = BIQUAD_PARAMETRIC;

    public static byte getType(Biquad b) {
        if (FloatUtility.isFloatEqualWithAccuracy(b.getB0(), b.getB2(), 16) &&
                FloatUtility.isFloatEqualWithAccuracy(b.getB1(), 2 * b.getB0(), 16)) {
            return BIQUAD_LOWPASS;
        }
        if (FloatUtility.isFloatEqualWithAccuracy(b.getB0(), b.getB2(), 16) &&
                FloatUtility.isFloatEqualWithAccuracy(b.getB1(), -2 * b.getB0(), 16)) {
            return BIQUAD_HIGHPASS;
        }
        if (FloatUtility.isFloatEqualWithAccuracy(b.getB1(), -b.getA1(), 16)) {
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

        return BIQUAD_USER;
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
