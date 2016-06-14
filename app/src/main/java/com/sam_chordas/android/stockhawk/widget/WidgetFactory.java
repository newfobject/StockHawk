package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockIntentService;


public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory{
    private Cursor data = null;
    private Context context;

    public WidgetFactory(Context applicationContext, Intent intent) {
        this.context = applicationContext;
    }

    @Override
    public void onCreate() {

        Intent serviceIntent = new Intent(context, StockIntentService.class);
        serviceIntent.putExtra("tag", "init");
        context.startService(serviceIntent);

    }

    @Override
    public void onDataSetChanged() {
        if (data!= null) {
            data.close();
        }
        data = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);

        views.setTextViewText(R.id.widget_stock_symbol, data.getString(data.getColumnIndex("symbol")));
        views.setTextViewText(R.id.widget_bid_price, data.getString(data.getColumnIndex("bid_price")));
        views.setTextViewText(R.id.widget_change, data.getString(data.getColumnIndex("percent_change")));
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return (null);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (data.moveToPosition(position))
            return data.getLong(data.getColumnIndex(QuoteColumns._ID));
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
