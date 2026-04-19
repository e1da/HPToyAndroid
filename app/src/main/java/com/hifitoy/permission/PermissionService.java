/*
 *   PermissionService.java
 */
package com.hifitoy.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import com.hifitoy.R;

public class PermissionService {
    private final Context context;

    public PermissionService(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT < 31) return true;

        return context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasBluetoothScanPermission() {
        if (Build.VERSION.SDK_INT < 31) return true;

        return context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasRequiredBlePermissions() {
        return hasBluetoothScanPermission() && hasBluetoothConnectPermission();
    }

    public boolean ensureBluetoothConnectPermission(boolean notifyUser) {
        if (hasBluetoothConnectPermission()) {
            return true;
        }

        if (notifyUser) {
            Toast.makeText(context, R.string.bluetooth_connect_permission_required,
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean ensureBluetoothScanPermission(boolean notifyUser) {
        if (hasBluetoothScanPermission()) {
            return true;
        }

        if (notifyUser) {
            Toast.makeText(context, R.string.bluetooth_scan_permission_required,
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
