package com.example.hackhaton_multiple_client_id;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class newsAdapter extends RecyclerView.Adapter<newsAdapter.ViewHolder>{

    private ArrayList<JSONObject> news;
    private Context mContext;

    public newsAdapter(Context context, ArrayList<JSONObject> news ) {
        this.news  = news;
        this.mContext = context;
    }

    @NonNull
    @Override
    public newsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(mContext)
                .asBitmap()
                .load(R.mipmap.news)
                .into(holder.image);

        try {
            holder.title.setText(news.get(position).getString("title"));
            holder.description.setText(news.get(position).getString("description"));
            holder.author.setText(news.get(position).getString("author"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshToken(position);
                /*
                try {
                    Intent intent = new Intent(mContext, NewsWebViewActivity.class);
                    intent.putExtra("NEWS_URL", news.get(position).getString("path"));
                    mContext.startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                */
            }
        });
    }

    @Override
    public int getItemCount() {
        return news.size();
    }


    public void save(String name, String tokens){
        SharedPreferences sharedPref = mContext.getSharedPreferences("secrets", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name, tokens);
        editor.commit();
    }

    private SharedPreferences getSecrets(){
        SharedPreferences sharedPref = mContext.getSharedPreferences("secrets", MODE_PRIVATE);
        return sharedPref;
    }

    private void refreshToken(final int position) {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        try {
            String clientId = mContext.getString(R.string.msClientId);
            String tenant = mContext.getString(R.string.tenant);
            SharedPreferences secrerts = this.getSecrets();
            JSONObject tokens = new JSONObject(secrerts.getString("msTokens", ""));

            String refreshToken = tokens.getString("refresh_token");

            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST, mContext.getString(R.string.microsoftonline) + "/token", new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject tokens = new JSONObject(response);
                                getCookie(tokens.getString("access_token"), position);
                                save("msTokens", response);
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

    private void getCookie(String accessToken, final int position) {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        try {
            Context context = mContext;
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST, mContext.getString(R.string.tenant) + "/_api/SP.OAuth.NativeClient/Authenticate", new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                Intent intent = new Intent(mContext, NewsWebViewActivity.class);
                                intent.putExtra("NEWS_URL", news.get(position).getString("path"));
                                mContext.startActivity(intent);
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
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params=new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + accessToken);
                    return params;
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        String cookie = response.headers.get("Set-Cookie");
                        String cookieString = "Cookie="+ cookie +"; path=/";
                        CookieManager.getInstance().setCookie(context.getString(R.string.tenant), cookieString);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (Exception ex) {
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;
        TextView description;
        TextView author;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            author = itemView.findViewById(R.id.author);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
