package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.FetchHistoricalData;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StockDetailsActivity extends AppCompatActivity implements FetchHistoricalData.HistoricalDataHandler {

    protected LineChart chart;
    protected ProgressBar loadingBar;
    protected JSONArray stockHistory;

    protected String symbol;
    protected String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        symbol = getIntent().getExtras().getString("symbol");

        Cursor cursor = getContentResolver().query(
                QuoteProvider.Quotes.withSymbol(symbol),
                new String[]{QuoteColumns.NAME},
                null, null, null
        );
        cursor.moveToFirst();
        name = cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME));

        setTitle(name + " ("+symbol+")");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chart = (LineChart) findViewById(R.id.chart);
        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);

        FetchHistoricalData data = new FetchHistoricalData(this);
        data.execute(symbol);
    }

    protected LineData parseHistory()
    {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM");
        try {
            for (int i = stockHistory.length() - 1; i >= 0 ; i--) {
                JSONObject snapshot = stockHistory.getJSONObject(i);
                Date date = inputFormat.parse(snapshot.getString("Date"));
                entries.add(new Entry((float) snapshot.getDouble("Close"), i));
                labels.add(outputFormat.format(date));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new LineData(labels, new LineDataSet(entries, "Price"));
    }

    protected void drawChart()
    {
        loadingBar.setVisibility(View.GONE);
        chart.setVisibility(View.VISIBLE);
        chart.animateX(1000);
        chart.animateY(1000);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.setDescription("");
        chart.setClickable(false);
        chart.getAxisLeft().setGranularity(0.2f);
        chart.setData(parseHistory());
    }

    @Override
    public void handle(String data) {
        try {
            JSONObject object = new JSONObject(data);
            stockHistory = object.getJSONObject("query").getJSONObject("results").getJSONArray("quote");
            drawChart();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
