package uc.epband;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SelectWorkout extends AppCompatActivity {


    Intent returnIntent = new Intent();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_workout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent start_intent = getIntent();
        final String[] List = start_intent.getStringArrayExtra("ListData");
        final String Title = start_intent.getStringExtra("Title");
        setTitle(Title);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, List);
        final ListView WorkoutList = (ListView) findViewById(R.id.workoutList);
        WorkoutList.setAdapter(adapter);

        FloatingActionButton confirm = (FloatingActionButton) findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int item = WorkoutList.getCheckedItemPosition();
                if(item == ListView.INVALID_POSITION){
                    returnIntent.putExtra("result", "");
                    returnIntent.putExtra("index", -1);
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                }else{
                    String filename = List[item];
                    returnIntent.putExtra("result", filename);
                    returnIntent.putExtra("index", item);
                    setResult(Activity.RESULT_OK, returnIntent);
                }
                finish();
            }
        });

        FloatingActionButton cancel = (FloatingActionButton) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnIntent.putExtra("result", "");
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        System.out.println("SelectWorkout Created\n");
    }
}
