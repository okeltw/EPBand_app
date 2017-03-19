package uc.epband;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface Constants {
    String  DefaultPreferenceFile = "test";

    //TIME FORMATS
    DateFormat  C_FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"),
                C_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                C_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSSS"),
                C_TIME_SIMPLE_FORMAT = new SimpleDateFormat("HH:mm:ss");

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
        HEX_ORANGERED = "#FF4500",
        HEX_MEDIUMSLATEBLUE = "#7B68EE",
        HEX_GOLD = "#FFD700",
        HEX_DODGERBLUE = "#339FF2",
        HEX_RUBY = "#D40A68",
        HEX_MALACHITE = "#07D05A";


    int     C_X = Color.parseColor(HEX_ORANGERED), // Red
            C_Y = Color.parseColor(HEX_MEDIUMSLATEBLUE), // Blue
            C_Z = Color.parseColor(HEX_GOLD), // Green
            C_RX = Color.parseColor(HEX_RUBY), // Pink
            C_RY = Color.parseColor(HEX_DODGERBLUE), // Yellow
            C_RZ = Color.parseColor(HEX_MALACHITE); // Cyan

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

    class DegreesFormater implements IAxisValueFormatter {

        private String units = "\u00B0/s";

        public DegreesFormater() {
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return value + units;
        }
    }

    class TimeFormatter implements IAxisValueFormatter {

        private long sampleRate;

        public TimeFormatter(long sampleRateMilliseconds) {
            this.sampleRate = sampleRateMilliseconds;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            long milliseconds = Math.round(value*sampleRate) + 18000000;
            return C_TIME_SIMPLE_FORMAT.format(new Date(milliseconds));
        }
    }

    class DistanceFormatter implements IAxisValueFormatter {

        private String units = " g";

        public DistanceFormatter() { }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return value + units;
        }
    }

    class HeartRateFormater implements IAxisValueFormatter {

        private String units = " BPM";

        public HeartRateFormater() {

        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return Math.round(value) + units;
        }
    }
}
