package uc.epband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothBandService implements Constants{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler mHandler; // handler that gets info from Bluetooth service

    // Member fields
    private final BluetoothAdapter mAdapter;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;
    private Context mContext;

    private BluetoothSocket socket = null;


    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private Boolean mConnected = false;
    public String desiredDeviceName = Constants.DEVICE_NAME;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiverBTDiscover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                System.out.println("Device: " + deviceName + " " + deviceHardwareAddress);

                try {
                    if (deviceName.equals(desiredDeviceName)) {
                        device.createBond();
                        System.out.println("Created bond " + mAdapter.getBondedDevices());
                    }
                } catch (NullPointerException ex) {
                }
            }
        }
    };

    private final BroadcastReceiver mReceiverBTConnect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("mReceiverBTConnect");
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                System.out.println("Bond State Device: " + deviceName + " " + deviceHardwareAddress);
                System.out.println(BluetoothDevice.EXTRA_BOND_STATE);
            }
        }
    };

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Defines several constants used when transmitting messages between the
    // service and the UI.

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothBandService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.enable();
        mAdapter.startDiscovery();

        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
        mContext = context;

        IntentFilter filter_search = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiverBTDiscover, filter_search);

        IntentFilter filter_connect = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mContext.registerReceiver(mReceiverBTConnect, filter_connect);
    }

    public void close(){
        System.out.println("Closing BluetoothBandService.");
        disconnectSocket();
    }

    public void finalClose(){
        close();
        mContext.unregisterReceiver(mReceiverBTDiscover);
        mContext.unregisterReceiver(mReceiverBTConnect);
    }

    public boolean isConnected(){
        if(socket == null) return false;
        else if(socket.isConnected()) return true;
        else return false;
    }

    public boolean canConnect(){
        BluetoothDevice device = matchBluetoothDevice();
        if(device == null){
            System.out.println("No device found.");
            return false;
        }
        else{
            System.out.println("Device: " + device.getName() + " found.");
            return true;
        }
    }

    public boolean connect(){
        System.out.println("Called bluetooth connect");
        BluetoothDevice device = matchBluetoothDevice();
        if( connectSocket(device) ){
            mConnectedThread = new ConnectedThread(socket);
            mConnectedThread.start();
            return true;
        }
        else return false;
    }

    private BluetoothDevice matchBluetoothDevice() {
        BluetoothDevice device = null;
        Set<BluetoothDevice> BondedDevices = mAdapter.getBondedDevices();
        for (BluetoothDevice d : BondedDevices) {
            if (d.getName().equals(desiredDeviceName)) device = d;
        }
        return device;
    }

    private boolean connectSocket(BluetoothDevice device) {
        if (device == null) {
            System.out.println("Device is null");
            return false;
        } else {
            //No longer need to waste resources trying to discover devices
            if (mAdapter.isDiscovering()) mAdapter.cancelDiscovery();
            ParcelUuid[] uuids = device.getUuids();
            //attempt normal connection
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuids[0].getUuid());
                if (!socket.isConnected()) {
                    socket.connect();
                    return true;
                } else return false;
            } catch (IOException ex) {
                //If connection fails, use the workaround for Android 4.2 and above
                System.out.println("Attempting alternate bluetooth socket connection");
                try {
                    Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                    socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1)); // 1==RFCOMM channel code
                    socket.connect();
                    return true;
                } catch (Exception ex2) {
                    //All attempts failed
                    System.out.println("Completely unable to open bluetooth socket");
                    return false;
                }
            } catch (NullPointerException ex) {
                //Device doesn't properly offer a UUID
                System.out.println("No uuids provided by device");
                return false;
            }
        }
    }

    private boolean disconnectSocket() {
        mConnectedThread.cancel();
        if (socket.isConnected()) {
            try {
                socket.close();
                socket = null;
                return true;
            } catch (IOException ex) {
                System.out.println("Could not close socket");
                return false;
            }
        } else return true;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
                System.out.println("Input stream open");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
                System.out.println("Output stream open");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024*4];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    System.out.println("Attempt read from new thread");
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(Constants.MESSAGE_READ, numBytes, -1, mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    System.out.println("Disconnected");
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}

