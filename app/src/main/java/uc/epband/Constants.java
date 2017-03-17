package uc.epband;

import android.content.Context;
import android.graphics.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface Constants {
    //TIME FORMATS
    DateFormat  C_FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"),
                C_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                C_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSSS");

    //HEART RATE LEVELS
    float       HR_ANAEROBIC = 0.80f,
                HR_AEROBIC = 0.70f,
                HR_REST = 0.60f;

    final String
            BICEP_CURL = "Bicep Curl",
            SIDE_RAISE = "Side Raise",
            FRONT_RAISE = "Front Raise",
            SQUAT = "Back Squat",
            POWER_CLEAN = "Power Clean";

    String[] ExerciseList = {BICEP_CURL, SIDE_RAISE, FRONT_RAISE, SQUAT, POWER_CLEAN};

    //JSON TAG STRINGS
    String  /*Workout String*/
            EXAMPLE = "Example",
            MOTION = "Motion",
            HEART_RATE = "HeartRate",
            TIME_STAMP = "TimeStamp",
            /* GENERAL */
            VALID = "Valid",
            ANALYZED = "Analyzed",
            EXERCISE = "Exercise",
            START_TIME = "StartTime",
            TIME_LENGTH = "TimeLength",
            SAMPLE_STEP = "SampleStep",
            /* MOTION */
            EXERCISE_DATA = "ExerciseData",
            GOAL_ROM = "GoalROM",
            AVG_ROM = "AvgROM",
            REPS = "Reps",
            ROT_X = "RotationX",
            ROT_Y = "RotationY",
            ROT_Z = "RotationZ",
            DIST_X = "DistanceX",
            DIST_Y = "DistanceY",
            DIST_Z = "DistanceZ",
            /* HEART RATE */
            UNREAD = "Unread",
            REST = "Rest",
            AEROBIC = "Aerobic",
            ANAEROBIC = "Anaerobic",
            RAW_BPM = "RawBPM";

    String
        HEX_MIDNIGHT = "#1F2939",
        HEX_Gumbo = "#6F9489",
        HEX_Dodger_Blue = "#339FF2",
        HEX_RUBY = "#D40A68",
        HEX_MALACHITE = "#07D05A";


    int     C_X = Color.parseColor("#FF0000"), // Red
            C_Y = Color.parseColor("#008000"), // Blue
            C_Z = Color.parseColor("#0000FF"), // Green
            C_RX = Color.parseColor("#FF00FF"), // Pink
            C_RY = Color.parseColor("#FFFF00"), // Yellow
            C_RZ = Color.parseColor("#00FFFF"); // Cyan

    // Message types sent from the BluetoothBandService Handler
    int     MESSAGE_STATE_CHANGE = 1,
            MESSAGE_READ = 2,
            MESSAGE_WRITE = 3,
            MESSAGE_DEVICE_NAME = 4,
            MESSAGE_TOAST = 5;

    String TOAST = "toast", DEVICE_NAME = "EPBand";//"Andrew's iPhone";

    enum AXIS_OF_ROTATION{
        AXIS_X,
        AXIS_Y,
        AXIS_Z
    }
}


