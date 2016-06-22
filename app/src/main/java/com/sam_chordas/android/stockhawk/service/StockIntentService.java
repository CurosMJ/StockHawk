package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

import java.util.HashSet;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
  public static String TOAST = "TOAST";
  public static String SERVICE_RESULT = "SERVICE_RESULT";

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");

    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    int status = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    Intent serviceResult = new Intent(SERVICE_RESULT);
    serviceResult.putExtra("status", status);
    localBroadcastManager.sendBroadcast(serviceResult);

    if (intent.getStringExtra("tag").equals("add")) {
      Intent toastBroadcast = new Intent(TOAST);
      if (stockTaskService.invalidSymbolException != null) {
        toastBroadcast.putExtra("text", getString(R.string.invalid_symbol_toast));
      } else {
        HashSet<String> stocks = new HashSet<String>(preferences.getStringSet("stocks", new HashSet<String>()));
        stocks.add(intent.getStringExtra("symbol"));
        preferences.edit().putStringSet("stocks", stocks).commit();
        toastBroadcast.putExtra("text", intent.getStringExtra("symbol").concat(" ").concat(getString(R.string.symbol_added_toast_suffix)));
      }
      localBroadcastManager.sendBroadcast(toastBroadcast);
    }

    Intent updateIntent = new Intent(StockTaskService.STOCKS_UPDATE);
    this.sendBroadcast(updateIntent);
  }
}
