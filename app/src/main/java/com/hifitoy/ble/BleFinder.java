/*
 *   BleFinder.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class BleFinder {
    private static final String TAG = "HiFiToy";

    private final BleService bleService;
    private IBleFinderDelegate delegate;
    private final List<String> deviceAddressList;
    private boolean discovering;

    public interface IBleFinderDelegate {
        void didFindNewPeripheral(String macAddress, String peripheralName);
    }

    public BleFinder(BleService bleService) {
        this.bleService = bleService;
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

    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        clear();
        if ((!bleService.isEnabled()) || discovering) return;

        //add to list connected devices
        List<BluetoothDevice> bdList = bleService.getConnectedDevices();
        for (BluetoothDevice bd : bdList) {
            addDeviceToList(bd);
        }

        //start scanning
        BluetoothAdapter ba = bleService.getBluetoothAdapter();
        ba.getBluetoothLeScanner().startScan(new BleScanCallBack());

        discovering = true;
        Log.d(TAG, "BLE Scanning...");
    }

    @SuppressLint("MissingPermission")
    public void stopDiscovery() {
        if (!bleService.isEnabled()) return;

        BluetoothAdapter ba = bleService.getBluetoothAdapter();
        ba.getBluetoothLeScanner().stopScan(new BleScanCallBack());

        discovering = false;
        Log.d(TAG, "BLE Stop Scanning");
    }

    @SuppressLint("MissingPermission")
    private void addDeviceToList(BluetoothDevice bd) {
        String peripheralName = bd.getName();

        if ((peripheralName != null) && (!deviceAddressList.contains(bd.getAddress()))) {
            deviceAddressList.add(bd.getAddress());

            Log.d(TAG, "Find ble device: " + peripheralName + " " + bd.getAddress());

            if (delegate != null) {
                delegate.didFindNewPeripheral(bd.getAddress(), peripheralName);
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
