package com.sam_chordas.android.stockhawk;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.service.WidgetIntentService;
import com.sam_chordas.android.stockhawk.ui.StockDetailsActivity;

/**
 * Created by curos on 31/5/16.
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.equals(StockTaskService.STOCKS_UPDATE)) {
            context.startService(new Intent(context, WidgetIntentService.class));
        }
//        if (action.equals(WidgetIntentService.CLICK)) {
//            Intent stockActivityIntent = new Intent(context, StockDetailsActivity.class);
//            stockActivityIntent.putExtra("symbol", intent.getExtras().getString("symbol"));
//            context.startActivity(stockActivityIntent);
//        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, WidgetIntentService.class));
    }
}
