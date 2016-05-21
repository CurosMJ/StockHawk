package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
  public static String TOAST = "TOAST";

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));

    if (intent.getStringExtra("tag").equals("add")) {
      Intent broadcast = new Intent(TOAST);
      if (stockTaskService.invalidSymbolException != null) {
        broadcast.putExtra("text", getString(R.string.invalid_symbol_toast));
      } else {
        broadcast.putExtra("text", intent.getStringExtra("symbol").concat(" ").concat(getString(R.string.symbol_added_toast_suffix)));
      }
      LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }
  }
}
