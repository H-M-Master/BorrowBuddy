package com.example.borrowbuddy.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity(tableName = "loans")
public class Loan {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public Enums.LoanType type = Enums.LoanType.LOANED;
    @NonNull public String title = "";
    public String personName;
    public String phone;
    public String contactUri;
    // G_debug: Add notes field to match UI
    public String notes;
    @NonNull public LocalDate dueDate = LocalDate.now().plusDays(3);
    @NonNull public Enums.LoanStatus status = Enums.LoanStatus.OPEN;
    public Long amountCents;
    public String currency;
    public String photoUri;
    public Integer remindDaysBefore;
    public Instant nextNotifyAt;
    @NonNull public Instant createdAt = Instant.now();
    @NonNull public Instant updatedAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return id == loan.id &&
                type == loan.type &&
                status == loan.status &&
                Objects.equals(title, loan.title) &&
                Objects.equals(personName, loan.personName) &&
                Objects.equals(phone, loan.phone) &&
                Objects.equals(contactUri, loan.contactUri) &&
                Objects.equals(notes, loan.notes) &&
                Objects.equals(dueDate, loan.dueDate) &&
                Objects.equals(amountCents, loan.amountCents) &&
                Objects.equals(currency, loan.currency) &&
                Objects.equals(photoUri, loan.photoUri) &&
                Objects.equals(remindDaysBefore, loan.remindDaysBefore) &&
                Objects.equals(nextNotifyAt, loan.nextNotifyAt) &&
                Objects.equals(createdAt, loan.createdAt) &&
                Objects.equals(updatedAt, loan.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, title, personName, phone, contactUri, notes, dueDate, status, amountCents, currency, photoUri, remindDaysBefore, nextNotifyAt, createdAt, updatedAt);
    }
}
