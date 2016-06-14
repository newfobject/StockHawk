package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;


public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetID : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            Intent adapterIntent = new Intent(context, WidgetService.class);
            adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
            views.setRemoteAdapter(R.id.widget_list, adapterIntent);
            appWidgetManager.updateAppWidget(appWidgetID, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetID, R.id.widget_list);
        }
    }

}
