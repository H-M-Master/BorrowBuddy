package com.example.borrowbuddy.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.example.borrowbuddy.R;
import com.example.borrowbuddy.data.local.db.AppDatabase;
import com.example.borrowbuddy.data.model.Loan;
import com.example.borrowbuddy.domain.DateFmt;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowBuddyWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private List<Loan> mLoans = new ArrayList<>();

    public BorrowBuddyWidgetViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        final long identity = android.os.Binder.clearCallingIdentity();
        try {
            AppDatabase db = AppDatabase.get(mContext);
            mLoans = db.loanDao().top3UpcomingSync();
        } finally {
            android.os.Binder.restoreCallingIdentity(identity);
        }
    }

    @Override
    public void onDestroy() {
        mLoans.clear();
    }

    @Override
    public int getCount() {
        return mLoans.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= mLoans.size()) {
            return null;
        }
        Loan loan = mLoans.get(position);

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_loan_widget);

        views.setTextViewText(R.id.widget_item_title, loan.title);
        views.setTextViewText(R.id.widget_item_due_date, DateFmt.date(loan.dueDate));

        // Set text color for overdue items
        if (loan.dueDate.isBefore(LocalDate.now())) {
            views.setTextColor(R.id.widget_item_due_date, mContext.getColor(android.R.color.holo_red_dark));
        } else {
            views.setTextColor(R.id.widget_item_due_date, mContext.getColor(android.R.color.darker_gray));
        }

        // Fill-in intent for each item
        Bundle extras = new Bundle();
        extras.putLong("edit_id", loan.id);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return mLoans.get(position).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
