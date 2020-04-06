/*
 *   KeyboardNumber.java
 *
 *   Created by Artem Khlyupin on 28/12/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.dialogsystem;

import java.util.regex.Pattern;

public class KeyboardNumber {
    private NumberType  type;
    private String      value;

    public KeyboardNumber(NumberType type, String value) {
        this.type = type;
        this.value = value;
    }

    public KeyboardNumber(NumberType type, int value) {
        this.type = type;
        this.value = Integer.toString(value);
    }

    public KeyboardNumber(NumberType type, float value) {
        this.type = type;
        this.value = Float.toString(value);
    }

    public KeyboardNumber(NumberType type, double value) {
        this.type = type;
        this.value = Double.toString(value);
    }

    public void putChar(char numChar) {
        if (value == null) value = "";

        if (numChar == '\u232B') { //backspace
            if (!value.isEmpty()) {
                value = value.substring(0, value.length() - 1);
            }

        } else if (numChar == '-') {
            if ( (value.isEmpty()) && (!typeIsPositive()) ) value = "-";

        } else if (numChar == '.') {
            if ( (!value.contains(".")) && (!typeIsInteger()) && (lastCharIsNumber()) ) {
                value += ".";
            }

        } else if ( (numChar >= '0') && (numChar <= '9') && (getPermissionForAddChar())) {
            value += numChar;

        }

    }

    public void backspace() {
        putChar('\u232B');
    }

    private boolean lastCharIsNumber() {
        if ((value != null) && (!value.isEmpty())) {
            char lastChar = value.charAt(value.length() - 1);
            return ( (lastChar != '-') && (lastChar != '.') );
        }
        return false;
    }

    public boolean typeIsPositive() {
        return (type == NumberType.POSITIVE_INTEGER) |
                (type == NumberType.POSITIVE_FLOAT) |
                (type == NumberType.POSITIVE_DOUBLE);
    }

    public boolean typeIsInteger() {
        return (type == NumberType.POSITIVE_INTEGER) |
                (type == NumberType.INTEGER);
    }

    private boolean getPermissionForAddChar() {
        if (value.length() > 8) return false;
        if (!value.contains(".")) return true;

        int numAfterPoint;
        switch (type) {
            case FLOAT:
            case POSITIVE_FLOAT:
                numAfterPoint = 1;
                break;
            case DOUBLE:
            case POSITIVE_DOUBLE:
                numAfterPoint = 2;
                break;

            default:
                return true;
        }

        return (value.length() - value.indexOf('.') <= numAfterPoint);
    }



    static boolean stringIsFloat(String s) {
        String regex = "^[-]?[0-9]*[.,]?[0-9]+$";
        return Pattern.matches(regex, s);
    }

    static boolean stringIsInt(String s) {
        String regex = "^[-]?[0-9]+$";
        return Pattern.matches(regex, s);
    }


    public String getValue() {
        return value;
    }

    public enum NumberType {
        POSITIVE_INTEGER, INTEGER,
        POSITIVE_FLOAT, FLOAT,
        POSITIVE_DOUBLE, DOUBLE,
        MAX_REAL
    }

}
