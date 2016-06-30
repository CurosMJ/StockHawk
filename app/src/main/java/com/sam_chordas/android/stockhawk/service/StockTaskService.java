package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    static public String STOCKS_UPDATE = "com.sam_chordas.android.stockhawk.STOCKS_UPDATE";

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public InvalidStockSymbolException invalidStockSymbolException = null;

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
        if (mContext == null) {
            mContext = this;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol in (", "UTF-8"));
            if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
                isUpdate = true;
                Set<String> stocks = preferences.getStringSet("stocks", new HashSet<String>());
                for (String stock : stocks) {
                    Log.d("stocks", stock);
                    mStoredSymbols.append("\"").append(stock).append("\",");
                }
                if (stocks.size() == 0) mStoredSymbols.append(" ");
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
            }
            else if (params.getTag().equals("add")) {
                isUpdate = false;
                // get symbol from params.getExtra and build query
                String stockInput = params.getExtras().getString("symbol");
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString = urlStringBuilder.toString();
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;
        try {
            getResponse = fetchData(urlString);
            result = GcmNetworkManager.RESULT_SUCCESS;
            try {

                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                if (isUpdate) {
                    // update ISCURRENT to 0 (false) so new data is current
                    ContentProviderOperation.Builder markOldData = ContentProviderOperation.newUpdate(QuoteProvider.Quotes.CONTENT_URI);
                    markOldData.withValue(QuoteColumns.ISCURRENT, 0);
                    operations.add(markOldData.build());
                }
                operations.addAll(Utils.quoteJsonToContentVals(getResponse));
                mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, operations);

                mContext.sendBroadcast(new Intent(STOCKS_UPDATE));
                Log.d("StockHawk", "Stocks update");

            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, "Error applying batch insert", e);
            } catch (InvalidStockSymbolException e) {
                this.invalidStockSymbolException = e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
