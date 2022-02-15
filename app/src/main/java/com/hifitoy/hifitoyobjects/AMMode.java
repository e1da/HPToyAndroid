/*
 *   AMMode.java
 *
 *   Created by Artem Khlyupin on 17/05/21
 *   Copyright © 2021 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.hifitoyobjects;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.hifitoy.ApplicationContext;
import com.hifitoy.ble.BlePacket;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.ToyPreset;
import com.hifitoy.hifitoydevice.PeripheralData;
import com.hifitoy.hifitoynumbers.ByteUtility;
import com.hifitoy.tas5558.TAS5558;
import com.hifitoy.xml.XmlData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hifitoy.ApplicationContext.EXTRA_DATA;

public class AMMode implements HiFiToyObject, Cloneable, Serializable {
    private static final String TAG = "HiFiToy";

    private static final short FIRST_DATA_BUF_OFFSET = 0x24;

    private byte[] data = new byte[4];
    private boolean successImport   = false;

    public AMMode() {
        reset();
    }

    public void reset() {
        data[0] = 0x00;
        data[1] = 0x09;
        data[2] = 0x03;
        data[3] = (byte)0xF2;

        successImport = false;
    }

    public byte getData(int index) {
        if (index > 3) index = 3;
        if (index < 0) index = 0;
        return data[index];
    }

    public void setData(int index, byte d) {
        if (index > 3) index = 3;
        if (index < 0) index = 0;
        data[index] = d;
    }

    public boolean isEnabled() {
        return ((data[1] & 0x10) != 0);
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            data[1] |= 0x10; // set
        } else {
            data[1] &= ~0x10; // clear
        }
    }

    private boolean isSuccessImport() {
        return successImport;
    }

    @Override
    public byte getAddress() {
        return TAS5558.AM_MODE_REG;
    }

    @Override
    public String getInfo() {
        return String.format("D31-24: 0x" + ByteUtility.toHexString(data[0]) +
                " D23-16: 0x" + ByteUtility.toHexString(data[1]) +
                " D15-8: 0x" + ByteUtility.toHexString(data[2]) +
                " D7-0: 0x" + ByteUtility.toHexString(data[3]) );
    }

    @Override
    public void sendToPeripheral(boolean response) {
        BlePacket p = new BlePacket(BinaryOperation.getBinary(getDataBufs()), 20, response);
        HiFiToyControl.getInstance().sendDataToDsp(p);
    }

    @Override
    public List<HiFiToyDataBuf> getDataBufs() {
        HiFiToyDataBuf buf = new HiFiToyDataBuf(getAddress(), ByteBuffer.wrap(data));
        return new ArrayList<>(Collections.singletonList(buf));
    }

    @Override
    public boolean importFromDataBufs(List<HiFiToyDataBuf> dataBufs) {
        successImport = false;

        for (HiFiToyDataBuf buf : dataBufs) {
            if ((buf.getAddr() == getAddress()) && (buf.getLength() == 4)) {
                for (int i = 0; i < 4; i++) {
                    data[i] = buf.getData().get(i);
                }

                Log.d(TAG, "AMMode import success.");

                successImport = true;
                break;
            }
        }
        return successImport;
    }

    @Override
    public XmlData toXmlData() {
        return null;
    }

    @Override
    public void importFromXml(XmlPullParser xmlParser) throws XmlPullParserException, IOException {

    }

    private transient PostProcess afterReadFromDspProcess = null;

    public void readFromDsp(PostProcess postProcess) {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        afterReadFromDspProcess = postProcess;

        final Context c = ApplicationContext.getInstance().getContext();

        c.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (HiFiToyControl.DID_GET_PARAM_DATA.equals(action)) {
                    c.unregisterReceiver(this);

                    byte[] data = intent.getByteArrayExtra(EXTRA_DATA);
                    parseFirstDataBuf(data);
                }
            }
        }, new IntentFilter(HiFiToyControl.DID_GET_PARAM_DATA));

        //send ble command
        HiFiToyControl.getInstance().getDspDataWithOffset(FIRST_DATA_BUF_OFFSET);
    }

    private void parseFirstDataBuf(byte[] data) {
        HiFiToyDataBuf buf = new HiFiToyDataBuf(ByteBuffer.wrap(data));
        importFromDataBufs(new ArrayList<>(Collections.singletonList(buf)));
        ApplicationContext.getInstance().setupOutlets();

        if (afterReadFromDspProcess != null) {
            afterReadFromDspProcess.onPostProcess();
        }

    }

    public void storeToPeripheral() {
        ToyPreset p = HiFiToyControl.getInstance().getActiveDevice().getActivePreset();

        PeripheralData peripheralData = new PeripheralData(p.getFilters().getBiquadTypes(),
                p.getDataBufs());
        peripheralData.exportPresetWithDialog("Beat-tones update...");
    }
}
