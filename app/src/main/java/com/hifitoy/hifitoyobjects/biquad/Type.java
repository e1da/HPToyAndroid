/*
 *   Type.java
 *
 *   Created by Artem Khlyupin on 16/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import java.io.Serializable;
import java.util.Objects;

public class Type implements Cloneable, Serializable {
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
    }
}
