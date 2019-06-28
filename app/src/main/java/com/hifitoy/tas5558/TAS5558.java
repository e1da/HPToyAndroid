/*
 *   TAS5558.java
 *
 *   Created by Artem Khlyupin on 12/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.tas5558;

public class TAS5558 {

    public final static byte I2C_ADDR   = (byte) 0x34;
    public final static int TAS5558_FS  = 96000;

    //Regiser Map
    public final static byte CLOCK_CONTROL_REG      = (byte) 0x00;
    public final static byte GENERAL_STATUS_REG     = (byte) 0x01;
    public final static byte ERROR_STATUS_REG       = (byte) 0x02;
    public final static byte SYSTEM_CONTROL1_REG    = (byte) 0x03;
    public final static byte SYSTEM_CONTROL2_REG    = (byte) 0x04;

    public final static byte CH1_CONFIG_CONTROL_REG = (byte) 0x05;
    public final static byte CH2_CONFIG_CONTROL_REG = (byte) 0x06;
    public final static byte CH3_CONFIG_CONTROL_REG = (byte) 0x07;
    public final static byte CH4_CONFIG_CONTROL_REG = (byte) 0x08;
    public final static byte CH5_CONFIG_CONTROL_REG = (byte) 0x09;
    public final static byte CH6_CONFIG_CONTROL_REG = (byte) 0x0A;
    public final static byte CH7_CONFIG_CONTROL_REG = (byte) 0x0B;
    public final static byte CH8_CONFIG_CONTROL_REG = (byte) 0x0C;

    public final static byte HEADPHONE_CONFIG_CONTROL_REG       = (byte) 0x0D;
    public final static byte SERIAL_DATA_INTERFACE_CONTROL_REG  = (byte) 0x0E;
    public final static byte SOFT_MUTE_REG                      = (byte) 0x0F;
    public final static byte ENERGY_MANAGERS_REG                = (byte) 0x10;
    public final static byte RESERVED0                          = (byte) 0x11;
    public final static byte OSCILLATOR_TRIM                    = (byte) 0x12;
    public final static byte RESERVED                           = (byte) 0x13;
    public final static byte AUTOMUTE_CONTROL1_REG              = (byte) 0x14;
    public final static byte AUTOMUTE_CONTROL2_REG              = (byte) 0x15;

    public final static byte MODULATION12_LIMIT_REG = (byte) 0x16;
    public final static byte MODULATION34_LIMIT_REG = (byte) 0x17;
    public final static byte MODULATION56_LIMIT_REG = (byte) 0x18;
    public final static byte MODULATION78_LIMIT_REG = (byte) 0x19;
    public final static byte RESERVED1              = (byte) 0x1A;

    public final static byte DELAY_CH1_REG = (byte) 0x1B;
    public final static byte DELAY_CH2_REG = (byte) 0x1C;
    public final static byte DELAY_CH3_REG = (byte) 0x1D;
    public final static byte DELAY_CH4_REG = (byte) 0x1E;
    public final static byte DELAY_CH5_REG = (byte) 0x1F;
    public final static byte DELAY_CH6_REG = (byte) 0x20;
    public final static byte DELAY_CH7_REG = (byte) 0x21;
    public final static byte DELAY_CH8_REG = (byte) 0x22;

    public final static byte OFFSET_DELAY_REG           = (byte) 0x23;
    public final static byte PWM_SEQUENCE_TIMING_REG    = (byte) 0x24;
    public final static byte PWM_ENERGY_MANAGER_REG     = (byte) 0x25;
    public final static byte RESERVED2                  = (byte) 0x26;
    public final static byte INDIVIDUAL_CH_SHUTDOWN_REG = (byte) 0x27;
    public final static byte RESERVED3                  = (byte) 0x28; //0x28-0x2F

    public final static byte INPUT_MUX_CH12_REG = (byte) 0x30;
    public final static byte INPUT_MUX_CH34_REG = (byte) 0x31;
    public final static byte INPUT_MUX_CH56_REG = (byte) 0x32;
    public final static byte INPUT_MUX_CH78_REG = (byte) 0x33;

    public final static byte PWM_MUX_CH12_REG = (byte) 0x34;
    public final static byte PWM_MUX_CH34_REG = (byte) 0x35;
    public final static byte PWM_MUX_CH56_REG = (byte) 0x36;
    public final static byte PWM_MUX_CH78_REG = (byte) 0x37;

    public final static byte DELAY_CH1_BD_MODE_REG = (byte) 0x38;
    public final static byte DELAY_CH2_BD_MODE_REG = (byte) 0x39;
    public final static byte DELAY_CH3_BD_MODE_REG = (byte) 0x3A;
    public final static byte DELAY_CH4_BD_MODE_REG = (byte) 0x3B;
    public final static byte DELAY_CH5_BD_MODE_REG = (byte) 0x3C;
    public final static byte DELAY_CH6_BD_MODE_REG = (byte) 0x3D;
    public final static byte DELAY_CH7_BD_MODE_REG = (byte) 0x3E;
    public final static byte DELAY_CH8_BD_MODE_REG = (byte) 0x3F;

    public final static byte BANK_SWITCHING_CMD_REG = (byte) 0x40;
    public final static byte INPUT_MIXER_REG        = (byte) 0x41; //0x41-0x48

    public final static byte BASS_MIXER             = (byte) 0x49; //0x49-0x50
    public final static byte BIQUAD_FILTER_REG      = (byte) 0x51; //0x51-0x88, 7biquads * 8channels
    public final static byte BASS_TREBLE_REG        = (byte) 0x89; //0x89-0x90

    public final static byte LOUDNESS_LOG2_GAIN_REG     = (byte) 0x91;
    public final static byte LOUDNESS_LOG2_OFFSET_REG   = (byte) 0x92;
    public final static byte LOUDNESS_GAIN_REG          = (byte) 0x93;
    public final static byte LOUDNESS_OFFSET_REG        = (byte) 0x94;
    public final static byte LOUDNESS_BIQUAD_REG        = (byte) 0x95;

    public final static byte DRC1_CONTROL_REG           = (byte) 0x96;
    public final static byte DRC2_CONTROL_REG           = (byte) 0x97;

    public final static byte DRC1_ENERGY_REG            = (byte) 0x98;
    public final static byte DRC1_THRESHOLD_REG         = (byte) 0x99;
    public final static byte DRC1_SLOPE_REG             = (byte) 0x9A;
    public final static byte DRC1_OFFSET_REG            = (byte) 0x9B;
    public final static byte DRC1_ATTACK_DECAY_REG      = (byte) 0x9C;

    public final static byte DRC2_ENERGY_REG            = (byte) 0x9D;
    public final static byte DRC2_THRESHOLD_REG         = (byte) 0x9E;
    public final static byte DRC2_SLOPE_REG             = (byte) 0x9F;
    public final static byte DRC2_OFFSET_REG            = (byte) 0xA0;
    public final static byte DRC2_ATTACK_DECAY_REG      = (byte) 0xA1;

    public final static byte DRC_BYPASS1_REG    = (byte) 0xA2;
    public final static byte DRC_BYPASS2_REG    = (byte) 0xA3;
    public final static byte DRC_BYPASS3_REG    = (byte) 0xA4;
    public final static byte DRC_BYPASS4_REG    = (byte) 0xA5;
    public final static byte DRC_BYPASS5_REG    = (byte) 0xA6;
    public final static byte DRC_BYPASS6_REG    = (byte) 0xA7;
    public final static byte DRC_BYPASS7_REG    = (byte) 0xA8;
    public final static byte DRC_BYPASS8_REG    = (byte) 0xA9;

    public final static byte OUTPUT_TO_PWM1_REG = (byte) 0xAA;
    public final static byte OUTPUT_TO_PWM2_REG = (byte) 0xAB;
    public final static byte OUTPUT_TO_PWM3_REG = (byte) 0xAC;
    public final static byte OUTPUT_TO_PWM4_REG = (byte) 0xAD;
    public final static byte OUTPUT_TO_PWM5_REG = (byte) 0xAE;
    public final static byte OUTPUT_TO_PWM6_REG = (byte) 0xAF;
    public final static byte OUTPUT_TO_PWM7_REG = (byte) 0xB0;
    public final static byte OUTPUT_TO_PWM8_REG = (byte) 0xB1;

    public final static byte ENERGY_MANAGER_AVERAGING_REG       = (byte) 0xB2;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH1_REG   = (byte) 0xB3;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH2_REG   = (byte) 0xB4;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH3_REG   = (byte) 0xB5;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH4_REG   = (byte) 0xB6;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH5_REG   = (byte) 0xB7;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH6_REG   = (byte) 0xB8;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH7_REG   = (byte) 0xB9;
    public final static byte ENERGY_MANAGER_WEIGHTING_CH8_REG   = (byte) 0xBA;

    public final static byte ENERGY_MANAGER_HIGH_THRESHOLD_SATELLITE_REG    = (byte) 0xBB;
    public final static byte ENERGY_MANAGER_LOW_THRESHOLD_SATELLITE_REG     = (byte) 0xBC;
    public final static byte ENERGY_MANAGER_HIGH_THRESHOLD_SUBWOOFER_REG    = (byte) 0xBD;
    public final static byte ENERGY_MANAGER_LOW_THRESHOLD_SUBWOOFER_REG     = (byte) 0xBE;
    public final static byte RESERVED4                                      = (byte) 0xBF; //0xBF-0xC2

    public final static byte ASRC_STATUS_REG        = (byte) 0xC3;
    public final static byte ASRC_CONTROL_REG       = (byte) 0xC4;
    public final static byte ASRC_MODE_CONTROL_REG  = (byte) 0xC5;
    public final static byte RESERVED5              = (byte) 0xC6; //0xC6-0xCB

    public final static byte AUTO_MUTE_BEHAVIOUR                = (byte) 0xCC;
    public final static byte RESERVED6                          = (byte) 0xCD;
    public final static byte PSVC_VOLUME_BIQUAD                 = (byte) 0xCF;
    public final static byte VOLUME_TREBLE_BASS_SLEW_RATES_REG  = (byte) 0xD0;

    public final static byte CH1_VOLUME_REG     = (byte) 0xD1;
    public final static byte CH2_VOLUME_REG     = (byte) 0xD2;
    public final static byte CH3_VOLUME_REG     = (byte) 0xD3;
    public final static byte CH4_VOLUME_REG     = (byte) 0xD4;
    public final static byte CH5_VOLUME_REG     = (byte) 0xD5;
    public final static byte CH6_VOLUME_REG     = (byte) 0xD6;
    public final static byte CH7_VOLUME_REG     = (byte) 0xD7;
    public final static byte CH8_VOLUME_REG     = (byte) 0xD8;
    public final static byte MASTER_VOLUME_REG  = (byte) 0xD9;
    public final static byte BASS_FILTER_SET_REG        = (byte) 0xDA;
    public final static byte BASS_FILTER_INDEX_REG      = (byte) 0xDB;
    public final static byte TREBLE_FILTER_SET_REG      = (byte) 0xDC;
    public final static byte TREBLE_FILTER_INDEX_REG    = (byte) 0xDD;
    public final static byte AM_MODE_REG                = (byte) 0xDE;
    public final static byte PSVC_RANGE_REG             = (byte) 0xDF;
    public final static byte GENERAL_CONTROL_REG        = (byte) 0xE0;
    public final static byte RESERVED7                  = (byte) 0xE1;// 0xE1-0xE2

    public final static byte R_DOLBY_COEFLR_REG     = (byte) 0xE3;
    public final static byte R_DOLBY_COEFC_REG      = (byte) 0xE4;
    public final static byte R_DOLBY_COEFLSP_REG    = (byte) 0xE5;
    public final static byte R_DOLBY_COEFRSP_REG    = (byte) 0xE6;
    public final static byte R_DOLBY_COEFLSM_REG    = (byte) 0xE7;
    public final static byte R_DOLBY_COEFRSM_REG    = (byte) 0xE8;

    public final static byte THD_MANAGER_PRE_REG    = (byte) 0xE9;
    public final static byte THD_MANAGER_POST_REG   = (byte) 0xEA;
    public final static byte RESERVED8              = (byte) 0xEB;

    public final static byte SDIN5_INPUT1_MIX_REG   = (byte) 0xEC;
    public final static byte SDIN5_INPUT2_MIX_REG   = (byte) 0xED;
    public final static byte SDIN5_INPUT3_MIX_REG   = (byte) 0xEE;
    public final static byte SDIN5_INPUT4_MIX_REG   = (byte) 0xEF;
    public final static byte SDIN5_INPUT5_MIX_REG   = (byte) 0xF0;
    public final static byte SDIN5_INPUT6_MIX_REG   = (byte) 0xF1;
    public final static byte SDIN5_INPUT7_MIX_REG   = (byte) 0xF2;
    public final static byte SDIN5_INPUT8_MIX_REG   = (byte) 0xF3;

    public final static byte KHZ192_PROCESS_FLOW_OUTPUT_MIX1_REG    = (byte) 0xF4;
    public final static byte KHZ192_PROCESS_FLOW_OUTPUT_MIX2_REG    = (byte) 0xF5;
    public final static byte KHZ192_PROCESS_FLOW_OUTPUT_MIX3_REG    = (byte) 0xF6;
    public final static byte KHZ192_PROCESS_FLOW_OUTPUT_MIX4_REG    = (byte) 0xF7;
    public final static byte RESERVED9                              = (byte) 0xF8; //0xF8-0xF9

    public final static byte KHZ192_IMAGE_SELECT_REG        = (byte) 0xFA;
    public final static byte KHZ192_DOLBY_DOWNMIX_COEF_REG  = (byte) 0xFB;
    public final static byte RESERVED10                     = (byte) 0xFD;

    public final static byte SPECIAL_REG                    = (byte) 0xFE;
    public final static byte RESERVED11                     = (byte) 0xFF;

    //Clock Control Register Masks
    public final static byte DATA_RATE_MASK     = (byte) 0xE0;
    public final static byte DATA_RATE_32KHZ    = (byte) 0x00;
    public final static byte DATA_RATE_44_1KHZ  = (byte) 0x40;
    public final static byte DATA_RATE_48KHZ    = (byte) 0x60;
    public final static byte DATA_RATE_88_2KHZ  = (byte) 0x80;
    public final static byte DATA_RATE_96KHZ    = (byte) 0xA0;
    public final static byte DATA_RATE_176_4KHZ = (byte) 0xC0;
    public final static byte DATA_RATE_192KHZ   = (byte) 0xE0;

    public final static byte MCLK_FREQ_MASK     = (byte) 0x1C;
    public final static byte MCLK_FREQ_64       = (byte) 0x00;
    public final static byte MCLK_FREQ_128      = (byte) 0x04;
    public final static byte MCLK_FREQ_192      = (byte) 0x08;
    public final static byte MCLK_FREQ_256      = (byte) 0x0C;
    public final static byte MCLK_FREQ_384      = (byte) 0x10;
    public final static byte MCLK_FREQ_512      = (byte) 0x14;
    public final static byte MCLK_FREQ_768      = (byte) 0x18;

    public final static byte CLK_REG_VALID_MASK = (byte) 0x03;
    public final static byte CLK_REG_VALID      = (byte) 0x01;
    public final static byte CLK_REG_NOT_VALID  = (byte) 0x00;

    //Error Status Register Masks
    public final static byte FRAME_SLIP_MASK        = (byte) 0x08;
    public final static byte CLIP_INDICATOR_MASK    = (byte) 0x04;
    public final static byte FAULTZ_MASK            = (byte) 0x02;


}
