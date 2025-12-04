package com.example.borrowbuddy.util;

import android.content.Context;
import com.example.borrowbuddy.data.local.db.AppDatabase;
import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import java.time.Instant;
import java.time.LocalDate;

public class TestDataUtil {
    public static void insertTestLoans(Context context) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.get(context);
            db.loanDao().deleteAll();
            Loan l1 = new Loan();
            l1.type = Enums.LoanType.LOANED;
            l1.title = "Java编程思想";
            l1.personName = "张三";
            l1.dueDate = LocalDate.now().plusDays(2);
            l1.amountCents = 5000L;
            l1.currency = "CNY";
            l1.notes = "还书时请联系";
            l1.createdAt = l1.updatedAt = Instant.now();
            db.loanDao().insert(l1);

            Loan l2 = new Loan();
            l2.type = Enums.LoanType.BORROWED;
            l2.title = "U盘";
            l2.personName = "李四";
            l2.dueDate = LocalDate.now().minusDays(1);
            l2.amountCents = 2000L;
            l2.currency = "CNY";
            l2.notes = "重要资料";
            l2.createdAt = l2.updatedAt = Instant.now();
            db.loanDao().insert(l2);

            Loan l3 = new Loan();
            l3.type = Enums.LoanType.LOANED;
            l3.title = "篮球";
            l3.personName = "王五";
            l3.dueDate = LocalDate.now().plusDays(7);
            l3.amountCents = 0L;
            l3.currency = "CNY";
            l3.notes = "学校体育馆";
            l3.createdAt = l3.updatedAt = Instant.now();
            db.loanDao().insert(l3);
        }).start();
    }
}
