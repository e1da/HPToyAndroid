/*
 *   PeripheralData.java
 *
 *   Created by Artem Khlyupin on 16/03/2019
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoydevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.hifitoy.ApplicationContext;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.hifitoy.ApplicationContext.EXTRA_DATA;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;

public class PeripheralData {
    private static final String TAG = "HiFiToy";

    private final short PERIPHERAL_CONFIG_LENGTH    = 0x24;
    private final short PRESET_DATA_OFFSET          = 0x18;

    public static final byte I2C_ADDR = 0x34;
    public static final short VERSION = 11;

    private byte                    i2cAddr;            // 0x00
    private byte                    successWriteFlag;   // 0x01
    private short                   version;            // 0x02
    private int                     pairingCode;        // 0x04
    private byte                    audioSource;        // 0x08
    private byte                    advertiseMode;      // 0x09 0x01
    private short                   gainChannel3;       // 0x0A number format 1.15 unsign
    private EnergyConfig            energyConfig;       // 0x0C
    private byte[]                  biquadTypes;        // 0x18 0x07
    private byte                    outputMode;         // 0x1F balance/unbalance

    private short                   dataBufLength;      // 0x20
    private short                   dataBytesLength;    // 0x22
    private List<HiFiToyDataBuf>    dataBufs;           // 0x24

    private PeripheralDataDelegate delegate;

    private byte[] importData;

    public interface PeripheralDataDelegate {
        void didImportData(PeripheralData peripheralData);
    }

    public PeripheralData(HiFiToyDevice device) {
        i2cAddr = I2C_ADDR;
        successWriteFlag = 0;
        version = VERSION;
        pairingCode = device.getPairingCode();
        audioSource = device.getAudioSource().getSource();
        advertiseMode = device.getAdvertiseMode().getMode();
        gainChannel3 = 16384; // 0x4000
        energyConfig = device.getEnergyConfig();
        setBiquadTypes(device.getActivePreset().getFilters().getBiquadTypes());
        outputMode = device.getOutputMode().getValue();

        setDataBufs(device.getActivePreset().getDataBufs());
    }
    public PeripheralData() {
        clear();
    }

    public void setDelegate(PeripheralDataDelegate delegate) {
        this.delegate = delegate;
    }

    public void clear() {
        i2cAddr = I2C_ADDR;
        successWriteFlag = 0;
        version = VERSION;
        pairingCode = 0;
        audioSource = AudioSource.USB_SOURCE;
        advertiseMode = AdvertiseMode.ALWAYS_ENABLED;
        gainChannel3 = 16384; // 0x4000
        energyConfig = new EnergyConfig();
        setBiquadTypes(new byte[]{BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC,
                BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC});
        outputMode = OutputMode.UNBALANCE_BOOST_OUT_MODE;

        dataBufLength = 0;
        dataBytesLength = 0;
        dataBufs = null;
    }

    private short getDataBytesLength(List<HiFiToyDataBuf> dataBufs) {
        short length = 0;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);
            length += buf.getLength() + 2;
        }
        return (short)(length + PERIPHERAL_CONFIG_LENGTH);
    }

    public boolean setBiquadTypes(byte[] biquadTypes) {
        if (biquadTypes.length == 7) {
            this.biquadTypes = biquadTypes;
            return true;
        }
        return false;
    }

    public void setDataBufs(List<HiFiToyDataBuf> dataBufs) {
        if (dataBufs == null) {
            dataBufLength = 0;
            dataBytesLength = 0;
        } else {
            dataBufLength = (short)dataBufs.size();
            dataBytesLength = getDataBytesLength(dataBufs);
        }
        this.dataBufs = dataBufs;
    }
    public List<HiFiToyDataBuf> getDataBufs() {
        return dataBufs;
    }

    private ByteBuffer getBinary() {
        ByteBuffer data = ByteBuffer.allocate(PERIPHERAL_CONFIG_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        data.put(i2cAddr);
        data.put(successWriteFlag);
        data.putShort(version);
        data.putInt(pairingCode);
        data.put(audioSource);
        data.put(advertiseMode);
        data.putShort(gainChannel3);
        data.put(energyConfig.getBinary());
        data.put(biquadTypes);
        data.put(outputMode);
        data.putShort(dataBufLength);
        data.putShort(dataBytesLength);

        if (dataBufs != null) {
            for (int i = 0; i < dataBufs.size(); i++) {
                data = BinaryOperation.concatData(data, dataBufs.get(i).getBinary());
            }
        }
        data.position(0);

        return data;
    }

    private ByteBuffer getPresetBinary() {
        ByteBuffer data = getBinary();
        return BinaryOperation.copyOfRange(data, PRESET_DATA_OFFSET, data.capacity());
    }

    private void export() {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        HiFiToyControl.getInstance().sendBufToDsp((short)0, getBinary());
        HiFiToyControl.getInstance().sendWriteFlag((byte)1);
        HiFiToyControl.getInstance().setInitDsp();
    }

    public void exportWithDialog(String title) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        Context c = ApplicationContext.getInstance().getContext();
        c.registerReceiver(broadcastReceiver, makeExportIntentFilter());

        DialogSystem.getInstance().showProgressDialog(title, getBinary().capacity());
        export();
    }

    private void exportPreset() {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        HiFiToyControl.getInstance().sendWriteFlag((byte)0);
        HiFiToyControl.getInstance().sendBufToDsp(PRESET_DATA_OFFSET , getPresetBinary());
        HiFiToyControl.getInstance().sendWriteFlag((byte)1);
        HiFiToyControl.getInstance().setInitDsp();
    }

    public void exportPresetWithDialog(String title) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        Context c = ApplicationContext.getInstance().getContext();
        c.registerReceiver(broadcastReceiver, makeExportIntentFilter());

        DialogSystem.getInstance().showProgressDialog(title, getPresetBinary().capacity());
        exportPreset();
    }

    private void startImport(){
        if (!HiFiToyControl.getInstance().isConnected()) return;

        Context c = ApplicationContext.getInstance().getContext();
        c.registerReceiver(broadcastReceiver, makeImportIntentFilter());

        dataBytesLength = -1;
        importData = new byte[0];
        //start read first 20 bytes
        HiFiToyControl.getInstance().getDspDataWithOffset((short)0);
    }

    public void importWithDialog(String title) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        //real max packets calculate after get first packet
        DialogSystem.getInstance().showProgressDialog(title, PERIPHERAL_CONFIG_LENGTH);
        startImport();
    }

    public void didGet20Bytes(byte[] data20) {
        if (data20.length != 20) {
            DialogSystem.getInstance().showDialog("Warning", "Import preset is not success.", "Ok");
            return;
        }

        //add new 20bytes to importData
        importData = BinaryOperation.concatData(importData, data20);

        if (importData.length == 40) {
            ByteBuffer b = ByteBuffer.wrap(importData).order(ByteOrder.LITTLE_ENDIAN);

            //parse peripheral config
            i2cAddr = b.get();
            successWriteFlag = b.get();
            version = b.getShort();
            pairingCode = b.getInt();
            audioSource = b.get();
            advertiseMode = b.get();
            gainChannel3 = b.getShort();
            energyConfig.parseBinary(b);

            biquadTypes = new byte[7];
            b.get(biquadTypes, 0, 7);

            outputMode = b.get();
            dataBufLength = b.getShort();
            dataBytesLength = b.getShort();

            DialogSystem.getInstance().getProgressDialog().setMax(dataBytesLength - 40);

        } else if (dataBytesLength != -1) {

            //then reading finished
            if (importData.length >= dataBytesLength) {
                //parse dataBufs
                ByteBuffer b = ByteBuffer.wrap(importData).order(ByteOrder.LITTLE_ENDIAN);
                b.position(PERIPHERAL_CONFIG_LENGTH);

                dataBufs = new ArrayList<>();
                for (int i = 0; i < dataBufLength; i++) {
                    dataBufs.add(new HiFiToyDataBuf(b));
                }

                Log.d(TAG, "Peripheral data import finished success.");
                DialogSystem.getInstance().closeProgressDialog();

                Context c = ApplicationContext.getInstance().getContext();
                c.unregisterReceiver(broadcastReceiver);

                if (delegate != null) delegate.didImportData(this);

                return;
            }
        }

        //update progress dialog
        Log.d(TAG, "import " + Integer.toString(importData.length));

        //read next 20bytes
        HiFiToyControl.getInstance().getDspDataWithOffset((short)importData.length);

    }

    private static IntentFilter makeImportIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HiFiToyControl.DID_GET_PARAM_DATA);

        return intentFilter;
    }
    private static IntentFilter makeExportIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HiFiToyControl.DID_WRITE_DATA);
        intentFilter.addAction(HiFiToyControl.DID_WRITE_ALL_DATA);

        return intentFilter;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.DID_GET_PARAM_DATA.equals(action)) {
                byte[] data = intent.getByteArrayExtra(EXTRA_DATA);
                DialogSystem.getInstance().updateProgressDialog(data.length);

                didGet20Bytes(data);
            }
            if (HiFiToyControl.DID_WRITE_DATA.equals(action)) {
                int v = intent.getIntExtra(EXTRA_DATA, -1);
                DialogSystem.getInstance().updateProgressDialog(16);
            }
            if (HiFiToyControl.DID_WRITE_ALL_DATA.equals(action)) {
                DialogSystem.getInstance().closeProgressDialog();

                Context c = ApplicationContext.getInstance().getContext();
                c.unregisterReceiver(broadcastReceiver);
            }
        }
    };

}