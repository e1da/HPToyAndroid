/*
 *   Ble.java
 *
 *   Created by Artem Khlyupin on 04/12/2020
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.hifitoy.R;
import com.hifitoy.permission.PermissionService;

import java.util.Collections;
import java.util.List;

import static android.bluetooth.BluetoothProfile.GATT;

public class BleService {
    private final Context context;
    private final PermissionService permissionService;
    private final BluetoothAdapter bluetoothAdapter;

    public BleService(Context context, PermissionService permissionService) {
        this.context = context.getApplicationContext();
        this.permissionService = permissionService;

        if (!this.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this.context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }

        bluetoothAdapter = (getBluetoothManager() != null) ? getBluetoothManager().getAdapter() : null;
    }

    public boolean isEnabled() {
        return ((bluetoothAdapter != null) && bluetoothAdapter.isEnabled());
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public BluetoothDevice getRemoteDevice(String mac) {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.getRemoteDevice(mac);
        }
        return null;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (!permissionService.hasBluetoothConnectPermission()) {
            return Collections.emptyList();
        }

        try {
            if (getBluetoothManager() == null) {
                return Collections.emptyList();
            }

            return getBluetoothManager().getConnectedDevices(GATT);
        } catch (SecurityException e) {
            return Collections.emptyList();
        }
    }

    private BluetoothManager getBluetoothManager() {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }
}
