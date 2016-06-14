package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) throws IOException {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws IOException {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            String symbol = jsonObject.getString("symbol");
            String bidPrice = jsonObject.getString("Bid");
            String changeInPercent = jsonObject.getString("ChangeinPercent");
            String name = jsonObject.getString("Name");
            String stockName = jsonObject.getString("StockExchange");


            if (notNullEmpty(change, symbol, bidPrice, changeInPercent, name)) {

                builder.withValue(QuoteColumns.SYMBOL, symbol);
                builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(bidPrice));
                builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(changeInPercent, true));
                builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
                builder.withValue(QuoteColumns.ISCURRENT, 1);
                builder.withValue(QuoteColumns.NAME, name);
                builder.withValue(QuoteColumns.STOCK_NAME, stockName);
                if (change.charAt(0) == '-') {
                    builder.withValue(QuoteColumns.ISUP, 0);
                } else {
                    builder.withValue(QuoteColumns.ISUP, 1);
                }
                return builder.build();

            } else {
                if (symbol != null)
                    throw new IOException(symbol);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        throw new IOException("server returned null");
    }

    static boolean notNullEmpty(String... strings) {
        for (String string : strings) {
            if (string == null || string.equals("") || string.equals("null")) {
                return false;
            }
        }
        return true;
    }

    public static String strSeparator = "__,__";

    public static String convertArrayToString(String[] array) {
        String str = "";
        for (int i = 0; i < array.length; i++) {
            str = str + array[i];
            // Do not append comma at the end of last element
            if (i < array.length - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str) {
        String[] arr = str.split(strSeparator);
        List<String> list = Arrays.asList(arr);
        Collections.reverse(list);
        arr = (String[]) list.toArray();
        return arr;
    }

    public static List<Float> convertFloatToArray(String str) {
        String[] arr = str.split(strSeparator);
        List<Float> list = new ArrayList<>();
        for (String string : arr) {
            list.add(Float.valueOf(string));
        }
        Collections.reverse(list);
        return list;
    }

    public static ArrayList pricesJsonToStr(String JSON) throws IOException {
        ArrayList<String> list = null;
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");


                jsonObject = jsonObject.getJSONObject("results");
                resultsArray = jsonObject.getJSONArray("quote");
                StringBuilder builderDate = new StringBuilder();
                StringBuilder builderPrice = new StringBuilder();
                for (int i = 0; i < resultsArray.length(); i++) {
                    builderDate.append(resultsArray.getJSONObject(i).get("Date"))
                            .append(strSeparator);
                    builderPrice.append(resultsArray.getJSONObject(i).get("Close"))
                            .append(strSeparator);
                }
                list = new ArrayList<>();
                String dates = builderDate.toString();
                String prices = builderPrice.toString();
                list.add(dates);
                list.add(prices);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return list;
    }
}

