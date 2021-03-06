package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.FetchHistoricalData;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHawk;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

import java.util.HashSet;
import java.util.Set;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, RecyclerViewItemClickListener.OnItemClickListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    boolean isConnected;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noStocks;
    private RecyclerView stocks;
    private SharedPreferences preferences;
    private BroadcastReceiver toastBroadcastReceiver;
    private BroadcastReceiver serviceResultBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private boolean forConfiguration = false;
    private Integer widgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_my_stocks);

        if (getIntent().getAction().equals(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)) {
            forConfiguration = true;
            findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
            widgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            setResult(RESULT_CANCELED);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.accent,
                R.color.material_indigo_700,
                R.color.material_blue_500);

        noStocks = (TextView) findViewById(R.id.no_stocks);
        stocks = (RecyclerView) findViewById(R.id.recycler_view);
        stocks.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        stocks.addOnItemTouchListener(new RecyclerViewItemClickListener(this, this));
        stocks.setAdapter(mCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (forConfiguration) {
            fab.setVisibility(View.GONE);
            mTitle = getString(R.string.widget_configuration_title);
        } else {
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(stocks);
            mTitle = getTitle();
        }
        fab.attachToRecyclerView(stocks);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    createAddStockDialog();
                } else {
                    networkToast();
                }

            }
        });

        if (isConnected) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }

    protected MaterialDialog createAddStockDialog()
    {
        return new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                .content(R.string.content_test)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // On FAB click, receive user input. Make sure the stock doesn't already exist
                        // in the DB and proceed accordingly
                        Set<String> stocks = preferences.getStringSet("stocks", new HashSet<String>());
                        if (stocks.contains(input.toString().toUpperCase())) {
                            Toast toast =
                                    Toast.makeText(MyStocksActivity.this, getString(R.string.already_exists),
                                            Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                            toast.show();
                        } else {
                            if (input.toString().length() > 0) {
                                // Add the stock to DB
                                mServiceIntent.putExtra("tag", "add");
                                mServiceIntent.putExtra("symbol", input.toString().toUpperCase());
                                startService(mServiceIntent);
                            }
                        }
                    }
                })
                .show();
    }

    public void handleCancel(View v) {
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
        if (toastBroadcastReceiver == null) {
            toastBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(mContext, intent.getStringExtra("text"), Toast.LENGTH_SHORT).show();
                }
            };
        }
        if (serviceResultBroadcastReceiver == null) {
            serviceResultBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    refreshComplete();
                }
            };
        }
        localBroadcastManager.registerReceiver(toastBroadcastReceiver, new IntentFilter(StockIntentService.TOAST));
        localBroadcastManager.registerReceiver(serviceResultBroadcastReceiver, new IntentFilter(StockIntentService.SERVICE_RESULT));
    }

    public void refreshComplete() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void refreshData() {
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        // Run the initialize task service every time we resume
        mServiceIntent.putExtra("tag", "init");
        if (isConnected) {
            startService(mServiceIntent);
        } else {
            networkToast();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        localBroadcastManager.unregisterReceiver(toastBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(serviceResultBroadcastReceiver);
    }

    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!forConfiguration) {
            getMenuInflater().inflate(R.menu.my_stocks, menu);
        }
        restoreActionBar();
        if (!forConfiguration) {
            setChangeUnitsTitle(menu.findItem(R.id.action_change_units));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
            setChangeUnitsTitle(item);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setChangeUnitsTitle(MenuItem item) {
        // Show a % icon when showPercent is false
        // Show a $ icon when showPercent is true
        if (Utils.showPercent) {
            item.setIcon(R.drawable.ic_attach_money_white_24dp);
        } else {
            item.setIcon(R.drawable.ic_money_off_white_24dp);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.NAME, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;

        if (mCursor.getCount() == 0) {
            noStocks.setVisibility(View.VISIBLE);
            stocks.setVisibility(View.GONE);
        } else {
            noStocks.setVisibility(View.GONE);
            stocks.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    // Handle clicks on stocks
    @Override
    public void onItemClick(View v, int position) {
        mCursor.moveToPosition(position);
        if (forConfiguration) {
            preferences.edit()
                    .putString(StockHawk.widgetStockPreferenceKey(widgetId), mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)))
                    .apply();

            Intent intent = new Intent(StockTaskService.STOCKS_UPDATE);
            sendBroadcast(intent);

            Intent configSuccess = new Intent();
            configSuccess.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setResult(RESULT_OK, configSuccess);
            finish();
        } else {
            Intent intent = new Intent(this, StockDetailsActivity.class);
            intent.putExtra("symbol", mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
            startActivity(intent);
        }
    }
}
