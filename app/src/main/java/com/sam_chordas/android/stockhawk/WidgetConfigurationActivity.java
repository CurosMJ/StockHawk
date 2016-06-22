package com.sam_chordas.android.stockhawk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.widget.ListView;

import java.util.List;

public class WidgetConfigurationActivity extends AppCompatActivity {

  private ListView stocksListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_widget_configuration);

    getSupportActionBar().setTitle(R.string.widget_configuration_title);

    stocksListView = (ListView) findViewById(R.id.stocksList);
  }
}
