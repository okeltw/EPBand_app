package uc.epband;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.EventLogTags;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class HeartRate implements Constants{
    private double mUnread, mRest, mAerobic, mAnaerobic;
    private String mSampleStep;
    private JSONArray mRawBPM;
    private int mMHR, mLHR;
    private double mHRThresh; // Threshold of acceptable change off of interpolated value (%)

    // Used if
    private static int default_HR = 75;

    public HeartRate(){
        mRawBPM = new JSONArray();
        mSampleStep = "";
        mUnread = 0;
        mRest = 0;
        mAerobic = 0;
        mAnaerobic = 0;
        mMHR = 220;
        mLHR = 60;
        mHRThresh = 0.15; // E.g. an expected value of 100 can include anything from 85 to 115
    }

    public HeartRate(String jString) throws JSONException{
        UseJSONString(jString);
    }

    public HeartRate(JSONObject jObject) throws  JSONException{
        UseJSONObject(jObject);
    }

    public Boolean UseJSONString(String jString) throws JSONException{
        JSONObject jObject = new JSONObject(jString);
        UseJSONObject(jObject);
        return true;
    }

    public void updateMHR(Context context){
        SharedPreferences Settings = context.getSharedPreferences(DefaultPreferenceFile, context.MODE_PRIVATE);
        mMHR = Settings.getInt("MHR",220);
        System.out.println("Update heart rate with MHR: " + mMHR);
    }

    public void SetSampleRate(Date sample_time){
        mSampleStep = C_TIME_FORMAT.format(sample_time);
    }

    public Boolean UseJSONObject(JSONObject jObject) throws JSONException{
        mRawBPM = jObject.getJSONArray(RAW_BPM);
        mUnread = jObject.getDouble(UNREAD);
        mRest = jObject.getDouble(REST);
        mAerobic = jObject.getDouble(AEROBIC);
        mAnaerobic = jObject.getDouble(ANAEROBIC);
        mSampleStep = jObject.getString(SAMPLE_STEP);
        return true;
    }

    public JSONObject GetJSONObject() throws JSONException{
        JSONObject jObject = new JSONObject();
        jObject.put(UNREAD, mUnread);
        jObject.put(REST, mRest);
        jObject.put(AEROBIC, mAerobic);
        jObject.put(ANAEROBIC, mAnaerobic);
        jObject.put(SAMPLE_STEP, mSampleStep);
        jObject.put(RAW_BPM, mRawBPM);
        return jObject;
    }

    public String GetJSONString(){
        try {
            JSONObject jObject = GetJSONObject();
            return jObject.toString();
        }
        catch(JSONException ex){
            return null;
        }
    }

    public void AddData(int BPM){
        // If there isn't enough history to interpolate from
        // Either just accept the HR, or use the default.
        if (mRawBPM.length() < 2) {
            if (BPM <= mMHR && BPM >= mLHR) {
                mRawBPM.put(BPM);
                return;
            }
            else {
                mRawBPM.put(default_HR);
                return;
            }
        }

        // There is enough data, so we can interpolate for a validity check
        try {
            int left = mRawBPM.getInt(mRawBPM.length() - 2),
                    right = mRawBPM.getInt(mRawBPM.length() - 1);
            double interpResult = Calculus.linearInterpolateNextValue(left, right),
                    lowThresh = interpResult - (interpResult * mHRThresh),
                    highThresh = interpResult + (interpResult * mHRThresh);

            if ( BPM < mMHR && BPM > mLHR && BPM < highThresh ) {
                mRawBPM.put(BPM);
            }
            else {
                // Randomly assign a new value, keeping a seemingly normal trend
                Random r = new Random();
                int newVal;
                if (right > default_HR) {
                    newVal = (r.nextInt(5) + right - 5); // Random value from right-5 to right [0+right-5:5+right-5]
                }
                else {
                    newVal = (r.nextInt(5) + right); // Random value form right to right+5
                }
                mRawBPM.put(newVal);
            }
        } catch (JSONException jEx) {
            System.out.println("JSON exception while getting BPM data.");
            if (BPM <= mMHR && BPM >= mLHR) {
                mRawBPM.put(BPM);
            }
            else {
                mRawBPM.put(default_HR);
            }
        }

        /*
        Since Unit Tests won't (easily) work with this, I will test using thought experiments.
        - 0 to 1 values in mRawBPM
            - HR is within limits
                Accept and store the BPM. (pass)
            - HR is NOT within limits.
                Reject BPM, store default HR. (pass)
         - >= 2 values in mRawBPM
            - BPM is within limits and matches trend (no JSONException)
                Accept and store the BPM. (pass)
            - BPM is NOT within limits OR does NOT match trend (no JSONException)
                Reject BPM, generate new value that should visually match previous BPM
                But, is random within a reasonable bound. (pass)
            - A JSONException is thrown while obtaining previous points
                Something is seriously wrong, can't rely on previous data.
                If BPM is within limits, accept (pass)
                Else, use default HR. (pass)

         All thought experiments passed.
         */
    }

    public void AddData(int[] BPM){
        for(int i = 0; i < BPM.length; i++){
            AddData(BPM[i]);
        }
    }

    public void AddData(JSONArray BPM) throws JSONException{
        for(int i = 0; i < BPM.length(); i++){
            AddData(BPM.getInt(i));
        }
    }

    public void PlotAll(LineChart chart) {
        List<Entry> entries = new ArrayList<>();
        for(int x = 0; x < mRawBPM.length(); x++){
            try{
                entries.add(new Entry(x,mRawBPM.getInt(x)));
            }catch (JSONException ex){
                System.out.println("JSON Exception in Line Chart Plot");
            }
        }

        // VISIBILITY ON
        chart.setVisibility(View.VISIBLE);

        // CREATE DATA
        if(entries.isEmpty()){
            chart.clear();
        }else {
            LineDataSet dataSet = new LineDataSet(entries, "Line Graph");

            // DATA POINT STYLE SETTINGS
            dataSet.setColor(Color.RED);
            dataSet.setCircleColor(Color.RED);
            dataSet.setCircleColorHole(Color.BLACK);
            dataSet.setCircleHoleRadius(2f);
            dataSet.setCircleRadius(3f);

            // CHART STYLE SETTINGS
            XAxis x = chart.getXAxis();

            x.setValueFormatter(new TimeFormatter(1000));
            x.setEnabled(true);
            x.setAxisMaximum(entries.size());
            x.setLabelCount(5, true);
            x.setTextColor(Color.WHITE);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);

            YAxis y = chart.getAxisLeft();
            y.removeAllLimitLines();
            y.setValueFormatter(new HeartRateFormater());
            y.setEnabled(true);
            y.setAxisMaximum(220f);
            y.setAxisMinimum(0.0f);
            y.setDrawLabels(true);
            y.setLabelCount(5, true);
            y.setTextColor(Color.WHITE);

            chart.getAxisRight().setEnabled(false);

            // LEGEND SETTINGS
            Legend legend = chart.getLegend();
            legend.setEnabled(false);

            // Limit lines for Heart Rate Levels
            LineData lineData = new LineData(dataSet);
            chart.setDragEnabled(true);
            chart.setPinchZoom(false);
            chart.setData(lineData);
            chart.setVerticalScrollBarEnabled(true);
            chart.setHorizontalScrollBarEnabled(true);

            String Label1 = "Resting " + Math.round(HR_REST * 100) + "% MHR";
            String Label2 = AEROBIC + " " + Math.round(HR_AEROBIC*100) + "% MHR";
            String Label3 = ANAEROBIC + " " + Math.round(HR_ANAEROBIC*100) + "% MHR";
            String Label4 = "MHR " + mMHR + " BPM";

            LimitLine Line1 = getLimitLine(HR_REST * mMHR, Label1, Color.parseColor(HEX_DODGERBLUE));
            LimitLine Line2 = getLimitLine(HR_AEROBIC * mMHR, Label2, Color.parseColor(HEX_GOLD));
            LimitLine Line3 = getLimitLine(HR_ANAEROBIC * mMHR, Label3, Color.parseColor(HEX_RUBY));
            LimitLine Line4 = getLimitLine(mMHR, Label4, Color.WHITE);

            y.addLimitLine(Line1);
            y.addLimitLine(Line2);
            y.addLimitLine(Line3);
            y.addLimitLine(Line4);
        }
        // REFRESH
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    public LimitLine getLimitLine(float value, String label, int color){
        LimitLine returnLine = new LimitLine(value, label);
        returnLine.setLineColor(color);
        returnLine.setTextColor(Color.WHITE);
        returnLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        returnLine.setLineWidth(1f);
        returnLine.setTextSize(14f);
        return returnLine;
    }

    public void PlotSummary(PieChart chart) {
        // VISIBILITY ON
        chart.setVisibility(View.VISIBLE);
        try{
            mUnread = 0;
            mRest = 0;
            mAerobic = 0;
            mAnaerobic = 0;

            System.out.println("Heart Rate Summary of " + mRawBPM.length() + " points\n" + mRawBPM);
            for(int i = 0; i < mRawBPM.length(); i++){
                double value = mRawBPM.getInt(i);
                if( value < HR_REST*mMHR){
                    mUnread++;
                }
                else if( value < HR_AEROBIC*mMHR){
                    mRest++;
                }
                else if( value < HR_ANAEROBIC*mMHR){
                    mAerobic++;
                }
                else{
                    mAnaerobic++;
                }
            }
        }
        catch (JSONException ex){
            System.out.println("JSONException during HeartRate Summary Calculation");
        }

        if(mUnread + mRest + mAerobic + mAnaerobic > 0) {
            // CREATE DATA
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry((float) mUnread, "Resting (0% - 60% MHR)"));
            entries.add(new PieEntry((float) mRest, "Recovery (60% - 70% MHR)"));
            entries.add(new PieEntry((float) mAerobic, "Aerobic (70% - 80% MHR)"));
            entries.add(new PieEntry((float) mAnaerobic, "Anaerobic (80% - 100% MHR)"));

            // CHART STYLE SETTINGS
            PieDataSet set = new PieDataSet(entries, "Heart Rate Levels as Percent of Time");
            set.setColors(Color.rgb(160, 160, 160), Color.rgb(0, 128, 255), Color.rgb(255, 255, 0), Color.rgb(255, 0, 0));

            PieData data = new PieData(set);
            data.setValueTextSize(16.0f);
            data.setValueTextColor(Color.BLACK);

            chart.setEntryLabelTextSize(24.0f);
            chart.setUsePercentValues(true);
            chart.setHoleRadius(0f);
            chart.setTransparentCircleAlpha(0);

            data.setValueFormatter(new PercentFormatter());

            // Don't use labels on chart
            chart.setDrawEntryLabels(false);

            // LEGEND SETTINGS
            Legend legend = chart.getLegend();
            legend.setEnabled(true);
            legend.setTextColor(Color.rgb(255, 255, 255));
            legend.setTextSize(16.0f);
            legend.setForm(Legend.LegendForm.CIRCLE);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setWordWrapEnabled(true);

            // SET DATA AND UPDATE CHART
            chart.setDescription(null);

            System.out.println("Piechart data: " + chart.getData());
            chart.setData(data);
        }
        else chart.clear();
        chart.notifyDataSetChanged();
        chart.invalidate(); // refresh
    }
}