package uc.epband;

import android.graphics.Color;
import android.util.Size;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static uc.epband.Constants.MOTION;

public class Exercise implements Constants{
    private Boolean mValid = false, mAnalyzed = false;
    private String mExercise, mStartTime, mTimeLength, mSampleStep;
    private Double mGoalROM, mAverageROM;
    private int mReps;
    private JSONArray mRotX, mRotY, mRotZ, mDistX, mDistY, mDistZ;
    private int dataLen;

    private String  defaultExercise = "default",
                    defaultStartTime = DateFormat.getDateTimeInstance().format(new Date()),
                    defaultTimeLength = "0.1", //I don't know how this is formatted
                    defaultSampleStep = "0.1";
    private Double  defaultGoalROM = 0.8,
                    defaultAverageROM = 0.0;
    private int     defaultmReps = 0;
    private Boolean defaultmValid = true,
                    defaultmAnalyzed = false;



    public Exercise(){
        mRotX = new JSONArray();
        mRotY = new JSONArray();
        mRotZ = new JSONArray();
        mDistX = new JSONArray();
        mDistY = new JSONArray();
        mDistZ = new JSONArray();
        mReps = 0;
        mGoalROM = 0.8;
        mAverageROM = 0.0;
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
        jObject = Prepare(jObject);
        mValid = false;
        mExercise = jObject.getString(EXERCISE);
        mStartTime = jObject.getString(START_TIME);
        mTimeLength = jObject.getString(TIME_LENGTH);
        mSampleStep = jObject.getString(SAMPLE_STEP);
        mGoalROM = jObject.getDouble(GOAL_ROM);
        mAverageROM = jObject.getDouble(AVG_ROM);
        mReps = jObject.getInt(REPS);

        JSONObject motion = jObject.getJSONObject(MOTION);
        mRotX = motion.getJSONArray(ROT_X);
        mRotY = motion.getJSONArray(ROT_Y);
        mRotZ = motion.getJSONArray(ROT_Z);
        mDistX = motion.getJSONArray(DIST_X);
        mDistY = motion.getJSONArray(DIST_Y);
        mDistZ = motion.getJSONArray(DIST_Z);

        mValid = jObject.getBoolean(VALID);
        mAnalyzed = jObject.getBoolean(ANALYZED);


        dataLen = mRotX.length();
        return mValid;
    }

    public JSONObject Prepare(JSONObject jObject) throws JSONException{
        if(!jObject.has(EXERCISE))      jObject.put(EXERCISE, defaultExercise);
        if(!jObject.has(START_TIME))    jObject.put(START_TIME, defaultStartTime);
        if(!jObject.has(TIME_LENGTH))   jObject.put(TIME_LENGTH, defaultTimeLength);
        if(!jObject.has(SAMPLE_STEP))   jObject.put(SAMPLE_STEP, defaultSampleStep);
        if(!jObject.has(GOAL_ROM))      jObject.put(GOAL_ROM, defaultGoalROM);
        if(!jObject.has(AVG_ROM))       jObject.put(AVG_ROM, defaultAverageROM);
        if(!jObject.has(REPS))          jObject.put(REPS, defaultmReps);
        if(!jObject.has(VALID))         jObject.put(VALID, defaultmValid);
        if(!jObject.has(ANALYZED))      jObject.put(ANALYZED, defaultmAnalyzed);

        return jObject;
    }

