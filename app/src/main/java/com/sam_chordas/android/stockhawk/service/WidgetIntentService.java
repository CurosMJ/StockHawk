package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHawk;
import com.sam_chordas.android.stockhawk.WidgetProvider;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailsActivity;

/**
 * Created by curos on 14/6/16.
 */
public class WidgetIntentService extends IntentService {

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    public WidgetIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));

        final int N = appWidgetIds.length;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        for (int i = 0; i < N; i++) {
            Integer appWidgetId = appWidgetIds[i];

            String symbol = preferences.getString(StockHawk.widgetStockPreferenceKey(appWidgetId), "YHOO");

            Cursor data = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{
                            QuoteColumns._ID,
                            QuoteColumns.NAME,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP
                    },
                    QuoteColumns.SYMBOL + " = ? AND "+QuoteColumns.ISCURRENT+" = 1",
                    new String[]{symbol},
                    null);

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);

            if (data.getCount() == 0) {
                views.setTextViewText(R.id.stock_symbol_name, getString(R.string.stock_deleted));
                views.setViewVisibility(R.id.stock_symbol, View.GONE);
                views.setViewVisibility(R.id.bid_price, View.GONE);
                views.setViewVisibility(R.id.change_up, View.GONE);
                views.setViewVisibility(R.id.change_down, View.GONE);

            } else {

                data.moveToPosition(0);

                Intent clickIntent = new Intent(this, StockDetailsActivity.class);
                clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                clickIntent.putExtra("symbol", symbol);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);


                String symbolName = data.getString(data.getColumnIndex(QuoteColumns.NAME));
                String bidPrice = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
                String change = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));

                views.setViewVisibility(R.id.stock_symbol, View.VISIBLE);
                views.setViewVisibility(R.id.bid_price, View.VISIBLE);
                views.setViewVisibility(R.id.change_up, View.VISIBLE);
                views.setViewVisibility(R.id.change_down, View.VISIBLE);

                views.setTextViewText(R.id.stock_symbol_name, symbolName);
                views.setTextViewText(R.id.stock_symbol, symbol);
                views.setTextViewText(R.id.bid_price, bidPrice);
                views.setTextViewText(R.id.change_up, change);
                views.setTextViewText(R.id.change_down, change);

                if (data.getInt(data.getColumnIndex("is_up")) == 1) {
                    views.setViewVisibility(R.id.change_up, View.VISIBLE);
                    views.setViewVisibility(R.id.change_down, View.GONE);
                } else {
                    views.setViewVisibility(R.id.change_up, View.GONE);
                    views.setViewVisibility(R.id.change_down, View.VISIBLE);
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
