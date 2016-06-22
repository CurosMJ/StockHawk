package com.sam_chordas.android.stockhawk;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.HashSet;

/**
 * Created by curos on 20/5/16.
 */
public class StockHawk extends Application {

  private SharedPreferences preferences;

  public static String widgetStockPreferenceKey(Integer widgetId)
  {
    return "stock_widget_".concat(widgetId.toString());
  }

  @Override
  public void onCreate() {
    super.onCreate();
    preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

    String defaultUnit = preferences.getString(getString(R.string.preference_default_unit), getString(R.string.change_default_unit));

    Utils.showPercent = defaultUnit.equals("%");

    if ( ! preferences.getBoolean("default_stocks_set", false)) {
      setDefaultStocks();
      preferences.edit().putBoolean("default_stocks_set", true).commit();
    }
  }

  public void setDefaultStocks()
  {
    Log.d("stocks", "Stocks reset");
    HashSet<String> defaultStocks = new HashSet<>();
    defaultStocks.add("YHOO");
    defaultStocks.add("AAPL");
    defaultStocks.add("GOOG");
    defaultStocks.add("MSFT");
    preferences.edit().putStringSet("stocks", defaultStocks).commit();
  }
}
