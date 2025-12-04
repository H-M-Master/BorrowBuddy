package com.example.borrowbuddy.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.borrowbuddy.data.local.db.AppDatabase;
import com.example.borrowbuddy.data.model.Enums;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_MARK_RETURNED = "com.example.borrowbuddy.ACTION_MARK_RETURNED";
    public static final String EXTRA_LOAN_ID = "extra_loan_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_MARK_RETURNED.equals(intent.getAction())) {
            long loanId = intent.getLongExtra(EXTRA_LOAN_ID, -1);
            int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

            if (loanId != -1) {
                executor.execute(() -> {
                    AppDatabase db = AppDatabase.get(context.getApplicationContext());
                    db.loanDao().updateStatus(loanId, Enums.LoanStatus.RETURNED, Instant.now());
                });

                if (notificationId != -1) {
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationId);
                }
            }
        }
    }
}
