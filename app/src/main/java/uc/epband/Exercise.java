package uc.epband;

import android.util.Size;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Exercise {
    private Boolean mValid = false, mAnalyzed = false;
    private String mExercise = "", mStartTime = "", mTimeLength = "", mSampleStep = "";
    private Double mGoalROM = 0.9d, mAverageROM = 0d;
    private int mReps = 0;
    private JSONArray mRotX, mRotY, mRotZ, mDistX, mDistY, mDistZ;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");

    //JSON Member names
    private static final String
            VALID = "Valid",
            ANALYZED = "Analyzed",
            EXERCISE = "Exercise",
            START_TIME = "StartTime",
            TIME_LENGTH = "TimeLength",
            SAMPLE_STEP = "SampleStep",
            GOAL_ROM = "GoalROM",
            AVG_ROM = "AvgROM",
            REPS = "Reps",
            ROT_X = "RotationX",
            ROT_Y = "RotationY",
            ROT_Z = "RotationZ",
            DIST_X = "DistanceX",
            DIST_Y = "DistanceY",
            DIST_Z = "DistanceZ";

    public Exercise(){

    }

    public Exercise(String jString) throws JSONException{
        UseJSONString(jString);
    }

    public Exercise(JSONObject jObject) throws JSONException{
        UseJSONObject(jObject);
    }

    public Boolean UseJSONString(String jString) throws JSONException{
        JSONObject jObject = new JSONObject(jString);
        UseJSONObject(jObject);
        return true;
    }

    public Boolean UseJSONObject(JSONObject jObject) throws JSONException {
        mValid = false;
        mAnalyzed = jObject.getBoolean(ANALYZED);
        mExercise = jObject.getString(EXERCISE);
        mStartTime = jObject.getString(START_TIME);
        mTimeLength = jObject.getString(TIME_LENGTH);
        mSampleStep = jObject.getString(SAMPLE_STEP);
        mGoalROM = jObject.getDouble(GOAL_ROM);
        mAverageROM = jObject.getDouble(AVG_ROM);
        mReps = jObject.getInt(REPS);
        mRotX = jObject.getJSONArray(ROT_X);
        mRotY = jObject.getJSONArray(ROT_Y);
        mRotZ = jObject.getJSONArray(ROT_Z);
        mDistX = jObject.getJSONArray(DIST_X);
        mDistY = jObject.getJSONArray(DIST_Y);
        mDistZ = jObject.getJSONArray(DIST_Z);
        mValid = jObject.getBoolean(VALID);
        mAnalyzed = jObject.getBoolean(ANALYZED);
        return mValid;
    }

    public JSONObject GetJSONObject() throws JSONException{
        JSONObject jObject = new JSONObject();
        jObject.put(VALID, mValid);
        jObject.put(ANALYZED, mAnalyzed);
        jObject.put(EXERCISE, mExercise);
        jObject.put(START_TIME, mStartTime);
        jObject.put(TIME_LENGTH, mTimeLength);
        jObject.put(SAMPLE_STEP, mSampleStep);
        jObject.put(GOAL_ROM, mGoalROM);
        jObject.put(AVG_ROM, mAverageROM);
        jObject.put(REPS, mReps);
        jObject.put(ROT_X, mRotX);
        jObject.put(ROT_Y, mRotY);
        jObject.put(ROT_Z, mRotZ);
        jObject.put(DIST_X, mDistX);
        jObject.put(DIST_Y, mDistY);
        jObject.put(DIST_Z, mDistZ);
        return jObject;
    }

    public String GetJSONString() throws JSONException{
        JSONObject jObject = GetJSONObject();
        return jObject.toString();
    }

    public void AddData(double distX, double distY, double distZ, double rotX, double rotY, double rotZ) throws JSONException{
        mDistX.put(distX);
        mDistZ.put(distY);
        mDistZ.put(distZ);
        mRotX.put(rotX);
        mRotY.put(rotY);
        mRotZ.put(rotZ);
        mAnalyzed = false;
    }

    public void AddRotation(double X, double Y, double Z) throws JSONException{
        mRotX.put(X);
        mRotY.put(Y);
        mRotZ.put(Z);
    }

    public void AddDistance(double X, double Y, double Z) throws JSONException{
        mDistX.put(X);
        mDistY.put(Y);
        mDistZ.put(Z);
    }

    public void SetStartTime(Date date){
        mStartTime = dateFormat.format(date);
    }

    public void SetEndTime(Date endDate) throws ParseException{
        Date start = dateFormat.parse(mStartTime);
        if(endDate.after(start)){
            mTimeLength = dateFormat.format(new Date(endDate.getTime() - start.getTime()));
        }
        else{
            mTimeLength = dateFormat.format(new Date(0));
            mValid = false;
        }
    }


}
