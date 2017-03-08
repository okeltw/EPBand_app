package uc.epband;

import android.Manifest;
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
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.Math;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MenuScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static float anaerobic = 0.8f;
    static float aerobic = 0.7f;
    static float rest = 0.6f;

    private String mUUID = "1e0ca4ea-299d-4335-93eb-27fcfe7fa848";

    public String desiredDeviceName = "Andrew's iPhone";

    private BluetoothSocket socket = null;
    private OutputStream outputStream = null;
    private InputStream inStream = null;

    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

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

        //GET NECESSARY PERMISSIONS
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        //BLUETOOTH
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();

        System.out.println("Bluetooth enabled: " + mBluetoothAdapter.getName() + " " + mBluetoothAdapter.isEnabled());
        mBluetoothAdapter.startDiscovery();
        System.out.println("Is discovering: " + mBluetoothAdapter.isDiscovering());

        IntentFilter filter_search = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiverBTDiscover, filter_search);

        IntentFilter filter_connect = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiverBTConnect, filter_connect);

        hideAllCharts();
        System.out.println("Created App");
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiverBTDiscover);
        unregisterReceiver(mReceiverBTConnect);
        System.out.println("Destroyed EP Band");
        super.onDestroy();
    }

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

                try{
                    if(deviceName.equals(desiredDeviceName)){
                        device.createBond();
                        System.out.println("Created bond " + mBluetoothAdapter.getBondedDevices());
                    }
                }catch(NullPointerException ex){
                }
            }
        }
    };

    private final BroadcastReceiver mReceiverBTConnect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("mReceiverBTConnect");
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                System.out.println("Bond State Device: " + deviceName + " " + deviceHardwareAddress);
                System.out.println(BluetoothDevice.EXTRA_BOND_STATE);
            }
        }
    };

    private void establishBluetooth(){
        BluetoothDevice device = matchBluetoothDevice();
        if(!(device == null)) {
            Snackbar.make(findViewById(R.id.nav_view), "EP Band", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CONNECT", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            System.out.println("Connecting");
                            if (connectSocket(matchBluetoothDevice())) {
                                if(openStreams()){
                                    Toast.makeText(MenuScreen.this, "CONNECTED", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    Toast.makeText(MenuScreen.this, "CONNECTION ERROR", Toast.LENGTH_LONG).show();
                                }
                            }
                            else Toast.makeText(MenuScreen.this, "FAILED CONNECTION", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.greenSuccess))
                    .show();
        }
        else{
            if(!mBluetoothAdapter.isDiscovering()){
                Toast.makeText(MenuScreen.this, "Searching for EP Band", Toast.LENGTH_LONG).show();
                mBluetoothAdapter.startDiscovery();
                System.out.println("Start Discovery");
            }
            else System.out.println("Still discovering");
        }
    }

    private void endBluetooth(){
        Snackbar.make(findViewById(R.id.nav_view), "EP Band", Snackbar.LENGTH_LONG)
                .setAction("DISCONNECT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("Disconnecting");
                        if(disconnectSocket()){
                            socket = null;
                            Toast.makeText(MenuScreen.this, "DISCONNECTED", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorAccent))
                .show();

    }

    private boolean disconnectSocket(){
        closeStreams();
        if(socket.isConnected()) {
            try {
                socket.close();
                socket = null;
                return true;
            }
            catch (IOException ex){
                System.out.println("Could not close socket");
                return false;
            }
        }
        else return true;
    }

    private boolean connectSocket(BluetoothDevice device){
        if(device == null){
            System.out.println("Device is null");
            return false;
        }
        else{
            //No longer need to waste resources trying to discover devices
            if(mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();
            ParcelUuid[] uuids = device.getUuids();
            //attempt normal connection
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuids[0].getUuid());
                if(!socket.isConnected()){
                    socket.connect();
                    return true;
                }
                else return false;
            } catch (IOException ex) {
                //If connection fails, use the workaround for Android 4.2 and above
                System.out.println("Attempting alternate bluetooth socket connection");
                try {
                    Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                    socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1)); // 1==RFCOMM channel code
                    socket.connect();
                    return true;
                }catch(Exception ex2){
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

    private boolean openStreams(){
        if(socket.isConnected()) {
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

    private boolean closeStreams(){
        boolean result = true;
        try {
            outputStream.close();
            outputStream = null;
        } catch(IOException ex2){
            System.out.println("Could not close outputStream");
            result = false;
        } catch (NullPointerException ex2){
            System.out.println("outputStream wasn't open, no need to close");
        }
        try {
            inStream.close();
            inStream = null;
        }catch(IOException ex2){
            System.out.println("Could not close inStream");
            result = false;
        }catch (NullPointerException ex2){
            System.out.println("inStream wasn't open, no need to close");
        }
        return result;
    }

    private BluetoothDevice matchBluetoothDevice(){
        BluetoothDevice device = null;
        Set<BluetoothDevice> BondedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice d: BondedDevices){
            if(d.getName().equals(desiredDeviceName)) device = d;
        }
        return device;
    }

    private boolean bluetoothWrite(String message){
        try {
            outputStream.write(message.getBytes());
            System.out.println("Message write to outputStream succeeded");
            return true;
        }
        catch (IOException ex){
            System.out.println("Message write to outputStream failed");
            return false;
        }
    }

    private String bluetoothRead(){
        try{
            int size = inStream.available();
            if(size > 0) {
                byte[] buffer = new byte[size];
                inStream.read(buffer);
                System.out.println("Read " + size + " bytes");
                return buffer.toString();
            }
            else{
                System.out.println("No bytes read");
                return "";
            }
        }catch (IOException ex){
            System.out.println("Could not read from inStream");
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        hideAllCharts();
        switch(id){
            case R.id.nav_device:
                System.out.println("Menu: Device Setup");
                if(socket == null) establishBluetooth();
                else endBluetooth();
                //AppendFile("TestWorkout", "__A__");
                //ReadFile("TestWorkout");
                //CreateFile("HELLO WORLD");
                //ReadFile("TestWorkout");
                //ListFiles();

                break;
            case R.id.nav_edit_workout:
                System.out.println("Menu: Workout Templates");
                bluetoothWrite("Hello World");
                bluetoothRead();
                //CreateFile("TestWorkout");
                //ReadFile("TestWorkout");
                break;
            case R.id.nav_profile:
                System.out.println("Menu: Profile");
                Intent intent = new Intent(MenuScreen.this, ProfileActivity.class);
                startActivity(intent);
                PrintProfile();
                break;
            case R.id.nav_review:
                System.out.println("Menu: View Analysis");
                List<Entry> entries = TestLineData();
                float[] counts = AnalyzeHeartRateData(entries, 220);
                CreateHeartRateSummary((PieChart) findViewById(R.id.piechart), counts[0], counts[1], counts[2], counts[3]);
                break;
            case R.id.nav_workout_start:
                System.out.println("Menu: Start Workout");
                TestLineChart();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideAllCharts(){
        ((PieChart) findViewById(R.id.piechart)).setVisibility(View.INVISIBLE);
        ((BarChart) findViewById(R.id.barchart)).setVisibility(View.INVISIBLE);
        ((LineChart) findViewById(R.id.linechart)).setVisibility(View.INVISIBLE);
    }

    private void TestLineChart(){
        List<Entry> entries = TestLineData();
        LineChart chart = (LineChart) findViewById(R.id.linechart);
        CreateLineChart(chart, entries);
    }

    private float[] AnalyzeHeartRateData(List<Entry> entries, int MHR){

        float Y;
        float[] counts = {0f, 0f, 0f, 0f};
        Entry entry;
        for(int i = 0; i < entries.size(); i++){
            entry = entries.get(i);
            Y = entry.getY();
            if( Y < rest*MHR){
                counts[0]++;
            }
            else if( Y < aerobic*MHR){
                counts[1]++;
            }
            else if( Y < anaerobic*MHR){
                counts[2]++;
            }
            else{
                counts[3]++;
            }
        }
        return counts;
    }

    private List<Entry> TestLineData(){
        List<Entry> entries = new ArrayList<Entry>();
        float Y;
        int X = 0;
        int MHR = 220;

        for (; X < 200; X++){
            Y = (float)(0.5*MHR + (X % 5));
            entries.add(new Entry(X,Y));
        }
        for (; X < 650; X++){
            Y = (float)(0.6*MHR + (X % 5));
            entries.add(new Entry(X,Y));
        }
        for (; X < 900; X++){
            Y = (float)(0.7*MHR + (X % 5));
            entries.add(new Entry(X,Y));
        }
        for (; X < 1000; X++){
            Y = (float)(0.8*MHR + (X % 5));
            entries.add(new Entry(X,Y));
        }
        return entries;
    }

    public void CreateLineChart(LineChart chart, List<Entry> entries){
        // VISIBILITY ON
        chart.setVisibility(View.VISIBLE);

        // CREATE DATA
        LineDataSet dataSet = new LineDataSet(entries, "Line Graph");

        // DATA POINT STYLE SETTINGS
        dataSet.setColor(Color.WHITE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleColorHole(Color.BLACK);
        dataSet.setCircleHoleRadius(1f);
        dataSet.setCircleRadius(2f);

        // CHART STYLE SETTINGS
        chart.setVisibility(View.VISIBLE);
        chart.getXAxis().setEnabled(true);
        chart.getAxisLeft().setAxisMaximum(220f);
        chart.getAxisLeft().setEnabled(false);

        // LEGEND SETTINGS
        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        // REFRESH
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void CreateHeartRateSummary(PieChart chart, float unread, float rest, float aerobic, float anaerobic){
        // VISIBILITY ON
        chart.setVisibility(View.VISIBLE);

        // CREATE DATA
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(unread, "Unread"));
        entries.add(new PieEntry(rest, "Resting"));
        entries.add(new PieEntry(aerobic, "Aerobic"));
        entries.add(new PieEntry(anaerobic, "Anaerobic"));
        PieDataSet set = new PieDataSet(entries, "Heart Rate Over Workout");

        // CHART STYLE SETTINGS
        set.setColors(Color.rgb(160, 160, 160), Color.rgb(0, 128, 255), Color.rgb(255, 255, 0), Color.rgb(255, 0, 0));
        PieData data = new PieData(set);
        data.setValueTextSize(16.0f);
        data.setValueTextColor(Color.BLACK);
        chart.setEntryLabelTextSize(24.0f);
        chart.setUsePercentValues(true);
        chart.setHoleRadius(0f);
        chart.setTransparentCircleAlpha(0);

        if(true){
            // Don't use labels on chart
            chart.setDrawEntryLabels(false);
        }
        else{
            // Use labels on chart
            chart.setDrawEntryLabels(true);
            chart.setEntryLabelColor(Color.rgb(0, 0, 0));
        }

        // LEGEND SETTINGS
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.rgb(255, 255, 255));
        legend.setTextSize(16.0f);
        //legend.setTypeface(Typeface TF);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);

        // SET DATA AND UPDATE CHART
        chart.setDescription(null);
        chart.setData(data);
        chart.invalidate(); // refresh
    }

    private int[] GetProfile(String filename){
        SharedPreferences Settings = getSharedPreferences(filename,MODE_PRIVATE);
        int[] profile = new int[6];
        profile[0] = Settings.getInt("height", 70);
        profile[1] = Settings.getInt("weight", 170);
        profile[2] = Settings.getInt("age", 20);
        profile[3] = Settings.getInt("gender", 0);
        profile[4] = Settings.getInt("MHR", 200);
        profile[5] = Settings.getInt("arm", 32);
        return profile;
    }

    public void PrintProfile(){
        int[] profile = GetProfile("test");
        System.out.println("height: " + profile[0]);
        int[] measure = GetHeight(profile[0]);
        System.out.println(measure[0] + "'" + measure[1] + "\"");
        System.out.println("weight: " + profile[1]);
        System.out.println("age: " + profile[2]);
        System.out.println("gender: " + profile[3]);
        System.out.println("MHR: " + profile[4]);
        System.out.println("arm: " + profile[5]);
    }

    public String NewFilename(String WorkoutName){
        //Returns the current date and time used for the file
        String filename = WorkoutName + DateFormat.getDateTimeInstance().format(new Date());
        String[] List = fileList();
        for(String f: List){
            if(f == filename){
                filename = filename + "-";
            }
        }
        return filename;
    }

    public void ListFiles(){
        File Dir = getFilesDir();
        String[] List = fileList();

        System.out.println("Absolute path: " + Dir.getPath());
        System.out.println("Directory Name: " + Dir.getName());
        System.out.println("Size: " + Dir.getTotalSpace() + " Free: " + Dir.getFreeSpace());
        System.out.println("\nFiles:");
        for(String f: List){
            System.out.println(f);
        }
    }

    public void CreateFile(String filename){
        try{
            String msg = "Output File";
            FileOutputStream f = openFileOutput(filename, MODE_PRIVATE);
            f.write(msg.getBytes());
            System.out.println("Created output file with: " + msg.getBytes());
            f.close();
        } catch (Exception ex){
            System.out.println("Well fuck... Can't open files");
        }
    }

    public void AppendFile(String filename, String message){
        try {
            FileOutputStream f = openFileOutput(filename, MODE_APPEND);
            f.write(message.getBytes());
            f.close();
        } catch (Exception ex){
            System.out.println("Well fuck... Can't append to files");
        }
    }

    public void ReadFile(String filename){
        try{
            FileInputStream f = openFileInput(filename);
            int size = f.available();
            byte[] content = new byte[size];
            f.read(content);
            System.out.println("File contains " + size + " bytes :\n" + new String(content));
            f.close();
        } catch(Exception ex){
            System.out.println("Well fuck... Can't read from files");
        }
    }

    public int[] GetHeight(int height){
        int[] measure = new int[2];
        measure[0] = height/12;
        measure[1] = height - 12*measure[0];
        return measure;
    }

    public int GetInches(int feet, int inches){
        return (12*feet + inches);
    }

}