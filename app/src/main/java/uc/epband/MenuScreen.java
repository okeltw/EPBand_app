package uc.epband;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.lang.Math;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadLocalRandom;
import org.json.*;

public class MenuScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Workout mWorkout = null;
    private HeartRate mHeartRate = null;
    private Motion mMotion = null;
    private Exercise mExercise = new Exercise();;

    //public String desiredDeviceName = "Andrew's iPhone";

    private boolean isWorkout = false;

    // TODO: replace isWorkout with mWorkoutInProgress
    public enum workoutState{
        WORKOUT, REVIEW, NONE
    }
    private workoutState mWorkoutInProgress;

    public enum graphState{
        HEARTRATE_REALTIME, HEARTRATE_SUMMARY, MOTION_REALTIME, MOTION_SUMMARY
    }
    private graphState mGraphState;
    private LineChart mLineChart;
    private PieChart mPieChart;

    private Context mContext;

    //RETURN CODES
    private final int SELECT_WORKOUT = 1, SELECT_EXERCISE = 2, REVIEW_EXERCISE = 3;

    private BluetoothBandService BTservice;

    /**
     * The Handler that gets information back from the BluetoothBandService
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
                    System.out.println("Write: " + writeMessage);
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
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != mContext) {
                        Toast.makeText(mContext, "Connected to "+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != mContext) {
                        Toast.makeText(mContext, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mContext = getApplicationContext();

        //GET NECESSARY PERMISSIONS
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        BTservice = new BluetoothBandService(mContext, mHandler);

        //Setup graph variables
        mGraphState = graphState.HEARTRATE_REALTIME;
        mLineChart = (LineChart)findViewById(R.id.linechart);
        mPieChart = (PieChart)findViewById(R.id.piechart);

        mWorkoutInProgress = workoutState.NONE;


        hideAllCharts();
        System.out.println("Created App");
    }

    @Override
    protected void onDestroy() {
        BTservice.close();
        System.out.println("Destroyed EP Band");
        super.onDestroy();
    }

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
                break;
            case R.id.workout2:
                SampleWorkout2();
                break;
            case R.id.time_toggle:
                OptionCallbackTimeSwitch();
                break;
            case R.id.graph_toggle:
                OptionCallbackDataSwitch();
                break;
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
                else{
                    if(mWorkoutInProgress == workoutState.WORKOUT){
                        Toast.makeText(mContext,"Cannot disconnect bluetooth while workout in progress",Toast.LENGTH_LONG);
                    }
                    else endBluetooth();
                }
                break;
            case R.id.nav_edit_workout:
                isWorkout = false;
                System.out.println("Menu: Workout Templates");
                break;
            case R.id.nav_profile:
                isWorkout = false;
                System.out.println("Menu: Profile");
                Intent intent = new Intent(MenuScreen.this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_review:
                isWorkout = false;
                System.out.println("Menu: View Analysis");
                Intent intent_review = new Intent(MenuScreen.this, SelectWorkout.class);
                String[] List = fileList();
                Arrays.sort(List);
                intent_review.putExtra("ListData", List);
                intent_review.putExtra("Title", "Select Past Workout");
                startActivityForResult(intent_review, 1);
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
                // TODO: save previous data?
                mExercise = new Exercise();

                //mWorkout.getHeartRate().PlotAll((LineChart) findViewById(R.id.linechart));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void chooseExercise(){
        Intent intent_review = new Intent(MenuScreen.this, SelectWorkout.class);
        String[] data = Constants.ExerciseList;
        Arrays.sort(data);
        intent_review.putExtra("ListData", Constants.ExerciseList);
        intent_review.putExtra("Title", "Choose Next Exercise");
        startActivityForResult(intent_review, 1);
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
                deleteFile(f);
                System.out.println("\tDeleted: " + f);
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
        if(resultCode == Activity.RESULT_OK){
            switch(requestCode) {
                case SELECT_WORKOUT:
                    /*
                    mLineChart.clear();
                    mPieChart.clear();
                    hideAllCharts();
                    */
                    String filename = data.getStringExtra("result");
                    System.out.println("Chose workout: " + filename);
                    try {
                        mWorkout = new Workout(mContext, filename);
                        System.out.println("Successfully loaded saved workout");
                        System.out.println(ReadFile(filename));
                    } catch (Exception ex) {
                        mWorkout = new Workout(mContext);
                        System.out.println("Could not load saved workout");
                    }
                    mWorkoutInProgress = workoutState.REVIEW;
                    break;
                case SELECT_EXERCISE:
                    String exercise2 = data.getStringExtra("result");
                    System.out.println("Chose exercise: " + exercise2);
                    break;
                case REVIEW_EXERCISE:
                    mWorkoutInProgress = workoutState.REVIEW;
                    String exercise = data.getStringExtra("result");
                    int index = data.getIntExtra("index", -1);
                    System.out.println("Index: " + index);
                    if (index > 0) {
                        try {
                            JSONObject jObject = mMotion.mExerciseData.getJSONObject(index-1);
                            System.out.println("Exercise JSON: " + jObject.toString());
                            mExercise = new Exercise(jObject);
                            System.out.println("Chose to review: " + exercise + "\n" + mExercise.GetJSONString());
                            mExercise.PlotAll(mLineChart);
                            mGraphState = graphState.MOTION_REALTIME;
                        } catch (JSONException ex) {
                            System.out.println("JSONException for exercise review");
                        }
                    }
                    else if(index == 0){
                        mHeartRate.PlotAll(mLineChart);
                        mGraphState = graphState.HEARTRATE_REALTIME;
                        System.out.println("Chose to review: Heart Rate");
                    }
                    else{
                        System.out.println("Chose to review: NOTHING!");
                    }
                    break;
            }
        }
        else if(resultCode == Activity.RESULT_CANCELED){
            System.out.println("Canceled");
        }
        else{
            System.out.println("Unknown activity result");
        }
        System.out.println("onActivityResult() finished");
    }

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

        mMotion = mWorkout.getMotion();
        Exercise exercise = new Exercise();
        try {
            double X, Y, Z, rX, rY, rZ;
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
            exercise.mExercise = "Sample Data";
            System.out.println(exercise.GetJSONString());
            mWorkout.getMotion().AddExerciseData(exercise);
            mWorkout.writeFile();
            ListFiles();
        }catch(JSONException ex){
            System.out.println("JSON Exception in exercise");
        }catch(IOException ex){
            System.out.println("IO Exception for workout");
        }
    }

    private void SampleWorkout2(){
        mWorkout = new Workout(mContext);
        mWorkout.setExampleFileName("2");

        //SAMPLE HEART RATE DATA
        HeartRate HR = mWorkout.getHeartRate();
        HR.SetSampleRate(new Date(6000));
        int[] BPM = new int[1000];
        for(int i = 0; i < BPM.length; i++){
            BPM[i] = (int)(220*Math.pow(Math.cos(i * Math.PI / 1000.0), 2));
        }
        HR.AddData(BPM);

        mMotion = mWorkout.getMotion();
        Exercise exercise = new Exercise();
        try {
            double X = 0, Y = 0, Z = 0, rX = 0, rY = 0, rZ = 0;
            for(int i = 0; i < 1000; i++){
                rX = 45*Math.cos(2 * Math.PI * i / 100 + Math.PI)+45;
                exercise.AddData(X, Y, Z, rX, rY, rZ);
            }
            exercise.PlotAll((LineChart) findViewById(R.id.linechart));
            exercise.mExercise = "Sample Data";
            System.out.println(exercise.GetJSONString());
            mWorkout.getMotion().AddExerciseData(exercise);
        }catch(JSONException ex){
            System.out.println("JSON Exception in exercise");
        }

        exercise = new Exercise();
        try {
            double X = 0, Y = 0, Z = 0, rX = 0, rY = 0, rZ = 0;
            for(int i = 0; i < 1000; i++){
                rY = 45*Math.cos(2 * Math.PI * i / 100 + Math.PI)+45;
                exercise.AddData(X, Y, Z, rX, rY, rZ);
            }
            exercise.PlotAll((LineChart) findViewById(R.id.linechart));
            exercise.mExercise = "Sample Data2";
            System.out.println(exercise.GetJSONString());
            mWorkout.getMotion().AddExerciseData(exercise);
        }catch(JSONException ex){
            System.out.println("JSON Exception in exercise");
        }

        exercise = new Exercise();
        try {
            double X = 0, Y = 0, Z = 0, rX = 0, rY = 0, rZ = 0;
            for(int i = 0; i < 1000; i++){
                rZ = 45*Math.cos(2 * Math.PI * i / 100 + Math.PI)+45;
                exercise.AddData(X, Y, Z, rX, rY, rZ);
            }
            exercise.PlotAll((LineChart) findViewById(R.id.linechart));
            exercise.mExercise = "Sample Data3";
            System.out.println(exercise.GetJSONString());
            mWorkout.getMotion().AddExerciseData(exercise);
        }catch(JSONException ex){
            System.out.println("JSON Exception in exercise");
        }

        try {
            mWorkout.writeFile();
        }catch(Exception ex){
            System.out.println("IO Exception for workout");
        }
    }

    private void updateGraphVisibility(){
        hideAllCharts();
        if(mWorkout == null){
            System.out.println("mWorkout is null");
            return;
        }
        else if(mWorkoutInProgress != workoutState.NONE){
            redrawGraph();
            switch (mGraphState) {
                case HEARTRATE_REALTIME:
                    System.out.println("updateGraphVisibility() for HEARTRATE_REALTIME");
                    mLineChart.setVisibility(View.VISIBLE);
                    mWorkout.plotHeartRateRealTime(mLineChart);
                    break;
                case HEARTRATE_SUMMARY:
                    System.out.println("updateGraphVisibility() for HEARTRATE_SUMMARY");
                    mPieChart.setVisibility(View.VISIBLE);
                    mWorkout.plotHeartRateSummary(mPieChart);
                    break;
                case MOTION_REALTIME:
                    System.out.println("updateGraphVisibility() for MOTION_REALTIME");
                    mLineChart.setVisibility(View.VISIBLE);
                    try{
                        mExercise.PlotAll(mLineChart);
                    }catch (JSONException ex){
                        System.out.println("JSONException with exercise real time plot");
                    }
                    break;
                case MOTION_SUMMARY:
                    System.out.println("updateGraphVisibility() for MOTION_SUMMARY");
                    //mPieChart.setVisibility(View.VISIBLE);

                    break;
            }
        }
    }

    private void redrawGraph(){
        System.out.println("redrawGraph()");
        switch (mGraphState){
            case HEARTRATE_REALTIME:
            case MOTION_REALTIME:
                //mLineChart.invalidate();
                break;

            case HEARTRATE_SUMMARY:
                //mPieChart.invalidate();
                break;
            case MOTION_SUMMARY:

                break;

        }
    }

    private void OptionCallbackTimeSwitch(){
        System.out.println("OptionCallbackTimeSwitch()");
        switch(mWorkoutInProgress) {
            case WORKOUT:
            case REVIEW:
                switch (mGraphState) {
                    case HEARTRATE_REALTIME:
                        mGraphState = graphState.HEARTRATE_SUMMARY;
                        break;
                    case MOTION_REALTIME:
                        mGraphState = graphState.MOTION_SUMMARY;
                        break;
                    case HEARTRATE_SUMMARY:
                        mGraphState = graphState.HEARTRATE_REALTIME;
                        break;
                    case MOTION_SUMMARY:
                        mGraphState = graphState.MOTION_REALTIME;
                        break;
                }
                break;
            case NONE:
                Toast.makeText(mContext, "No workout being made/reviewed", Toast.LENGTH_SHORT).show();
                break;
        }
        updateGraphVisibility();
    }

    private void OptionCallbackDataSwitch(){
        if(mWorkoutInProgress == workoutState.REVIEW){
            System.out.println("Choose review exercise");
            chooseReview();
        }
        else if(mWorkoutInProgress == workoutState.WORKOUT){
            System.out.println("Choose next exercise");
            chooseExercise();
        }
        else{
            Toast.makeText(mContext,"No workout being made/reviewed",Toast.LENGTH_SHORT).show();
        }

    }

    private void chooseReview(){
        System.out.println("chooseReview");
        List<String> fullList = new ArrayList<>();
        mHeartRate = mWorkout.getHeartRate();
        mMotion = mWorkout.getMotion();
        if(mHeartRate != null){
            fullList.add("Heart Rate");
        }
        try {
            List<String> list = mMotion.GetExerciseList();
            fullList.addAll(list);
        }catch(JSONException ex){
            System.out.println("No exercises in workout");
        }
        Intent intent_review = new Intent(MenuScreen.this, SelectWorkout.class);
        String[] passList = new String[fullList.size()];
        passList = fullList.toArray(passList);
        intent_review.putExtra("ListData", passList);
        intent_review.putExtra("Title", "Choose Exercise to Review");
        startActivityForResult(intent_review, REVIEW_EXERCISE);
    }
}