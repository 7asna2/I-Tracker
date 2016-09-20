package com.example.hasna2.soccifillia;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity  {

    private  BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice>Devices;
    private  BluetoothDevice device;
    private ListView listView;
    private ServiceStatusUpdate bluetoothService;
    private boolean isbound= false;
    final private String LOG_TAG = "BluetoothActivity";
//    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BA = BluetoothAdapter.getDefaultAdapter();
//        textView = (TextView)findViewById(R.id.connected_device);
        listView = (ListView)findViewById(R.id.bluetoothList);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                device = Devices.get(position);
                Toast.makeText(getApplicationContext(), "Device registered", Toast.LENGTH_LONG).show();
                Intent serviceIntent = new Intent(getApplicationContext(),ServiceStatusUpdate.class);
                serviceIntent.putExtra(Intent.EXTRA_TEXT,(Parcelable)device);
                startService(serviceIntent);
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        on();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.ACTION_ACL_CONNECTED");
        filter.addAction("android.bluetooth.device.action.ACTION_ACL_DISCONNECTED");
        filter.addAction("android.bluetooth.device.action.ACTION_ACL_DISCONNECT_REQUESTED");
//        registerReceiver(BTReceiver,filter);


    }
    public void list(){
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();
        Devices = new ArrayList<>();

        for(BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
            Devices.add(bt);
        }
        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }


    public void on() {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turning on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServiceStatusUpdate.LocalBinder binder = (ServiceStatusUpdate.LocalBinder)service;
            bluetoothService=binder.getService();
            isbound=true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isbound=false;
        }
    };
//
//    public final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
//                //Do something if connected
//                textView.setText(device.getName());
//            }
//            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)||BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
//                //Do something if disconnected
//                textView.setText("NOO");
//                Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();
//            }
//            //else if...
//        }
//    };

}
