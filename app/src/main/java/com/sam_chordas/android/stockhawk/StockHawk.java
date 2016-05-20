package com.sam_chordas.android.stockhawk;

import android.app.Application;
import android.preference.PreferenceManager;

import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by curos on 20/5/16.
 */
public class StockHawk extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    String defaultUnit = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.preference_default_unit), getString(R.string.change_default_unit));

    Utils.showPercent = defaultUnit.equals("%");
  }
}
