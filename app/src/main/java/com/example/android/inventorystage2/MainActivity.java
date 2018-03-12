package com.example.android.inventorystage2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.content.ContentUris;
import android.content.Intent;

import android.database.Cursor;
import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventorystage2.data.ItemContract.ItemEntry;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private int BARCODE_REQUEST_CODE = 1;
    private String scannedBarcode = "";
    private int CURSOR_LOADER_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Start the items Fragment to show the list
        setFragmentForOpen(new ItemsFragment());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar list_item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_insert_dummy_data) {
            insertItem();
            return true;
        }
        if (id == R.id.action_delete) {
            int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
            Toast.makeText(MainActivity.this, rowsDeleted + getString(R.string.rows_deleted),Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_item_list) {
            setFragmentForOpen(new ItemsFragment());
        } else if (id == R.id.nav_suppliers_list) {
            //to be implemented on next version
        } else if (id == R.id.nav_scan) {
            Intent intent = new Intent(MainActivity.this, BarcodeActivity.class);
            startActivityForResult(intent, BARCODE_REQUEST_CODE);

        } else if (id == R.id.nav_totals) {
            setFragmentForOpen(new TotalsFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setFragmentForOpen(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    //On barcode scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == resultCode) {
            scannedBarcode = data.getExtras().getString(getString(R.string.barcode_key_code));
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.initLoader(CURSOR_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_BARCODE
        };
        String selection = ItemEntry.COLUMN_ITEM_BARCODE + "=?";
        String[] selectionArgs = new String[]{scannedBarcode};


        return new CursorLoader(this,
                ItemEntry.CONTENT_URI,
                projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int idColumnIndex = data.getColumnIndex(ItemEntry._ID);
            int currentId = data.getInt(idColumnIndex);
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            intent.setData(ContentUris.withAppendedId(ItemEntry.CONTENT_URI, currentId));
            startActivity(intent);
        } else
            Toast.makeText(this, R.string.barcode_not_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void insertItem() {


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
        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
    }

    //remove keyboard on touch
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}
