package uc.epband;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MenuScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id){
            case R.id.nav_device:
                System.out.println("Menu: Device Setup");
                AppendFile("TestWorkout", "__A__");
                ReadFile("TestWorkout");
                CreateFile("HELLO WORLD");
                ReadFile("TestWorkout");
                break;
            case R.id.nav_edit_workout:
                System.out.println("Menu: Workout Templates");
                CreateFile("TestWorkout");
                ReadFile("TestWorkout");
                //R.id.
                break;
            case R.id.nav_profile:
                System.out.println("Menu: Profile");
                int[] profile = GetProfile("test");
                System.out.println("height: " + profile[0]);

                int[] measure = GetHeight(profile[0]);
                System.out.println(measure[0] + "'" + measure[1] + "\"");
                System.out.println("weight: " + profile[1]);
                System.out.println("age: " + profile[2]);
                System.out.println("gender: " + profile[3]);
                break;
            case R.id.nav_review:
                System.out.println("Menu: View Analysis");
                LineChart chart = (LineChart) findViewById(R.id.chart);

                List<Entry> entries = new ArrayList<Entry>();
                int[] X = {0,1,2,3,4,5,6,7,8,9};
                int[] Y = {0,1,4,9,16,25,36,49,64,81};
                for (int i = 0; i < 10; i++){
                    entries.add(new Entry(X[i],Y[i]));
                }
                LineDataSet dataSet = new LineDataSet(entries, "Line Graph");
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                chart.invalidate(); // refresh
                System.out.println("New Chart");
                break;
            case R.id.nav_workout_start:
                System.out.println("Menu: Start Workout");
                ListFiles();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String NewFilename(String WorkoutName){
        //Returns the current date and time used for the file
        String filename = WorkoutName + DateFormat.getDateTimeInstance().format(new Date());
        String[] List = fileList();
        for(String f: List){
            if(f == filename){
                filename = filename + "(1)";
                break;
            }
        }
        return filename;
    };

    public void SetProfile(String filename, int height, int weight, int age, boolean gender){
        // gender: 0 is male, 1 is female
        SharedPreferences Settings = getSharedPreferences(filename,MODE_PRIVATE);
        SharedPreferences.Editor editor = Settings.edit();
        editor.putInt("height", height);
        editor.putInt("weight", weight);
        editor.putInt("age", age);
        editor.putInt("gender", ((gender) ? 1 : 0) );
        editor.apply();
    };

    public int[] GetProfile(String filename){
        SharedPreferences Settings = getSharedPreferences(filename,MODE_PRIVATE);
        int[] profile = new int[4];
        profile[0] = Settings.getInt("height", 70);
        profile[1] = Settings.getInt("weight", 170);
        profile[2] = Settings.getInt("age", 21);
        profile[3] = Settings.getInt("gender", 0);
        return profile;
    };

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
    };

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
    };

    public void AppendFile(String filename, String message){
        try {
            FileOutputStream f = openFileOutput(filename, MODE_APPEND);
            f.write(message.getBytes());
            f.close();
        } catch (Exception ex){
            System.out.println("Well fuck... Can't append to files");
        }
    };

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
    };

    public int[] GetHeight(int height){
        int[] measure = new int[2];
        measure[0] = height/12;
        measure[1] = height - 12*measure[0];
        return measure;
    };

    public int GetInches(int feet, int inches){
        return (12*feet + inches);
    }


}
