package com.example.borrowbuddy.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.example.borrowbuddy.MainActivity;
import com.example.borrowbuddy.R;

public class BorrowBuddyWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.borrow_buddy_widget);

        Intent titleIntent = new Intent(context, MainActivity.class);
        PendingIntent titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_title, titlePendingIntent);

        Intent serviceIntent = new Intent(context, BorrowBuddyWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setRemoteAdapter(R.id.widget_list_view, serviceIntent);

        // This intent template is used to create the click listeners for each item in the list.
        // It must be mutable so the system can fill in the extras from the fill-in intent.
        Intent clickIntentTemplate = new Intent(context, MainActivity.class);
        PendingIntent clickPendingIntentTemplate = PendingIntent.getActivity(context, 1, clickIntentTemplate,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE); // FIX: Must be mutable
        views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntentTemplate);

        views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
