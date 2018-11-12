package com.hifitoy.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class BleDevice {
    private String TAG = "BleDevice";

    private IBleDeviceDelegate delegate;

    private Context context;
    private Queue<BlePacket> packets = null;
    private Queue<BluetoothGattCharacteristic> descriptorPackets = null;

    private BluetoothDevice device;
    private BluetoothGatt mBluetoothGatt;

    private enum DeviceState {
        DISCONNECTED, CONNECTING, CONNECTED, CONNECTION_READY;
    };
    private DeviceState deviceState;

    public final static UUID FFF0_UUID =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public final static UUID FFF1_UUID =
            UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public final static UUID FFF2_UUID =
            UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public final static UUID FFF3_UUID =
            UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    public final static UUID FFF4_UUID =
            UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    public final static UUID CLIENT_CHAR_CFG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static BluetoothGattCharacteristic FFF1_Char = null;
    public static BluetoothGattCharacteristic FFF2_Char = null;
    public static BluetoothGattCharacteristic FFF3_Char = null;
    public static BluetoothGattCharacteristic FFF4_Char = null;

    interface IBleDeviceDelegate {
        void didError(String error);

        void didConnect();
        void didDisconnect();

        void didWriteData(int remainPackets);
        void didWriteAllData();

        void didGetFeedback(byte[] cmd);
        void didGetParamData(byte[] data);
    }

    public BleDevice(Context context, BluetoothDevice device) {
        this.context = context;
        this.device = device;
        deviceState = DeviceState.DISCONNECTED;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public boolean connect(){
        if (device == null) {
            Log.d(TAG, "Connection error. Device is null.");
            return false;
        }

        if (deviceState == DeviceState.CONNECTION_READY) {
            Log.d(TAG, "Already connection complete.");
            return true;
        }
        if (deviceState == DeviceState.CONNECTED){
            disconnect();
            return true;
        }

        // we want to directly connect to the device, because it is fast method
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);

        deviceState = DeviceState.CONNECTING;
        Log.d(TAG, "Trying to create a new connection.");

        return true;
    }

    public void disconnect(){
        device = null;
        deviceState = DeviceState.DISCONNECTED;

        mBluetoothGatt.disconnect();
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != 0){
                Log.d(TAG, "Connection state status = " + status);

            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                deviceState = DeviceState.CONNECTED;

                // Attempts to discover services after successful connection.
                Log.d(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                deviceState = DeviceState.DISCONNECTED;

                connect(); // auto re-connect

                if (delegate != null) delegate.didDisconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();

                for (BluetoothGattService gattService : gattServices) {
                    List<BluetoothGattCharacteristic> gattCharacteristics =
                            gattService.getCharacteristics();

                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                        if (FFF1_UUID.equals(gattCharacteristic.getUuid())) {
                            FFF1_Char = gattCharacteristic;
                            Log.d(TAG, "FFF1 Characteristic find complete");
                        }
                        if (FFF2_UUID.equals(gattCharacteristic.getUuid())) {
                            FFF2_Char = gattCharacteristic;
                            Log.d(TAG, "FFF2 Characteristic find complete");
                        }
                        if (FFF3_UUID.equals(gattCharacteristic.getUuid())) {
                            FFF3_Char = gattCharacteristic;
                            Log.d(TAG, "FFF3 Characteristic find complete");
                        }
                        if (FFF4_UUID.equals(gattCharacteristic.getUuid())) {
                            FFF4_Char = gattCharacteristic;
                            Log.d(TAG, "FFF4 Characteristic find complete");
                        }

                    }


                }

                descriptorPackets = new ArrayDeque<BluetoothGattCharacteristic>();
                descriptorPackets.add(FFF2_Char);
                descriptorPackets.add(FFF3_Char);
                descriptorPackets.add(FFF4_Char);

                setCharacteristicNotification(descriptorPackets.poll(), true);
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

            if (characteristic.getUuid().equals(FFF1_UUID)){
                packets.remove();

                Log.d(TAG, String.format("left pack count = %d", packets.size()));
                if (delegate != null) delegate.didWriteData(packets.size());

                if (packets.size() > 0) {
                    BlePacket packet = packets.element();
                    //send packet
                    FFF1_Char.setValue(packet.data);

                    if (!mBluetoothGatt.writeCharacteristic(FFF1_Char)){
                        Log.d(TAG, "Write characteristic is unsuccesful!");
                    }
                } else {
                    if (delegate != null) delegate.didWriteAllData();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();

            if (characteristic.getUuid().equals(FFF2_UUID)){

            }
            if (characteristic.getUuid().equals(FFF3_UUID)){
                if (delegate != null) delegate.didGetFeedback(data);
            }
            if (characteristic.getUuid().equals(FFF4_UUID)){
                if (delegate != null) delegate.didGetParamData(data);
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.d(TAG, "onDescWrite: " + descriptor.getCharacteristic().getUuid().toString());

            if (descriptorPackets.size() != 0) {
                setCharacteristicNotification(descriptorPackets.poll(), true);
            } else {
                if (delegate != null) delegate.didConnect();
            }
        }
    };

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        /*if ((bleState < BLE_CONNECTED) || (mBluetoothAdapter == null) || (mBluetoothGatt == null)) {
            Log.d(TAG, "setCharacteristicNotification is not successful.");
            return;
        }*/

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHAR_CFG);
        byte[] value = (enabled) ? (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) :
                (new byte[]{0x00, 0x00});
        descriptor.setValue(value);
        mBluetoothGatt.writeDescriptor(descriptor);
    }



}

