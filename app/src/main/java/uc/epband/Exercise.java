package uc.epband;

import org.json.JSONArray;

public class Exercise {
    private Boolean mValid = false;
    private String mExercise = "", mStartTime = "", mTimeLength = "", mSampleStep = "";
    private int mReps = 0;
    private Double mGoalROM = 90d, mAverageROM = 0d;
    private JSONArray mRotX, mRotY, mRotZ, mDistX, mDistY, mDistZ;

    //JSON Member names
    private static final String
            VALID = "Valid",
            ANALYZED = "Analyzed",
            EXERCISE = "Exercise",
            START_TIME = "StartTime",
            TIME_LENGTH = "TimeLength",
            SAMPLE_STEP = "SampleStep",
            GOAL_ROM = "GoalROM",
            AVG_ROM = "AvgROM",
            REPS = "Reps",
            ROT_X = "RotationX",
            ROT_Y = "RotationY",
            ROT_Z = "RotationZ",
            DIST_X = "DistanceX",
            DIST_Y = "DistanceY",
            DIST_Z = "DistanceZ";

    public Exercise(){

    }
}
