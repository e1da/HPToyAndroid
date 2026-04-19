/*
 *   Ble.java
 *
 *   Created by Artem Khlyupin on 04/12/2020
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.hifitoy.R;

import java.util.List;

import static android.bluetooth.BluetoothProfile.GATT;

public class BleService {
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;

    public BleService(Context context) {
        this.context = context.getApplicationContext();

        if (!this.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this.context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }

        bluetoothAdapter = (getBluetoothManager() != null) ? getBluetoothManager().getAdapter() : null;
    }

    public boolean isEnabled() {
        return ((bluetoothAdapter != null) && bluetoothAdapter.isEnabled());
    }

    public BluetoothManager getBluetoothManager() {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
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

    @SuppressLint("MissingPermission")
    public List<BluetoothDevice> getConnectedDevices() {
        return getBluetoothManager().getConnectedDevices(GATT);
    }
}
