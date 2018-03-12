package com.example.android.inventorystage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.android.inventorystage2.data.ItemDbHelper;
import com.example.android.inventorystage2.data.ItemContract.ItemEntry;


public class ItemsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    private ItemDbHelper mDbHelper;
    private ItemCursorAdapter mCursorAdapter;
    private int CURSOR_LOADER_ID = 1;
    private LoaderManager loaderManager;
    private String filterString;

    public ItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.activity_items, container, false);
        final Context context = getContext();

        SearchView searchView = currentView.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = currentView.findViewById(R.id.fab_add_item);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditorActivity.class);
                startActivity(intent);
            }
        });
        mDbHelper = new ItemDbHelper(context);

        ListView list = currentView.findViewById(R.id.list_view);

        mCursorAdapter = new ItemCursorAdapter(context, null);
        list.setAdapter(mCursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                adapterView.getItemAtPosition(position);
                Intent intent = new Intent(context, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        loaderManager = getLoaderManager();
        loaderManager.initLoader(CURSOR_LOADER_ID, null, this);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = currentView.findViewById(R.id.empty_view);
        list.setEmptyView(emptyView);

        //Set store name title if any
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String title = sharedPrefs.getString(getString(R.string.store_name_key), getString(R.string.store_name_default_value));
        getActivity().setTitle(title);

        return currentView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void deleteAllItems() {
        int rowsDeleted = getContext().getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
    }

    private void insertItem() {
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and dummy item attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, getString(R.string.dummy_item_name));
        values.put(ItemEntry.COLUMN_ITEM_PRICE, getString(R.string.dummy_item_price));
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, getString(R.string.dummy_item_quantity));

        // Insert a new row for dummy item in the database, returning the ID of that new row.
        // The first argument for db.insert() is the items table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the price for dummy item.
        Uri newUri = getContext().getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_IMAGE_URI};

        String selection = null;
        String[] selectionArgs = null;
        if(!TextUtils.isEmpty(filterString)){
            selection = ItemEntry.COLUMN_ITEM_NAME + " LIKE ?";
            selectionArgs = new String[]{"%" + filterString + "%"};
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        Boolean orderByName = sharedPrefs.getBoolean(
                getString(R.string.order_by_name_key),
                false);
        String sortOrder;
        if(orderByName)
            sortOrder = ItemEntry.COLUMN_ITEM_NAME;
        else
            sortOrder = null;


        return new CursorLoader(getContext(),
                ItemEntry.CONTENT_URI,
                projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterString = newText;
        loaderManager.restartLoader(CURSOR_LOADER_ID, null, this);
        return false;
    }


}