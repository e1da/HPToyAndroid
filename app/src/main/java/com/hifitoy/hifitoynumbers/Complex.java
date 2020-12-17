/*
 *   Complex.java
 *
 *   Created by Artem Khlyupin on 17/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoynumbers;

import android.support.annotation.NonNull;

public class Complex {
    private double real;
    private double img;

    public Complex(double real, double img) {
        this.real = real;
        this.img = img;
    }

    public static Complex trigonometricForm(double mod, double arg) {
        return new Complex(mod * Math.cos(arg), mod * Math.sin(arg));
    }

    public Complex() {
        this(0, 0);
    }

    public Complex(Complex c) {
        this(c.real, c.img);
    }

    public Complex add(Complex c) {
        return new Complex(real + c.real, img + c.img);
    }

    public Complex sub(Complex c) {
        return new Complex(real - c.real, img - c.img);
    }

    public Complex mul(Complex c) {
        return new Complex(this.real * c.real - this.img * c.img,
                this.real * c.img + this.img * c.real);
    }

    public Complex conj() {
        return new Complex(real, -img);
    }

    public Complex reciprocal() {
        double mod2 = mul(conj()).real;
        return new Complex(real / mod2, -img / mod2);
    }

    public Complex div(Complex c) {
        return mul(c.reciprocal());
    }

    public double mod() {
        return Math.sqrt(real * real + img * img);

    }

    @NonNull
    @Override
    public String toString() {
        return "real=" + real + ", img=" + img;
    }


}
