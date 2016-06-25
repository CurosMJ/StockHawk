package com.sam_chordas.android.stockhawk;

import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by curos on 25/6/16.
 */
public class FetchHistoricalData extends AsyncTask<String, Void, String> {

    protected HistoricalDataHandler handler;

    public FetchHistoricalData(HistoricalDataHandler handler) {
        super();
        this.handler = handler;
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private String currentDate()
    {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        return dateFormat.format(time.getTime());
    }

    private String tenDaysAgoDate()
    {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.DAY_OF_MONTH, -10);
        return dateFormat.format(time.getTime());
    }

    @Override
    protected String doInBackground(String... strings) {
        String symbol = strings[0];
        StringBuilder urlBuilder = new StringBuilder();
        String utf8 = "UTF-8";
        try {
            urlBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where ", utf8));
            urlBuilder.append(URLEncoder.encode("symbol = \""+symbol+"\" AND ", utf8));
            urlBuilder.append(URLEncoder.encode("startDate = \""+ tenDaysAgoDate()+"\" AND ", utf8));
            urlBuilder.append(URLEncoder.encode("endDate = \""+ currentDate()+"\" ", utf8));
            urlBuilder.append("&format=json&env=store://datatables.org/alltableswithkeys&callback=");

            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(urlBuilder.toString());
            return client.newCall(builder.build()).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        handler.handle(s);
    }

    public interface HistoricalDataHandler {
        void handle(String data);
    }
}
