package com.bytes18.example.salesplus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BillActivity extends AppCompatActivity {

    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;
    public ArrayList<BillItemList> billItemLists = new ArrayList<>();
    public static ArrayList<Integer> stockArray = new ArrayList<>();
    public TextView totalAmountTV;
    public TextView totalQuantityTV;
    public static String toastText = "Bill Deleted!";
    private EditText customerName;
    private EditText customerContact;
    private EditText customerEmail;
    private ArrayList<ArrayList<String>> rowsListSpreadsheet;
    AlertDialog.Builder dialogBuilder;
    String url = "https://sheets.googleapis.com/v4/spreadsheets/1SUx5uyVlO07xmZY88WJGECV3Jb2g40eJ_Qt3XcK0EeI/values/Sheet1!A2:E10000?key=AIzaSyBaFMAkHIWZmYZjECPnKRN9l9IaYPHASw8";
    SQLiteDatabase DataBase;


    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    //BUTTON
    public void addItem(View view) {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
    }

    public void executeBill(View view) {
        rowsListSpreadsheet = new ArrayList<>();

        Cursor cursor = DataBase.rawQuery("SELECT * FROM History", null);
        int rowCount = cursor.getCount();

        if (rowCount > 0) {
            int nameIndex = cursor.getColumnIndex("name");
            int barcodeIndex = cursor.getColumnIndex("barcode");
            int quantityIndex = cursor.getColumnIndex("quantity");
            int timestampIndex = cursor.getColumnIndex("timestamp");
            int customerNameIndex = cursor.getColumnIndex("customerName");
            int customerContactIndex = cursor.getColumnIndex("customerContact");
            int customerEmailIndex = cursor.getColumnIndex("customerEmail");
            int billNumber = cursor.getColumnIndex("billNumber");

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                ArrayList<String> rowSpreadsheet = new ArrayList<>();
                rowSpreadsheet.add(cursor.getString(nameIndex)); // name
                rowSpreadsheet.add(cursor.getString(barcodeIndex));// barcode
                rowSpreadsheet.add(cursor.getString(quantityIndex));// quantity
                rowSpreadsheet.add(cursor.getString(timestampIndex));// timestamp
                rowSpreadsheet.add(cursor.getString(customerNameIndex));// customer name
                rowSpreadsheet.add(cursor.getString(customerContactIndex));// customer contact
                rowSpreadsheet.add(cursor.getString(customerEmailIndex));// customer email
                rowSpreadsheet.add(cursor.getString(billNumber));// bill number

                rowsListSpreadsheet.add(rowSpreadsheet);

                cursor.moveToNext();
            }

            DataBase.execSQL("DELETE FROM History");

        }

        // for timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yy");
        Date date = new Date();

        // for shared preferences
        // get billNumber
        SharedPreferences sharedPreferences = getSharedPreferences("PlusAppPreferences", MODE_PRIVATE);
        int billNumber = sharedPreferences.getInt("billNumber", 0);

        int i = 0;

        for (BillItemList row : billItemLists) {
            while (i < 1) {

                billNumber += 1;
                // update billNumber
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("billNumber", billNumber);
                editor.apply();

                i++;
            }
            ArrayList<String> rowSpreadsheet = new ArrayList<>();
            rowSpreadsheet.add(row.getItemName()); // name
            rowSpreadsheet.add(row.getItemBarcode());// barcode
            rowSpreadsheet.add(row.getItemQuantity());// quantity
            rowSpreadsheet.add(formatter.format(date));// timestamp
            rowSpreadsheet.add(customerName.getText().toString());// customer name
            rowSpreadsheet.add(customerContact.getText().toString());// customer contact
            rowSpreadsheet.add(customerEmail.getText().toString());// customer email
            rowSpreadsheet.add(Integer.toString(billNumber));// bill number

            rowsListSpreadsheet.add(rowSpreadsheet);
        }

        if (isNetworkAvailable(this)) {
            new InsertDataActivity().execute();
            new JsonTask().execute(url);
            toastText = "Bill Executed!";
        } else {
            for (ArrayList<String> row : rowsListSpreadsheet) {
                //History Database order: billNumber VARCHAR, name VARCHAR, barcode VARCHAR, quantity VARCHAR, timestamp VARCHAR, customerName VARCHAR, cutomerContact VARCHAR, customerEmail VARCHAR
                DataBase.execSQL("INSERT INTO History (billNumber, name, barcode, quantity, timestamp, customerName, customerContact, customerEmail) VALUES ('"+row.get(7)+"', '"+row.get(0)+"', '"+row.get(1)+"', '"+row.get(2)+"', '"+row.get(3)+"', '"+row.get(4)+"', '" + row.get(5)+"', '"+row.get(6)+"')");
            }
            toastText = "Bill Executed!";

            dialogBuilder = new AlertDialog.Builder(this);

            dialogBuilder.setMessage("Please connect to Internet\nData not updated in the Server!")
                    .setTitle("Offline")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            if (billItemLists.size() > 0) {
                                CustomDialogClass cdd = new CustomDialogClass(BillActivity.this, billItemLists, rowsListSpreadsheet.get(rowsListSpreadsheet.size() - 1).get(7), customerName.getText().toString(), totalAmountTV.getText().toString(), totalQuantityTV.getText().toString());
                                cdd.show();
                            } else {
                                toastText = "No Internet!";
                                finish();
                            }
                        }
                    });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
        cursor.close();

    }

    public void updateTotal(){
        int totalAmount = 0;
        int totalQuantity = 0;

        for (BillItemList itemRow : billItemLists) {
            totalAmount += Integer.parseInt(itemRow.getItemAmount());
            totalQuantity += Integer.parseInt(itemRow.getItemQuantity());
        }
        totalAmountTV.setText(String.valueOf(totalAmount));
        totalQuantityTV.setText(String.valueOf(totalQuantity));
    }

    //UPDATE LIST VIEW
    public void updateListView() {

        ListView billItemListview = findViewById(R.id.itemListView);

        BillItemListAdapter billItemListAdapter = new BillItemListAdapter(this, R.layout.item_list_custom_layout, billItemLists);

        billItemListview.setAdapter(billItemListAdapter);

    }

    public void updateBillItemListView(String barcode) {

        // DataBase order: Stocklist (barcode VARCHAR, name VARCHAR, codename VARCHAR, price VARCHAR, stock VARCHAR)
        Cursor cursor = DataBase.rawQuery("SELECT * FROM Stocklist WHERE barcode LIKE '%"+barcode+"'", null);
        int nameIndex = cursor.getColumnIndex("name");
        int codenameIndex = cursor.getColumnIndex("codename");
        int priceIndex = cursor.getColumnIndex("price");
        int stockIndex = cursor.getColumnIndex("stock");

        String name;
        String codename;
        int price;
        int stock;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            name = cursor.getString(nameIndex);
            codename = cursor.getString(codenameIndex);
            price = Integer.parseInt(cursor.getString(priceIndex));
            stock = Integer.parseInt(cursor.getString(stockIndex));

            stockArray.add(stock);

            BillItemList billItemList = new BillItemList(name, barcode, codename, Integer.toString(price), Integer.toString(price), "1");
            billItemLists.add(billItemList);

            updateTotal();

            cursor.moveToNext();
        }

        cursor.close();

        updateListView();
    }


    public void updateStocklist(String dataJSONString) throws JSONException {

        JSONObject object = new JSONObject(dataJSONString);
        String valuesStringFormatted = object.getString("values").replaceAll("[\\[]+", "");
        valuesStringFormatted = valuesStringFormatted.replaceAll("]]", "");

        String[] rowStringArray = valuesStringFormatted.split("],");
        String[] cellValues;

        DataBase.execSQL("DELETE FROM Stocklist");

        for (String row : rowStringArray) {
            cellValues = row.split(",");
            // Cell Values in order: Barcode, Name, Codename, Price, Stock;

            // FOR UPDATING DATABASE
            // DataBase order: Stocklist (barcode VARCHAR, name VARCHAR, codename VARCHAR, price VARCHAR, stock VARCHAR)
            DataBase.execSQL("INSERT INTO Stocklist (barcode, name, codename, price, stock ) VALUES (" + cellValues[0] + ", " + cellValues[1] + ", " + cellValues[2] + ", " + cellValues[3] + ", " + cellValues[4] + ")");
        }

        if (billItemLists.size() > 0) {
            CustomDialogClass cdd = new CustomDialogClass(this, billItemLists, rowsListSpreadsheet.get(rowsListSpreadsheet.size() - 1).get(7), customerName.getText().toString(), totalAmountTV.getText().toString(), totalQuantityTV.getText().toString());
            cdd.show();
        } else {
            toastText = "Stock Sheet Online Updated!";
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        totalAmountTV = findViewById(R.id.totalAmountBill);
        totalQuantityTV = findViewById(R.id.totalQuantityBill);

        customerName = findViewById(R.id.editTextCustomerName);
        customerContact = findViewById(R.id.editTextCustomerContact);
        customerEmail = findViewById(R.id.editTextCustomerEmail);

        DataBase = openOrCreateDatabase("DataBase", Context.MODE_PRIVATE, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String barcode = data.getStringExtra("barcodeScanned");

                updateBillItemListView(barcode);

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        billItemLists = new ArrayList<>();
        stockArray = new ArrayList<>();
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        toastText = "Bill Deleted!";
    }

    class InsertDataActivity extends AsyncTask< Void, Void, Void > {

        ProgressDialog pd;

        String result = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(BillActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... voids) {
            for (ArrayList<String> row : rowsListSpreadsheet) {
                // row index order: name, barcode, quantity, timestamp, customerName, customerContact, customerEmail, billNumber: All in String
                // insertData: args order: date, time, id, name, quantity, customerName, customerContact, customerEmail, billNumber: all in String
                JSONObject jsonObject = SpreadsheetController.insertData(row.get(3).substring(9), row.get(3).substring(0, 9), row.get(1), row.get(0), row.get(2), row.get(4), row.get(5), row.get(6), row.get(7));

                try {
                    if (jsonObject != null) {
                        result = jsonObject.getString("result");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
        }
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    Log.e("RESULT", result);
                    updateStocklist(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}