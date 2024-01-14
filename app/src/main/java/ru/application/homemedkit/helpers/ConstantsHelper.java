package ru.application.homemedkit.helpers;

import java.text.SimpleDateFormat;

public class ConstantsHelper {
    public static final String ADD = "add";
    public static final String ADDING = "adding";
    public static final String ALARM_ID = "alarmId";
    public static final String BLANK = "";
    public static final int BOUND = 200;
    public static final String CATEGORY = "drugs";
    public static final String CIS = "cis";
    public static final String CHECK_EXP_DATE = "check_exp_date";
    public static final String COLON = ":";
    public static final long DAY = 86400000L;
    public static final String DOWN_DASH = "_";
    public static final String DUPLICATE = "duplicate";
    public static final int EXP_CODE = 81000;
    public static final String FINISH = "finish";
    public static final String HASHTAG = "#";
    public static final String ID = "id";
    public static final String INTAKE_ID = "intakeId";
    public static final String INTERVAL = "interval";
    public static final String MEDICINE_ID = "medicine_id";
    public static final String NEW_INTAKE = "newIntake";
    public static final String NEW_MEDICINE = "newMedicine";
    public static final String PATTERN = "dd.MM.yyyy";
    public static final String PERIOD = "period";
    public static final SimpleDateFormat RUS = new SimpleDateFormat(PATTERN, DateHelper.RUSSIAN);
    public static final String SEMICOLON = ",";
    public static final String SETTINGS_CHANGED = "settingsChanged";
    public static final String SOUND_GROUP = "Sound group";
    public static final String START_DATE = "startDate";
    public static final String TIME = "time";
    public static final long WEEK = 604800000L;
    public static final String WHITESPACE_R = "\\s";
}
