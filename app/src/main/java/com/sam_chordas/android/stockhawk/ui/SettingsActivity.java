package com.sam_chordas.android.stockhawk.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;

public class SettingsActivity extends AppCompatActivity {

  protected SharedPreferences preferences;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    preferences = PreferenceManager.getDefaultSharedPreferences(this);

    getFragmentManager().beginTransaction()
            .replace(R.id.settings_fragment, new SettingsFragment())
            .commit();
  }
}
