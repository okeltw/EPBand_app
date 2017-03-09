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

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

public class HeartRate {
    private Boolean mValid = false, mAnalyzed = false;
    private double mUnread = 0, mRest = 0, mAerobic = 0, mAnaerobic = 0;
    private String mSampleStep = "";
    private JSONArray mRawBPM = new JSONArray();
    private static float anaerobic = 0.8f, aerobic = 0.7f, rest = 0.6f;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");

    //JSON Member names
    private static final String VALID = "Valid", ANALYZED = "Analyzed", UNREAD = "Unread",
            REST = "Rest", AEROBIC = "Aerobic", ANAEROBIC = "Anaerobic", SAMPLE_STEP = "SampleStep",
            RAW_BPM = "RawBPM";

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

    public void SetSampleRate(String rate){
        mSampleStep = rate;
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

        double value = 0;
        for(int i = 0; i < mRawBPM.length(); i++){
           value = mRawBPM.getInt(i);
            if( value < rest*MHR){
                mUnread++;
            }
            else if( value < aerobic*MHR){
                mRest++;
            }
            else if( value < anaerobic*MHR){
                mAerobic++;
            }
            else{
                mAnaerobic++;
            }
        }
        mValid = true;
    }

    public void PlotAll(LineChart chart) {
        List<Entry> entries = new ArrayList<Entry>();
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
        entries.add(new PieEntry((float)mUnread, "Unread"));
        entries.add(new PieEntry((float)mRest, "Resting"));
        entries.add(new PieEntry((float)mAerobic, "Aerobic"));
        entries.add(new PieEntry((float)mAnaerobic, "Anaerobic"));
        PieDataSet set = new PieDataSet(entries, "Heart Rate Over Workout");

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