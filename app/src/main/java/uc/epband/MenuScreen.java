package uc.epband;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.Math;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadLocalRandom;
import org.json.*;

public class MenuScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //public String desiredDeviceName = "Andrew's iPhone";

    private Workout mWorkout;
    private Exercise mExercise = new Exercise();
    private Context mContext;

    private Set<String> messageQueue;
    private boolean isWorkout = false;


    /*
    private BluetoothSocket socket = null;
    private OutputStream outputStream = null;
    private InputStream inStream = null;

    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    private Boolean mConnected = false;
    */

    //RETURN CODES
    private int SELECT_WORKOUT = 1;

    private BluetoothBandService BTservice;

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            //System.out.println("Handling message");
            //System.out.print(msg.what);
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:

                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    //System.out.println("Received message from bluetooth");

                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer
                    try {
                        String str = new String(readBuf, 0, msg.arg1);
                        System.out.println(str);
                        mExercise.AddStringData(str);

                        if (isWorkout) {
                            mExercise.PlotAll((LineChart) findViewById(R.id.linechart));
                        }
                    } catch(JSONException ex){
                        System.out.println("[mHandler] Error Adding/Plotting exercise data");
                        ex.printStackTrace();
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    //mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    /*if (null != activity) {
                        //Toast.makeText(activity, "Connected to "
                                //+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }*/
                    break;
                case Constants.MESSAGE_TOAST:
                    /*if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }*/
                    break;
            }
        }
    };

    private Handler gHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*
        ImageView imageGronk = (ImageView) findViewById(R.id.background_image);
        imageGronk.setVisibility(View.VISIBLE);
        */

        mContext = getApplicationContext();

        //GET NECESSARY PERMISSIONS
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        //BLUETOOTH
        /*
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();

        System.out.println("Bluetooth enabled: " + mBluetoothAdapter.getName() + " " + mBluetoothAdapter.isEnabled());
        mBluetoothAdapter.startDiscovery();
        System.out.println("Is discovering: " + mBluetoothAdapter.isDiscovering());

        IntentFilter filter_search = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiverBTDiscover, filter_search);

        IntentFilter filter_connect = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiverBTConnect, filter_connect);
        */

        BTservice = new BluetoothBandService(mContext, mHandler);


        hideAllCharts();
        System.out.println("Created App");
    }

    @Override
    protected void onDestroy() {
        /*
        unregisterReceiver(mReceiverBTDiscover);
        unregisterReceiver(mReceiverBTConnect);
        */
        BTservice.close();
        System.out.println("Destroyed EP Band");
        super.onDestroy();
    }

    /*
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
                        System.out.println("Created bond " + mBluetoothAdapter.getBondedDevices());
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
    */

    /*
    private void establishBluetooth() {
        BluetoothDevice device = matchBluetoothDevice();
        if (!(device == null)) {
            Snackbar.make(findViewById(R.id.nav_view), "EP Band", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CONNECT", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            System.out.println("Connecting");
                            if (connectSocket(matchBluetoothDevice())) {
                                if (openStreams()) {
                                    Snackbar.make(findViewById(R.id.nav_view), "CONNECTED", Snackbar.LENGTH_SHORT).show();
                                    mConnected = true;
                                } else {
                                    Snackbar.make(findViewById(R.id.nav_view), "CONNECTION ERROR", Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(findViewById(R.id.nav_view), "FAILED CONNECTION", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.greenSuccess))
                    .show();
        } else {
            if (!mBluetoothAdapter.isDiscovering()) {
                //Toast.makeText(MenuScreen.this, "Searching for EP Band", Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(R.id.nav_view), "SEARCHING FOR EP BAND", Snackbar.LENGTH_SHORT).show();
                mBluetoothAdapter.startDiscovery();
                System.out.println("Start Discovery");
            } else System.out.println("Still discovering");
        }
    }
    */
    //BLUETOOTH CLASS REWRITE VERSION
    private void establishBluetooth() {
        if (BTservice.canConnect()) {
            Snackbar.make(findViewById(R.id.nav_view), "EP Band", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CONNECT", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            System.out.println("Connecting");
                            if ( BTservice.connect() ) {
                                Snackbar.make(findViewById(R.id.nav_view), "CONNECTED", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(findViewById(R.id.nav_view), "FAILED CONNECTION", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.greenSuccess))
                    .show();
        }
    }

    private void endBluetooth() {
        Snackbar.make(findViewById(R.id.nav_view), "EP Band", Snackbar.LENGTH_LONG)
                .setAction("DISCONNECT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BTservice.close();
                        Snackbar.make(findViewById(R.id.nav_view), "DISCONNECTED", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorAccent))
                .show();

    }

    /*
    private boolean disconnectSocket() {
        closeStreams();
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

    private boolean connectSocket(BluetoothDevice device) {
        if (device == null) {
            System.out.println("Device is null");
            return false;
        } else {
            //No longer need to waste resources trying to discover devices
            if (mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();
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

    private boolean openStreams() {
        if (socket.isConnected()) {
            try {
                outputStream = socket.getOutputStream();
                System.out.println("Output Stream Open");

                inStream = socket.getInputStream();
                System.out.println("Input Stream Open");
                return true;
            } catch (IOException ex) {
                System.out.println("Unable to open both streams");
                closeStreams();
                return false;
            }
        } else return false;
    }

    private boolean closeStreams() {
        boolean result = true;
        try {
            outputStream.close();
            outputStream = null;
        } catch (IOException ex2) {
            System.out.println("Could not close outputStream");
            result = false;
        } catch (NullPointerException ex2) {
            System.out.println("outputStream wasn't open, no need to close");
        }
        try {
            inStream.close();
            inStream = null;
        } catch (IOException ex2) {
            System.out.println("Could not close inStream");
            result = false;
        } catch (NullPointerException ex2) {
            System.out.println("inStream wasn't open, no need to close");
        }
        return result;
    }

    private BluetoothDevice matchBluetoothDevice() {
        BluetoothDevice device = null;
        Set<BluetoothDevice> BondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice d : BondedDevices) {
            if (d.getName().equals(desiredDeviceName)) device = d;
        }
        return device;
    }

    private boolean bluetoothWrite(String message) {
        try {
            outputStream.write(message.getBytes());
            System.out.println("Message write to outputStream succeeded");
            return true;
        } catch (IOException ex) {
            System.out.println("Message write to outputStream failed");
            return false;
        }
    }

    private String bluetoothRead() {
        try {
            int size = inStream.available();
            if (size > 0) {
                byte[] buffer = new byte[size];
                inStream.read(buffer);
                System.out.println("Read " + size + " bytes");
                return new String(buffer);
            } else {
                System.out.println("No bytes read");
                return "";
            }
        } catch (IOException ex) {
            System.out.println("Could not read from inStream");
            return null;
        }
    }
    */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.bluetooth_status:
                String Message = (BTservice.isConnected()) ? "EP Band is Connected" : "No bluetooth connection";
                Snackbar.make(findViewById(R.id.nav_view), Message, Snackbar.LENGTH_SHORT)
                        .setAction("CLOSE", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.colorAccent))
                        .show();
                break;
            case R.id.delete_files:
                Snackbar.make(findViewById(R.id.nav_view), "Delete all saved files?", Snackbar.LENGTH_SHORT)
                        .setAction("CONFIRM", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DeleteAllFiles();
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.colorAccent))
                        .show();
                break;
            case R.id.workout1:
                SampleWorkout1();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        hideAllCharts();

        switch (id) {
            case R.id.nav_device:
                isWorkout = false;
                System.out.println("Menu: Device Setup");
                if (!BTservice.isConnected()) establishBluetooth();
                else endBluetooth();
                //AppendFile("TestWorkout", "__A__");
                //ReadFile("TestWorkout");
                //CreateFile("HELLO WORLD");
                //ReadFile("TestWorkout");
                //ListFiles();

                break;
            case R.id.nav_edit_workout:
                isWorkout = false;
                System.out.println("Menu: Workout Templates");
                //bluetoothWrite("Hello World");
                //bluetoothRead();
                //String filename = "JOBJECT";
                //CreateFile(filename);
                /*JSONObject jObj = testJSON();
                AppendFile(filename,jObj.toString());
                String content = ReadFile(filename);
                try {
                    jObj = new JSONObject(content);
                    System.out.println("File contained:\n"+jObj);
                }catch(Exception ex){
                    System.out.println("Couldn't read JSON");
                }*/
                //ListFiles();

                break;
            case R.id.nav_profile:
                isWorkout = false;
                System.out.println("Menu: Profile");
                Intent intent = new Intent(MenuScreen.this, ProfileActivity.class);
                startActivity(intent);
                PrintProfile();
                ListFiles();
                break;
            case R.id.nav_review:
                isWorkout = false;
                System.out.println("Menu: View Analysis");
                Intent intent_review = new Intent(MenuScreen.this, SelectWorkout.class);
                String[] List = fileList();
                intent_review.putExtra("Files", List);
                startActivityForResult(intent_review, 1);
/*
                System.out.println("Return from activity:\n");
                System.out.println("intent result: " + intent_review.getStringExtra("result"));
                try {
                    mWorkout.getHeartRate().CalculateSummary(GetMHR());
                    mWorkout.getHeartRate().PlotSummary((PieChart) findViewById(R.id.piechart));
                }catch(JSONException ex){
                    System.out.println("JSON ERROR with Summary");
                }
                */
                break;
            case R.id.nav_workout_start:
                if(BTservice.isConnected()) {
                    System.out.println("Menu: Start Workout");
                } else {
                    System.out.println("Bluetooth is not connected.");
                    //TODO: make this visible in app
                    break;
                }
                isWorkout = true;
                mExercise = new Exercise();

                //mWorkout.getHeartRate().PlotAll((LineChart) findViewById(R.id.linechart));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideAllCharts() {
        findViewById(R.id.piechart).setVisibility(View.INVISIBLE);
        findViewById(R.id.barchart).setVisibility(View.INVISIBLE);
        findViewById(R.id.linechart).setVisibility(View.INVISIBLE);
    }

    private int GetMHR(){
        int[] stats = GetProfile();
        return stats[4];
    }

    private int[] GetProfile(){
        return GetProfile("test");
    }

    private int[] GetProfile(String filename) {
        SharedPreferences Settings = getSharedPreferences(filename, MODE_PRIVATE);
        int[] profile = new int[6];
        profile[0] = Settings.getInt("height", 70);
        profile[1] = Settings.getInt("weight", 170);
        profile[2] = Settings.getInt("age", 20);
        profile[3] = Settings.getInt("gender", 0);
        profile[4] = Settings.getInt("MHR", 200);
        profile[5] = Settings.getInt("arm", 32);
        return profile;
    }

    public void PrintProfile() {
        int[] profile = GetProfile();
        System.out.println("height: " + profile[0]);
        System.out.println("weight: " + profile[1]);
        System.out.println("age: " + profile[2]);
        System.out.println("gender: " + profile[3]);
        System.out.println("MHR: " + profile[4]);
        System.out.println("arm: " + profile[5]);
    }

    public void ListFiles() {
        File Dir = getFilesDir();
        String[] List = fileList();

        System.out.println("Absolute path: " + Dir.getPath());
        System.out.println("Directory Name: " + Dir.getName());
        System.out.println("Size: " + Dir.getTotalSpace() + " Free: " + Dir.getFreeSpace());
        System.out.println("\nFiles:");
        for (String f : List) {
            System.out.println(f);
            try{
                System.out.println(ReadFile(f));
            }catch(IOException ex){
                System.out.println("UNREADABLE");
            }
        }
    }

    private void DeleteAllFiles(){
        System.out.println("Deleting all EPBand files.");
        String[] List = fileList();
        for(String f: List){
            if(f.startsWith(Constants.EXAMPLE) && !f.contains("(")) {

            }else{
                deleteFile(f);
                System.out.println("\tDeleted: " + f);
            }
        }
        System.out.println("Delete finished.\n");
    }

    public String ReadFile(String filename) throws IOException {
        try {
            FileInputStream f = openFileInput(filename);
            int size = f.available();
            byte[] content = new byte[size];
            f.read(content);
            f.close();
            return new String(content);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_WORKOUT) {
            if(resultCode == Activity.RESULT_OK){
                String filename = data.getStringExtra("result");
                System.out.println("Chose workout: " + filename);
                try {
                    mWorkout = new Workout(mContext, filename);
                    System.out.println("Successfully loaded saved workout");
                }catch(Exception ex){
                    mWorkout = new Workout(mContext);
                    System.out.println("Could not load saved workout");
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                mWorkout = new Workout(mContext);
                System.out.println("Canceled workout select");
            }
        }
    }//onActivityResult

    private void SampleWorkout1(){
        mWorkout = new Workout(mContext);
        mWorkout.setExampleFileName("1");

        //SAMPLE HEART RATE DATA
        HeartRate HR = mWorkout.getHeartRate();
        HR.SetSampleRate(new Date(6000));
        int[] BPM = new int[1000];
        for(int i = 0; i < BPM.length; i++){
            BPM[i] = (int)(220*Math.pow(Math.cos(i*Math.PI/1000.0),2));
        }
        HR.AddData(BPM);

        Exercise exercise = new Exercise();
        try {
            double X = 0.0, Y = 0.0, Z = 0.0, rX = 0.0, rY = 0.0, rZ = 0.0;
            for(int i = 0; i < 100; i++){
                rX = 180*Math.sin(2 * Math.PI * i / 100);
                rY = 180*Math.sin(2 * Math.PI * i / 100 + Math.PI/2);
                rZ = 180*Math.sin(2 * Math.PI * i / 100 + Math.PI);
                X = 360*i/100-180;
                Y = -360*i/100+180;
                Z = 0;
                exercise.AddData(X, Y, Z, rX, rY, rZ);
            }
            exercise.PlotAll((LineChart) findViewById(R.id.linechart));
            mWorkout.writeFile();
        }catch(JSONException ex){
            System.out.println("JSON Exception in exercise");
        }catch(IOException ex){
            System.out.println("IO Exception for workout");
        }
    }

    private void SampleWorkout2(){

    }

    private void NewWorkout(){
        mWorkout = new Workout(mContext);
    }
}