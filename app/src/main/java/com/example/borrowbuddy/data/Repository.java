package com.example.borrowbuddy.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.borrowbuddy.data.local.db.AppDatabase;
import com.example.borrowbuddy.data.local.dao.LoanDao;
import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;

public class Repository {
    private final LoanDao dao;
    public Repository(Context c){ dao = AppDatabase.get(c).loanDao(); }

    public LiveData<List<Loan>> getOpenByType(Enums.LoanType t){ return dao.getOpenByTypeLive(t); }
    public LiveData<List<Loan>> searchOpen(String q){ return dao.searchOpen("%"+q+"%"); }
    public List<Loan> getOpenSync(){ return dao.getOpenSync(); }
    public List<Loan> top3UpcomingSync(){ return dao.top3UpcomingSync(); }
    public Loan getByIdSync(long id){ return dao.getByIdSync(id); }

    public void addAsync(Loan l){
        Executors.newSingleThreadExecutor().submit(() -> {
            l.createdAt = Instant.now(); l.updatedAt = l.createdAt;
            dao.insert(l);
        });
    }
    public void updateAsync(Loan l){
        Executors.newSingleThreadExecutor().submit(() -> {
            l.updatedAt = Instant.now(); dao.update(l);
        });
    }
    public void markReturnedAsync(long id){
        Executors.newSingleThreadExecutor().submit(() -> dao.updateStatus(id, Enums.LoanStatus.RETURNED, Instant.now()));
    }
}
