package com.bytes18.example.salesplus;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomDialogClass extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button done;
    public ArrayList<BillItemList> itemLists;
    public String  billNumber;
    public String customerName;
    public String totalAmount;
    public String totalQuantity;

    public CustomDialogClass(Activity a, ArrayList<BillItemList> itemLists, String billNumber, String customerName, String totalAmount, String totalQuantity) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.itemLists = itemLists;
        this.billNumber = billNumber;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.totalQuantity = totalQuantity;
        this.setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        done = (Button) findViewById(R.id.btn_done);
        done.setOnClickListener(this);

        ListView DialogListview = findViewById(R.id.dialogListView);

        DialogtemListAdapter dialogtemListAdapter = new DialogtemListAdapter(this.c, R.layout.custom_dialog_list_layout, this.itemLists);

        DialogListview.setAdapter(dialogtemListAdapter);

        TextView tvBillNumber = findViewById(R.id.billNumber);
        tvBillNumber.setText(tvBillNumber.getText().toString() + this.billNumber);

        TextView tvCustomerName = findViewById(R.id.customerName);
        tvCustomerName.setText(this.customerName);

        TextView tvTotalAmount = findViewById(R.id.totalAmount);
        tvTotalAmount.setText("\u20B9" + this.totalAmount);

        TextView tvTotalQuantity = findViewById(R.id.totalQty);
        tvTotalQuantity.setText(this.totalQuantity);
    }

    @Override
    public void onClick(View v) {
        c.finish();
        dismiss();

        // write code to save bill Image
    }

}