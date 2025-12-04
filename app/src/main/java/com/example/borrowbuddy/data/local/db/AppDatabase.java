package com.example.borrowbuddy.data.local.db;

import android.content.Context;
import androidx.room.*;
import com.example.borrowbuddy.data.local.dao.LoanDao;
import com.example.borrowbuddy.data.model.Loan;

@Database(entities = {Loan.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract LoanDao loanDao();
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase get(Context c){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(c.getApplicationContext(),
                            AppDatabase.class, "borrowbuddy.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
