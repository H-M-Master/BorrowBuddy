package com.example.borrowbuddy.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.borrowbuddy.R;
import com.example.borrowbuddy.data.local.db.AppDatabase;
import com.example.borrowbuddy.data.model.Loan;
import com.example.borrowbuddy.receiver.NotificationActionReceiver;
import com.example.borrowbuddy.ui.share.ShareFromNotificationActivity;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReminderWorker extends Worker {

    public static final String WORK_NAME = "reminder_worker";
    private static final String CHANNEL_ID = "borrow_buddy_reminders";
    private static final int NOTIFICATION_ID_START = 1000;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        createNotificationChannel(context);

        LocalDate today = LocalDate.now();
        LocalDate reminderCutoffDate = today.plusDays(7);

        AppDatabase db = AppDatabase.get(context);
        List<Loan> loansToRemind = db.loanDao().getUpcomingAndOverdueSync(reminderCutoffDate);

        for (int i = 0; i < loansToRemind.size(); i++) {
            Loan loan = loansToRemind.get(i);
            sendNotification(context, loan, NOTIFICATION_ID_START + i);
        }

        return Result.success();
    }

    private void sendNotification(Context context, Loan loan, int notificationId) {
        // FIX: If for some reason a loan without a due date gets here, just ignore it.
        if (loan.dueDate == null) {
            return;
        }

        Intent intent = new Intent(context, com.example.borrowbuddy.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String title;
        String content;
        String formattedDueDate = loan.dueDate.format(DateTimeFormatter.ofPattern("MMM dd"));

        if (loan.dueDate.isBefore(LocalDate.now())) {
            title = context.getString(R.string.notification_title_overdue);
            content = context.getString(R.string.notification_content_overdue, loan.title, formattedDueDate);
        } else {
            title = context.getString(R.string.notification_title_upcoming);
            content = context.getString(R.string.notification_content_upcoming, loan.title, formattedDueDate);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Intent markAsReturnedIntent = new Intent(context, NotificationActionReceiver.class);
        markAsReturnedIntent.setAction(NotificationActionReceiver.ACTION_MARK_RETURNED);
        markAsReturnedIntent.putExtra(NotificationActionReceiver.EXTRA_LOAN_ID, loan.id);
        markAsReturnedIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent markAsReturnedPendingIntent = PendingIntent.getBroadcast(context, notificationId, markAsReturnedIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_done, context.getString(R.string.action_mark_returned), markAsReturnedPendingIntent);

        Intent sendReminderIntent = new Intent(context, ShareFromNotificationActivity.class);
        sendReminderIntent.putExtra(ShareFromNotificationActivity.EXTRA_LOAN_ID, loan.id);
        sendReminderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent sendReminderPendingIntent = PendingIntent.getActivity(context, notificationId, sendReminderIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_send, context.getString(R.string.send_reminder), sendReminderPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
