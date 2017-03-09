package uc.epband;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Motion {
    private JSONArray mExerciseData = new JSONArray();
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");

    private static final String EXERCISE_DATA = "ExerciseData";

    public Motion(){

    }

    public Motion(String jString) throws JSONException{
        UseJSONString(jString);
    }

    public Motion(JSONObject jObject) throws JSONException{
        UseJSONObject(jObject);
    }

    public Boolean UseJSONString(String jString) throws JSONException {
        JSONObject jObject = new JSONObject(jString);
        UseJSONObject(jObject);
        return true;
    }

    public void UseJSONObject(JSONObject jObject) throws JSONException {
        mExerciseData = jObject.getJSONArray(EXERCISE_DATA);
    }

    public JSONObject GetJSONObject() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put(EXERCISE_DATA, mExerciseData);
        return jObject;
    }

    public List<String> GetExerciseList() throws JSONException{
        List<String> Exercises = new ArrayList<>();
        for(int i = 0; i < mExerciseData.length(); i++){
            Exercises.add(mExerciseData.getString(i));
        }
        return Exercises;
    }

    public void AddExerciseData(Exercise exercise) throws JSONException{
        JSONObject jObject = exercise.GetJSONObject();
        mExerciseData.put(jObject);
    }

}
