package com.bytes18.example.salesplus;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DialogtemListAdapter extends ArrayAdapter<BillItemList> {

    private Context mContext;
    int mResource;

    public DialogtemListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BillItemList> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // get the item's variables
        String name = getItem(position).getItemName();
        String price = getItem(position).getItemPrice();
        String amount = getItem(position).getItemAmount();
        String quantity = getItem(position).getItemQuantity();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvItemCodename = convertView.findViewById(R.id.itemNameDialog);
        TextView tvItemPrice = convertView.findViewById(R.id.itemPriceDialog);
        TextView tvItemAmount = convertView.findViewById(R.id.itemAmountDialog);
        TextView tvItemQuantity = convertView.findViewById(R.id.itemQuantityDialog);

        tvItemCodename.setText(name);
        tvItemPrice.setText(price);
        tvItemAmount.setText(amount);
        tvItemQuantity.setText(quantity);

        return convertView;
    }
}
