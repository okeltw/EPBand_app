package uc.epband;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int[] profile = GetProfile("test");
        EditText Age = (EditText) findViewById(R.id.editTextAge);
        EditText Height = (EditText) findViewById(R.id.editTextHeight);
        EditText Weight = (EditText) findViewById(R.id.editTextWeight);
        EditText ROM = (EditText) findViewById(R.id.editTextROM);
        ToggleButton Gender = (ToggleButton) findViewById(R.id.toggleButtonGender);


        Height.setText(Integer.toString(profile[0]));
        Weight.setText(Integer.toString(profile[1]));
        Age.setText(Integer.toString(profile[2]));
        ROM.setText(Integer.toString(profile[3]));

        if(profile[4] == 1){
            Gender.setChecked(true);
        }
        else{
            Gender.setChecked(false);
        }

        FloatingActionButton commit = (FloatingActionButton) findViewById(R.id.commit);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // COMMIT ALL CHANGES TO THE SHARED PREFERENCES PROFILE
                EditText Age = (EditText) findViewById(R.id.editTextAge);
                EditText Height = (EditText) findViewById(R.id.editTextHeight);
                EditText Weight = (EditText) findViewById(R.id.editTextWeight);
                EditText ROM = (EditText) findViewById(R.id.editTextROM);
                ToggleButton Gender = (ToggleButton) findViewById(R.id.toggleButtonGender);

                int age = Integer.parseInt(Age.getText().toString());
                int height = Integer.parseInt(Height.getText().toString());
                int weight = Integer.parseInt(Weight.getText().toString());
                int rom = Integer.parseInt(ROM.getText().toString());
                boolean gender = Gender.isChecked();

                SetProfile("test", height, weight, age, rom, gender);
                finish();
            }
        });

        FloatingActionButton cancel = (FloatingActionButton) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // REJECT ALL CHANGES
                finish();
            }
        });
    }

    public void SetProfile(String filename, int height, int weight, int age, int ROM, boolean gender){
        // gender: 0 is male, 1 is female
        if(age < 10) age = 18;
        else if (age > 100) age = 100;

        if(ROM < 60) ROM = 60;
        else if(ROM > 100) ROM = 100;

        int MHR = 220 - age;

        if(height < 58) height = 58;
        else if(height > 96) height = 70;
        int ArmLength = (height*4/10); //assume each arm is 40% the height

        SharedPreferences Settings = getSharedPreferences(filename,MODE_PRIVATE);
        SharedPreferences.Editor editor = Settings.edit();
        editor.putInt("height", height);
        editor.putInt("weight", weight);
        editor.putInt("age", age);
        editor.putInt("ROM", ROM);
        editor.putInt("gender", ((gender) ? 1 : 0) );
        editor.putInt("MHR", MHR);
        editor.putInt("arm", ArmLength);
        editor.apply();
    }

    public int[] GetProfile(String filename){
        SharedPreferences Settings = getSharedPreferences(filename,MODE_PRIVATE);
        int[] profile = new int[5];
        profile[0] = Settings.getInt("height", 70);
        profile[1] = Settings.getInt("weight", 170);
        profile[2] = Settings.getInt("age", 21);
        profile[3] = Settings.getInt("ROM", 70);
        profile[4] = Settings.getInt("gender", 0);
        return profile;
    }
}
