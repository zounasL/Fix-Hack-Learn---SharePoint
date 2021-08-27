package com.example.hackhaton_multiple_client_id;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        this.refreshToken();
        // this.postRequest();
    }

    public void initNewsView(ArrayList<JSONObject> news) {
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        newsAdapter adapter = new newsAdapter(this, news);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void save(String name, String tokens){
        SharedPreferences sharedPref = getSharedPreferences("secrets", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name, tokens);
        editor.commit();
    }

    private SharedPreferences getSecrets(){
        SharedPreferences sharedPref = getSharedPreferences("secrets", MODE_PRIVATE);
       return sharedPref;
    }

    private void refreshToken() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        try {
            String clientId = this.getString(R.string.clientId);
            String tenant = this.getString(R.string.tenant);
            SharedPreferences secrerts = this.getSecrets();
            JSONObject tokens = new JSONObject(secrerts.getString("tokens", ""));

            String refreshToken = tokens.getString("refresh_token");

            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST, this.getString(R.string.microsoftonline) + "/token", new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject tokens = new JSONObject(response);
                                postRequest(tokens.getString("access_token"));
                                save("tokens", response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String body;
                            //get response body and parse with appropriate encoding
                            try {
                                body = new String(error.networkResponse.data,"UTF-8");
                                Log.d("Response", body);
                            } catch (UnsupportedEncodingException e) {
                                // exception
                            }
                        }
                    }){

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params=new HashMap<String, String>();
                    params.put("Content-Type","application/x-www-form-urlencoded");
                    return params;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("refresh_token", refreshToken);
                    params.put("client_id", clientId);
                    params.put("scope", tenant + "/.default");
                    params.put("grant_type", "refresh_token");

                    Log.d("Response", params.toString());
                    return params;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        // responseString = String.valueOf(response.statusCode);
                        responseString = new String(response.data, StandardCharsets.UTF_8);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (Exception ex) {
        }
    }

    private void postRequest(final String accessToken) {
        RequestQueue requestQueue=Volley.newRequestQueue(NewsActivity.this);
        String url= this.getString(R.string.tenant) + "/_api/search/query?querytext='(contenttypeId:0x0101009D1CB255DA76424F860D91F20E6C41180065789619A4EFB44992AF42CEEBB13C9A01* OR contenttypeId:0x0101009D1CB255DA76424F860D91F20E6C41180065789619A4EFB44992AF42CEEBB13C9A02*)'";
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //let's parse json data
                try {
                    Log.d("News", response);
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONObject("PrimaryQueryResult").getJSONObject("RelevantResults").getJSONObject("Table").getJSONArray("Rows");
                    ArrayList<JSONObject> newsList = new ArrayList<JSONObject>();
                    for (int i=0; i < result.length(); i++) {

                        JSONArray cells = result.getJSONObject(i).getJSONArray("Cells");
                        JSONObject news = new JSONObject();
                        String title = "";
                        String description = "";
                        String author = "";
                        String path = "";

                        for (int j = 0; j < cells.length(); j++)
                        {
                            if(cells.getJSONObject(j).getString("Key").contentEquals("Title")) {
                                title = cells.getJSONObject(j).getString("Value");
                                if(title.length() > 20) {
                                    title = title.substring(0, 20);
                                }
                            }
                            if(cells.getJSONObject(j).getString("Key").contentEquals("Author")) {
                                author = cells.getJSONObject(j).getString("Value");
                            }
                            if(cells.getJSONObject(j).getString("Key").contentEquals("Description")) {
                                description = cells.getJSONObject(j).getString("Value");
                                if(description.length() > 50) {
                                    description = description.substring(0, 50) + "...";
                                }
                            }
                            if(cells.getJSONObject(j).getString("Key").contentEquals("Path")) {
                                path = cells.getJSONObject(j).getString("Value");
                            }
                        }

                        if(!path.isEmpty()) {
                            news.put("title", title);
                            news.put("description", description);
                            news.put("author", author);
                            news.put("path", path);

                            newsList.add(news);
                            Log.d("News", news.toString());
                        }
                    }

                    initNewsView(newsList);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Query", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params=new HashMap<String, String>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> params=new HashMap<String, String>();
                params.put("Accept","application/json");
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    /*
    public void initRecyclerView(final ArrayList<JSONObject> newsList){
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        newsAdapter adapter = new newsAdapter(this, newsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    */
}