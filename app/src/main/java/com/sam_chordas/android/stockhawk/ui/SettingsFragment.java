package com.sam_chordas.android.stockhawk.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHawk;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by curos on 20/5/16.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

  protected SettingsActivity activity;
  protected SharedPreferences preferences;
  protected StockHawk application;

  @Override
  public void onResume() {
    super.onStart();
    preferences.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onStop();
    preferences.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference_screen);
    activity = (SettingsActivity) getActivity();
    preferences = activity.preferences;
    application = (StockHawk) getActivity().getApplication();

    findPreference(getString(R.string.preference_reset_stocks)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.preference_reset_stocks_confirmation)
                .cancelable(true)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.cancel)
                .negativeColor(getResources().getColor(R.color.material_green_700))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                  @Override
                  public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    application.setDefaultStocks();
                  }
                })
                .show();
        return false;
      }
    });

    setChangeUnitSummary();
  }
  protected void setChangeUnitSummary() {
    ListPreference changeUnitPreference = (ListPreference) findPreference(getString(R.string.preference_default_unit));
    String changeUnit = preferences.getString(getString(R.string.preference_default_unit), getString(R.string.change_default_unit));
    switch (changeUnit) {
      case "%":
        changeUnitPreference.setSummary(R.string.preference_default_unit_summary_percent);
        break;
      case "$":
        changeUnitPreference.setSummary(R.string.preference_default_unit_summary_dollar);
        break;
    }
    // Update Utils class's static member also
    Utils.showPercent = changeUnit.equals("%");
    Log.d(SettingsFragment.class.toString(), changeUnit);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    setChangeUnitSummary();
  }
}
