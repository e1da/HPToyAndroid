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
import android.support.annotation.NonNull;
import android.util.Log;

import com.hifitoy.ApplicationContext;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoyobjects.AMMode;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.PostProcess;
import com.hifitoy.hifitoyobjects.ToyDelayDataBuf;
import com.hifitoy.tas5558.TAS5558;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.hifitoy.ApplicationContext.EXTRA_DATA;
import static com.hifitoy.hifitoyobjects.Biquad.BiquadParam.Type.BIQUAD_PARAMETRIC;

public class PeripheralData {
    private static final String TAG = "HiFiToy";

    private final short PERIPHERAL_CONFIG_LENGTH    = 0x24;
    private final short BIQUAD_TYPE_OFFSET          = 0x18;
    private final short PRESET_DATA_OFFSET          = 0x20;

    public static final byte    I2C_ADDR            = 0x34;
    public static final short   VERSION             = 12;
    public static final byte    INIT_DSP_DELAY      = (byte)32; // 32 << 3 = 256ms

    private byte                    i2cAddr;            // 0x00
    private byte                    successWriteFlag;   // 0x01
    private short                   version;            // 0x02
    private int                     pairingCode;        // 0x04
    private byte                    initDspDelay;       // 0x08
    private byte                    advertiseMode;      // 0x09 0x01
    private short                   gainChannel3;       // 0x0A number format 1.15 unsign
    private EnergyConfig            energyConfig;       // 0x0C
    private byte[]                  biquadTypes;        // 0x18 0x07
    private byte                    outputMode;         // 0x1F balance/unbalance

    private short                   dataBufLength;      // 0x20
    private short                   dataBytesLength;    // 0x22
    @NonNull
    private List<HiFiToyDataBuf>    dataBufs = new ArrayList<>(); // 0x24


    private byte[] importData;

    public PeripheralData(HiFiToyDevice device) {
        i2cAddr = I2C_ADDR;
        successWriteFlag = 0;
        version = VERSION;
        pairingCode = device.getPairingCode();
        initDspDelay = INIT_DSP_DELAY;
        advertiseMode = device.getAdvertiseMode().getMode();
        gainChannel3 = device.getOutputMode().getGainCh3(); // 0x4000
        energyConfig = device.getEnergyConfig();
        setBiquadTypes(device.getActivePreset().getFilters().getBiquadTypes());
        outputMode = device.getOutputMode().isUnbalance() ? (byte)1 : 0;
        dataBufs = new ArrayList<>();

        List<HiFiToyDataBuf> dataBufs = device.getActivePreset().getDataBufs();
        appendAmModeDataBuf(dataBufs, device.getAmMode(), device.getNewPDV21Hw());

        setDataBufs(dataBufs);
    }
    public PeripheralData(byte[] biquadTypes, List<HiFiToyDataBuf> dataBufs) {
        clear();

        HiFiToyDevice dev = HiFiToyControl.getInstance().getActiveDevice();

        setBiquadTypes(biquadTypes);
        outputMode = dev.getOutputMode().isUnbalance() ? (byte)1 : 0;

        appendAmModeDataBuf(dataBufs, dev.getAmMode(), dev.getNewPDV21Hw());

        setDataBufs(dataBufs);
    }

    public PeripheralData() {
        clear();
    }

    public void clear() {
        i2cAddr = I2C_ADDR;
        successWriteFlag = 0;
        version = VERSION;
        pairingCode = 0;
        initDspDelay = INIT_DSP_DELAY;
        advertiseMode = AdvertiseMode.ALWAYS_ENABLED;

        OutputMode om = new OutputMode();

        gainChannel3 = om.getGainCh3(); // 0x4000
        energyConfig = new EnergyConfig();
        setBiquadTypes(new byte[]{BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC,
                BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC, BIQUAD_PARAMETRIC});
        outputMode = om.isUnbalance() ? (byte)1 : 0;

        dataBufLength = 0;
        dataBytesLength = 0;
        dataBufs = new ArrayList<>();
    }

    public short getDataBytesLength() {
        return dataBytesLength;
    }
    public short getDataBufLength() {
        return (short)(dataBytesLength - PERIPHERAL_CONFIG_LENGTH);
    }

