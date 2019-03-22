package com.hifitoy.hifitoynumbers;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class ChecksummerTest {

    @Test
    public void testCalc() {
        assertTrue(Checksummer.calc(new byte[]{0x01, 0x01}) == (short)0x0302);
        assertTrue(Checksummer.calc(new byte[]{(byte)0x80, (byte)0x80}) == (short)0x8000);
        assertTrue(Checksummer.calc(new byte[]{(byte)0x40, (byte)0x40}) == (short)0xC080);

        assertTrue(presetbin.length == 658);
        assertTrue(Checksummer.calc(presetbin) == (short)0xb419);
    }

    byte[] presetbin = new byte[]{/*(byte)0x34, (byte)0x01, (byte)0x0b, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x5c, (byte)0xc2, (byte)0xc4, (byte)0x09, (byte)0x00, (byte)0x00,
            (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x03, (byte)0x00,
            (byte)0x29, (byte)0x00, (byte)0xb6, (byte)0x02,*/ (byte)0x51, (byte)0x14, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x00, (byte)0x99, (byte)0x26, (byte)0x00, (byte)0x7f,
            (byte)0x68, (byte)0x40, (byte)0x00, (byte)0xff, (byte)0x66, (byte)0xda, (byte)0xff, (byte)0x80,
            (byte)0x97, (byte)0xc0, (byte)0x58, (byte)0x14, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0x00, (byte)0x99, (byte)0x26, (byte)0x00, (byte)0x7f, (byte)0x68, (byte)0x40,
            (byte)0x00, (byte)0xff, (byte)0x66, (byte)0xda, (byte)0xff, (byte)0x80, (byte)0x97, (byte)0xc0,
            (byte)0x52, (byte)0x14, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x01,
            (byte)0x34, (byte)0x61, (byte)0x00, (byte)0x7e, (byte)0xd1, (byte)0x35, (byte)0x00, (byte)0xfe,
            (byte)0xcb, (byte)0x9f, (byte)0xff, (byte)0x81, (byte)0x2e, (byte)0xcb, (byte)0x59, (byte)0x14,
            (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x01, (byte)0x34, (byte)0x61,
            (byte)0x00, (byte)0x7e, (byte)0xd1, (byte)0x35, (byte)0x00, (byte)0xfe, (byte)0xcb, (byte)0x9f,
            (byte)0xff, (byte)0x81, (byte)0x2e, (byte)0xcb, (byte)0x53, (byte)0x14, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x01, (byte)0xd1, (byte)0xac, (byte)0x00, (byte)0x7e,
            (byte)0x3a, (byte)0xdf, (byte)0x00, (byte)0xfe, (byte)0x2e, (byte)0x54, (byte)0xff, (byte)0x81,
            (byte)0xc5, (byte)0x21, (byte)0x5a, (byte)0x14, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0x01, (byte)0xd1, (byte)0xac, (byte)0x00, (byte)0x7e, (byte)0x3a, (byte)0xdf,
            (byte)0x00, (byte)0xfe, (byte)0x2e, (byte)0x54, (byte)0xff, (byte)0x81, (byte)0xc5, (byte)0x21,
            (byte)0x54, (byte)0x14, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x02,
            (byte)0x71, (byte)0x00, (byte)0x00, (byte)0x7d, (byte)0xa5, (byte)0x40, (byte)0x00, (byte)0xfd,
            (byte)0x8f, (byte)0x00, (byte)0xff, (byte)0x82, (byte)0x5a, (byte)0xc0, (byte)0x5b, (byte)0x14,
            (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x02, (byte)0x71, (byte)0x00,
            (byte)0x00, (byte)0x7d, (byte)0xa5, (byte)0x40, (byte)0x00, (byte)0xfd, (byte)0x8f, (byte)0x00,
            (byte)0xff, (byte)0x82, (byte)0x5a, (byte)0xc0, (byte)0x55, (byte)0x14, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x03, (byte)0x12, (byte)0x59, (byte)0x00, (byte)0x7d,
            (byte)0x10, (byte)0x56, (byte)0x00, (byte)0xfc, (byte)0xed, (byte)0xa7, (byte)0xff, (byte)0x82,
            (byte)0xef, (byte)0xaa, (byte)0x5c, (byte)0x14, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0x03, (byte)0x12, (byte)0x59, (byte)0x00, (byte)0x7d, (byte)0x10, (byte)0x56,
            (byte)0x00, (byte)0xfc, (byte)0xed, (byte)0xa7, (byte)0xff, (byte)0x82, (byte)0xef, (byte)0xaa,
            (byte)0x56, (byte)0x14, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x03,
            (byte)0xb5, (byte)0xb2, (byte)0x00, (byte)0x7c, (byte)0x7c, (byte)0x23, (byte)0x00, (byte)0xfc,
            (byte)0x4a, (byte)0x4e, (byte)0xff, (byte)0x83, (byte)0x83, (byte)0xdd, (byte)0x5d, (byte)0x14,
            (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x03, (byte)0xb5, (byte)0xb2,
            (byte)0x00, (byte)0x7c, (byte)0x7c, (byte)0x23, (byte)0x00, (byte)0xfc, (byte)0x4a, (byte)0x4e,
            (byte)0xff, (byte)0x83, (byte)0x83, (byte)0xdd, (byte)0x57, (byte)0x14, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x04, (byte)0x5b, (byte)0x03, (byte)0x00, (byte)0x7b,
            (byte)0xe8, (byte)0xa8, (byte)0x00, (byte)0xfb, (byte)0xa4, (byte)0xfd, (byte)0xff, (byte)0x84,
            (byte)0x17, (byte)0x58, (byte)0x5e, (byte)0x14, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0x04, (byte)0x5b, (byte)0x03, (byte)0x00, (byte)0x7b, (byte)0xe8, (byte)0xa8,
            (byte)0x00, (byte)0xfb, (byte)0xa4, (byte)0xfd, (byte)0xff, (byte)0x84, (byte)0x17, (byte)0x58,
            (byte)0xd9, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x48, (byte)0x89, (byte)0x08,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0x8a, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0x8b, (byte)0x08, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x8c, (byte)0x08, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x8d, (byte)0x08,
            (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x8e, (byte)0x08, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x8f, (byte)0x08, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x90, (byte)0x08, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xda, (byte)0x10,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x12, (byte)0x12, (byte)0x12, (byte)0x12,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x12, (byte)0x12, (byte)0x12, (byte)0x12,
            (byte)0x91, (byte)0x10, (byte)0xff, (byte)0xc0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x95, (byte)0x14, (byte)0x00, (byte)0x00, (byte)0x41, (byte)0x48,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0xff, (byte)0xbe, (byte)0xb8,
            (byte)0x00, (byte)0xff, (byte)0x7c, (byte)0xee, (byte)0xff, (byte)0x80, (byte)0x82, (byte)0x92,
            (byte)0x99, (byte)0x1c, (byte)0x05, (byte)0xfa, (byte)0xbe, (byte)0x90, (byte)0x01, (byte)0xfe,
            (byte)0x3f, (byte)0x86, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9e, (byte)0x1c,
            (byte)0x05, (byte)0xfa, (byte)0xbe, (byte)0x90, (byte)0x01, (byte)0xfe, (byte)0x3f, (byte)0x86,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x98, (byte)0x08, (byte)0x00, (byte)0x18,
            (byte)0x12, (byte)0x79, (byte)0x00, (byte)0x67, (byte)0xed, (byte)0x87, (byte)0x9c, (byte)0x10,
            (byte)0x00, (byte)0x00, (byte)0x44, (byte)0x33, (byte)0x00, (byte)0x7f, (byte)0xbb, (byte)0xcd,
            (byte)0x00, (byte)0x00, (byte)0x06, (byte)0xd4, (byte)0x00, (byte)0x7f, (byte)0xf9, (byte)0x2c,
            (byte)0x9d, (byte)0x08, (byte)0x00, (byte)0x18, (byte)0x12, (byte)0x79, (byte)0x00, (byte)0x67,
            (byte)0xed, (byte)0x87, (byte)0xa1, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x44, (byte)0x33,
            (byte)0x00, (byte)0x7f, (byte)0xbb, (byte)0xcd, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0xd4,
            (byte)0x00, (byte)0x7f, (byte)0xf9, (byte)0x2c, (byte)0x96, (byte)0x08, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x0a, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xa2, (byte)0x08,
            (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xa3, (byte)0x08, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0xa4, (byte)0x08, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xa5, (byte)0x08, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xa6, (byte)0x08,
            (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xa7, (byte)0x08, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0xa8, (byte)0x08, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xa9, (byte)0x08, (byte)0x00, (byte)0x80,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
}