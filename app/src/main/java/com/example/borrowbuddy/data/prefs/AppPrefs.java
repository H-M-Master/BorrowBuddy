package com.example.borrowbuddy.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {
    private static final String FILE = "bb_prefs";
    public static final String PREF_REMIND_HOUR="remind_hour";
    public static final String PREF_REMIND_MIN="remind_min";
    public static final String PREF_GLOBAL_DAYS_BEFORE="global_days_before";
    public static final String PREF_SILENT_START_HOUR="silent_start_hour";
    public static final String PREF_SILENT_END_HOUR="silent_end_hour";
    public static final String PREF_CURRENCY="currency";

    private final SharedPreferences sp;
    public AppPrefs(Context c){ sp = c.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    public int remindHour(){ return sp.getInt(PREF_REMIND_HOUR, 9); }
    public int remindMin(){ return sp.getInt(PREF_REMIND_MIN, 0); }
    public int globalDaysBefore(){ return sp.getInt(PREF_GLOBAL_DAYS_BEFORE, 2); }
    public int silentStartHour(){ return sp.getInt(PREF_SILENT_START_HOUR, 22); }
    public int silentEndHour(){ return sp.getInt(PREF_SILENT_END_HOUR, 7); }
    public String currency(){ return sp.getString(PREF_CURRENCY, "CNY"); }
}
