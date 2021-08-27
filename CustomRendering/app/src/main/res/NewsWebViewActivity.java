package com.example.hackhaton_multiple_client_id;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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