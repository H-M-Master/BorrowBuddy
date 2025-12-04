package com.example.borrowbuddy.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import java.time.LocalDate;
import java.util.List;

@Dao
public interface LoanDao {
    @Insert long insert(Loan loan);
    @Update void update(Loan loan);

    @Query("UPDATE loans SET status=:status, updatedAt=:updatedAt WHERE id=:id")
    void updateStatus(long id, Enums.LoanStatus status, java.time.Instant updatedAt);

    @Query("SELECT * FROM loans WHERE id=:id LIMIT 1")
    Loan getByIdSync(long id);

    @Query("SELECT * FROM loans WHERE status='OPEN' ORDER BY dueDate IS NULL, dueDate ASC, createdAt ASC")
    LiveData<List<Loan>> getOpenLive();

    @Query("SELECT * FROM loans WHERE status='OPEN' AND type=:type ORDER BY dueDate IS NULL, dueDate ASC, createdAt ASC")
    LiveData<List<Loan>> getOpenByTypeLive(Enums.LoanType type);

    @Query("SELECT * FROM loans WHERE status='OPEN' AND (title LIKE :q OR personName LIKE :q OR phone LIKE :q) ORDER BY dueDate IS NULL, dueDate ASC, createdAt ASC")
    LiveData<List<Loan>> searchOpen(String q);

    @Query("SELECT * FROM loans WHERE status='OPEN' ORDER BY dueDate IS NULL, dueDate ASC, createdAt ASC")
    List<Loan> getOpenSync();

    @Query("SELECT * FROM loans WHERE status = 'OPEN' AND dueDate IS NOT NULL AND dueDate <= :maxDueDate ORDER BY dueDate ASC")
    List<Loan> getUpcomingAndOverdueSync(LocalDate maxDueDate);

    @Query("SELECT * FROM loans WHERE status='OPEN' AND dueDate IS NOT NULL ORDER BY dueDate ASC LIMIT 3")
    List<Loan> top3UpcomingSync();

    @Query("DELETE FROM loans") void deleteAll();

    @Query("SELECT * FROM loans")
    java.util.List<com.example.borrowbuddy.data.model.Loan> getAllSync();
}
