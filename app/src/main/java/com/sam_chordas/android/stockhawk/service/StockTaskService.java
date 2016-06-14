package com.sam_chordas.android.stockhawk.service;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();
    public static String ACTION_RECEIVER = "com.sam_chordas.android.stockhawk.service.result.broadcast";
    public static String RECEIVER_EXTRA_KEY_BOOL = "receiver_extra_stock_task";
    public static String RECEIVER_EXTRA_KEY_MSG = "receiver_extra_stock_task_msg";

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();

        // here we load data for our ActivityLineGraph
        if (params.getTag().equals("chart")) {
            String stockInput = params.getExtras().getString("symbol");

            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            try {
                @SuppressLint("SimpleDateFormat")
                DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                Calendar calendar = Calendar.getInstance();
                String currentDate = dateFormat.format(calendar.getTime());
                calendar.add(Calendar.MONTH, -1);
                String monthAgo = dateFormat.format(calendar.getTime());
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata" +
                        " where symbol in (\"" + stockInput  + "\") and " +
                        "startDate=\'" + monthAgo  + "\'  and endDate=\'" + currentDate  + "\'"
                        ,"UTF-8"));
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");
                String result = fetchData(urlStringBuilder.toString());
                List<String> list = Utils.pricesJsonToStr(result);

                if (list != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(QuoteColumns.DATES, list.get(0));
                    contentValues.put(QuoteColumns.BIDPRICES, list.get(1));
                    mContext.getContentResolver().update(
                            QuoteProvider.Quotes.CONTENT_URI,
                            contentValues,
                            QuoteColumns.SYMBOL + " =?",
                            new String[] {stockInput});
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return 1;

        } else {

            try {
                // Base URL for the Yahoo query
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                        + "in (", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
                isUpdate = true;
                initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                        null, null);
                if (initQueryCursor == null || initQueryCursor.getCount() == 0) {
                    // Init task. Populates DB with quotes for the symbols seen below
                    try {
                        urlStringBuilder.append(
                                URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (initQueryCursor != null) {
                    DatabaseUtils.dumpCursor(initQueryCursor);
                    initQueryCursor.moveToFirst();
                    for (int i = 0; i < initQueryCursor.getCount(); i++) {
                        mStoredSymbols.append("\"" +
                                initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                        initQueryCursor.moveToNext();
                    }
                    mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                    try {
                        urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            } else if (params.getTag().equals("add")) {
                isUpdate = false;
                // get symbol from params.getExtra and build query
                String stockInput = params.getExtras().getString("symbol");
                try {
                    urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // finalize the URL for the API query.
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");

            String urlString;
            String getResponse;

            int result = GcmNetworkManager.RESULT_FAILURE;

            if (urlStringBuilder != null) {
                urlString = urlStringBuilder.toString();
                try {
                    getResponse = fetchData(urlString);
                    try {
                        ContentValues contentValues = new ContentValues();
                        // update ISCURRENT to 0 (false) so new data is current
                        if (isUpdate) {
                            contentValues.put(QuoteColumns.ISCURRENT, 0);
                            mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                    null, null);
                        }
                        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                Utils.quoteJsonToContentVals(getResponse));

                    } catch (RemoteException | OperationApplicationException e) {
                        Log.e(LOG_TAG, "Error applying batch insert", e);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Intent errorBroadcast = new Intent(ACTION_RECEIVER);
                    errorBroadcast.putExtra(RECEIVER_EXTRA_KEY_BOOL, false);
                    errorBroadcast.putExtra(RECEIVER_EXTRA_KEY_MSG, e.getMessage());
                    mContext.sendBroadcast(errorBroadcast);
                }
            }

            return result;

        }
    }
}
