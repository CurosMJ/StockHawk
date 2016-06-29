package com.sam_chordas.android.stockhawk;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.service.WidgetIntentService;

/**
 * Created by curos on 28/6/16.
 */
public class ListWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.equals(StockTaskService.STOCKS_UPDATE)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int ids[] = manager.getAppWidgetIds(new ComponentName(context, ListWidgetProvider.class));
            onUpdate(context, manager, ids);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, ListWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list_widget);

            rv.setEmptyView(R.id.list, R.id.emptyView);
            rv.setRemoteAdapter(R.id.list, intent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }
}
