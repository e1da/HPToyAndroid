/*
 *   TextBiquad.java
 *
 *   Created by Artem Khlyupin on 20/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import java.util.concurrent.TimeoutException;

public class TextBiquad extends Biquad {

    public TextBiquad(Biquad b) {
        super(b);
    }

    public TextBiquad(byte addr) {
        super(addr);
    }

    public TextBiquad() {
        super();
    }

    public void setB0(float b0) {
        this.b0 = b0;
    }
    public float getB0(){
        return b0;
    }

    public void setB1(float b1) {
        this.b1 = b1;
    }
    public float getB1(){
        return b1;
    }

    public void setB2(float b2) {
        this.b2 = b2;
    }
    public float getB2(){
        return b2;
    }

    public void setA1(float a1) {
        this.a1 = a1;
    }
    public float getA1(){
        return a1;
    }

    public void setA2(float a2) {
        this.a2 = a2;
    }
    public float getA2(){
        return a2;
    }
}