    public String getName(){
        return mExercise;
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

    public void AddStringData(String jString) throws JSONException{
        JSONObject jObject = new JSONObject(jString);
        AddJSONData(jObject);
    }

    public void AddJSONData(JSONObject jObject) throws JSONException{
        JSONObject motion = jObject.getJSONObject(MOTION);
        JSONArray   dX = motion.getJSONArray(DIST_X),
                    dY = motion.getJSONArray(DIST_Y),
                    dZ = motion.getJSONArray(DIST_Z),
                    rX = motion.getJSONArray(ROT_X),
                    rY = motion.getJSONArray(ROT_Y),
                    rZ = motion.getJSONArray(ROT_Z);

        for(int index = 0; index < dX.length(); index++){
            AddData(dX.getDouble(index),
                    dY.getDouble(index),
                    dZ.getDouble(index),
                    rX.getDouble(index),
                    rY.getDouble(index),
                    rZ.getDouble(index));
        }
    }

    public void AddData(double distX, double distY, double distZ, double rotX, double rotY, double rotZ) throws JSONException{
        mDistX.put(distX);
        mDistY.put(distY);
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

    public void AddDistanceX(double val) throws JSONException{
        mDistX.put(val);
    }

    public void AddDistanceY(double val) throws JSONException{
        mDistY.put(val);
    }

    public void AddDistanceZ(double val) throws JSONException{
        mDistZ.put(val);
    }

    public void AddRotationX(double val) throws JSONException{
        mRotX.put(val);
    }

    public void AddRotationY(double val) throws JSONException{
        mRotY.put(val);
    }

    public void AddRotationZ(double val) throws JSONException{
        mRotZ.put(val);
    }

    public void SetStartTime(Date date){
        mStartTime = C_DATE_FORMAT.format(date);
    }

    public void SetEndTime(Date endDate) throws ParseException{
        Date start = C_DATE_FORMAT.parse(mStartTime);
        if(endDate.after(start)){
            mTimeLength = C_DATE_FORMAT.format(new Date(endDate.getTime() - start.getTime()));
        }
        else{
            mTimeLength = C_DATE_FORMAT.format(new Date(0));
            mValid = false;
        }
    }

    public void PlotAll(LineChart chart) throws JSONException{
        ArrayList<ILineDataSet> lines = new ArrayList<> ();
        ArrayList<Entry> dX = GetDataset(mDistX), dY = GetDataset(mDistY), dZ = GetDataset(mDistZ),
                        rX = GetDataset(mRotX), rY = GetDataset(mRotY), rZ = GetDataset(mRotZ);
        lines = MultipleLines(lines, dX, "X Distance", C_X);
        lines = MultipleLines(lines, dY, "Y Distance", C_Y);
        lines = MultipleLines(lines, dZ, "Z Distance", C_Z);
        lines = MultipleLines(lines, rX, "X Angle", C_RX);
        lines = MultipleLines(lines, rY, "Y Angle", C_RY);
        lines = MultipleLines(lines, rZ, "Z Angle", C_RZ);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setWordWrapEnabled(true);

        chart.setVisibility(View.VISIBLE);
        chart.invalidate();
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        XAxis x = chart.getXAxis();
        x.setDrawAxisLine(true);
        x.setDrawLabels(true);
        x.setTextColor(Color.WHITE);
        x.setAxisMaximum(dX.size());

        YAxis yDist = chart.getAxisLeft();
        YAxis yRot = chart.getAxisRight();
        yDist.setAxisMinimum(-180.0f);
        yDist.setAxisMaximum(180.0f);

        chart.setData(new LineData(lines));
    }

    public ArrayList<ILineDataSet> MultipleLines(ArrayList<ILineDataSet> dataset, ArrayList<Entry> line, String name, int color){
        LineDataSet newLineData = new LineDataSet(line, name);
        newLineData.setColor(color);
        newLineData.setCircleColor(color);
        newLineData.setCircleColorHole(color);
        dataset.add(newLineData);
        return dataset;
    }

    public ArrayList<Entry> GetDataset(JSONArray raw_data) throws JSONException{
        ArrayList<Entry> dataset = new ArrayList<>();
        for(int i = 0; i < raw_data.length(); i++){
            dataset.add(new Entry(i,(float)raw_data.getDouble(i)));
        }
        return dataset;
    }

    public JSONArray GetmRotX(){ return this.mRotX; }
    public double[] GetmRotXArray() throws JSONException{
        double[] ret = new double[this.dataLen];
        for (int i = 0; i < this.dataLen; i++) ret[i] = mRotX.getDouble(i);
        return ret;
    }

    public JSONArray GetmRotY(){ return this.mRotY; }
    public double[] GetmRotYArray() throws JSONException{
        double[] ret = new double[this.dataLen];
        for (int i = 0; i < this.dataLen; i++) ret[i] = mRotY.getDouble(i);
        return ret;
    }

    public JSONArray GetmRotZ() { return this.mRotZ; }
    public double[] GetmRotZArray() throws JSONException {
        double[] ret = new double[this.dataLen];
        for(int i = 0; i < this.dataLen; i++) ret[i] = mRotZ.getDouble(i);
        return ret;
    }

    public int countReps(AXIS_OF_ROTATION axis, Boolean startLow){
        int reps = 0;
        double[] data;

        // Extract the data, handling a potential JSON error by printing the stack trace.
        // ATM, I do not know how to gracefully handle such an error.
        // We may need to construct an error statement on the app
        // and/or figure out how to recover
        try {
            switch (axis) {
                case AXIS_X:
                    data = this.GetmRotXArray();
                    break;
                case AXIS_Y:
                    data = this.GetmRotYArray();
                    break;
                case AXIS_Z:
                    data = this.GetmRotZArray();
                    break;
                default: // This is not possible, but initialize empty array to catch weirdness
                    data = new double[0];
                    break;
            }
        } catch (JSONException ex){
            System.out.println("Could not read Java data.");
            ex.printStackTrace();
            return 0;
        }

        // Flag for direction
        boolean upstroke = startLow;

        // For each point of data
        for (double point : data){
            // If user was moving up previously, but the acceleration is now in a different direction...
            if (upstroke && (point < 0)){
                // Signal new direction
                upstroke = false;

                // If we started high, this change of direction marks a repetition.
                if(!startLow)
                    reps += 1;
            }
            // Same idea as last block
            else if(!upstroke && (point > 0)) {
                upstroke = true;
                if(startLow)
                    reps += 1;
            }
            // else the user is moving in the same direction, continue to the next point
        }

        // Return the count
        return reps;
    }
}