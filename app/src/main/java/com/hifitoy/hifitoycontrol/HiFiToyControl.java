/*
 *   HiFiToyControl.java
 *
 *   Created by Artem Khlyupin on 13/11/2018.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.hifitoycontrol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.ble.Ble;
import com.hifitoy.ble.BlePacket;
import com.hifitoy.ble.BlePacketQueue;
import com.hifitoy.ble.BleFinder;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;
import com.hifitoy.hifitoyobjects.AMMode;
import com.hifitoy.hifitoyobjects.BinaryOperation;
import com.hifitoy.hifitoyobjects.HiFiToyDataBuf;
import com.hifitoy.hifitoyobjects.PostProcess;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;

import static com.hifitoy.hifitoycontrol.CommonCommand.GET_ADVERTISE_MODE;
import static com.hifitoy.hifitoycontrol.CommonCommand.GET_OUTPUT_MODE;
import static com.hifitoy.hifitoycontrol.CommonCommand.GET_TAS5558_CH3_MIXER;

public class HiFiToyControl implements BleFinder.IBleFinderDelegate {
    private static final String TAG = "HiFiToy";

    private static HiFiToyControl instance;
    private DiscoveryDelegate   discoveryDelegate = null;
    private ConnectionDelegate  connectionDelegate = null;

    private BleFinder           bleFinder;
    private HiFiToyDevice       activeDevice = null;
    private BluetoothGatt       mBluetoothGatt = null;
    private boolean             bleBusy = false;

    private final static short CC2540_PAGE_SIZE   = 2048;
    private final static short ATTACH_PAGE_OFFSET = (3 * CC2540_PAGE_SIZE); // 3 page

    public final static String DID_GET_PARAM_DATA       = "com.hifitoy.DID_GET_PARAM_DATA";
    public final static String DID_WRITE_DATA           = "com.hifitoy.DID_WRITE_DATA";
    public final static String DID_WRITE_ALL_DATA       = "com.hifitoy.DID_WRITE_ALL_DATA";

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
                    bleBusy = false;

                    ApplicationContext.getInstance().setupOutlets();
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


                    ApplicationContext.getInstance().setupOutlets();
                    if (connectionDelegate != null) connectionDelegate.didConnect(activeDevice);
                    break;
            }
        }
    }

    private ConnectionState state = new ConnectionState();

    private BlePacketQueue packets = new BlePacketQueue();
    private Queue<BluetoothGattCharacteristic> descriptorPackets = null;

    public interface DiscoveryDelegate {
        void didFindPeripheral(HiFiToyDevice device);
    }
    public interface ConnectionDelegate {
        void didConnect(HiFiToyDevice device);
        void didDisconnect();
    }
    public void setConnectionDelegate(ConnectionDelegate delegate) {
        this.connectionDelegate = delegate;
    }

    public static synchronized HiFiToyControl getInstance() {
        if (instance == null){
            instance = new HiFiToyControl();
        }
        return instance;
    }

    public HiFiToyControl() {
        bleFinder = new BleFinder();
        activeDevice = HiFiToyDeviceManager.getInstance().getDevice("demo");
    }

    public HiFiToyDevice getActiveDevice() {
        return activeDevice;
    }

    public void startDiscovery(DiscoveryDelegate discoveryDelegate) {
        this.discoveryDelegate = discoveryDelegate;

        if ( (!Ble.getInstance().isEnabled()) || (bleFinder.isDiscovering()) ) {
            bleFinder.clear();
            return;
        }

        bleFinder.setBleFinderDelegate(this);
        bleFinder.startDiscovery();

    }
    public void stopDiscovery() {
        bleFinder.setBleFinderDelegate(null);
        this.discoveryDelegate = null;

        bleFinder.stopDiscovery();
    }

    public boolean isConnected() {
        return ( (activeDevice != null) && (state.getState() >= ConnectionState.CONNECTED) );
    }
    public boolean isConnectionReady() {
        return ( (activeDevice != null) && (state.getState() == ConnectionState.CONNECTION_READY) );
    }

    public boolean connect(HiFiToyDevice device) {
        Context context = ApplicationContext.getInstance().getContext();

        //check if ble disabled or demo connect
        if ( (!Ble.getInstance().isEnabled()) || (device == null) || (device.getMac().equals("demo")) ) {
            disconnect();
            activeDevice = device;
            Toast.makeText(context, R.string.demo_mode, Toast.LENGTH_SHORT).show();
            return true;
        }

        if ( (activeDevice != null) &&
                (activeDevice.getMac().equals(device.getMac())) &&
                (state.getState() == ConnectionState.CONNECTION_READY) ) {
            Log.d(TAG, "Already connection complete.");
            return true;

        } else if (state.getState() != ConnectionState.DISCONNECTED) {

            disconnect();
        }

        activeDevice = device;


        //get device from macAddress
        BluetoothDevice d = Ble.getInstance().getRemoteDevice(activeDevice.getMac());
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
        if ( (Ble.getInstance().isEnabled()) && (mBluetoothGatt != null)) {

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
                // Attempts to discover services after successful connection.
                Log.d(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

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

                state.setState(ConnectionState.CONNECTED);

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
            Log.d(TAG, "onCharacteristicWrite " + status);

            // check output mode hw support
            BlePacket sentPacket = packets.element();
            if (sentPacket.getData().length == CommonCommand.LENGTH) {

                byte cmd = sentPacket.getData()[0];

                if ((cmd >= CommonCommand.SET_TAS5558_CH3_MIXER) &&
                        (cmd <= CommonCommand.GET_OUTPUT_MODE)) {

                    if (status != 0) {
                        Log.d(TAG, "Output Mode is unsupported. ");
                        activeDevice.getOutputMode().setHwSupported(false);
                    } else {
                        activeDevice.getOutputMode().setHwSupported(true);
                    }
                    ApplicationContext.getInstance().setupOutlets();
                }
            }

            if (characteristic.getUuid().equals(FFF1_UUID)){
                packets.remove();

                Log.d(TAG, String.format("left pack count = %d", packets.size()));
                ApplicationContext.getInstance().broadcastUpdate(DID_WRITE_DATA, packets.size());

                if (packets.size() > 0) {
                    bleBusy = true;

                    BlePacket packet = packets.element();
                    //send packet
                    FFF1_Char.setValue(packet.getData());

                    writeCharacterstic(FFF1_Char);
                } else {
                    bleBusy = false;
                    ApplicationContext.getInstance().broadcastUpdate(DID_WRITE_ALL_DATA);
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
            Log.d(TAG, "onCharacteristicChanged");

            byte[] data = characteristic.getValue();

            if ( (data.length == 13) && (data[0] == CommonCommand.GET_ENERGY_CONFIG) ) { // get energy config
                Log.d(TAG, "GET_ENERGY_CONFIG");

                data = Arrays.copyOfRange(data, 1, data.length);
                activeDevice.getEnergyConfig().parseBinary(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));

                ApplicationContext.getInstance().setupOutlets();
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
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    DialogSystem.getInstance().showPairingCodeDialog();
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                        break;
                    case CommonCommand.SET_PAIR_CODE:
                        if (status != 0) {
                            Log.d(TAG, "SET_PAIR_CODE_SUCCESS");
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

                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    activeDevice.restoreFactorySettings();
                                }
                            };
                            mainHandler.post(myRunnable);

                        }
                        break;

                    case CommonCommand.GET_VERSION:
                        int version = (data[1] & 0xFF) | ((data[2] << 8) & 0xFF00);

                        if (version == activeDevice.getVersion()) {
                            Log.d(TAG, "GET_VERSION_OK");
                            activeDevice.getAudioSource().readFromDsp();
                        } else {
                            Log.d(TAG, "GET_VERSION_FAIL=" + version + "CURRENT=" + activeDevice.getVersion());
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    activeDevice.restoreFactorySettings();
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                        break;

                    case CommonCommand.GET_CHECKSUM:
                        short checksum = (short)( (data[1] & 0xFF) | ((data[2] << 8) & 0xFF00) );

                        //
                        AMMode am = activeDevice.getAmMode();
                        if (am.isSuccessImport()) {
                            HiFiToyDataBuf b = am.getDataBufs().get(0);

                        }



                        String info = String.format(Locale.getDefault(),
                                            "GET_CHECKSUM=0x%x APP_CHECKSUM=0x%x",
                                                    checksum, activeDevice.getActivePreset().getChecksum());
                        Log.d(TAG, info);

                        state.setState(ConnectionState.CONNECTION_READY);

                        if (activeDevice.getActivePreset().getChecksum() != checksum) {
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {

                                    DialogSystem.OnClickDialog dialogListener = new DialogSystem.OnClickDialog() {
                                        public void onPositiveClick(){
                                            activeDevice.importPreset();
                                        }
                                        public void onNegativeClick(){
                                            activeDevice.getActivePreset().storeToPeripheral();
                                        }
                                    };

                                    DialogSystem.getInstance().showDialog(dialogListener,
                                                                    "Preset info",
                                                                "Import preset from peripheral?",
                                                                "Ok", "Cancel");
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                        break;

                    case CommonCommand.GET_AUDIO_SOURCE:
                        Log.d(TAG, "GET_AUDIO_SOURCE " + status);

                        activeDevice.getAudioSource().setSource(data[1]);
                        ApplicationContext.getInstance().setupOutlets();

                        activeDevice.getAmMode().readFromDsp(new PostProcess() {
                            @Override
                            public void onPostProcess() {
                                AMMode am = activeDevice.getAmMode();
                                Log.d(TAG, "GET_AM_MODE " + am.isSuccessImport() + " " + activeDevice.getAmMode().getInfo());
                                getChecksumParamData();
                            }
                        });

                        break;

                    case GET_ADVERTISE_MODE:
                        Log.d(TAG, "GET_ADVERTISE_MODE " + status);
                        activeDevice.getAdvertiseMode().setMode(status);

                        ApplicationContext.getInstance().setupOutlets();
                        break;

                    case GET_OUTPUT_MODE: //cmd not uses. bug on hw side
                        Log.d(TAG, "GET_OUTPUT_MODE " + status);
                        break;

                    case GET_TAS5558_CH3_MIXER: //not uses. bug on hw side in GET_OUTPUT_MODE cmd
                        short val = (short)(data[1] + (data[2] << 8));
                        Log.d(TAG, "GET_TAS5558_CH3_MIXER " + val);
                        break;

                    case CommonCommand.CLIP_DETECTION:
                        Log.d(TAG, "CLIP_DETECTION " + status);
                        activeDevice.setClipFlag(status != 0);
                        ApplicationContext.getInstance().updateClipView();
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
                ApplicationContext.getInstance().broadcastUpdate(DID_GET_PARAM_DATA, data);
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
                (BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        descriptor.setValue(value);
        mBluetoothGatt.writeDescriptor(descriptor);
    }
    private void writeFFF1Characterstic(BlePacket packet) {
        if ((mBluetoothGatt == null) ||
                (state.getState() == ConnectionState.DISCONNECTED) ||
                (state.getState() == ConnectionState.CONNECTING)) return;

        packets.add(packet); // with response

        if (!bleBusy) {
            bleBusy = true;
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
        } else {
            Log.d(TAG, "writeCharacteristic");
        }
    }
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if ((mBluetoothGatt == null) ||
                (state.getState() == ConnectionState.DISCONNECTED) ||
                (state.getState() == ConnectionState.CONNECTING)) return;

        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //base send command
    public void sendDataToDsp(BlePacket packet) {
        if (activeDevice != null) {
            writeFFF1Characterstic(packet);
        }
    }
    public void sendDataToDsp(byte[] data, boolean response) {
        sendDataToDsp(new BlePacket(data, response));
    }
    public void sendDataToDsp(ByteBuffer data, boolean response) {
        sendDataToDsp(data.array(), response);
    }

    //send 16 bytes to DSP_Data[offset]
    private void send16Bytes(short offset, ByteBuffer data) {
        if (data.capacity() != 16) return;

        ByteBuffer b = ByteBuffer.allocate(18).order(ByteOrder.LITTLE_ENDIAN);
        b.putShort(offset);
        data.position(0);
        b.put(data);

        sendDataToDsp(b, true);
    }
    private void moveAttachPgToDspData(short offset, short length){
        ByteBuffer b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        b.putShort(offset);
        b.putShort(length);

        sendDataToDsp(b, true);
    }

    //this method used attach page
    public void sendBufToDsp(short offsetInDspData, ByteBuffer data) {
        int l = CC2540_PAGE_SIZE - offsetInDspData % CC2540_PAGE_SIZE;
        if (l > data.capacity()) l = data.capacity();

        short offset = 0;

        do {
            //send buf to attach page
            for (int i = 0; i < l; i += 16){
                ByteBuffer b = ByteBuffer.allocate(16);
                b.put(BinaryOperation.copyOfRange(data, offset + i, offset + i + 16));

                send16Bytes((short)((ATTACH_PAGE_OFFSET + i) >>> 2), b);
            }
            //move attach pg -> dsp data
            moveAttachPgToDspData((short)(offsetInDspData + offset), (short)l);

            //update
            offset += l;
            l = data.capacity() - offset;
            if (l > CC2540_PAGE_SIZE) l = CC2540_PAGE_SIZE;

            //condition
        } while (offset < data.capacity());
    }

    //get 20 bytes from DSP_Data[offset]
    public void getDspDataWithOffset(short offset) {
        ByteBuffer b = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        b.putShort(offset);
        sendDataToDsp(b, true);
    }

    //sys command
    public void sendNewPairingCode(int pairingCode) {

        byte[] d = {    CommonCommand.SET_PAIR_CODE,
                (byte)(pairingCode),
                (byte)(pairingCode >> 8),
                (byte)(pairingCode >> 16),
                (byte)(pairingCode >> 24) };

        sendDataToDsp(d, true);

    }
    public void startPairedProccess(int pairingCode) {
        byte[] d = {    CommonCommand.ESTABLISH_PAIR,
                (byte)(pairingCode),
                (byte)(pairingCode >> 8),
                (byte)(pairingCode >> 16),
                (byte)(pairingCode >> 24) };

        sendDataToDsp(d, true);
    }
    public void sendWriteFlag(byte writeFlag) {
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
    public void setInitDsp() {
        byte[] d = { CommonCommand.INIT_DSP, 0, 0, 0, 0 };
        sendDataToDsp(d, true);
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
                if (discoveryDelegate == null) stopDiscovery();
            }
        }

        if (discoveryDelegate != null) {
            discoveryDelegate.didFindPeripheral(device);
        }
    }


}
