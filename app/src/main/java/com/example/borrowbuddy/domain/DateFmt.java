package com.example.borrowbuddy.domain;

import android.content.Context;
import com.example.borrowbuddy.data.prefs.AppPrefs;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

public class DateFmt {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static String date(LocalDate d){ return d==null?"":DATE.format(d); }
    public static String money(Context c, Long cents){
        if (cents==null) return "";
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        try { nf.setCurrency(Currency.getInstance(new AppPrefs(c).currency())); } catch (Exception ignored){}
        return nf.format(cents/100.0);
    }
}
