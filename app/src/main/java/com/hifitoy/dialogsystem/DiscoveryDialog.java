/*
 *   DiscoveryDialog.java
 *
 *   Created by Artem Khlyupin on 09/12/20
 *   Copyright © 2020 Artem Khlyupin. All rights reserved.
 */
package com.hifitoy.dialogsystem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;

import java.util.ArrayList;

public class DiscoveryDialog extends BaseDialog implements HiFiToyControl.DiscoveryDelegate,
                                        HiFiToyControl.ConnectionDelegate {
    private BleDeviceListAdapter adapter = new BleDeviceListAdapter();

    public DiscoveryDialog(Context context) {
        super(context);

        Activity a = (Activity)context;
        View body = a.getLayoutInflater().inflate(R.layout.dialog_discovery, null);
        View title = a.getLayoutInflater().inflate(R.layout.dialog_discovery_title, null);
        ListView lv = body.findViewById(R.id.deviceListView_outl);
        lv.setAdapter(adapter);

        setCustomTitle(title);
        setView(body);
        setOnDismissListener(listener);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HiFiToyDevice d = adapter.getItem(position);
                HiFiToyDevice activeDev = HiFiToyControl.getInstance().getActiveDevice();
                boolean connected = HiFiToyControl.getInstance().isConnected();
                boolean equals = activeDev.getMac().equals(d.getMac());

                if ((equals) && (connected)) {
                    DialogSystem.OnClickDialog handler = new DialogSystem.OnClickDialog() {
                        @Override
                        public void onPositiveClick() {
                            HiFiToyControl.getInstance().disconnect();
                        }

                        @Override
                        public void onNegativeClick() {

                        }
                    };
                    DialogSystem.getInstance().showDialog(handler, "Warning",
                            "Are you sure want to disconnect?", "Disconnect", "Cancel");

                } else {
                    HiFiToyControl.getInstance().connect(d);
                }

                adapter.notifyDataSetInvalidated();
            }
        });

    }

    @Override
    public void show() {
        super.show();

        HiFiToyControl.getInstance().setConnectionDelegate(this);
        HiFiToyControl.getInstance().startDiscovery(this);
    }

    private DialogInterface.OnDismissListener listener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            HiFiToyControl.getInstance().stopDiscovery();
            HiFiToyControl.getInstance().setConnectionDelegate(null);
        }
    };

    @Override
    public void didFindPeripheral(HiFiToyDevice device) {
        adapter.addDevice(device);
        notifyDataSetChanged();
    }

    @Override
    public void didConnect(HiFiToyDevice device) {
        notifyDataSetChanged();
    }

    @Override
    public void didDisconnect() {
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        };
        mainHandler.post(myRunnable);
    }

    private static class BleDeviceListAdapter extends BaseAdapter {
        private ArrayList<HiFiToyDevice> devices = new ArrayList<>();

        private void addDevice(HiFiToyDevice device) {
            if(!devices.contains(device)) {
                devices.add(0, device);
            }
        }

        private void clear() {
            devices.clear();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public HiFiToyDevice getItem(int i) {
            return devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Activity a = (Activity) ApplicationContext.getInstance().getContext();

            if (view == null) {
                view = a.getLayoutInflater().inflate(R.layout.device_item, null);
            }
            TextView deviceName = view.findViewById(R.id.deviceName_outl);
            TextView connectBtn = view.findViewById(R.id.deviceConnectBtn_outl);

            HiFiToyDevice device = devices.get(i);
            HiFiToyDevice activeDevice = (HiFiToyControl.getInstance().getActiveDevice());
            boolean equals = device.getMac().equals(activeDevice.getMac());
            boolean connected = HiFiToyControl.getInstance().isConnectionReady();

            deviceName.setText(device.getName());

            Drawable drawable;
            if ( (equals) && (connected) ) {
                drawable = a.getResources().getDrawable(R.drawable.active_discovery_btn, a.getTheme());
            } else {
                drawable = a.getResources().getDrawable(R.drawable.discovery_btn, a.getTheme());
            }
            connectBtn.setBackground(drawable);

            return view;
        }
    }
}
