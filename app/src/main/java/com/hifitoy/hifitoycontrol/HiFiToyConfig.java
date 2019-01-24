/*
 *   HiFiToyConfig.java
 *
 *   Created by Artem Khlyupin on 14/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoycontrol;

import com.hifitoy.hifitoydevice.AudioSource;
import com.hifitoy.hifitoydevice.EnergyConfig;
import com.hifitoy.hifitoyobjects.BiquadType;

public class HiFiToyConfig {
    private static HiFiToyConfig instance;

    private final byte  i2cAddr = 0x34;                 // 0x00
    private byte        successWriteFlag;               // 0x01
    public final short  version = 11;                   // 0x02
    public int          pairingCode;                    // 0x04
    public byte         audioSource;                    // 0x08

    public EnergyConfig energy;                         // 0x0C
    public byte[]       biquadTypes;                    // 0x18

    private short       dataBufLength;                  // 0x20
    private short       dataBytesLength;                // 0x22
    DataBufHeader       firstDataBuf;                   // 0x24

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

        audioSource = AudioSource.USB_SOURCE;
        energy = new EnergyConfig();
        energy.setDefault();

        biquadTypes = new byte[7];
        for (int i = 0; i < biquadTypes.length; i++) biquadTypes[i] = BiquadType.getDefault();

        dataBufLength     = 0;//[self getDataBufLength:defaultPresetData];
        dataBytesLength   = 0;//sizeof(HiFiToyPeripheral_t) - sizeof(DataBufHeader_t) + defaultPresetData.length;

        firstDataBuf = new DataBufHeader((byte)0,(byte)0);
    }


    class DataBufHeader {
        byte addr;      // in TAS5558 registers
        byte length;    // [byte] unit

        public DataBufHeader(byte addr, byte length) {
            this.addr = addr;
            this.length = length;
        }

    }


}
