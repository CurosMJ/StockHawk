package com.sam_chordas.android.stockhawk;

import android.content.Context;
import android.database.Cursor;
import android.os.Binder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class ListWidgetViewFactory implements RemoteViewsService.RemoteViewsFactory{

    private Context context;
    private Cursor cursor;

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

        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            views.setViewVisibility(R.id.change_up, View.VISIBLE);
            views.setViewVisibility(R.id.change_down, View.GONE);
        } else {
            views.setViewVisibility(R.id.change_up, View.GONE);
            views.setViewVisibility(R.id.change_down, View.VISIBLE);
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.list_widget_loading);
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
