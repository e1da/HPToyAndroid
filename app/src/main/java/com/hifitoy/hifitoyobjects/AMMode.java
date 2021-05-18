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
import com.hifitoy.hifitoycontrol.CommonCommand;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
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

    private byte[] data             = new byte[]{0x00, 0x1a, 0x09, (byte)0xd0};
    private boolean successImport   = false;

    public AMMode() {
        reset();
    }

    public void reset() {
        data[0] = 0x00;
        data[1] = 0x1a;
        data[2] = 0x09;
        data[3] = (byte)0xd0;

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

    @Override
    public byte getAddress() {
        return TAS5558.AM_MODE_REG;
    }

    @Override
    public String getInfo() {
        return String.format("D31-24: " + Integer.toHexString(data[0]) +
                " D23-16: " + Integer.toHexString(data[1]) +
                " D15-8: " + Integer.toHexString(data[2]) +
                " D7-0: " + Integer.toHexString(data[3]) );
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

    public void readFromDsp() {
        if (!HiFiToyControl.getInstance().isConnected()) return;

        Context c = ApplicationContext.getInstance().getContext();
        c.registerReceiver(broadcastReceiver,
                new IntentFilter(HiFiToyControl.DID_GET_PARAM_DATA));

        //send ble command
        HiFiToyControl.getInstance().getDspDataWithOffset(FIRST_DATA_BUF_OFFSET);
    }

    private transient final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (HiFiToyControl.DID_GET_PARAM_DATA.equals(action)) {
                Context c = ApplicationContext.getInstance().getContext();
                c.unregisterReceiver(broadcastReceiver);

                byte[] data = intent.getByteArrayExtra(EXTRA_DATA);
                parseFirstDataBuf(data);
            }
        }
    };

    private void parseFirstDataBuf(byte[] data) {
        HiFiToyDataBuf buf = new HiFiToyDataBuf(ByteBuffer.wrap(data));
        boolean res = importFromDataBufs(new ArrayList<>(Collections.singletonList(buf)));
        ApplicationContext.getInstance().setupOutlets();

        if (!res) {
            ApplicationContext.getInstance().showToast("Settings not found on the hardware.");
        }

    }

    public void storeToPeripheral() {
        HiFiToyControl.getInstance().getActiveDevice().getActivePreset().storeToPeripheral();
    }
}
