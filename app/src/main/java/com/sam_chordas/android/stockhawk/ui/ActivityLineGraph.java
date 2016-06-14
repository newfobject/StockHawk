package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.events.RequestUpdateEvent;
import com.sam_chordas.android.stockhawk.events.UpdateEvent;
import com.sam_chordas.android.stockhawk.rest.TabsAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;


public class ActivityLineGraph extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 0;
    private static final int TABS_AMOUNT = 3;
    private List<UpdateEvent> updateEventList;
    private TextView tmView;
    private TextView bidPriceView;
    private TextView bidPriceChangeView;
    private TextView bidPriceChangePercentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_stock_activity);
        if (savedInstanceState == null) {
            updateValues();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(0f);
            getSupportActionBar().setTitle("");
        }

        initViewPager();
        initOtherViews();



        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    private void initOtherViews() {
        tmView = (TextView) findViewById(R.id.tm_name_detail);
        bidPriceView = (TextView) findViewById(R.id.bid_price_detail);
        bidPriceChangeView = (TextView) findViewById(R.id.bid_price_change_detail);
        bidPriceChangePercentView = (TextView) findViewById(R.id.percent_change_detail);
    }

    private void initViewPager() {
        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.viewpager_title_five_days));
        titles.add(getString(R.string.viewpager_title_two_weeks));
        titles.add(getString(R.string.viewpager_title_one_month));
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager(), titles);
        if (viewPager != null) {
            viewPager.setAdapter(tabsAdapter);
        }
    }

    private void updateValues() {
        Intent serviceIntent = new Intent(this, StockIntentService.class);
        serviceIntent.putExtra("tag", "chart");
        serviceIntent.putExtra("symbol", getIntent().getStringExtra("symbol"));
        startService(serviceIntent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.NAME,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.BIDPRICES,
                        QuoteColumns.DATES,
                        QuoteColumns.STOCK_NAME,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE},
                QuoteColumns.SYMBOL + " =?",
                new String[]{getIntent().getStringExtra("symbol")}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) {
            Toast.makeText(this, R.string.no_data_loaded, Toast.LENGTH_SHORT).show();
        } else {
            tmView.setText(data.getString(data.getColumnIndex(QuoteColumns.NAME)));
            bidPriceView.setText(getString(R.string.currency_format,
                    data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE))));
            bidPriceChangeView.setText(data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
            bidPriceChangePercentView.setText(getString(R.string.change,
                    data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE))));



            String rawDates = data.getString(data.getColumnIndex(QuoteColumns.DATES));
            String rawPrices = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICES));
            if (rawDates != null && rawPrices != null) {
                List<Float> pricesList = Utils.convertFloatToArray(rawPrices);
                String[] dates = Utils.convertStringToArray(rawDates);
                updateEventList = new ArrayList<>();
                for (int i = 0; i < TABS_AMOUNT; i++) {
                    UpdateEvent updateEvent = createEventForEachTab(i,dates, pricesList);
                    EventBus.getDefault().post(updateEvent);
                    updateEventList.add(updateEvent);
                }
            }
        }
    }

    private UpdateEvent createEventForEachTab(int tabId, String[] dates, List<Float> pricesList) {
        String[] limitedDates;
        float[] limitedPrices;
        int days;
        switch (tabId) {
            case 0:
                days = 5;
                if (days > dates.length && days > pricesList.size()) {
                    days = Math.min(dates.length, pricesList.size());
                }
                limitedDates = new String[days];
                System.arraycopy(dates, pricesList.size() - days, limitedDates, 0, days);
                limitedDates[4] = "";
                break;
            case 1:
                days = 14;
                if (days > dates.length && days > pricesList.size()) {
                    days = Math.min(dates.length, pricesList.size());
                }
                limitedDates = new String[days];
                System.arraycopy(dates, pricesList.size() - days, limitedDates, 0, days);
                for (int i = 0; i < limitedDates.length ; i++) {
                    if (i % 4 != 0) {
                        limitedDates[i] = "";
                    }
                }
                break;
            case 2:
                days = 30;
                if (days > dates.length && days > pricesList.size()) {
                    days = Math.min(dates.length, pricesList.size());
                }
                limitedDates = new String[days];
                System.arraycopy(dates, pricesList.size() - days, limitedDates, 0, days);
                for (int i = 0; i < days - 1 ; i++) {
                    if (i % 6 != 0) {
                        limitedDates[i] = "";
                    }
                }
                limitedDates[limitedDates.length-1] = "";
                break;

            default: throw new RuntimeException("No tab with this id");
        }

        limitedPrices = new float[days];
        int max = Math.round(pricesList.get(0));
        int min = max;
        for (int i = 0; i < days; i++) {
            if (max < pricesList.get(i)) max = Math.round(pricesList.get(i));
            if (min > pricesList.get(i)) min = Math.round(pricesList.get(i));
            limitedPrices[i] = pricesList.get(i);
        }

        return new UpdateEvent(tabId, limitedDates, limitedPrices, max, min);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Subscribe
    public void requestEvent(RequestUpdateEvent event) {
        if (updateEventList != null && updateEventList.size() == TABS_AMOUNT) {
            EventBus.getDefault().post(updateEventList.get(event.getId()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
