package com.example.android.inventorystage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventorystage2.data.ItemContract.ItemEntry;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find fields to populate in inflated template
        TextView itemName = view.findViewById(R.id.list_item_name);
        TextView itemPrice = view.findViewById(R.id.list_item_price);
        TextView itemQuantity = view.findViewById(R.id.list_item_quantity);
        ImageView itemImage = view.findViewById(R.id.list_item_image);
        // Extract properties from cursor
        // Figure out the index of each column
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        final String name = cursor.getString(nameColumnIndex);
        Double price = cursor.getDouble(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int id = cursor.getInt(idColumnIndex);
        if(showImages(context)){
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE_URI);
            String imageUri = cursor.getString(imageColumnIndex);
            if(!TextUtils.isEmpty(imageUri))
                itemImage.setImageURI(Uri.parse(imageUri));
            itemImage.setVisibility(View.VISIBLE);
        }
        else
            itemImage.setVisibility(View.GONE);

        if (price == 0 || price == null)
            price = 0.0;
        // Populate fields with extracted properties
        itemName.setText(name);
        //Get local currency
        Currency currency = Currency.getInstance(Locale.getDefault());
        itemPrice.setText(currency.getSymbol() + " " + formatDouble(price));
        itemQuantity.setText(formatDouble(quantity));

        //Create content values
        final ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, name);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, price);

        Button btnSale = view.findViewById(R.id.list_item_sale_button);
        btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 0) {
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity - 1);
                    updateQuantity(context, values, id);
                }
            }
        });

    }

    private boolean showImages(Context context){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(
                "show_items_image",
                false);
    }

    private void updateQuantity(Context context, ContentValues values, int id) {
        int nrRow = context.getContentResolver().update(ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id),
                values,
                null,
                null);
    }

    String formatDouble(double num) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(num);
    }

}
