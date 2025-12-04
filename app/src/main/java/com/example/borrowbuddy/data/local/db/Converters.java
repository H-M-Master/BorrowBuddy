package com.example.borrowbuddy.data.local.db;

import androidx.room.TypeConverter;
import com.example.borrowbuddy.data.model.Enums;
import java.time.Instant;
import java.time.LocalDate;

public class Converters {
    @TypeConverter
    public static Long fromLocalDate(LocalDate d) {
        return d == null ? null : d.toEpochDay();
    }

    @TypeConverter
    public static LocalDate toLocalDate(Long days) {
        return days == null ? LocalDate.now() : LocalDate.ofEpochDay(days);
    }

    @TypeConverter
    public static Long fromInstant(Instant i) {
        return i == null ? null : i.toEpochMilli();
    }

    @TypeConverter
    public static Instant toInstant(Long ms) {
        return ms == null ? Instant.now() : Instant.ofEpochMilli(ms);
    }

    @TypeConverter
    public static String fromLoanType(Enums.LoanType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Enums.LoanType toLoanType(String name) {
        if (name == null) return Enums.LoanType.LOANED;
        try {
            return Enums.LoanType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Enums.LoanType.LOANED; // Return a default value on parsing failure
        }
    }

    @TypeConverter
    public static String fromLoanStatus(Enums.LoanStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static Enums.LoanStatus toLoanStatus(String name) {
        if (name == null) return Enums.LoanStatus.OPEN;
        try {
            return Enums.LoanStatus.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Enums.LoanStatus.OPEN; // Return a default value on parsing failure
        }
    }
}
