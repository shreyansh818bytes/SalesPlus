package com.bytes18.example.salesplus;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

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

public class StocklistActivity extends AppCompatActivity {

    ProgressDialog pd;
    String url = "https://sheets.googleapis.com/v4/spreadsheets/1SUx5uyVlO07xmZY88WJGECV3Jb2g40eJ_Qt3XcK0EeI/values/Sheet1!A2:E10000?key=AIzaSyBaFMAkHIWZmYZjECPnKRN9l9IaYPHASw8";
    public static ArrayList<StocklistItem> stocklistItems;
    AlertDialog.Builder dialogBuilder;
    View parentLayout;
    SQLiteDatabase DataBase;
    String searchByColumn;

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void updateStocklistView() {


        ListView stocklistView = findViewById(R.id.stocklistView);

        StocklistItemAdapter stocklistItemAdapter = new StocklistItemAdapter(this, R.layout.stocklist_item_layout, stocklistItems);

        stocklistView.setAdapter(stocklistItemAdapter);
    }

    public void updateStocklist(String dataJSONString) throws JSONException {

        JSONObject object = new JSONObject(dataJSONString);
        String valuesStringFormatted = object.getString("values").replaceAll("[\\[]+", "");
        valuesStringFormatted = valuesStringFormatted.replaceAll("]]", "");

        String[] rowStringArray = valuesStringFormatted.split("],");
        String[] cellValues;

        stocklistItems = new ArrayList<>();
        DataBase.execSQL("DELETE FROM Stocklist");

        for (String row: rowStringArray) {
            cellValues = row.split(",");
            // Cell Values in order: Barcode, Name, Codename, Price, Stock;

            StocklistItem stocklistItem = new StocklistItem(cellValues[1].replaceAll("\"", ""), cellValues[2].replaceAll("\"", ""),
                    cellValues[3].replaceAll("\"", ""), cellValues[4].replaceAll("\"", ""));
            stocklistItems.add(stocklistItem);

            // FOR UPDATING DATABASE
            // DataBase order: Stocklist (barcode VARCHAR, name VARCHAR, codename VARCHAR, price VARCHAR, stock VARCHAR)
            DataBase.execSQL("INSERT INTO Stocklist (barcode, name, codename, price, stock ) VALUES ("+cellValues[0]+", "+cellValues[1]+", "+cellValues[2]+", "+cellValues[3]+", "+cellValues[4]+")");
        }

        updateStocklistView();

    }

    private void getDatabaseStocklist(String columnName, String itemName){
        stocklistItems = new ArrayList<>();

        Cursor cursor = DataBase.rawQuery("SELECT * FROM Stocklist WHERE "+columnName+" LIKE '%"+itemName+"%'", null);
        int nameIndex = cursor.getColumnIndex("name");
        int codenameIndex = cursor.getColumnIndex("codename");
        int priceIndex = cursor.getColumnIndex("price");
        int stockIndex = cursor.getColumnIndex("stock");

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            StocklistItem stocklistItem = new StocklistItem(cursor.getString(nameIndex), cursor.getString(codenameIndex), cursor.getString(priceIndex),
                    cursor.getString(stockIndex));
            stocklistItems.add(stocklistItem);

            cursor.moveToNext();
        }

        cursor.close();

        updateStocklistView();
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(StocklistActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

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
                    updateStocklist(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Snackbar snackbar = Snackbar.make(parentLayout, "No Data returned from Google Spreadsheet!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            if (pd.isShowing()){
                pd.dismiss();
            }
        }
    }

    public void refresh(View view) {
        new JsonTask().execute(url);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocklist);

        parentLayout = findViewById(android.R.id.content);

        DataBase = openOrCreateDatabase("DataBase", Context.MODE_PRIVATE, null);

        dialogBuilder = new AlertDialog.Builder(this);

        if(isNetworkAvailable(StocklistActivity.this)) {
            new JsonTask().execute(url);
        } else {
            dialogBuilder.setMessage("Showing the Last Updated Stock List.\nPlease connect to Internet for Updated Stock List")
                .setTitle("Offline")
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        Snackbar snackbar = Snackbar.make(parentLayout, "No Internet Connection!", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });

            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
            getDatabaseStocklist("", "");
        }

        Spinner spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(StocklistActivity.this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.spinnerList));
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        searchByColumn = "name";
                        break;
                    case 1:
                        searchByColumn = "price";
                        break;
                    case 2:
                        searchByColumn = "codename";
                        break;
                    case 3:
                        searchByColumn = "stock";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getDatabaseStocklist(searchByColumn, newText);
                return true;
            }
        });
    }
}