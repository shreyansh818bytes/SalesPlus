package com.bytes18.example.salesplus;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class StocklistItemAdapter extends ArrayAdapter<StocklistItem> {

    private static final String TAG = "ItemListAdapter";

    private Context mContext;
    int mResource;

    public StocklistItemAdapter(@NonNull Context context, int resource, @NonNull ArrayList<StocklistItem> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // get the item's variables
        String name = getItem(position).getItemName();
        String codename = getItem(position).getItemCodename();
        String price = getItem(position).getItemPrice();
        String stock = getItem(position).getItemStock();

        //Create the item object with the variables
        StocklistItem item = new StocklistItem(name, codename, price, stock);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvItemName = convertView.findViewById(R.id.itemName);
        TextView tvItemCodename = convertView.findViewById(R.id.itemCodename);
        TextView tvItemPrice = convertView.findViewById(R.id.itemPrice);
        TextView tvItemStock = convertView.findViewById(R.id.itemStock);

        tvItemName.setText(name);
        tvItemCodename.setText(codename);
        tvItemPrice.setText(price);
        tvItemStock.setText(stock);

        return convertView;
    }
}
