package uc.epband;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.lang.Math;
import org.json.*;

public class MenuScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Constants {
    private Workout mWorkout = null;
    private HeartRate mHeartRate = null;
    private Motion mMotion = null;
    private Exercise mExercise = null;

    private boolean[] mLineToggles;

    public enum workoutState{
        WORKOUT, REVIEW, NONE
    }
    private workoutState mWorkoutInProgress;
    private Boolean mExerciseInProgress;

    public enum graphState{
        HEARTRATE_REALTIME, HEARTRATE_SUMMARY, MOTION_REALTIME, MOTION_SUMMARY, DISPLAY_SUMMARY, NONE
    }
    private graphState mGraphState;
    private LineChart mLineChart;
    private PieChart mPieChart;
    private ListView mSummaryList;

    private Context mContext;
    private AlertDialog.Builder mDialogConnect, mDialogDisconnect, mDialogCreateWorkout, mDialogEndWorkout,
            mDialogEndExercise, mDialogNextExercise, mDialogDeleteFiles, mDialogToggleLines, mDialogDisplayAnalysis;

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
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    System.out.println("MenuScreen: Read: " + readMessage);
                    try {
                        handleBluetoothData(readMessage);
                        if(mWorkoutInProgress == workoutState.WORKOUT){
                            switch(mGraphState){
                                case HEARTRATE_REALTIME:
                                    mWorkout.plotHeartRateRealTime(mLineChart);
                                    break;
                                case MOTION_REALTIME:
                                    try{
                                        mExercise.PlotAll(mContext, mLineChart);
                                    }catch(JSONException ex){
                                        System.out.println("Couldn't plot motion realtime");
                                    }
                                    break;
                                default:
                                    System.out.println("No valid graphs to plot realtime data");
                                    mGraphState = graphState.HEARTRATE_REALTIME;
                                    break;
                            }
                        }
                    }catch (JSONException ex){
                        System.out.println("Could not read message from EPBand, JSON Exception");
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != mContext) {
                        Toast.makeText(mContext, "MenuScreen: Connected to "+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != mContext) {
                        Toast.makeText(mContext, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_DISCONNECTED:
                    BTservice.disconnected();
                    System.out.println("Bluetooth Service was disconnected");
                    Toast.makeText(mContext, "Disconnected from EPBand", Toast.LENGTH_LONG).show();
                    if(mExerciseInProgress){
                        try{
                            mExercise.SetEndTime(new Date());
                            mWorkout.getMotion().AddExerciseData(mExercise);
                            mExercise = null;
                            mExerciseInProgress = false;
                            invalidateOptionsMenu();
                        }catch(Exception ex){
                            Toast.makeText(mContext,"Error with exercise. Discarding data.",Toast.LENGTH_SHORT);
                        }
                    }
                    if(mWorkoutInProgress == workoutState.WORKOUT){
                        try{
                            mWorkout.writeFile();
                            mWorkoutInProgress = workoutState.NONE;
                            mWorkout = null;
                            mHeartRate = null;
                            mExercise = null;
                            invalidateOptionsMenu();
                        }catch(Exception ex){

                        }
                    }

                    break;
            }
        }
    };

    void handleBluetoothData(String jString) throws JSONException{
        JSONObject readObject = new JSONObject(jString);
        if(mWorkoutInProgress == workoutState.WORKOUT){
            int BPM = (int)Math.round(readObject.getDouble("BPM"));
            mWorkout.getHeartRate().AddData(BPM);

            JSONObject motionObject = readObject.getJSONObject("Motion");
            if(mExerciseInProgress == true){
                System.out.println("\nmExerciseInProgress");
                JSONArray X = motionObject.getJSONArray("X");
                JSONArray Y = motionObject.getJSONArray("Y");
                JSONArray Z = motionObject.getJSONArray("Z");
                JSONArray RX = motionObject.getJSONArray("RX");
                JSONArray RY = motionObject.getJSONArray("RY");
                JSONArray RZ = motionObject.getJSONArray("RZ");
                System.out.println("Add data");
                try {
                    mExercise.AddDataJSONArray(X, Y, Z, RX, RY, RZ);
                }catch (JSONException ex){
                    System.out.println("AddDataJSONArray is the problem");
                }
            }
        }
    }

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

        String[] files = fileList();
        Boolean[] examplesPresent = {false, false};
        for(String f: files){
            if(f.equals("Example_1")){
                examplesPresent[0] = true;
            }
            else if(f.equals("Example_2")){
                examplesPresent[1] = true;
            }
        }
        if(!examplesPresent[0]) SampleWorkout1();
        if(!examplesPresent[1]) SampleWorkout2();

