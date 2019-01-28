/*
 *   DiscoveryActivity.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;

import java.util.ArrayList;

public class DiscoveryActivity extends ListActivity implements HiFiToyControl.DiscoveryDelegate {
    private static final String TAG = "HiFiToy";
    private LeDeviceListAdapter mLeDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApplicationContext.getInstance().setContext(this);

        HiFiToyControl.getInstance().init(this);

        requestPermissions(new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);

        mLeDeviceListAdapter = new LeDeviceListAdapter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){

        if(requestCode == 1) {
            boolean blePermission = true;

            for (int i = 0; i < permissions.length; i++) {


                switch (permissions[i]) {
                    case Manifest.permission.BLUETOOTH:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.BLUETOOTH is not granted.");
                        }
                        break;
                    case Manifest.permission.BLUETOOTH_ADMIN:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.BLUETOOTH_ADMIN is not granted.");
                        }
                        break;
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.ACCESS_FINE_LOCATION is not granted.");
                        }
                        break;
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            blePermission = false;
                            Log.d(TAG, "permission.ACCESS_COARSE_LOCATION is not granted.");
                        }
                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "permission.READ_EXTERNAL_STORAGE is not granted.");
                        }
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "permission.WRITE_EXTERNAL_STORAGE is not granted.");
                        }
                        break;


                }
            }

            if (blePermission) {
                HiFiToyControl.getInstance().setBlePermissionGranted(true);

                if (!HiFiToyControl.getInstance().isBleEnabled()) {
                    //show ble enable request dialog
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { // ble enabled dialog result
            if (resultCode != Activity.RESULT_OK) {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setListAdapter(mLeDeviceListAdapter);
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();

        HiFiToyControl.getInstance().startDiscovery(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.discovery_menu, menu);
        return true;
    }

    //info button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.discovery_info:
                DialogSystem.getInstance().showDialog("Info",
                        getString(R.string.discovery_info), "Close");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HiFiToyDevice device = mLeDeviceListAdapter.getDevice(position);

        if (device == null) return;

        HiFiToyControl.getInstance().stopDiscovery();

        if (device.getMac().equals("demo")){
            Toast.makeText(getApplicationContext(), R.string.demo_mode, Toast.LENGTH_SHORT).show();
        }

        HiFiToyControl.getInstance().connect(device);

        Intent intentActivity = new Intent(this, MainControlActivity.class);
        startActivity(intentActivity);
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<HiFiToyDevice> devices;
        private LayoutInflater mInflator;

        private LeDeviceListAdapter() {
            super();
            devices = new ArrayList<>();
            addDemoDevice();

            mInflator = DiscoveryActivity.this.getLayoutInflater();
        }

        private void addDevice(HiFiToyDevice device) {
            if(!devices.contains(device)) {
                devices.add(0, device);
            }
        }

        private void addDemoDevice() {
            HiFiToyDevice demoDevice = HiFiToyDeviceManager.getInstance().getDevice("demo");
            if (demoDevice == null){//not exist
                demoDevice = new HiFiToyDevice();
                HiFiToyDeviceManager.getInstance().setDevice("demo", demoDevice);
            }
            addDevice(demoDevice);
        }

        private HiFiToyDevice getDevice(int position) {
            return devices.get(position);
        }

        private void clear() {
            devices.clear();
            addDemoDevice();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int i) {
            return devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            if (view == null) {
                view = mInflator.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceBorder_outl = (TextView) view.findViewById(R.id.device_border);
                viewHolder.deviceName_outl = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder= (ViewHolder) view.getTag();
            }

            HiFiToyDevice device = devices.get(i);
            if (device.getMac().equals("demo")){
                viewHolder.deviceBorder_outl.setVisibility(View.VISIBLE);
            } else {
                viewHolder.deviceBorder_outl.setVisibility(View.INVISIBLE);
            }

            String name = device.getName();
            if ((name != null) && (name.length() > 0)) {
                viewHolder.deviceName_outl.setText(name);
            } else {
                viewHolder.deviceName_outl.setText(R.string.unknown_device);
            }

            return view;
        }

        private class ViewHolder {
            TextView    deviceBorder_outl;
            TextView    deviceName_outl;
        }
    }

    /* ------------------------- DiscoveryDelegate ----------------------------*/
    public void didFindPeripheral(HiFiToyDevice device) {
        mLeDeviceListAdapter.addDevice(device);
        mLeDeviceListAdapter.notifyDataSetChanged();
    }

}
