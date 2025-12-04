package com.example.borrowbuddy.ui.edit;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.example.borrowbuddy.data.Repository;
import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import java.time.LocalDate;
import java.util.concurrent.Executors;

public class LoanEditViewModel extends AndroidViewModel {
    private final Repository repo;
    public final MutableLiveData<Loan> loan = new MutableLiveData<>();

    public LoanEditViewModel(@NonNull Application app) {
        super(app);
        repo = new Repository(app);
    }

    public void newLoan(Enums.LoanType type){
        Loan l = new Loan(); l.type = type; l.dueDate = LocalDate.now().plusDays(3);
        loan.setValue(l);
    }

    public void load(long id){
        Executors.newSingleThreadExecutor().submit(() -> {
            Loan l = repo.getByIdSync(id);
            loan.postValue(l);
        });
    }

    public void save(){
        Loan l = loan.getValue(); if (l==null) return;
        if (l.id==0) repo.addAsync(l); else repo.updateAsync(l);
    }
}
