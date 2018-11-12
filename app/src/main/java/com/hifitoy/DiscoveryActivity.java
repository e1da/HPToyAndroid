package com.hifitoy;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.hifitoy.ble.BleFinder;

public class DiscoveryActivity extends AppCompatActivity implements BleFinder.IBleFinderDelegate {
    private String TAG = "DiscoveryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        BleFinder bleFinder = new BleFinder(this);
        bleFinder.startDiscovery();
    }

    public void didFindNewPeripheral(BluetoothDevice device) {
        Log.d(TAG, device.getName());

    }


}