    private short calcDataBytesLength(List<HiFiToyDataBuf> dataBufs) {
        short length = 0;

        for (int i = 0; i < dataBufs.size(); i++) {
            HiFiToyDataBuf buf = dataBufs.get(i);
            length += buf.getLength() + 2;
        }
        return (short)(length + PERIPHERAL_CONFIG_LENGTH);
    }

    private boolean setBiquadTypes(byte[] biquadTypes) {
        if (biquadTypes.length == 7) {
            this.biquadTypes = biquadTypes;
            return true;
        }
        return false;
    }

    private HiFiToyDataBuf findDataBufWithAddr(byte addr, List<HiFiToyDataBuf> dataBufs) {
        for (HiFiToyDataBuf db : dataBufs) {
            if (db.getAddr() == addr) {
                return db;
            }
        }
        return null;
    }

    private void restrictBassFilterGain(List<HiFiToyDataBuf> dataBufs) {
        HiFiToyDataBuf bassFilterBuf = findDataBufWithAddr(TAS5558.BASS_FILTER_SET_REG, dataBufs);
        if (bassFilterBuf != null) {

            byte[] bb = bassFilterBuf.getData().array();
            byte bassFilterGain = bb[7];

            // 0x12 - 0db, if < 0x12 -> gain > 0db
            if (bassFilterGain < 0x12) {
                bb[7] = 0x12;
                bassFilterBuf.setData(ByteBuffer.wrap(bb));
            }
        }
    }

    private void appendAmModeDataBuf(List<HiFiToyDataBuf> dataBufs, AMMode amMode, boolean newHW) {
        //set AMMode reg for fix whistles bug in PDV2.1
        if (amMode.isEnabled()) {
            dataBufs.add(0, amMode.getDataBufs().get(0));

            //and delete bass filter buf for fix bug incorrect launch hw for old hw
            if (!newHW) {
                restrictBassFilterGain(dataBufs);
            }
        }
    }

    private void setDataBufs(List<HiFiToyDataBuf> dataBufs) {
        dataBufLength = (short)dataBufs.size();
        dataBytesLength = calcDataBytesLength(dataBufs);

        this.dataBufs = dataBufs;
    }
    @NonNull
    public List<HiFiToyDataBuf> getDataBufs() {
        return dataBufs;
    }

    private ByteBuffer getBinary() {
        ByteBuffer data = ByteBuffer.allocate(PERIPHERAL_CONFIG_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        data.put(i2cAddr);
        data.put(successWriteFlag);
        data.putShort(version);
        data.putInt(pairingCode);
        data.put(initDspDelay);
        data.put(advertiseMode);
        data.putShort(gainChannel3);
        data.put(energyConfig.getBinary());
        data.put(biquadTypes);
        data.put(outputMode);
        data.putShort(dataBufLength);
        data.putShort(dataBytesLength);

        for (HiFiToyDataBuf db : dataBufs) {
            data = BinaryOperation.concatData(data, db.getBinary());
        }
        data.position(0);

        return data;
    }

    private ByteBuffer getBiquadTypeBinary() {
        ByteBuffer data = getBinary();
        //length  = 7
        return BinaryOperation.copyOfRange(data, BIQUAD_TYPE_OFFSET, BIQUAD_TYPE_OFFSET + 7);
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
        //need skip outputType field
        HiFiToyControl.getInstance().sendBufToDsp(BIQUAD_TYPE_OFFSET , getBiquadTypeBinary());
        HiFiToyControl.getInstance().sendBufToDsp(PRESET_DATA_OFFSET , getPresetBinary());
        HiFiToyControl.getInstance().sendWriteFlag((byte)1);
        HiFiToyControl.getInstance().setInitDsp();
    }

    public void exportPresetWithDialog(String title) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        Context c = ApplicationContext.getInstance().getContext();
        c.registerReceiver(broadcastReceiver, makeExportIntentFilter());

        int cap = getBiquadTypeBinary().capacity() + getPresetBinary().capacity();

        DialogSystem.getInstance().showProgressDialog(title, cap);
        exportPreset();
    }

