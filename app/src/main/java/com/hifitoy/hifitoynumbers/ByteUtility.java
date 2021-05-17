package com.hifitoy.hifitoynumbers;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtility {
    private static final String TAG = "HiFiToy";

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toString(byte b) {
        ByteBuffer bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(b);
        bb.put((byte)0);
        short s = bb.getShort(0);

        return Short.toString(s);
    }

    public static String toString(byte[] b) {
        String s = "";
        for (int i = 0; i < b.length; i++) {
            s += String.format("%x ", b[i]);

            if (i % 4 == 3) s += "\n";
        }
        return s;
    }

    public static String toHexString(byte b) {
        return String.format("%c%c", HEX_ARRAY[(b >>> 4) & 0x0F], HEX_ARRAY[b & 0xF]);
    }

    public static String toBinaryString(byte b) {
        StringBuilder sb = new StringBuilder();
        int num = byteToInt(b);


        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                sb.append(" ");
            }

            if (num % 2 != 0) {
                sb.append("1");
            } else {
                sb.append("0");
            }
            num /= 2;
        }

        return sb.reverse().toString();
    }

    public static byte parse(String s) {
        try {
            short num = Short.parseShort(s);
            if ( (num >= 0) && (num < 256) ) {
                return (byte)num;
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        }

        return 0;
    }

    public static int byteToInt(byte b) {
        ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        buf.put(b);buf.put((byte)0); buf.put((byte)0);buf.put((byte)0);
        buf.position(0);
        return buf.getInt();
    }

    public static byte getIntLSB(int i) {
        ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i);
        return buf.get(0);
    }
}
