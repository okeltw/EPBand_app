package uc.epband;

import android.graphics.Color;
import android.util.Size;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Exercise implements Constants{
    private Boolean mValid, mAnalyzed;
    public String mExercise, mStartTime, mTimeLength, mSampleStep;
    private Double mGoalROM, mAverageROM;
    private int mReps;
    private JSONArray mRotX, mRotY, mRotZ, mDistX, mDistY, mDistZ;
    private int dataLen;


    public Exercise(){
        mValid = false;
        mAnalyzed = false;
        mExercise = "";
        mStartTime = "";
        mTimeLength = "";
        mSampleStep = "";
        mRotX = new JSONArray();
        mRotY = new JSONArray();
        mRotZ = new JSONArray();
        mDistX = new JSONArray();
        mDistY = new JSONArray();
        mDistZ = new JSONArray();
        mReps = 0;
        mGoalROM = 0.8;
        mAverageROM = 0.0;

        SetStartTime(new Date());
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
        ImportData(jObject);
        System.out.println("Used JSON Object to get ImportData");
        try {
            ImportExtra(jObject);
            System.out.println("Used JSON Object to get ImportExtra");
        }catch(JSONException ex){
            System.out.println("Couldn't get ImportExtra");
        }
        return mValid;
    }

    private void ImportData(JSONObject jObject) throws JSONException{
        mRotX = jObject.getJSONArray(ROT_X);
        mRotY = jObject.getJSONArray(ROT_Y);
        mRotZ = jObject.getJSONArray(ROT_Z);
        mDistX = jObject.getJSONArray(DIST_X);
        mDistY = jObject.getJSONArray(DIST_Y);
        mDistZ = jObject.getJSONArray(DIST_Z);
        dataLen = mRotX.length();
    }

    private void ImportExtra(JSONObject jObject) throws JSONException {
        mAnalyzed = jObject.getBoolean(ANALYZED);
        mExercise = jObject.getString(EXERCISE);
        mStartTime = jObject.getString(START_TIME);
        mTimeLength = jObject.getString(TIME_LENGTH);
        mSampleStep = jObject.getString(SAMPLE_STEP);
        mGoalROM = jObject.getDouble(GOAL_ROM);
        mAverageROM = jObject.getDouble(AVG_ROM);
        mReps = jObject.getInt(REPS);
        mValid = jObject.getBoolean(VALID);
        mAnalyzed = jObject.getBoolean(ANALYZED);
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

        lines = MultipleLines(lines, dX, "X Distance", C_X, YAxis.AxisDependency.LEFT);
        lines = MultipleLines(lines, dY, "Y Distance", C_Y, YAxis.AxisDependency.LEFT);
        lines = MultipleLines(lines, dZ, "Z Distance", C_Z, YAxis.AxisDependency.LEFT);
        lines = MultipleLines(lines, rX, "X Angle", C_RX, YAxis.AxisDependency.RIGHT);
        lines = MultipleLines(lines, rY, "Y Angle", C_RY, YAxis.AxisDependency.RIGHT);
        lines = MultipleLines(lines, rZ, "Z Angle", C_RZ, YAxis.AxisDependency.RIGHT);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setWordWrapEnabled(true);

        chart.setVisibility(View.VISIBLE);
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
        yDist.removeAllLimitLines();
        yDist.setDrawLimitLinesBehindData(true);
        yDist.resetAxisMaximum();
        yDist.resetAxisMinimum();
        yDist.setDrawZeroLine(true);
        yDist.setDrawLabels(true);

        YAxis yRot = chart.getAxisRight();
        yRot.removeAllLimitLines();
        yRot.setDrawLimitLinesBehindData(true);
        yRot.setAxisMinimum(-180.0f);
        yRot.setAxisMaximum(180.0f);
        yRot.setDrawZeroLine(true);
        yRot.setDrawLabels(true);
        yRot.setLabelCount(9, true);

        /*
        LimitLine[] lLine = getLimitLine(90.0f,0.0f,80.0f,"Shoulder Raise");
        yRot.addLimitLine(lLine[0]);
        yRot.addLimitLine(lLine[1]);
        yRot.addLimitLine(lLine[2]);
        */
        System.out.println("Chart setData()");
        chart.setData(new LineData(lines));
        chart.invalidate();
        System.out.println("End plotAll()");

    }

    public LimitLine[] getLimitLine(float goal, float minimum, float tolerancePercent, String label){
        if(tolerancePercent > 1.0) tolerancePercent = tolerancePercent/100.0f;
        LimitLine[] returnLines = new LimitLine[3];
        returnLines[0] = new LimitLine(goal*tolerancePercent);
        returnLines[0].setLineColor(Color.RED);
        returnLines[0].enableDashedLine(12.0f, 12.0f,1.0f);
        //returnLines[0].setLineWidth(1f);

        returnLines[1] = new LimitLine(goal, label);
        returnLines[1].setLineColor(Color.BLACK);
        returnLines[1].setTextColor(Color.WHITE);
        returnLines[1].setLineWidth(1f);
        returnLines[1].setTextSize(14f);

        returnLines[2] = new LimitLine(goal*(2f-tolerancePercent));
        returnLines[2].setLineColor(Color.RED);
        returnLines[2].enableDashedLine(12.0f, 12.0f,1.0f);
        //returnLines[2].setLineWidth(1f);

        return returnLines;
    }

    public ArrayList<ILineDataSet> MultipleLines(ArrayList<ILineDataSet> dataset, ArrayList<Entry> line, String name, int color, YAxis.AxisDependency axis){
        LineDataSet newLineData = new LineDataSet(line, name);
        newLineData.setColor(color);
        newLineData.setCircleColor(color);
        newLineData.setCircleColorHole(color);
        newLineData.setAxisDependency(axis);
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