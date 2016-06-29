package com.sam_chordas.android.stockhawk;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by curos on 29/6/16.
 */
public class ListWidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListWidgetViewFactory(getBaseContext());
    }

}
