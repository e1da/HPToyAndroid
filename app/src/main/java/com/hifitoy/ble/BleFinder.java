package com.hifitoy.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import com.hifitoy.R;
import java.util.LinkedList;
import java.util.List;

public class BleFinder {
    private String TAG = "BleFinder";

    private IBleFinderDelegate delegate = null;

    private List<BluetoothDevice> devices = new LinkedList<>();
    private BluetoothAdapter mBluetoothAdapter;

    public BleFinder(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

    }

    public static BleFinder BleFinder(Context context, IBleFinderDelegate delegate) {
        BleFinder finder = new BleFinder(context);
        finder.setBleFinderDelegate(delegate);

        return finder;
    }



    public void setBleFinderDelegate(IBleFinderDelegate delegate) {
        this.delegate = delegate;
    }

    public List<BluetoothDevice> getDevices() {
        return devices;
    }

    public void clear() {
        devices.clear();
    }

    public void startDiscovery() {
        devices.clear();

        if (mBluetoothAdapter == null) return;

        mBluetoothAdapter.getBluetoothLeScanner().startScan(new BleScanCallBack());
        Log.d(TAG, "BLE Scanning...");
    }

    public void stopDiscovery() {
        if (mBluetoothAdapter == null) return;

        mBluetoothAdapter.getBluetoothLeScanner().stopScan(new BleScanCallBack());
        Log.d(TAG, "BLE Stop Scanning");
    }

    private class BleScanCallBack extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if ((!devices.contains(device)) &&
                    (device.getName() != null) &&
                    (device.getName().equals("HiFiToy"))) {

                devices.add(device);

                if (delegate != null) {
                    delegate.didFindNewPeripheral(device);
                }

                Log.d(TAG, "Find ble device: " + device.getName());
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

    public interface IBleFinderDelegate {
        void didFindNewPeripheral(BluetoothDevice device);
    }

}
