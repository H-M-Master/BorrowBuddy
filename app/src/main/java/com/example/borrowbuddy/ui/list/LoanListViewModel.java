package com.example.borrowbuddy.ui.list;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.example.borrowbuddy.data.Repository;
import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoanListViewModel extends AndroidViewModel {

    public enum FilterMode {
        ALL,
        OVERDUE,
        DUE_THIS_WEEK
    }

    private final Repository repo;
    private final MutableLiveData<Enums.LoanType> type = new MutableLiveData<>(Enums.LoanType.LOANED);
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<FilterMode> filterMode = new MutableLiveData<>(FilterMode.ALL);

    private final MediatorLiveData<List<Loan>> filteredList = new MediatorLiveData<>();

    public LoanListViewModel(@NonNull Application app) {
        super(app);
        repo = new Repository(app);

        // The source of truth from the database
        LiveData<List<Loan>> sourceList = Transformations.switchMap(type, repo::getOpenByType);

        filteredList.addSource(sourceList, this::applyFilters);
        filteredList.addSource(query, q -> applyFilters(sourceList.getValue()));
        filteredList.addSource(filterMode, fm -> applyFilters(sourceList.getValue()));
    }

    private void applyFilters(List<Loan> loans) {
        if (loans == null) {
            filteredList.setValue(null);
            return;
        }

        Stream<Loan> stream = loans.stream();

        // Apply filter mode
        FilterMode mode = filterMode.getValue();
        if (mode != null) {
            LocalDate today = LocalDate.now();
            switch (mode) {
                case OVERDUE:
                    stream = stream.filter(l -> l.dueDate != null && l.dueDate.isBefore(today));
                    break;
                case DUE_THIS_WEEK:
                    LocalDate nextWeek = today.plusWeeks(1);
                    stream = stream.filter(l -> l.dueDate != null && !l.dueDate.isBefore(today) && l.dueDate.isBefore(nextWeek));
                    break;
            }
        }

        // Apply search query
        String q = query.getValue();
        if (q != null && !q.isEmpty()) {
            String lowerCaseQuery = q.toLowerCase();
            stream = stream.filter(l -> 
                (l.title != null && l.title.toLowerCase().contains(lowerCaseQuery)) ||
                (l.personName != null && l.personName.toLowerCase().contains(lowerCaseQuery))
            );
        }

        filteredList.setValue(stream.collect(Collectors.toList()));
    }

    public void setType(Enums.LoanType t) { type.setValue(t); }
    public void setQuery(String q) { query.setValue(q == null ? "" : q); }
    public void setFilterMode(FilterMode mode) { filterMode.setValue(mode); }

    public LiveData<List<Loan>> getList() {
        return filteredList;
    }
}
