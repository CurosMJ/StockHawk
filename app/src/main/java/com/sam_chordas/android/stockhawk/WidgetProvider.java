package com.sam_chordas.android.stockhawk;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.sam_chordas.android.stockhawk.service.WidgetIntentService;

/**
 * Created by curos on 31/5/16.
 */
public class WidgetProvider extends AppWidgetProvider {

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    String action = intent.getAction();
    context.startService(new Intent(context, WidgetIntentService.class));
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    context.startService(new Intent(context, WidgetIntentService.class));
  }
}
