package uc.epband;

import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HeartRate implements Constants{
    private Boolean mValid = false, mAnalyzed = false;
    private double mUnread = 0, mRest = 0, mAerobic = 0, mAnaerobic = 0;
    private String mSampleStep = "";
    private JSONArray mRawBPM = new JSONArray();

    public HeartRate(){

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

    public void SetSampleRate(Date sample_time){
        mSampleStep = C_TIME_FORMAT.format(sample_time);
    }

    public Boolean UseJSONObject(JSONObject jObject) throws JSONException{
        mValid = false;
        mRawBPM = jObject.getJSONArray(RAW_BPM);
        mUnread = jObject.getDouble(UNREAD);
        mRest = jObject.getDouble(REST);
        mAerobic = jObject.getDouble(AEROBIC);
        mAnaerobic = jObject.getDouble(ANAEROBIC);
        mValid = jObject.getBoolean(VALID);
        mAnalyzed = jObject.getBoolean(ANALYZED);
        mSampleStep = jObject.getString(SAMPLE_STEP);
        return mValid;
    }

    public JSONObject GetJSONObject() throws JSONException{
        JSONObject jObject = new JSONObject();
        jObject.put(VALID, mValid);
        jObject.put(ANALYZED, mAnalyzed);
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
        mRawBPM.put(BPM);
        mAnalyzed = false;
    }

    public void AddData(int[] BPM){
        for(int i = 0; i < BPM.length; i++){
            mRawBPM.put(BPM[i]);
        }
        mAnalyzed = false;
    }

    public void AddData(JSONArray BPM) throws JSONException{
        for(int i = 0; i < BPM.length(); i++){
            mRawBPM.put(BPM.getInt(i));
        }
    }

    public void CalculateSummary(int MHR) throws JSONException{
        mUnread = 0;
        mRest = 0;
        mAerobic = 0;
        mUnread = 0;

        double value;
        for(int i = 0; i < mRawBPM.length(); i++){
            value = mRawBPM.getInt(i);
            if( value < HR_REST*MHR){
                mUnread++;
            }
            else if( value < HR_AEROBIC*MHR){
                mRest++;
            }
            else if( value < HR_ANAEROBIC*MHR){
                mAerobic++;
            }
            else{
                mAnaerobic++;
            }
        }
        mValid = true;
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
        LineDataSet dataSet = new LineDataSet(entries, "Line Graph");

        // DATA POINT STYLE SETTINGS
        dataSet.setColor(Color.WHITE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleColorHole(Color.BLACK);
        dataSet.setCircleHoleRadius(1f);
        dataSet.setCircleRadius(2f);

        // CHART STYLE SETTINGS
        chart.setVisibility(View.VISIBLE);
        chart.getXAxis().setEnabled(true);
        chart.getAxisLeft().setAxisMaximum(220f);
        chart.getAxisLeft().setEnabled(false);

        // LEGEND SETTINGS
        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        // REFRESH
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void PlotSummary(PieChart chart) {
        // VISIBILITY ON
        chart.setVisibility(View.VISIBLE);

        // CREATE DATA
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float)mUnread, "Unread (0% - 60% MHR)"));
        entries.add(new PieEntry((float)mRest, "Resting (60% - 70% MHR)"));
        entries.add(new PieEntry((float)mAerobic, "Aerobic (70% - 80% MHR)"));
        entries.add(new PieEntry((float)mAnaerobic, "Anaerobic (80% - 100% MHR)"));
        PieDataSet set = new PieDataSet(entries, "Heart Rate Levels as Percent of Time");

        // CHART STYLE SETTINGS
        set.setColors(Color.rgb(160, 160, 160), Color.rgb(0, 128, 255), Color.rgb(255, 255, 0), Color.rgb(255, 0, 0));
        PieData data = new PieData(set);
        data.setValueTextSize(16.0f);
        data.setValueTextColor(Color.BLACK);
        chart.setEntryLabelTextSize(24.0f);
        chart.setUsePercentValues(true);
        chart.setHoleRadius(0f);
        chart.setTransparentCircleAlpha(0);

        // Don't use labels on chart
        chart.setDrawEntryLabels(false);

        // LEGEND SETTINGS
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.rgb(255, 255, 255));
        legend.setTextSize(16.0f);
        //legend.setTypeface(Typeface TF);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);

        // SET DATA AND UPDATE CHART
        chart.setDescription(null);
        chart.setData(data);
        chart.invalidate(); // refresh
    }
}