package com.hifitoy.hifitoynumbers;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class ComplexTest {
    double b0 = 1.021;
    double b1 = -1.913;
    double b2 = 0.908;
    double a1 = -1.913;
    double a2 = 0.929;

    @Test
    public void testDrawBiquad() {
        double freq = 1000;// Hz
        double w = 2.0 * Math.PI * freq / 48000;
        Complex Z1 = Complex.trigonometricForm(1, w).reciprocal();
        Complex Z2 = Z1.mul(Z1);

        /* Z(f) = e^(i * w(f))
        *   H(f) = (b0 + b1 * Z^-1 + b2 * Z^-2) / (1 + a1 * Z^-1 + a2 * Z^-2)
         */
        Complex num = new Complex(b0, 0).add(new Complex(b1, 0).mul(Z1)).add(new Complex(b2, 0).mul(Z2));
        Complex denom = new Complex(1, 0).add(new Complex(a1, 0).mul(Z1)).add(new Complex(a2, 0).mul(Z2));
        Complex H = num.div(denom);

        double ampl = H.mod();
        double db = Math.log10(ampl) * 20;

        System.out.println(num.toString());
        System.out.println(denom.toString());
        System.out.println(H.toString());
        System.out.println("ampl=" + ampl + " db=" + db);

        assertTrue((db > 3.9) && (db < 4.1));
    }
}