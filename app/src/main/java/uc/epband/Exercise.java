package uc.epband;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Size;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    private boolean mValid, mAnalyzed;
    public String mExercise, mStartTime, mTimeLength, mSampleStep;
    private double mGoalROM, mAverageROM;
    private int mReps;
    private JSONArray mRotX, mRotY, mRotZ, mDistX, mDistY, mDistZ;
    private int dataLen;

    double[] mXAngle, mYAngle, mZAngle;


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
        mGoalROM = 0.0;
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

    public void AddDataJSONArray(JSONArray X, JSONArray Y, JSONArray Z, JSONArray RX, JSONArray RY, JSONArray RZ) throws JSONException{
        for(int i = 0; i < X.length(); i++){
            AddData(X.getDouble(i), Y.getDouble(i), Z.getDouble(i), RX.getDouble(i), RY.getDouble(i), RZ.getDouble(i));
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

    public void PlotAngles(LineChart chart) throws JSONException{
        ArrayList<ILineDataSet> lines = new ArrayList<> ();
        ArrayList<Entry> X_angle = new ArrayList<>();
        ArrayList<Entry> Y_angle = new ArrayList<>();
        ArrayList<Entry> Z_angle = new ArrayList<>();

        /*
        System.out.println("Start: Get all angles");
        mXAngle = getAllAngles(mDistY, mDistX, mDistZ);
        mYAngle = getAllAngles(mDistX, mDistY, mDistZ);
        mZAngle = getAllAngles(mDistZ, mDistX, mDistY);
        System.out.println("Stop: Got all angles");
        */

        for(int i = 0; i < mDistX.length(); i++){

            double x = mDistX.getDouble(i);
            double y = mDistY.getDouble(i);
            double z = mDistZ.getDouble(i);
            double XAngle = getAngle(y, x, z);
            double YAngle = getAngle(x, y, z);
            double ZAngle = getAngle(z, x, y);
            X_angle.add(new Entry(i, (float) XAngle));
            Y_angle.add(new Entry(i, (float) YAngle));
            Z_angle.add(new Entry(i, (float) ZAngle));

            /*
            X_angle.add(new Entry(i, (float) mXAngle[i]));
            Y_angle.add(new Entry(i, (float) mYAngle[i]));
            Z_angle.add(new Entry(i, (float) mZAngle[i]));
            */
        }

        if(!X_angle.isEmpty()) lines = MultipleLines(lines, X_angle, "X Angle", C_RX, YAxis.AxisDependency.LEFT);
        if(!Y_angle.isEmpty()) lines = MultipleLines(lines, Y_angle, "Y Angle", C_RY, YAxis.AxisDependency.LEFT);
        if(!Z_angle.isEmpty()) lines = MultipleLines(lines, Z_angle, "Z Angle", C_RZ, YAxis.AxisDependency.LEFT);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setWordWrapEnabled(true);

        chart.setVisibility(View.VISIBLE);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
        chart.setDragEnabled(true);

        XAxis x = chart.getXAxis();
        x.setEnabled(true);
        x.setDrawAxisLine(true);
        x.setDrawLabels(true);
        x.setTextColor(Color.WHITE);
        x.setAxisMaximum(X_angle.size());
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new TimeFormatter(10));

        chart.getAxisRight().setEnabled(false);
        YAxis yRot = chart.getAxisLeft();
        yRot.setEnabled(true);
        yRot.removeAllLimitLines();
        yRot.setDrawLimitLinesBehindData(true);
        yRot.setAxisMinimum(-180.0f);
        yRot.setAxisMaximum(180.0f);
        yRot.setValueFormatter(new AngleFormatter());
        yRot.setLabelCount(9, true);
        yRot.setDrawLabels(true);
        yRot.setTextColor(Color.WHITE);
        yRot.setGranularity(1);

        System.out.println("Chart setData()");

        if(!lines.isEmpty()) {
            chart.setData(new LineData(lines));
            chart.setDragEnabled(true);
        }
        else chart.clear();
        chart.invalidate();
        System.out.println("End plotAngles()");
        PrintAnalysis(chart);
    }

    private double[] getAllAngles(JSONArray arg1, JSONArray arg2, JSONArray arg3) throws JSONException{
        int size = arg1.length();
        if(size > 0) {
            double[] angles = new double[size];
            for (int i = 0; i < size; i++) {
                angles[i] = getAngle(arg1.getDouble(i), arg2.getDouble(i), arg3.getDouble(i));
                System.out.println("Angles:\n" + angles.toString());
                return angles;
            }
        }
        return null;
    }

    private double getAngle(double arg1, double arg2, double arg3){
        return 180 * Math.atan2(arg1, Math.sqrt(arg2*arg2+arg3*arg3) ) / Math.PI;
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

    public void PlotAll(Context context, LineChart chart) throws JSONException{
        ArrayList<ILineDataSet> lines = new ArrayList<> ();
        ArrayList<Entry> dX = GetDataset(mDistX), dY = GetDataset(mDistY), dZ = GetDataset(mDistZ),
                        rX = GetDataset(mRotX), rY = GetDataset(mRotY), rZ = GetDataset(mRotZ);

        mGoalROM = (double) context.getSharedPreferences(DefaultPreferenceFile, context.MODE_PRIVATE).getInt("ROM",80)/100;

        final SharedPreferences Settings = context.getSharedPreferences("SETTINGS", context.MODE_PRIVATE);
        boolean[] mLineToggles = new boolean[6];
        mLineToggles[0] = Settings.getBoolean("X", true);
        mLineToggles[1] = Settings.getBoolean("Y", true);
        mLineToggles[2] = Settings.getBoolean("Z", true);
        mLineToggles[3] = Settings.getBoolean("RX", true);
        mLineToggles[4] = Settings.getBoolean("RY", true);
        mLineToggles[5] = Settings.getBoolean("RZ", true);

        if((!dX.isEmpty()) && mLineToggles[0]) lines = MultipleLines(lines, dX, "X Linear", C_X, YAxis.AxisDependency.LEFT);
        if((!dY.isEmpty()) && mLineToggles[1]) lines = MultipleLines(lines, dY, "Y Linear", C_Y, YAxis.AxisDependency.LEFT);
        if((!dZ.isEmpty()) && mLineToggles[2]) lines = MultipleLines(lines, dZ, "Z Linear", C_Z, YAxis.AxisDependency.LEFT);
        if((!rX.isEmpty()) && mLineToggles[3]) lines = MultipleLines(lines, rX, "X Gyro", C_RX, YAxis.AxisDependency.RIGHT);
        if((!rY.isEmpty()) && mLineToggles[4]) lines = MultipleLines(lines, rY, "Y Gyro", C_RY, YAxis.AxisDependency.RIGHT);
        if((!rZ.isEmpty()) && mLineToggles[5]) lines = MultipleLines(lines, rZ, "Z Gyro", C_RZ, YAxis.AxisDependency.RIGHT);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setWordWrapEnabled(true);

        chart.setVisibility(View.VISIBLE);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
        chart.setDragEnabled(true);

        XAxis x = chart.getXAxis();
        x.setEnabled(true);
        x.setDrawAxisLine(true);
        x.setDrawLabels(true);
        x.setTextColor(Color.WHITE);
        x.setAxisMaximum(dX.size());
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new TimeFormatter(100));


        YAxis yDist = chart.getAxisLeft();
        yDist.removeAllLimitLines();
        yDist.setDrawLimitLinesBehindData(true);
        yDist.resetAxisMaximum();
        yDist.resetAxisMinimum();
        yDist.setValueFormatter(new DistanceFormatter());
        yDist.setLabelCount(9, true);
        yDist.setDrawLabels(true);
        yDist.setTextColor(Color.WHITE);
        yDist.setGranularity(1);

        YAxis yRot = chart.getAxisRight();
        yRot.setEnabled(true);
        yRot.removeAllLimitLines();
        yRot.setDrawLimitLinesBehindData(true);
        yRot.setAxisMinimum(-180.0f);
        yRot.setAxisMaximum(180.0f);
        yRot.setValueFormatter(new DegreesFormater());
        yRot.setLabelCount(9, true);
        yRot.setDrawLabels(true);
        yRot.setTextColor(Color.WHITE);
        yRot.setGranularity(1);

        System.out.println("Chart setData()");

        if(!lines.isEmpty()) {
            chart.setData(new LineData(lines));
            chart.setDragEnabled(true);
            //chart.setVisibleXRangeMaximum(200);
            //if(lines.size() > 200) chart.moveViewToX(lines.size() - 200);
        }
        else chart.clear();
        chart.invalidate();
        System.out.println("End plotAll()");
    }

    public void DisplaySummary(Context context, ListView summaryList){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        adapter.add("Name: " + mExercise);
        adapter.add("Start Time: " + mStartTime);
        adapter.add("Total Time: " + mTimeLength);
        adapter.add("Sampled at: " + mSampleStep);

        summaryList.setAdapter(adapter);
        summaryList.setVisibility(View.VISIBLE);
    }

    public LimitLine[] getLimitLine(LineChart chart, float goal, float tolerancePercent, String label, int color){
        if(tolerancePercent > 1.0) tolerancePercent = tolerancePercent/100.0f;
        LimitLine[] returnLines = new LimitLine[3];
        returnLines[0] = new LimitLine(goal*tolerancePercent);
        returnLines[0].setLineColor(color);
        returnLines[0].enableDashedLine(12.0f, 12.0f,1.0f);
        //returnLines[0].setLineWidth(1f);

        returnLines[1] = new LimitLine(goal, label);
        returnLines[1].setLineColor(Color.BLACK);
        returnLines[1].setTextColor(Color.WHITE);
        returnLines[1].setLineWidth(1f);
        returnLines[1].setTextSize(14f);

        returnLines[2] = new LimitLine(goal*(2f-tolerancePercent));
        returnLines[2].setLineColor(color);
        returnLines[2].enableDashedLine(12.0f, 12.0f, 1.0f);
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

    public void PrintAnalysis(LineChart chart) throws JSONException{
        int size = mDistX.length();
        System.out.println("Data size: " + size);
        if(size > 0) {
            mXAngle = new double[size];
            mYAngle = new double[size];
            mZAngle = new double[size];
            for (int i = 0; i < size; i++) {
                double x = mDistX.getDouble(i);
                double y = mDistY.getDouble(i);
                double z = mDistZ.getDouble(i);
                mXAngle[i] = getAngle(y, x, z);
                mYAngle[i] = getAngle(x, y, z);
                mZAngle[i] = getAngle(z, x, y);
            }

            LimitLine[] limits;
            switch (mExercise) {
                case BICEP_CURL:
                    limits = getLimitLine(chart, 70.0f, (float)mGoalROM, "End Position", Color.RED);
                    chart.getAxisLeft().addLimitLine(limits[0]);
                    chart.getAxisLeft().addLimitLine(limits[1]);
                    chart.getAxisLeft().addLimitLine(limits[2]);
                    SingleAngleAnalysis(mXAngle, 70.0, -70.0, false);
                    break;
                case SIDE_RAISE:
                case FRONT_RAISE:
                    limits = getLimitLine(chart, 90.0f, (float)mGoalROM, "End Position", Color.RED);
                    chart.getAxisLeft().addLimitLine(limits[0]);
                    chart.getAxisLeft().addLimitLine(limits[1]);
                    chart.getAxisLeft().addLimitLine(limits[2]);
                    SingleAngleAnalysis(mXAngle, 90.0, 0.0, false);
                    break;
                case REVERSE_CURL:
                    limits = getLimitLine(chart, 70.0f, (float)mGoalROM, "End Position", Color.RED);
                    chart.getAxisLeft().addLimitLine(limits[0]);
                    chart.getAxisLeft().addLimitLine(limits[1]);
                    chart.getAxisLeft().addLimitLine(limits[2]);
                    SingleAngleAnalysis(mXAngle, 70.0, -70.0, false);
                    break;
                case HAMMER_CURL:
                    limits = getLimitLine(chart, 70.0f, (float)mGoalROM, "End Position", Color.RED);
                    chart.getAxisLeft().addLimitLine(limits[0]);
                    chart.getAxisLeft().addLimitLine(limits[1]);
                    chart.getAxisLeft().addLimitLine(limits[2]);
                    SingleAngleAnalysis(mXAngle, 70.0, -70.0, false);
                    break;
                default:
                    System.out.println("Not a valid exercise");
                    break;
            }
        }
        else System.out.println("No data to analyze");
    }

    private void SingleAngleAnalysis(double[] data, double upGoal, double downGoal, boolean startUpstroke){
        double fullRange = upGoal - downGoal;
        double median = (upGoal + downGoal)/2;
        double TopPosition = median + mGoalROM * fullRange/2;
        double BottomPosition = median - mGoalROM * fullRange/2;
        boolean upstroke = startUpstroke;

        double FinalPosition = data[0];
        ArrayList<Double> RepMaxs = new ArrayList();
        mReps = 0;

        for(int i = 0; i < data.length; i++){
            if(upstroke && data[i] >= TopPosition){
                if(startUpstroke){
                    mReps++;
                }
                else{
                    RepMaxs.add(FinalPosition);
                    FinalPosition = data[i];
                }
                upstroke = false;
            }else if(!upstroke && data[i] <= BottomPosition){
                if(!startUpstroke){
                    mReps++;
                }
                else{
                    RepMaxs.add(FinalPosition);
                    FinalPosition = data[i];
                }
                upstroke = true;
            }

            if(upstroke && data[i] <= FinalPosition) FinalPosition = data[i];
            else if(!upstroke && data[i] >= FinalPosition) FinalPosition = data[i];
        }

        mAverageROM = 0.0;
        if(RepMaxs.size() > 0) {
            for(int i = 0; i < RepMaxs.size(); i++) mAverageROM += RepMaxs.get(i);

            mAverageROM /= RepMaxs.size();
            if (startUpstroke) {
                mAverageROM /= upGoal;
            } else {
                mAverageROM /= downGoal;
            }
            ;
        }

        System.out.println("Fullrange = " + fullRange);
        System.out.println("Median = " + median);
        System.out.println("GOAL ROM = " + mGoalROM);
        System.out.println("Top position = " + TopPosition);
        System.out.println("Bottom position = " + BottomPosition);
        System.out.println("Reps = " + mReps);
        System.out.println("Average ROM = " + mAverageROM);
        System.out.println("Each rep = " + RepMaxs.toArray());

    }
}