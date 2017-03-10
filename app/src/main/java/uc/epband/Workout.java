package uc.epband;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class Workout implements Constants{
    private Context mContext;
    private String mTimeStamp;
    private Motion mMotion;
    private HeartRate mHeartRate;
    private static final String MOTION = "Motion", HEART_RATE = "HeartRate", TIME_STAMP = "TimeStamp";

    public Workout(Context context){
        mContext = context;
        mTimeStamp = createTimeStamp(new Date());
        mMotion = new Motion();
        mHeartRate = new HeartRate();
    }

    public Workout(Context context, String filename) throws IOException, JSONException {
        mContext = context;
        String jString = readFile(filename);
        JSONObject jObject = new JSONObject(jString);
        mMotion = new Motion(jObject.getJSONObject(MOTION));
        mTimeStamp = jObject.getString(TIME_STAMP);
        mHeartRate = new HeartRate(jObject.getJSONObject(HEART_RATE));
    }

    private String createTimeStamp(Date date){
        return C_DATE_FORMAT.format(date);
    }

    public String readFile(String filename) throws IOException {
        try {
            FileInputStream fileStream = mContext.openFileInput(filename);
            int size = fileStream.available();
            byte[] content = new byte[size];
            fileStream.read(content);
            fileStream.close();
            return new String(content);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    private void writeFile() throws IOException{
        writeFile(mTimeStamp, "");
    }

    private void writeFile(String filename, String content) throws IOException{
        try {
            FileOutputStream fileStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            fileStream.write(content.getBytes());
            fileStream.close();
        }
        catch(FileNotFoundException ex){
            System.out.println("File: " + filename + " could not be found.");
        }
    }

}
