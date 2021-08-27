package com.example.hackhaton_multiple_client_id;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NewsWebViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_webview);

        Button closeButton = (Button) findViewById(R.id.closeNewsWebViewButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeNewsView();
            }
        });

        WebView webView = (WebView) findViewById(R.id.newsWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        NewsWebview newsWebViewClient = new NewsWebview(this);
        webView.setWebViewClient(newsWebViewClient);
        webView.loadUrl(getIntent().getStringExtra("NEWS_URL"));
    }

    private void closeNewsView() {
        finish();
    }
}