        //Setup graph variables
        mGraphState = graphState.NONE;
        mLineChart = (LineChart)findViewById(R.id.linechart);
        mPieChart = (PieChart)findViewById(R.id.piechart);
        mSummaryList = (ListView) findViewById(R.id.summaryList);

        mExerciseInProgress = false;
        mWorkoutInProgress = workoutState.NONE;
        invalidateOptionsMenu();

        createDialogTemplates();
        hideAllCharts();
        System.out.println("Created App");
    }

    @Override
    protected void onDestroy() {
        BTservice.finalClose();
        System.out.println("Destroyed EP Band");
        super.onDestroy();
    }

    private void establishBluetooth() {
        System.out.println("establishBluetooth()");
        if (BTservice.canConnect()) {
            System.out.println("builder.create()");
            mDialogConnect.show();
        }
        else{
            System.out.println("BTservice can't connect");
            Toast.makeText(mContext,"Unable to locate EP Band",Toast.LENGTH_LONG).show();
        }
    }

    private void endBluetooth() {
        mDialogDisconnect.show();
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
        MenuItem timeToggle = menu.findItem(R.id.time_toggle);
        MenuItem graphToggle = menu.findItem(R.id.graph_toggle);
        MenuItem exerciseStart = menu.findItem(R.id.exercise_on);
        MenuItem exerciseStop = menu.findItem(R.id.exercise_off);
        MenuItem displayAnalysis = menu.findItem(R.id.display_analysis);
        switch(mWorkoutInProgress){
            case REVIEW:
                graphToggle.setEnabled(true).setVisible(true);
                exerciseStart.setEnabled(false).setVisible(false);
                exerciseStop.setEnabled(false).setVisible(false);
                if(mExercise != null || mHeartRate != null) {
                    timeToggle.setEnabled(true).setVisible(true);
                    if(mExercise != null) displayAnalysis.setEnabled(true).setVisible(true);
                    else displayAnalysis.setEnabled(false).setVisible(false);
                }
                else{
                    displayAnalysis.setEnabled(false).setVisible(false);
                    timeToggle.setEnabled(false).setVisible(false);
                }
                break;
            case WORKOUT:
                timeToggle.setEnabled(false).setVisible(false);
                graphToggle.setEnabled(false).setVisible(false);
                displayAnalysis.setEnabled(false).setVisible(false);
                if(mExerciseInProgress) {
                    exerciseStart.setEnabled(false).setVisible(false);
                    exerciseStop.setEnabled(true).setVisible(true);
                }
                else {
                    exerciseStart.setEnabled(true).setVisible(true);
                    exerciseStop.setEnabled(false).setVisible(false);
                }
                break;
            case NONE:
            default:
                displayAnalysis.setEnabled(false).setVisible(false);
                timeToggle.setEnabled(false).setVisible(false);
                graphToggle.setEnabled(false).setVisible(false);
                exerciseStart.setEnabled(false).setVisible(false);
                exerciseStop.setEnabled(false).setVisible(false);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.bluetooth_status:
                String Message = (BTservice.isConnected()) ? "EP Band is Connected" : "No bluetooth connection";
                Toast.makeText(this,Message,Toast.LENGTH_SHORT).show();
                break;
            case R.id.print_Files:
                String[] files = fileList();
                for(String f : files) {
                    try {
                        FileInputStream fileStream = mContext.openFileInput(f);
                        int size = fileStream.available();
                        byte[] content = new byte[size];
                        fileStream.read(content);
                        fileStream.close();
                        System.out.println(f + " " + size + " bytes");
                        System.out.println(new String(content));
                    }
                    catch(Exception ex) {
                    }
                }
                break;
            case R.id.delete_files:
                mDialogDeleteFiles.show();
                break;
            case R.id.time_toggle:
                OptionCallbackTimeSwitch();
                break;
            case R.id.graph_toggle:
                OptionCallbackDataSwitch();
                break;
            case R.id.exercise_on:
                mDialogNextExercise.show();
                break;
            case R.id.exercise_off:
                mDialogEndExercise.show();
                break;
            case R.id.line_toggles:
                mDialogToggleLines.show();
                break;
            case R.id.display_analysis:
                if (mExercise != null) {
                    if(mSummaryList.getVisibility() == View.VISIBLE) {
                        hideAllCharts();
                        try {
                            mExercise.PlotAngles(mContext, mLineChart);
                            mGraphState = graphState.MOTION_SUMMARY;
                        }catch(JSONException ex){

                        }
                    }
                    else{
                        try {
                            mExercise.PrintAnalysis(mLineChart);
                            mExercise.DisplaySummary(mContext, mSummaryList);
                        }catch(JSONException ex){

                        }
                    }
                }
                else{
                    System.out.println("No exercise to display summary");
                }
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
                System.out.println("Menu: Device Setup");
                if (!BTservice.isConnected() || !BTservice.isBonded()) establishBluetooth();
                else{
                    if(mWorkoutInProgress == workoutState.WORKOUT){
                        Toast.makeText(mContext,"Cannot disconnect bluetooth while workout in progress",Toast.LENGTH_LONG);
                    }
                    else endBluetooth();
                }
                break;
            case R.id.nav_edit_workout:
                System.out.println("Menu: Workout Templates");
                if(mExercise != null){
                    try {
                        Bundle bundle = mExercise.PrintAnalysis(mLineChart);
                        System.out.println(bundle.getString("Exercise"));
                        updateDisplayAnalysisDialog(bundle);
                        //mDialogDisplayAnalysis.show();
                    }catch(JSONException ex){
                        System.out.println("Fuck");
                    }
                }
                //mDialogDisplayAnalysis.show();
                break;
            case R.id.nav_profile:
                System.out.println("Menu: Profile");
                Intent intent = new Intent(MenuScreen.this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_review:
                if(mWorkoutInProgress == workoutState.WORKOUT){
                    Toast.makeText(mContext,"Cannot review while workout in progress",Toast.LENGTH_SHORT);
                }else {
                    System.out.println("Menu: View Analysis");
                    Intent intent_review = new Intent(MenuScreen.this, SelectWorkout.class);
                    String[] List = fileList();
                    Arrays.sort(List);
                    intent_review.putExtra("ListData", List);
                    intent_review.putExtra("Title", "Select Past Workout");
                    startActivityForResult(intent_review, 1);
                }
                break;
            case R.id.nav_workout_start:
                System.out.println("Menu: Start Workout");
                if(mWorkoutInProgress == workoutState.WORKOUT){
                    mDialogEndWorkout.show();
                }
                else if(BTservice.isConnected()){
                    mDialogCreateWorkout.show();
                }
                else{
                    Toast.makeText(mContext,"Must be connected to EP Band",Toast.LENGTH_SHORT).show();
                }
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
        startActivityForResult(intent_review, SELECT_EXERCISE);
    }

    private void hideAllCharts() {
        mSummaryList.setVisibility(View.INVISIBLE);
        mPieChart.setVisibility(View.INVISIBLE);
        mLineChart.setVisibility(View.INVISIBLE);
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

    private void DeleteAllFilesAndExamples(){
        System.out.println("Deleting all EPBand files and Examples.");
        String[] List = fileList();
        for(String f: List){
            deleteFile(f);
            System.out.println("\tDeleted: " + f);
        }
        System.out.println("Delete finished.\n");
    }

    private void DeleteAllFiles(){
        System.out.println("Deleting all EPBand files.");
        String[] List = fileList();
        for(String f: List){
            if(f.startsWith(Constants.EXAMPLE) && !f.contains("(")) {
                //deleteFile(f);
                System.out.println("\tKept: " + f);
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
                    invalidateOptionsMenu();
                    break;
                case SELECT_EXERCISE:
                    String exercise_name = data.getStringExtra("result");
                    System.out.println("Chose exercise: " + exercise_name);
                    mExercise.mExercise = exercise_name;
                    break;
                case REVIEW_EXERCISE:
                    mWorkoutInProgress = workoutState.REVIEW;
                    invalidateOptionsMenu();
                    String exercise = data.getStringExtra("result");
                    int index = data.getIntExtra("index", -1);
                    System.out.println("Index: " + index);
                    if (index > 0) {
                        hideAllCharts();
                        try {
                            JSONObject jObject = mMotion.mExerciseData.getJSONObject(index-1);
                            System.out.println("Exercise JSON: " + jObject.toString());
                            mExercise = new Exercise(jObject);
                            System.out.println("Chose to review: " + exercise + "\n" + mExercise.GetJSONString());
                            mExercise.PlotAll(mContext, mLineChart);
                            mGraphState = graphState.MOTION_REALTIME;
                        } catch (JSONException ex) {
                            System.out.println("JSONException for exercise review");
                        }
                    }
                    else if(index == 0){
                        hideAllCharts();
                        mExercise = null;
                        mWorkout.plotHeartRateRealTime(mLineChart);
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
            //exercise.PlotAll((LineChart) findViewById(R.id.linechart));
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
            switch (mGraphState) {
                case HEARTRATE_REALTIME:
                    System.out.println("updateGraphVisibility() for HEARTRATE_REALTIME");
                    mWorkout.plotHeartRateRealTime(mLineChart);
                    break;
                case HEARTRATE_SUMMARY:
                    System.out.println("updateGraphVisibility() for HEARTRATE_SUMMARY");
                    mWorkout.plotHeartRateSummary(mPieChart);
                    break;
                case MOTION_REALTIME:
                    System.out.println("updateGraphVisibility() for MOTION_REALTIME");
                    try{
                        mExercise.PlotAll(mContext, mLineChart);
                    }catch (JSONException ex){
                        System.out.println("JSONException with exercise real time plot");
                    }
                    break;
                case MOTION_SUMMARY:
                    System.out.println("updateGraphVisibility() for MOTION_SUMMARY");
                    try{
                        mExercise.PlotAngles(mContext, mLineChart);
                    }
                    catch( JSONException ex){
                        System.out.println("Couldn't print angles");
                    }
                    break;
                case DISPLAY_SUMMARY:
                    try {
                        mExercise.analyzeRawData();
                    }catch (JSONException ex){

                    }
                    mExercise.DisplaySummary(mContext, mSummaryList);
                    break;
            }
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

    public void createDialogTemplates(){
        createDialogConnect();
        createDialogDisconnect();
        createDialogNewWorkout();
        createDialogEndWorkout();
        createDialogNextExercise();
        createDialogEndExercise();
        createDialogDeleteFiles();
        createDialogToggleLines();
        createDialogDisplayAnalysis();
    }

    public void createDialogConnect(){
        mDialogConnect = new AlertDialog.Builder(this);
        mDialogConnect.setMessage("Attempt connection to EP Band?")
                .setTitle("Bluetooth")
                .setIcon(R.drawable.ic_bluetooth_connected_24dp);

        mDialogConnect.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("Bluetooth connect");
                BTservice.connect();
                if(!BTservice.isConnected()) Toast.makeText(mContext, "Cannot find EP Band",Toast.LENGTH_SHORT).show();
            }
        });
        mDialogConnect.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("cancel");
            }
        });
    }

    public void createDialogDisconnect(){
        mDialogDisconnect = new AlertDialog.Builder(this);
        mDialogDisconnect.setMessage("Disconnect from EP Band?")
                .setTitle("Bluetooth")
                .setIcon(R.drawable.ic_bluetooth_disabled_24dp);

        mDialogDisconnect.setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("Bluetooth disconnect");
                BTservice.close();
            }
        });
        mDialogDisconnect.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("cancel");
            }
        });
    }

    public void createDialogNewWorkout(){
        mDialogCreateWorkout = new AlertDialog.Builder(this);
        mDialogCreateWorkout.setMessage("Start new workout?")
                .setTitle("Workout")
                .setIcon(R.drawable.ic_note_add_24dp);

        mDialogCreateWorkout.setPositiveButton("Start Workout", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("New workout");
                mWorkoutInProgress = workoutState.WORKOUT;
                mWorkout = new Workout(mContext);
                invalidateOptionsMenu();
            }
        });
        mDialogCreateWorkout.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("cancel");
            }
        });
    }

    public void createDialogEndWorkout(){
        mDialogEndWorkout = new AlertDialog.Builder(this);
        mDialogEndWorkout.setMessage("End current workout?")
                .setTitle("Workout")
                .setIcon(R.drawable.ic_cancel_24dp);

        mDialogEndWorkout.setPositiveButton("End Workout", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("End workout");
                try{
                    mWorkout.writeFile();
                    mWorkoutInProgress = workoutState.NONE;
                    mWorkout = null;
                    mHeartRate = null;
                    mExercise = null;
                    invalidateOptionsMenu();
                }catch(Exception ex){

                }
            }
        });
        mDialogEndWorkout.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("cancel");
            }
        });
    }

    public void createDialogNextExercise(){
        mDialogNextExercise = new AlertDialog.Builder(this);
        mDialogNextExercise.setMessage("Start next exercise?")
                .setTitle("Exercise")
                .setIcon(R.drawable.ic_add_24dp);

        mDialogNextExercise.setPositiveButton("Next Exercise", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("Next exercise");
                mExercise = new Exercise();
                chooseExercise();
                mExerciseInProgress = true;
                invalidateOptionsMenu();
            }
        });
        mDialogNextExercise.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("cancel");
            }
        });
    }

    public void createDialogEndExercise(){
        mDialogEndExercise = new AlertDialog.Builder(this);
        mDialogEndExercise.setMessage("End current exercise?")
                .setTitle("Exercise")
                .setIcon(R.drawable.ic_stop_24dp);

        mDialogEndExercise.setPositiveButton("End Exercise", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try{
                    mExercise.SetEndTime(new Date());
                    mWorkout.getMotion().AddExerciseData(mExercise);
                    mExercise = null;
                    mExerciseInProgress = false;
                    invalidateOptionsMenu();
                }catch(Exception ex){
                    Toast.makeText(mContext,"Error with exercise. Discarding data.",Toast.LENGTH_SHORT);
                }
                System.out.println("End exercise");
            }
        });
        mDialogEndExercise.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("cancel");
            }
        });
    }

    public void createDialogDeleteFiles(){
        mDialogDeleteFiles = new AlertDialog.Builder(this);
        mDialogDeleteFiles.setMessage("Erase all saved workouts?")
                .setTitle("Erase Files")
                .setIcon(R.drawable.ic_backspace_24dp);

        mDialogDeleteFiles.setPositiveButton("Erase All", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("Delete all files");
                DeleteAllFilesAndExamples();
            }
        });
        mDialogDeleteFiles.setNegativeButton("Erase Non Example", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("Delete all non-example files");
                DeleteAllFiles();
            }
        });
        mDialogDeleteFiles.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("cancel");
            }
        });
    }

    public void createDialogDisplayAnalysis(){
        mDialogDisplayAnalysis = new AlertDialog.Builder(this);
        mDialogDisplayAnalysis.setView(getLayoutInflater().inflate(R.layout.test, null));

        mDialogDisplayAnalysis.setMessage("Erase all saved workouts?")
                .setTitle("Erase Files")
                .setIcon(R.drawable.ic_dumbell);

        mDialogDisplayAnalysis.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("Close analysis");
            }
        });
    }

    public void updateDisplayAnalysisDialog(Bundle bundle){
        //ViewParent parent = findViewById(R.layout.test).getParent();

        TextView ExerciseText =  (TextView) findViewById(R.id.textExercise);
        //ExerciseText.setText(bundle.getString("Exercise", ""));
        ExerciseText.setText("TEST EXERCISE");

        TextView ROMText =  (TextView) findViewById(R.id.textAvgROM);
        Double repsROM = bundle.getDouble("ROM",0.0);
        //ROMText.setText( repsROM.toString() );
        ROMText.setText("1");

        TextView RepsText =  (TextView) findViewById(R.id.textReps);
        Integer repsVal = bundle.getInt("Reps",0);
        //RepsText.setText(repsVal.toString());
        RepsText.setText("50");

        TextView ConsitencyText =  (TextView) findViewById(R.id.textConsistency);
        Double consistencyVal = bundle.getDouble("StdDev",0.0);
        //ConsitencyText.setText( consistencyVal.toString() );
        ConsitencyText.setText("100");
        createDialogDisplayAnalysis();
    }

    public void createDialogToggleLines(){
        mDialogToggleLines = new AlertDialog.Builder(this);
        final SharedPreferences Settings = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        mLineToggles = new boolean[9];
        mLineToggles[0] = Settings.getBoolean("X", true);
        mLineToggles[1] = Settings.getBoolean("Y", true);
        mLineToggles[2] = Settings.getBoolean("Z", true);
        mLineToggles[3] = Settings.getBoolean("RX", true);
        mLineToggles[4] = Settings.getBoolean("RY", true);
        mLineToggles[5] = Settings.getBoolean("RZ", true);
        mLineToggles[6] = Settings.getBoolean("AX", true);
        mLineToggles[7] = Settings.getBoolean("AY", true);
        mLineToggles[8] = Settings.getBoolean("AZ", true);
        mDialogToggleLines.setTitle("Toggle Graph Lines")
                .setIcon(R.drawable.ic_dumbell)

                .setMultiChoiceItems(R.array.graphtoggles, mLineToggles, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mLineToggles[which] = true;
                                } else {
                                    // Else, if the item is already in the array, remove it
                                    mLineToggles[which] = false;
                                }
                            }
                        }
                )
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        SharedPreferences.Editor editor = Settings.edit();
                        editor.putBoolean("X", mLineToggles[0]);
                        editor.putBoolean("Y", mLineToggles[1]);
                        editor.putBoolean("Z", mLineToggles[2]);
                        editor.putBoolean("RX", mLineToggles[3]);
                        editor.putBoolean("RY", mLineToggles[4]);
                        editor.putBoolean("RZ", mLineToggles[5]);
                        editor.putBoolean("AX", mLineToggles[6]);
                        editor.putBoolean("AY", mLineToggles[7]);
                        editor.putBoolean("AZ", mLineToggles[8]);
                        editor.apply();
                        updateGraphVisibility();
                    }
                });



    }
}