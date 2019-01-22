/*
 *   HiFiToyConfig.java
 *
 *   Created by Artem Khlyupin on 14/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoycontrol;

public class HiFiToyConfig {
    private static HiFiToyConfig instance;

    private final byte  i2cAddr = 0x34;                        // 0x00
    public byte         successWriteFlag;               // 0x01
    public final int    version = 11;                        // 0x02
    public int          pairingCode;                    // 0x04
    //public AudioSource  audioSource;                    // 0x08
    //private byte[]      reserved = new byte[3];         // 0x09

    //public EnergyConfig energy;                         // 0x0C
    public BiquadType[] biquadTypes = new BiquadType[7];// 0x18
    //private byte        reserved1;                      //

    int             dataBufLength;                  // 0x20
    int             dataBytesLength;                // 0x22
    DataBufHeader   firstDataBuf;                   // 0x24

    public static synchronized HiFiToyConfig getInstance() {
        if (instance == null){
            instance = new HiFiToyConfig();
        }
        return instance;
    }

    public HiFiToyConfig() {
        setDefault();
    }

    public void setDefault() {
        successWriteFlag            = 0x00; //must be assign '0' before sendFactorySettings
        pairingCode                 = 0;//[[HiFiToyDeviceList sharedInstance] getActiveDevice].pairingCode;

        AudioSource.getInstance().setDefaultValue();
        EnergyConfig.getInstance().setDefault();

        /*BiquadType_t * types = [preset.filters getBiquadTypes];
        memcpy(&hiFiToyConfig.biquadTypes, types, 7 * sizeof(BiquadType_t));
        free(types);*/

        dataBufLength     = 0;//[self getDataBufLength:defaultPresetData];
        dataBytesLength   = 0;//sizeof(HiFiToyPeripheral_t) - sizeof(DataBufHeader_t) + defaultPresetData.length;
    }

    class BiquadType {

    }

    class DataBufHeader {
        byte addr;     // in TAS5558 registers
        byte length;    // [byte] unit
    }


}
