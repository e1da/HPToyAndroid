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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.ble.BlePacket;
import com.hifitoy.ble.BlePacketQueue;
import com.hifitoy.ble.BleFinder;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class HiFiToyControl implements BleFinder.IBleFinderDelegate {
    private static final String TAG = "HiFiToy";

    private static HiFiToyControl instance;
    private DiscoveryDelegate   discoveryDelegate = null;
    private ConnectionDelegate  connectionDelegate = null;

    private BluetoothAdapter    mBluetoothAdapter = null;
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

    private class ConnectionState {
        private final static int    DISCONNECTED = 0;
        private final static int    CONNECTING = 1;
        private final static int    RECONNECTING = 2;
        private final static int    CONNECTED = 3;
        private final static int    CONNECTION_READY = 4;

        private int state;

        ConnectionState() {
            state = DISCONNECTED;
        }

        int getState() {
            return state;
        }
        void setState(int state) {
            if (state < DISCONNECTED) state = DISCONNECTED;
            if (state > CONNECTION_READY) state = CONNECTION_READY;

            this.state = state;

            switch (state) {
                case DISCONNECTED:
                    Log.d(TAG, "Disconnected from GATT server.");
                    packets.clear();

                    if (connectionDelegate != null) connectionDelegate.didDisconnect();

                    break;
                case CONNECTING:
                    Log.d(TAG, "Connecting to GATT server");

                    break;
                case RECONNECTING:
                    Log.d(TAG, "Re-connecting to GATT server");
                    startDiscovery(null);
                    break;
                case CONNECTED:
                    Log.d(TAG, "Connected to GATT server.");

                    // Attempts to discover services after successful connection.
                    Log.d(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());
                    break;
                case CONNECTION_READY:
                    String dialogMessage = DialogSystem.getInstance().getDialogMessage();
                    if ( (dialogMessage != null) && (dialogMessage.equals("Disconnected!")) ) {
                        DialogSystem.getInstance().closeDialog();
                    }

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ApplicationContext.getInstance().getContext(),
                                    R.string.connected, Toast.LENGTH_SHORT).show();
                        }
                    };
                    mainHandler.post(myRunnable);

                    if (connectionDelegate != null) connectionDelegate.didConnect();
                    break;
            }
        }
    };
    private ConnectionState state = new ConnectionState();

    private BlePacketQueue packets = new BlePacketQueue();
    private Queue<BluetoothGattCharacteristic> descriptorPackets = null;

    public interface DiscoveryDelegate {
        void didFindPeripheral(HiFiToyDevice device);
    }
    public interface ConnectionDelegate {
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

    public boolean init() {
        Context context = ApplicationContext.getInstance().getContext();

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) return false;

        bleFinder = new BleFinder(mBluetoothAdapter);
        return true;
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
        if ( (!isBleEnabled()) || (bleFinder.isDiscovering()) ) return;

        bleFinder.setBleFinderDelegate(this);
        this.discoveryDelegate = discoveryDelegate;


        if ((isConnected()) && (discoveryDelegate != null)) {
            discoveryDelegate.didFindPeripheral(activeDevice);
        }

        bleFinder.startDiscovery();

    }
    public void stopDiscovery() {
        bleFinder.setBleFinderDelegate(null);
        this.discoveryDelegate = null;

        if (isBleEnabled()) {
            bleFinder.stopDiscovery();
        }

    }

    public boolean isConnected() {
        return ( (activeDevice != null) && (state.getState() == ConnectionState.CONNECTION_READY) );
    }
    public boolean connect(HiFiToyDevice device) {
        if ( (!isBleEnabled()) || (device == null)) {
            activeDevice = null;
            return false;
        }

        if ( (activeDevice != null) &&
                (activeDevice.getMac().equals(device.getMac())) &&
                (state.getState() == ConnectionState.CONNECTION_READY) ) {
            Log.d(TAG, "Already connection complete.");
            return true;

        } else if (state.getState() != ConnectionState.DISCONNECTED) {

            disconnect();
        }

        Context context = ApplicationContext.getInstance().getContext();

        activeDevice = device;
        if (activeDevice.getMac().equals("demo")) {
            Toast.makeText(context, R.string.demo_mode, Toast.LENGTH_SHORT).show();
            return true;
        }


        //get device from macAddress
        BluetoothDevice d = mBluetoothAdapter.getRemoteDevice(activeDevice.getMac());
        if (d == null) {
            Log.d(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        Toast.makeText(context, R.string.connecting, Toast.LENGTH_SHORT).show();

        // we want to directly connect to the device, because it is fast method
        mBluetoothGatt = d.connectGatt(context, false, mGattCallback);

        state.setState(ConnectionState.CONNECTING);
        return true;
    }
    public boolean connect() {
        return connect(activeDevice);
    }
    public void disconnect() {
        if ( (isBleEnabled()) && (mBluetoothGatt != null)) {

            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        state.setState(ConnectionState.DISCONNECTED);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (status != BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG, "Error connection state status = " + status);
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                state.setState(ConnectionState.CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect();

                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        DialogSystem.getInstance().showDialog("Warning", "Disconnected!", "Ok");
                    }
                };
                mainHandler.post(myRunnable);

                //auto re-connect
                //connect(); // old method
                state.setState(ConnectionState.RECONNECTING);
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

            if ( (data.length == 13) && (data[0] == CommonCommand.GET_ENERGY_CONFIG) ) { // get energy config
                Log.d(TAG, "GET_ENERGY_CONFIG");

                data = Arrays.copyOfRange(data, 1, data.length - 1);
                activeDevice.getEnergyConfig().setValues(data);
                //[[NSNotificationCenter defaultCenter] postNotificationName:@"UpdateEnergyConfigNotification" object:nil];
            }

            if (data.length == 4) {

                byte feedbackMsg = data[0];
                byte status = data[1];

                switch (feedbackMsg) {
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
                            activeDevice.restoreFactorySettings();
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
                            activeDevice.restoreFactorySettings();
                        }
                        break;
                    }
                    case CommonCommand.GET_CHECKSUM:
                        int checksum = ((int)data[2]) * 256 + (int)data[1];
                        Log.d(TAG, "GET_CHECKSUM " + checksum);

                        //[self comparePreset:checksum];

                        state.setState(ConnectionState.CONNECTION_READY);
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

            if (data.length == 20){ // Get data from storage
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
                (state.getState() == ConnectionState.DISCONNECTED) ||
                (state.getState() == ConnectionState.CONNECTING)) return;

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHAR_CFG);
        byte[] value = (enabled) ? (BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) :
                (new byte[]{0x00, 0x00});
        descriptor.setValue(value);
        mBluetoothGatt.writeDescriptor(descriptor);
    }
    private void writeFFF1Characterstic(BlePacket packet) {
        if ((mBluetoothGatt == null) ||
                (state.getState() == ConnectionState.DISCONNECTED) ||
                (state.getState() == ConnectionState.CONNECTING)) return;

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
                (state.getState() == ConnectionState.DISCONNECTED) ||
                (state.getState() == ConnectionState.CONNECTING)) return;

        if (!mBluetoothGatt.writeCharacteristic(characteristic)){
            Log.d(TAG, "Write characteristic is unsuccesful!");
        }
    }
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if ((mBluetoothGatt == null) ||
                (state.getState() == ConnectionState.DISCONNECTED) ||
                (state.getState() == ConnectionState.CONNECTING)) return;

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

        if (state.getState() == ConnectionState.RECONNECTING) {
            if (activeDevice.getMac().equals(deviceAddress)) {
                connect(activeDevice);
                stopDiscovery();
            }
        }

        if (discoveryDelegate != null) discoveryDelegate.didFindPeripheral(device);
    }


}
