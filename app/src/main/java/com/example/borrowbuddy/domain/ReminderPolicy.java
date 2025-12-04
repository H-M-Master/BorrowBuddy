package com.example.borrowbuddy.domain;

import android.content.Context;
import com.example.borrowbuddy.data.model.Loan;
import com.example.borrowbuddy.data.prefs.AppPrefs;
import java.time.*;

public class ReminderPolicy {
    public enum Decision { NONE, UPCOMING, OVERDUE }
    public static class Result { public final Decision decision; public final String reason;
        public Result(Decision d,String r){ decision=d; reason=r; } }

    public static Result evaluate(Context c, Loan l, LocalDate today, Instant now){
        AppPrefs p = new AppPrefs(c);
        int start=p.silentStartHour(), end=p.silentEndHour();
        int hourNow = now.atZone(ZoneId.systemDefault()).getHour();
        boolean inSilent = (start<end)?(hourNow>=start && hourNow<end):(hourNow>=start || hourNow<end);
        if (inSilent) return new Result(Decision.NONE,"silent");

        if (l.nextNotifyAt!=null && now.isBefore(l.nextNotifyAt)) return new Result(Decision.NONE,"snoozed");
        if (l.dueDate.isBefore(today)) return new Result(Decision.OVERDUE,"overdue");

        int daysBefore = l.remindDaysBefore!=null? l.remindDaysBefore : p.globalDaysBefore();
        long daysLeft = Duration.between(today.atStartOfDay(), l.dueDate.atStartOfDay()).toDays();
        if (daysLeft <= daysBefore) return new Result(Decision.UPCOMING,"left="+daysLeft);
        return new Result(Decision.NONE,"notyet");
    }
}
