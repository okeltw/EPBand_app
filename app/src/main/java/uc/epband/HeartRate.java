package uc.epband;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;

public class HeartRate {
    private Boolean mValid = false, mAnalyzed = false;
    private double mUnread = 0, mRest = 0, mAerobic = 0, mAnaerobic = 0;
    private String mSampleStep = "";
    private JSONArray mRawBPM = new JSONArray();
    private static float anaerobic = 0.8f, aerobic = 0.7f, rest = 0.6f;

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

}
