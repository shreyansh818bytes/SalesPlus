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
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase myDatabase;
    AlertDialog.Builder dialogBuilder;
    ArrayList<ArrayList<String>> rowsSpreadsheetList;
    String url = "https://sheets.googleapis.com/v4/spreadsheets/1SUx5uyVlO07xmZY88WJGECV3Jb2g40eJ_Qt3XcK0EeI/values/Sheet1!A2:E10000?key=AIzaSyBaFMAkHIWZmYZjECPnKRN9l9IaYPHASw8";
    String url2 = "https://sheets.googleapis.com/v4/spreadsheets/1SUx5uyVlO07xmZY88WJGECV3Jb2g40eJ_Qt3XcK0EeI/values/Sheet2!A2:A?key=AIzaSyBaFMAkHIWZmYZjECPnKRN9l9IaYPHASw8";
    public int SHEET_NUMBER = 0;
    public int BillNumber = 0;
    View parentView;
    SharedPreferences sharedPreferences;


    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    //BUTTONS
    public void createNewSale(View view) {
        startActivity(new Intent(this, BillActivity.class));
    }

    public void showStocklist(View view) {
        Intent intent = new Intent(this, StocklistActivity.class);
        startActivity(intent);
    }


    public void updateStocklist(String dataJSONString) throws JSONException {

        JSONObject object = new JSONObject(dataJSONString);
        String valuesStringFormatted = object.getString("values").replaceAll("[\\[]+", "");
        valuesStringFormatted = valuesStringFormatted.replaceAll("]]", "");

        String[] rowStringArray = valuesStringFormatted.split("],");
        String[] cellValues;

        myDatabase.execSQL("DELETE FROM Stocklist");

        for (String row : rowStringArray) {
            cellValues = row.split(",");
            // Cell Values in order: Barcode, Name, Codename, Price, Stock;

            // FOR UPDATING DATABASE
            // DataBase order: Stocklist (barcode VARCHAR, name VARCHAR, codename VARCHAR, price VARCHAR, stock VARCHAR)
            myDatabase.execSQL("INSERT INTO Stocklist (barcode, name, codename, price, stock ) VALUES (" + cellValues[0] + ", " + cellValues[1] + ", " + cellValues[2] + ", " + cellValues[3] + ", " + cellValues[4] + ")");
        }

        myDatabase.execSQL("DELETE FROM History");
    }

    private void updateSpreadsheet() {

        Cursor cursor = myDatabase.rawQuery("SELECT * FROM History", null);
        int rowCount = cursor.getCount();
        if (rowCount > 0) {

            if (isNetworkAvailable(this)) {

                int nameIndex = cursor.getColumnIndex("name");
                int barcodeIndex = cursor.getColumnIndex("barcode");
                int quantityIndex = cursor.getColumnIndex("quantity");
                int timestampIndex = cursor.getColumnIndex("timestamp");
                int customerNameIndex = cursor.getColumnIndex("customerName");
                int customerContactIndex = cursor.getColumnIndex("customerContact");
                int customerEmailIndex = cursor.getColumnIndex("customerEmail");
                int billNoIndex = cursor.getColumnIndex("billNumber");

                rowsSpreadsheetList = new ArrayList<>();

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
                    rowSpreadsheet.add(cursor.getString(billNoIndex));// bill number

                    rowsSpreadsheetList.add(rowSpreadsheet);

                    cursor.moveToNext();
                }

                SHEET_NUMBER = 1;
                new InsertDataActivity().execute();
                new JsonTask().execute(url);

            } else {

                dialogBuilder = new AlertDialog.Builder(this);

                dialogBuilder.setMessage("Please connect to Internet\nData not updated!")
                        .setTitle("Offline")
                        .setCancelable(true)
                        .setPositiveButton("Continue Without Internet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();

                                View parentLayout = findViewById(android.R.id.content);
                                Snackbar snackbar = Snackbar.make(parentLayout, "No Internet Connection!", Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        }

        cursor.close();
    }


    private void getBillNumber(String jsonString) throws JSONException {

        JSONObject object = new JSONObject(jsonString);
        String valuesStringFormatted = object.getString("values").replaceAll("[\\[]+", "");
        valuesStringFormatted = valuesStringFormatted.replaceAll("]]", "");
        valuesStringFormatted = valuesStringFormatted.replaceAll("\"", "");

        String[] billNumbersStringArray = valuesStringFormatted.split("],");
        BillNumber = Integer.parseInt(billNumbersStringArray[billNumbersStringArray.length-1]);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("billNumber", BillNumber);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentView = findViewById(android.R.id.content);

        try {
            // Creating or Opening a sqlite database file
            myDatabase = openOrCreateDatabase("DataBase", MODE_PRIVATE, null);

            // Create Stocklist Table
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Stocklist (barcode VARCHAR, name VARCHAR, codename VARCHAR, price VARCHAR, stock VARCHAR)");
            // this database is to store the stocklist locally so as to find the item with barcode without internet and fast...
            /* it is updated on starting the StocklistActivity on pressing the refresh button in StocklistActivity and after
               updating the spreadsheet on bill execution. */

            // Create History Table
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS History (billNumber VARCHAR, name VARCHAR, barcode VARCHAR, quantity VARCHAR, timestamp VARCHAR, customerName VARCHAR, customerContact VARCHAR, customerEmail VARCHAR)");
            // this database will be used to update the history spreadsheet and once updated, delete all the rows...

        } catch(Exception e) {
            e.printStackTrace();
        }

        updateSpreadsheet();

        // To store Bill No.in shared preferences:
        sharedPreferences = getSharedPreferences("PlusAppPreferences", Context.MODE_PRIVATE);
        SHEET_NUMBER = 2;
        new JsonTask().execute(url2);
    }


    class InsertDataActivity extends AsyncTask< Void, Void, Void > {

        ProgressDialog pd;

        String result = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... voids) {
            for (ArrayList<String> row : rowsSpreadsheetList) {
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
                    if (SHEET_NUMBER == 1) {
                        updateStocklist(result);
                    } else if ( SHEET_NUMBER == 2) {
                        getBillNumber(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}