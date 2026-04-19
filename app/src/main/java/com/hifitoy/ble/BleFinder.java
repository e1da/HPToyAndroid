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

import com.hifitoy.permission.PermissionService;

import java.util.LinkedList;
import java.util.List;

public class BleFinder {
    private static final String TAG = "HiFiToy";

    private final BleService bleService;
    private final PermissionService permissionService;
    private IBleFinderDelegate delegate;
    private final List<String> deviceAddressList;
    private boolean discovering;

    public interface IBleFinderDelegate {
        void didFindNewPeripheral(String macAddress, String peripheralName);
    }

    public BleFinder(BleService bleService, PermissionService permissionService) {
        this.bleService = bleService;
        this.permissionService = permissionService;
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
        if ((!bleService.isEnabled()) || discovering) return;
        if (!permissionService.hasBluetoothScanPermission()) {
            Log.w(TAG, "BLE scan permission denied.");
            return;
        }

        //add to list connected devices
        List<BluetoothDevice> bdList = bleService.getConnectedDevices();
        for (BluetoothDevice bd : bdList) {
            addDeviceToList(bd);
        }

        //start scanning
        BluetoothAdapter adapter = bleService.getBluetoothAdapter();
        if (adapter == null || adapter.getBluetoothLeScanner() == null) return;

        try {
            adapter.getBluetoothLeScanner().startScan(new BleScanCallBack());
        } catch (SecurityException e) {
            Log.w(TAG, "BLE scan permission denied.");
            return;
        }

        discovering = true;
        Log.d(TAG, "BLE Scanning...");
    }

    public void stopDiscovery() {
        if (!bleService.isEnabled()) return;
        if (!permissionService.hasBluetoothScanPermission()) {
            Log.w(TAG, "BLE stop scan permission denied.");
            return;
        }

        BluetoothAdapter adapter = bleService.getBluetoothAdapter();
        if (adapter == null || adapter.getBluetoothLeScanner() == null) return;

        try {
            adapter.getBluetoothLeScanner().stopScan(new BleScanCallBack());
        } catch (SecurityException e) {
            Log.w(TAG, "BLE stop scan permission denied.");
            return;
        }

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