    private boolean parseHeader(byte[] data) {
        if (data.length < PERIPHERAL_CONFIG_LENGTH) {
            return false;
        }

        ByteBuffer b = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        //parse peripheral config
        i2cAddr = b.get();
        successWriteFlag = b.get();
        version = b.getShort();
        pairingCode = b.getInt();
        initDspDelay = b.get();
        advertiseMode = b.get();
        gainChannel3 = b.getShort();
        energyConfig.parseBinary(b);

        biquadTypes = new byte[7];
        b.get(biquadTypes, 0, 7);

        outputMode = b.get();
        dataBufLength = b.getShort();
        dataBytesLength = b.getShort();

        return true;
    }

    public void importHeader(final PostProcess postProcess) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        final Context c = ApplicationContext.getInstance().getContext();
        c.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (HiFiToyControl.DID_GET_PARAM_DATA.equals(intent.getAction())) {
                    byte[] data = intent.getByteArrayExtra(EXTRA_DATA);

                    importData = BinaryOperation.concatData(importData, data);

                    if (importData.length == 40) {
                        c.unregisterReceiver(this);
                        boolean res = parseHeader(importData);
                        if (!res) { // error
                            dataBytesLength = -1;
                        }

                        if (postProcess != null) {
                            postProcess.onPostProcess();
                        }

                    } else {
                        HiFiToyControl.getInstance().getDspDataWithOffset((short)20);
                    }

                }
            }
        }, new IntentFilter(HiFiToyControl.DID_GET_PARAM_DATA));

        importData = new byte[0];
        //start read first 20 bytes
        HiFiToyControl.getInstance().getDspDataWithOffset((short)0);

    }

    private void _import(final PostProcess postProcess){
        if (!HiFiToyControl.getInstance().isConnected()) return;

        importHeader(new PostProcess() {
            @Override
            public void onPostProcess() {
                if (dataBytesLength == -1) {
                    DialogSystem.getInstance().showDialog("Warning", "Import preset is not success.", "Ok");
                    return;
                }

                final Context c = ApplicationContext.getInstance().getContext();
                c.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (HiFiToyControl.DID_GET_PARAM_DATA.equals(intent.getAction())) {
                            byte[] data = intent.getByteArrayExtra(EXTRA_DATA);
                            DialogSystem.getInstance().updateProgressDialog(data.length);

                            //add new 20bytes to importData
                            importData = BinaryOperation.concatData(importData, data);
                            Log.d(TAG, "import " + importData.length);

                            //check finish, else read next 20 bytes
                            if (importData.length >= getDataBufLength()) {
                                c.unregisterReceiver(this);
                                DialogSystem.getInstance().closeProgressDialog();

                                handleImportData(ByteBuffer.wrap(importData).order(ByteOrder.LITTLE_ENDIAN));

                                if (postProcess != null) {
                                    postProcess.onPostProcess();
                                }

                            } else {
                                //read next 20bytes
                                short offset = (short)(PERIPHERAL_CONFIG_LENGTH + importData.length);
                                HiFiToyControl.getInstance().getDspDataWithOffset(offset);
                            }


                        }
                    }
                }, new IntentFilter(HiFiToyControl.DID_GET_PARAM_DATA));

                DialogSystem.getInstance().getProgressDialog().setMax(dataBytesLength - 40);

                importData = new byte[0];
                //start read first data 20 bytes
                HiFiToyControl.getInstance().getDspDataWithOffset(PERIPHERAL_CONFIG_LENGTH);
            }
        });
    }

    public void importWithDialog(String title, final PostProcess postProcess) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        //real max packets calculate after get first packet
        DialogSystem.getInstance().showProgressDialog(title, PERIPHERAL_CONFIG_LENGTH);
        _import(postProcess);
    }

    private void handleImportData(ByteBuffer b) {
        dataBufs = new ArrayList<>();
        for (int i = 0; i < dataBufLength; i++) {
            dataBufs.add(new HiFiToyDataBuf(b));
        }

        Log.d(TAG, "Peripheral data import finished success.");
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