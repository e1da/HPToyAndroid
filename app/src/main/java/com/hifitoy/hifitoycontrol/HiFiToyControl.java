/*
 *   HiFiToyControl.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoycontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import com.hifitoy.R;
import com.hifitoy.ble.BlePacket;
import com.hifitoy.ble.BlePacketQueue;
import com.hifitoy.ble.BleFinder;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class HiFiToyControl implements BleFinder.IBleFinderDelegate {
    private static final String TAG = "HiFiToy";

    private static HiFiToyControl instance;
    private Context context;
    private DiscoveryDelegate   discoveryDelegate = null;
    private ConnectionDelegate  connectionDelegate = null;

    private BluetoothAdapter    mBluetoothAdapter = null;
    private boolean             blePermissionGranted = false;
    private BleFinder           bleFinder = null;
    private HiFiToyDevice       activeDevice = null;
    private BluetoothGatt       mBluetoothGatt = null;

    private final static UUID FFF1_UUID =
            UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private final static UUID FFF2_UUID =
            UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private final static UUID FFF3_UUID =
            UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    private final static UUID CLIENT_CHAR_CFG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static BluetoothGattCharacteristic FFF1_Char = null;
    private static BluetoothGattCharacteristic FFF2_Char = null;
    private static BluetoothGattCharacteristic FFF3_Char = null;
    private static BluetoothGattCharacteristic FFF4_Char = null;

    private enum DeviceState {
        DISCONNECTED, CONNECTING, CONNECTED, CONNECTION_READY;
    };
    private DeviceState deviceState = DeviceState.DISCONNECTED;

    private BlePacketQueue packets = new BlePacketQueue();
    private Queue<BluetoothGattCharacteristic> descriptorPackets = null;

    public interface DiscoveryDelegate {
        void didFindPeripheral(HiFiToyDevice device);
    }
    public interface ConnectionDelegate {
        void didError(String error);

        void didConnect();
        void didDisconnect();

        void didWriteData(int remainPackets);
        void didWriteAllData();

        void didGetParamData(byte[] data);
    }

    public static synchronized HiFiToyControl getInstance() {
        if (instance == null){
            instance = new HiFiToyControl();
        }
        return instance;
    }


    public void init(Context context) {
        this.context = context;
        this.blePermissionGranted = false;

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        bleFinder = new BleFinder(mBluetoothAdapter);
    }

    public boolean isBlePermissionGranted() {
        return blePermissionGranted;
    }
    public void setBlePermissionGranted(boolean blePermissionGranted) {
        this.blePermissionGranted = blePermissionGranted;
    }


    public boolean isBleEnabled() {
        if ( (mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled()) ) {
            return false;
        }
        return true;
    }

    public HiFiToyDevice getActiveDevice() {
        return activeDevice;
    }

    public void startDiscovery(DiscoveryDelegate discoveryDelegate) {
        bleFinder.setBleFinderDelegate(this);
        this.discoveryDelegate = discoveryDelegate;

        if ( (blePermissionGranted) && (isBleEnabled()) ) {
            bleFinder.startDiscovery();
        }


    }
    public void stopDiscovery() {
        bleFinder.setBleFinderDelegate(null);
        this.discoveryDelegate = null;

        if ( (blePermissionGranted) && (isBleEnabled()) ) {
            bleFinder.stopDiscovery();
        }

    }

    public boolean isConnected() {
        return ( (activeDevice != null) && (deviceState == DeviceState.CONNECTION_READY) );
    }
    public boolean connect(HiFiToyDevice device) {
        this.activeDevice = device;

        if ( (!blePermissionGranted) || (!isBleEnabled()) || (device == null)) return false;

        if (device.getMac().equals("demo")) {
            disconnect();
            this.activeDevice = device;
            return true;
        }

        //get device from macAddress
        BluetoothDevice d = mBluetoothAdapter.getRemoteDevice(activeDevice.getMac());
        if (d == null) {
            Log.d(TAG, "Device not found.  Unable to connect.");
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
        mBluetoothGatt = d.connectGatt(context, false, mGattCallback);

        deviceState = DeviceState.CONNECTING;
        Log.d(TAG, "Trying to create a new connection.");

        return true;
    }
    public boolean connect() {
        return connect(activeDevice);
    }
    public void disconnect() {
        deviceState = DeviceState.DISCONNECTED;

        if ( (!blePermissionGranted) || (!isBleEnabled()) || (mBluetoothGatt == null)) return;

        mBluetoothGatt.disconnect();
        mBluetoothGatt = null;
        activeDevice = null;
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
                packets.clear();

                connect(); // auto re-connect

                if (connectionDelegate != null) connectionDelegate.didDisconnect();
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

                    }


                }

                descriptorPackets = new ArrayDeque<BluetoothGattCharacteristic>();
                descriptorPackets.add(FFF2_Char);
                descriptorPackets.add(FFF3_Char);

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
                if (connectionDelegate != null) connectionDelegate.didWriteData(packets.size());

                if (packets.size() > 0) {
                    BlePacket packet = packets.element();
                    //send packet
                    FFF1_Char.setValue(packet.getData());

                    if (!mBluetoothGatt.writeCharacteristic(FFF1_Char)){
                        Log.d(TAG, "Write characteristic is unsuccesful!");
                    }
                } else {
                    if (connectionDelegate != null) connectionDelegate.didWriteAllData();
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
                if (data.length != 9) return;

                byte cmd = data[0];
                byte status = data[1];

                switch (cmd) {
                    case CommonCommand.ESTABLISH_PAIR:
                        if (status != 0) {
                            Log.d(TAG, "PAIR_YES");
                            checkWriteFlag();

                        } else {
                            Log.d(TAG, "PAIR_NO");
                            //show input pair alert
                            //[[DialogSystem sharedInstance] showPairCodeInput];
                        }
                        break;
                    case CommonCommand.SET_PAIR_CODE:
                        if (status != 0) {
                            Log.d(TAG, "SET_PAIR_CODE_SUCCESS");

                            //[[DialogSystem sharedInstance] showAlert:NSLocalizedString(@"Change Pairing code is successful!", @"")];
                        } else {
                            Log.d(TAG, "SET_PAIR_CODE_FAIL");
                        }
                        break;

                    case CommonCommand.GET_WRITE_FLAG:
                        if (status != 0) {
                            Log.d(TAG, "CHECK_FIRMWARE_OK");
                            getVersion();

                        } else {
                            Log.d(TAG, "CHECK_FIRMWARE_FAIL");
                            //[self restoreFactorySettings];
                        }
                        break;

                    case CommonCommand.GET_VERSION:
                    {
                        int version = ((int)data[2]) * 256 + (int)data[1];

                        if (version == HiFiToyConfig.getInstance().version) {
                            Log.d(TAG, "GET_VERSION_OK");
                            activeDevice.getAudioSource().readFromDsp();
                        } else {
                            Log.d(TAG, "GET_VERSION_FAIL" + version);
                            //[self restoreFactorySettings];
                        }
                        break;
                    }
                    case CommonCommand.GET_CHECKSUM:
                        int checksum = ((int)data[2]) * 256 + (int)data[1];
                        Log.d(TAG, "GET_CHECKSUM " + checksum);

                        //[self comparePreset:checksum];
                        if (connectionDelegate != null) connectionDelegate.didConnect();
                        break;

                    case CommonCommand.GET_AUDIO_SOURCE:
                        Log.d(TAG, "GET_AUDIO_SOURCE " + status);

                        activeDevice.getAudioSource().setSource(data[1]);
                        getChecksumParamData();
                        break;

                    case CommonCommand.CLIP_DETECTION:
                        Log.d(TAG, "CLIP_DETECTION " + status);

                        //NSNumber * clip = [NSNumber numberWithInt:status];
                        //[[NSNotificationCenter defaultCenter] postNotificationName:@"ClipDetectionNotification" object:clip];
                        break;

                    case CommonCommand.OTW_DETECTION:
                        //Log.d(TAG, "OTW_DETECTION");
                        break;

                    case CommonCommand.PARAM_CONNECTION_ENABLED:
                        Log.d(TAG, "PARAM_CONNECTION_ENABLED");
                        //startPairedProccess(0);
                        break;

                    default:
                        break;
                }
            }
            if (characteristic.getUuid().equals(FFF3_UUID)){
                if (connectionDelegate != null) connectionDelegate.didGetParamData(data);
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.d(TAG, "onDescWrite: " + descriptor.getCharacteristic().getUuid().toString());

            if (descriptorPackets.size() != 0) {
                setCharacteristicNotification(descriptorPackets.poll(), true);
            } else {
                startPairedProccess(activeDevice.getPairingCode());
            }
        }
    };

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if ((mBluetoothGatt == null) ||
                (deviceState == DeviceState.DISCONNECTED) ||
                (deviceState == DeviceState.CONNECTING)) return;

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHAR_CFG);
        byte[] value = (enabled) ? (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) :
                (new byte[]{0x00, 0x00});
        descriptor.setValue(value);
        mBluetoothGatt.writeDescriptor(descriptor);
    }
    private void writeFFF1Characterstic(BlePacket packet) {
        if ((mBluetoothGatt == null) ||
                (deviceState == DeviceState.DISCONNECTED) ||
                (deviceState == DeviceState.CONNECTING)) return;

        packets.add(packet); // with response

        if (packets.size() == 1) {
            BlePacket p = packets.element();

            //send packet
            FFF1_Char.setValue(p.getData());
            writeCharacterstic(FFF1_Char);
        }
    }
    private void writeCharacterstic(BluetoothGattCharacteristic characteristic) {
        if ((mBluetoothGatt == null) ||
                (deviceState == DeviceState.DISCONNECTED) ||
                (deviceState == DeviceState.CONNECTING)) return;

        if (!mBluetoothGatt.writeCharacteristic(characteristic)){
            Log.d(TAG, "Write characteristic is unsuccesful!");
        }
    }
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if ((mBluetoothGatt == null) ||
                (deviceState == DeviceState.DISCONNECTED) ||
                (deviceState == DeviceState.CONNECTING)) return;

        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //base send command
    public void sendDataToDsp(byte[] data, boolean response) {
        if (activeDevice != null) {
            writeFFF1Characterstic(new BlePacket(data, response));
        }
    }

    //sys command
    public void sendNewPairingCode(int pairingCode) {

        byte[] d = {    CommonCommand.SET_PAIR_CODE,
                (byte)(pairingCode >> 24),
                (byte)(pairingCode >> 16),
                (byte)(pairingCode >> 8),
                (byte)(pairingCode) };

        sendDataToDsp(d, true);

    }
    private void startPairedProccess(int pairingCode) {
        byte[] d = {    CommonCommand.ESTABLISH_PAIR,
                (byte)(pairingCode >> 24),
                (byte)(pairingCode >> 16),
                (byte)(pairingCode >> 8),
                (byte)(pairingCode) };

        sendDataToDsp(d, true);
    }
    private void sendWriteFlag(byte writeFlag) {
        byte[] d = { CommonCommand.SET_WRITE_FLAG, writeFlag, 0, 0, 0 };
        sendDataToDsp(d, true);
    }
    private void checkWriteFlag() {
        byte[] d = { CommonCommand.GET_WRITE_FLAG, 0, 0, 0, 0 };
        sendDataToDsp(d, true);
    }
    private void getVersion() {
        byte[] d = { CommonCommand.GET_VERSION, 0, 0, 0, 0 };
        sendDataToDsp(d, true);
    }
    private void getChecksumParamData() {
        byte[] d = { CommonCommand.GET_CHECKSUM, 0, 0, 0, 0 };
        sendDataToDsp(d, true);
    }
    private void setInitDsp() {
        byte[] d = { CommonCommand.INIT_DSP, 0, 0, 0, 0 };
        sendDataToDsp(d, true);
    }


    //adv command (save/restore to/from storage)
   /* - (void) restoreFactorySettings;
    - (void) storePresetToDSP:(HiFiToyPreset *) preset;*/

    //get 20 bytes from DSP_Data[offset]
    public void getDspDataWithOffset(short offset) {
        //if (offset < 0) return;

        ByteBuffer b = ByteBuffer.allocate(2);
        b.putShort(offset);
        sendDataToDsp(b.array(), true);
    }

    /* ------------------------- IBleFinderDelegate ----------------------------*/
    public void didFindNewPeripheral(String deviceAddress) {

        HiFiToyDevice device = HiFiToyDeviceManager.getInstance().getDevice(deviceAddress);
        if (device == null) {
            device = new HiFiToyDevice();
            device.setMac(deviceAddress);
            device.setName(deviceAddress);
            HiFiToyDeviceManager.getInstance().setDevice(deviceAddress, device);
        }

        if (discoveryDelegate != null) discoveryDelegate.didFindPeripheral(device);
    }


}
