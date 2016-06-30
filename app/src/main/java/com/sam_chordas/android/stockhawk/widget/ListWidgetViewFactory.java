package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

public class ListWidgetViewFactory implements RemoteViewsService.RemoteViewsFactory{

    private Context context;
    private Cursor cursor;
    private static String CLICK = "click";

    ListWidgetViewFactory(Context context)
    {
        this.context = context;
    }
    @Override
    public void onCreate() {
        this.onDataSetChanged();
    }

    @Override
    public void onDataSetChanged() {
        final long identity = Binder.clearCallingIdentity();
        cursor = context.getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns._ID,
                        QuoteColumns.NAME,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        Binder.restoreCallingIdentity(identity);
    }

    @Override
    public void onDestroy() {
        cursor.close();
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        cursor.moveToPosition(i);

        String symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
        String symbolName = cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME));
        String bidPrice = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
        String change = cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));

        views.setTextViewText(R.id.stock_symbol_name, symbolName);
        views.setTextViewText(R.id.stock_symbol, symbol);
        views.setTextViewText(R.id.bid_price, bidPrice);
        views.setTextViewText(R.id.change_up, change);
        views.setTextViewText(R.id.change_down, change);

        views.setContentDescription(R.id.stock_symbol, Utils.addSpacesInSymbol(symbol));
        views.setContentDescription(R.id.bid_price, context.getString(R.string.bid_price_is)+" "+bidPrice);

        Intent clickIntent = new Intent();
        clickIntent.putExtra("symbol", symbol);
        views.setOnClickFillInIntent(R.id.widget_layout, clickIntent);

        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            views.setViewVisibility(R.id.change_up, View.VISIBLE);
            views.setContentDescription(R.id.change_up, context.getString(R.string.change_is)+" "+change);
            views.setViewVisibility(R.id.change_down, View.GONE);
        } else {
            views.setViewVisibility(R.id.change_up, View.GONE);
            views.setContentDescription(R.id.change_down, context.getString(R.string.change_is)+" "+change);
            views.setViewVisibility(R.id.change_down, View.VISIBLE);
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
