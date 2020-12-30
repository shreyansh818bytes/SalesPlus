package com.bytes18.example.salesplus;

import android.util.Log;

import androidx.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SpreadsheetController {
    public static final String TAG = "TAG";

    public static final String WAURL = "https://script.google.com/macros/s/AKfycbyw9hf6BKsCB_yN_1HnfOpliiuzvB5KZo0SuyS3a50wopWT_R8/exec?";

    private static Response response;

    public static JSONObject insertData(String date, String time, String id, String name, String quantity, String customerName, String customerContact, String customerEmail, String billNumber) {
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(WAURL+"action=insert&date="+date+"&time="+time+"&id="+id+"&name="+name+"&quantity="+quantity+"&customerName="+customerName+"&customerContact="+customerContact+"&customerEmail="+customerEmail+"&billNumber="+billNumber)
                    .build();
            response = client.newCall(request).execute();
            Log.e(TAG, "response from gs"+response.body().string());
            return new JSONObject(response.body().string());
        } catch (@NonNull IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
