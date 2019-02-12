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
import android.util.Log;
import java.util.LinkedList;
import java.util.List;

public class BleFinder {
    private static final String TAG = "HiFiToy";

    private BluetoothAdapter    mBluetoothAdapter;
    private IBleFinderDelegate  delegate;
    private List<String>        deviceAddressList;
    private boolean             discovering;


    public interface IBleFinderDelegate {
        void didFindNewPeripheral(String macAddress);
    }

    public BleFinder(BluetoothAdapter bluetoothAdapter) {
        deviceAddressList = new LinkedList<>();
        mBluetoothAdapter = bluetoothAdapter;
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

        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled()) ) return;

        mBluetoothAdapter.getBluetoothLeScanner().startScan(new BleScanCallBack());
        discovering = true;
        Log.d(TAG, "BLE Scanning...");
    }

    public void stopDiscovery() {
        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled()) ) return;

        mBluetoothAdapter.getBluetoothLeScanner().stopScan(new BleScanCallBack());
        discovering = false;
        Log.d(TAG, "BLE Stop Scanning");
    }

    private class BleScanCallBack extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (    (device.getName() != null) &&
                    (device.getName().equals("HiFiToyPeripheral")) &&
                    (!deviceAddressList.contains(device.getAddress()))) {

                deviceAddressList.add(device.getAddress());

                Log.d(TAG, "Find ble device: " + device.getName() + " " + device.getAddress());

                if (delegate != null) {
                    delegate.didFindNewPeripheral(device.getAddress());
                }
            }
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
