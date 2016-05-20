package com.sam_chordas.android.stockhawk.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Created by curos on 20/5/16.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

  protected SettingsActivity activity;
  protected SharedPreferences preferences;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference_screen);
    activity = (SettingsActivity) getActivity();
    preferences = activity.preferences;

    preferences.registerOnSharedPreferenceChangeListener(this);

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
