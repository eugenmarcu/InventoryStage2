package com.example.android.inventorystage2;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.inventorystage2.data.ItemContract;
import com.example.android.inventorystage2.data.ItemContract.ItemEntry;

public class TotalsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 3;
    private TextView itemsCountTextView;
    private TextView totalValueTextView;

    public TotalsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.activity_totals, container, false);

        itemsCountTextView = currentView.findViewById(R.id.totals_nr_items_text_view);
        totalValueTextView = currentView.findViewById(R.id.totals_value_text_view);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(CURSOR_LOADER_ID, null, this);
        return currentView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY
        };

        return new CursorLoader(getContext(),
                ItemContract.ItemEntry.CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        itemsCountTextView.setText(String.valueOf(data.getCount()));
        double total = 0;
        int priceColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        while (data.moveToNext()) {
            double price = data.getDouble(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            total += price * quantity;
        }
        totalValueTextView.setText(String.valueOf(total));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}