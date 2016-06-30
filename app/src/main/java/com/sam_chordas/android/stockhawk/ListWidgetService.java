package com.sam_chordas.android.stockhawk;

import android.content.Intent;
import android.widget.RemoteViewsService;
/**
 * Created by curos on 29/6/16.
 */
public class ListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListWidgetViewFactory(getBaseContext());
    }

}
