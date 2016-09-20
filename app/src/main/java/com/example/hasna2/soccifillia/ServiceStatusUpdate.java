package com.example.hasna2.soccifillia;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import xdroid.toaster.Toaster;

/**
 * Created by hasna2 on 02-Sep-16.
 */
public class ServiceStatusUpdate extends Service {

    final String TAG = getClass().getSimpleName();
    private NotificationManager mNM;
    private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean mAllowInsecureConnections;
    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;
    private ConnectedThread mConnectedThread;
    private BluetoothAdapter BA;
    private static final boolean D = true;
    private int mState;
    private final IBinder mBinder = new LocalBinder();


    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device



    private int NOTIFICATION = R.string.local_service_started;


    public class LocalBinder extends Binder {
        ServiceStatusUpdate getService() {
            return ServiceStatusUpdate.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        BA=BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mDevice=intent.getParcelableExtra(Intent.EXTRA_TEXT);
        }
        mNM.cancel(NOTIFICATION);

        connect(mDevice);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
//        Toaster.toast("Service stopped");
//        Toast.makeText(this,"Service stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MapsActivity.class), 0);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                long[] pattern = {500,500,500,500,500,500,500,500,500};
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icecream)  // the status icon
                .setTicker("Device out of range")  // the status text
                .setSound(alarmSound)
                .setVibrate(pattern)
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText("Device "+mDevice.getName()+" out of range")  // the contents of the entry
               // .setFullScreenIntent(,true);
                .setContentIntent(contentIntent)
                .build();  // The intent to send when the entry is clicked
        mNM.notify(NOTIFICATION, notification);
    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if ( mAllowInsecureConnections ) {
                    Method method;

                    method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                }
                else {
                    tmp = device.createRfcommSocketToServiceRecord( SerialPortServiceClass_UUID );
                }
                Log.d(TAG,"create) passed");
            } catch (Exception e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
          BA.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                Log.i(TAG,"Connecting to socket...");
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                //  e.printStackTrace();

                try {
                    Log.i(TAG,"Trying fallback...");
                    mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                    Log.i(TAG,"Connected");
                    Toaster.toast(mmDevice.getName()+" Connected");
//                    Toast.makeText(getApplicationContext(),mmDevice.getName()+" Connected",Toast.LENGTH_SHORT).show();
                } catch (Exception e2) {
                    Log.e(TAG, "Couldn't establish Bluetooth connection!");
                    try {
                        mmSocket.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "unable to close() " + "mSocketType" + " socket during connection failure", e3);
                    }
                    connectionFailed();
                    Toaster.toast(mmDevice.getName()+" failed to connect");
//                    Toast.makeText(getApplicationContext(),mDevice.getName()+" failed to connect",Toast.LENGTH_LONG).show();
                    return;
                }
                }

            // Reset the ConnectThread because we're done
            synchronized (ServiceStatusUpdate.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity

        setState(STATE_CONNECTED);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    Toaster.toast(mDevice.getName()+" lost connection");
//                    Toast.makeText(getApplicationContext(),mDevice.getName()+" lost connection",Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    private void connectionFailed() {
        setState(STATE_NONE);
        Log.d(TAG, "Connection failed");
        showNotification();
    }

    private void connectionLost() {
        setState(STATE_NONE);
        Log.d(TAG,"Connection lost");
        showNotification();
    }
    public void setAllowInsecureConnections( boolean allowInsecureConnections ) {
        mAllowInsecureConnections = allowInsecureConnections;
    }

    public boolean getAllowInsecureConnections() {
        return mAllowInsecureConnections;
    }




//    private Timer timer = new Timer();
//    private NotificationManager mNM;
//
//    // Unique Identification Number for the Notification.
//    // We use it on Notification start, and to cancel it.
//    private int NOTIFICATION = R.string.local_service_started;
//
//    public class LocalBinder extends Binder {
//        ServiceStatusUpdate getService() {
//            return ServiceStatusUpdate.this;
//        }
//    }
//
//    @Override
//    public void onCreate()
//    {
//        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//
//        // Display a notification about us starting.  We put an icon in the status bar.
//        showNotification();
////        super.onCreate();
////        Log.v("F","HII create");
////        timer.scheduleAtFixedRate(new TimerTask() {
////            @Override
////            public void run() {
////                unregisterReceiver(BluetoothActivity.mReceiver);
////                BluetoothActivity.BA.startDiscovery();
////                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
////                Log.v("F","HII");
////                registerReceiver(BluetoothActivity.mReceiver,filter);
////            }
////        }, 0, 10*1000);//5 Minutes
//    }
//

//    private class ConnectThread extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final BluetoothDevice mmDevice;
//        private final UUID MY_UUID = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB");
//
//        public ConnectThread(BluetoothDevice device) {
//            BluetoothSocket tmp = null;
//            mmDevice = device;
//            if (device.equals(null))
//                Log.v("hii","DEvise is null :3 :# ");
//            // Get a BluetoothSocket to connect with the given BluetoothDevice
//            try {
//                // MY_UUID is the app's UUID string, also used by the server code
//                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//            } catch (IOException e) {
//                Log.v("hii"," exception in constructor "+ e.getMessage());
//
//
//            }
//            mmSocket = tmp;
//            if (mmSocket.equals(null))
//            Log.v("hii"," mmsocket is  null :##");
//        }
//
//        public void run() {
//            Log.v("hii","connect  thread running");
//            // Cancel discovery because it will slow down the connection
//            if(mmSocket.equals(null))
//                Log.v("hii","connect  thread running and mmsocket is null");
//
//            BluetoothActivity.BA.cancelDiscovery();
//
//            try {
//                // Connect the device through the socket. This will block
//                // until it succeeds or throws an exception
//                mmSocket.connect();
//                Log.v("hii","connected");
//            } catch (IOException connectException) {
//                // Unable to connect; close the socket and get out
//                try {
//                    mmSocket.close();
//                } catch (IOException closeException) { }
//                connectException.printStackTrace();
//                Log.v("hii","not connected"+ connectException.getMessage());
//                return;
//            }
//
//            // Do work to manage the connection (in a separate thread)
//            while (mmSocket.isConnected()){
//
//            }
//            showNotification();
//        }
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) { }
//        }
//    }




}

