/*
 *   CommonCommand.java
 *
 *   Created by Artem Khlyupin on 23/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoycontrol;

public class CommonCommand {

    public static final byte ESTABLISH_PAIR      = 0x00;
    public static final byte SET_PAIR_CODE       = 0x01;
    public static final byte SET_WRITE_FLAG      = 0x02;
    public static final byte GET_WRITE_FLAG      = 0x03;
    public static final byte GET_VERSION         = 0x04;
    public static final byte GET_CHECKSUM        = 0x05;
    public static final byte INIT_DSP            = 0x06;
    public static final byte SET_AUDIO_SOURCE    = 0x07;
    public static final byte GET_AUDIO_SOURCE    = 0x08;
    public static final byte GET_ENERGY_CONFIG   = 0x09;
    public static final byte SET_ADVERTISE_MODE  = 0x0A;
    public static final byte GET_ADVERTISE_MODE  = 0x0B;

    //feedback msg
    public static final byte CLIP_DETECTION              = (byte)0xFD;
    public static final byte OTW_DETECTION               = (byte)0xFE;
    public static final byte PARAM_CONNECTION_ENABLED    = (byte)0xFF;

}
