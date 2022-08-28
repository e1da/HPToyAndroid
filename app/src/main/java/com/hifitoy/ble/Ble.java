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

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;

import java.util.List;

import static android.bluetooth.BluetoothProfile.GATT;

public class Ble {
    private static Ble instance;

    private BluetoothAdapter bluetoothAdapter;

    public static synchronized Ble getInstance() {
        if (instance == null){
            instance = new Ble();
        }
        return instance;
    }

    public Ble() {
        Context context = ApplicationContext.getInstance().getContext();

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }

        final BluetoothManager bluetoothManager = getBluetoothManager();
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = null;
        }
    }

    public boolean isSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isEnabled() {
        return ( (isSupported()) && (bluetoothAdapter.isEnabled()) );
    }

    public BluetoothManager getBluetoothManager() {
        Context context = ApplicationContext.getInstance().getContext();
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
