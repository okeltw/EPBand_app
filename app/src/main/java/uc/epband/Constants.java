package uc.epband;

import android.graphics.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public interface Constants {
    //TIME FORMATS
    DateFormat C_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    DateFormat C_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSSS");

    //HEART RATE LEVELS
    float HR_ANAEROBIC = 0.80f, HR_AEROBIC = 0.70f, HR_REST = 0.60f;

    //JSON TAG STRINGS
    String  /* GENERAL */
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

        //COLORS
        String
                C_RED_HEX = "#FF0000",
                C_GREEN_HEX = "#008000",
                C_BLUE_HEX = "#0000FF",
                C_MAROON_HEX = "#800000",
                C_LIME_HEX = "#00FF00",
                C_TEAL_HEX = "#008080";

        int
                C_X = Color.parseColor(C_RED_HEX),
                C_Y = Color.parseColor(C_GREEN_HEX),
                C_Z = Color.parseColor(C_BLUE_HEX),
                C_RX = Color.parseColor(C_MAROON_HEX),
                C_RY = Color.parseColor(C_LIME_HEX),
                C_RZ = Color.parseColor(C_TEAL_HEX);
}