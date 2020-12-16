/*
 *   Order.java
 *
 *   Created by Artem Khlyupin on 16/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects.biquad;

import java.io.Serializable;
import java.util.Objects;

public class Order implements Cloneable, Serializable {
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

    @Override
    public Order clone() throws CloneNotSupportedException{
        return (Order) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return value == order.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
