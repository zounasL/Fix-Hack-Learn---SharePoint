package com.example.customrendering;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class NewsReading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_reading);

        String content = getIntent().getStringExtra("canvas");

        // Document doc = Jsoup.parse();
        TextView text = findViewById(R.id.textContent);
        text.setText(Html.fromHtml(content));
    }
}