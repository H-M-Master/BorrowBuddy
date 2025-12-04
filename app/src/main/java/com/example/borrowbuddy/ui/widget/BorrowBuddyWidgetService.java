package com.example.borrowbuddy.ui.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class BorrowBuddyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new BorrowBuddyWidgetViewsFactory(this.getApplicationContext());
    }
}
