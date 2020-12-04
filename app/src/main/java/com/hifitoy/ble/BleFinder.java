/*
 *   BleFinder.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;

import java.util.LinkedList;
import java.util.List;

public class BleFinder {
    private static final String TAG = "HiFiToy";

    private IBleFinderDelegate  delegate;
    private List<String>        deviceAddressList;
    private boolean             discovering;


    public interface IBleFinderDelegate {
        void didFindNewPeripheral(String macAddress);
    }

    public BleFinder() {
        deviceAddressList = new LinkedList<>();
        this.delegate = null;
        this.discovering = false;
    }

    public void setBleFinderDelegate(IBleFinderDelegate delegate) {
        this.delegate = delegate;
    }

    public List<String> getDeviceAddressList() {
        return deviceAddressList;
    }

    public void clear() {
        deviceAddressList.clear();
    }

    public boolean isDiscovering() {
        return discovering;
    }

    public void startDiscovery() {
        clear();
        if ( (!Ble.getInstance().isEnabled()) || (discovering) ) return;

        //add to list connected devices
        List<BluetoothDevice> bdList = Ble.getInstance().getConnectedDevices();
        for (BluetoothDevice bd : bdList) {
            addDeviceToList(bd);
        }

        //start scanning
        BluetoothAdapter ba = Ble.getInstance().getBluetoothAdapter();
        ba.getBluetoothLeScanner().startScan(new BleScanCallBack());

        discovering = true;
        Log.d(TAG, "BLE Scanning...");
    }

    public void stopDiscovery() {
        if (!Ble.getInstance().isEnabled()) return;

        BluetoothAdapter ba = Ble.getInstance().getBluetoothAdapter();
        ba.getBluetoothLeScanner().stopScan(new BleScanCallBack());

        discovering = false;
        Log.d(TAG, "BLE Stop Scanning");
    }

    private void addDeviceToList(BluetoothDevice bd) {
        Context c = ApplicationContext.getInstance().getContext();

        if (    (bd.getName() != null) &&
                (bd.getName().equals(c.getResources().getString(R.string.ble_device_name))) &&
                (!deviceAddressList.contains(bd.getAddress()))) {

            deviceAddressList.add(bd.getAddress());

            Log.d(TAG, "Find ble device: " + bd.getName() + " " + bd.getAddress());

            if (delegate != null) {
                delegate.didFindNewPeripheral(bd.getAddress());
            }
        }
    }

    private class BleScanCallBack extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addDeviceToList(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed");
        }
    }

}
