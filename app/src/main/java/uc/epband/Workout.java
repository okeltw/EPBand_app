package uc.epband;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class Workout implements Constants{
    private Context mContext;
    private String mTimeStamp;
    private String mFilename;
    private Motion mMotion;
    private HeartRate mHeartRate;

    public Workout(Context context){
        mContext = context;
        Date date = new Date();
        mTimeStamp = createTimeStamp(date);
        mFilename = C_FILE_FORMAT.format(date);
        mMotion = new Motion();
        mHeartRate = new HeartRate();
    }

    public Workout(Context context, String filename) throws IOException, JSONException {
        mContext = context;
        mFilename = filename;
        String jString = readFile(filename);
        JSONObject jObject = new JSONObject(jString);
        mMotion = new Motion(jObject.getJSONObject(MOTION));
        mTimeStamp = jObject.getString(TIME_STAMP);
        mHeartRate = new HeartRate(jObject.getJSONObject(HEART_RATE));
    }

    public void setExampleFileName(String filename){
        mFilename = EXAMPLE + "_" + filename;
    }

    private String createTimeStamp(Date date){
        return C_DATE_FORMAT.format(date);
    }

    public Motion getMotion(){
        return mMotion;
    }

    public HeartRate getHeartRate(){
        return mHeartRate;
    }

    public void setMotion(Motion motion){
        mMotion = motion;
    }

    public void setHeartRate(HeartRate heartrate){
        mHeartRate = heartrate;
    }

    public String readFile(String filename) throws IOException {
        FileInputStream fileStream = mContext.openFileInput(filename);
        int size = fileStream.available();
        byte[] content = new byte[size];
        fileStream.read(content);
        fileStream.close();
        return new String(content);
    }

    public void writeFile() throws IOException, JSONException{
        String filename = NewFilename(mFilename);
        JSONObject jObject = getContentJSON();
        writeFile(filename, jObject.toString());
    }

    private JSONObject getContentJSON() throws JSONException{
        JSONObject jObject = new JSONObject();
        jObject.put(TIME_STAMP,mTimeStamp);
        jObject.put(MOTION,mMotion.GetJSONObject());
        jObject.put(HEART_RATE,mHeartRate.GetJSONObject());
        return jObject;
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

    private String NewFilename(String filename) {
        //Returns the current date and time used for the file
        String[] List = mContext.fileList();
        int count = 0;
        for (String f : List) {
            if (f.startsWith(filename)) {
                count++;
            }
        }
        if(count > 0){
            filename = filename + "(" + count + ")";
        }
        return filename;
    }

    private void AppendFile(String filename, String message) throws IOException {
        FileOutputStream f = mContext.openFileOutput(filename, Context.MODE_APPEND);
        f.write(message.getBytes());
        f.close();
    }

}
