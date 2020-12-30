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

public class BillItemListAdapter extends ArrayAdapter<BillItemList> {

    private static final String TAG = "ItemListAdapter";

    private Context mContext;
    int mResource;
    public static BillItemList billItemList;

    public BillItemListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BillItemList> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // get the item's variables
        String name = getItem(position).getItemName();
        String barcode = getItem(position).getItemBarcode();
        String codename = getItem(position).getItemCodename();
        String price = getItem(position).getItemPrice();
        String amount = getItem(position).getItemAmount();
        String quantity = getItem(position).getItemQuantity();

        //Create the item object with the variables
        billItemList = new BillItemList(name, barcode, codename, price, amount, quantity);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvItemCodename = convertView.findViewById(R.id.itemCodenameBill);
        TextView tvItemPrice = convertView.findViewById(R.id.itemPriceBill);
        TextView tvItemAmount = convertView.findViewById(R.id.itemAmountBill);
        EditText etItemQuantity = convertView.findViewById(R.id.itemQuantityBill);
        ImageButton deleteButton = convertView.findViewById(R.id.removeItem);

        tvItemCodename.setText(codename);
        tvItemPrice.setText(price);
        tvItemAmount.setText(amount);
        etItemQuantity.setText(quantity);

        etItemQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){

                    try {
                        tvItemAmount.setText(String.valueOf(Integer.parseInt(String.valueOf(etItemQuantity.getText())) * Integer.parseInt(tvItemPrice.getText().toString())));

                        ((BillActivity) mContext).billItemLists.get(position).setItemAmount(String.valueOf(Integer.parseInt(String.valueOf(etItemQuantity.getText())) * Integer.parseInt(tvItemPrice.getText().toString())));
                        ((BillActivity) mContext).billItemLists.get(position).setItemQuantity(etItemQuantity.getText().toString());

                        ((BillActivity) mContext).updateTotal();
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                        tvItemAmount.setText("0");

                        ((BillActivity) mContext).billItemLists.get(position).setItemAmount("0");
                        ((BillActivity) mContext).billItemLists.get(position).setItemQuantity("0");

                        ((BillActivity) mContext).updateTotal();
                    }

                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((BillActivity) mContext).billItemLists.remove(position);
                ((BillActivity) mContext).updateListView();

                ((BillActivity) mContext).updateTotal();
                
                Toast.makeText(v.getContext(), "An Item Deleted!", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}
