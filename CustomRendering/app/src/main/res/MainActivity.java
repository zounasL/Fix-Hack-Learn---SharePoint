package com.example.hackhaton_multiple_client_id;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = (Button) findViewById(R.id.loginBtn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginWebView(false);
            }
        });
    }

    private void openLoginWebView(boolean isMsAuth) {
        Intent intent = new Intent(this, OauthActivity.class);
        startActivityForResult(intent, 1);
    }

    private void openNewsView(Intent intent) {
        startActivity(intent);
        finish();
    }

    public void save(String name, String tokens){
        SharedPreferences sharedPref = getSharedPreferences("secrets", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name, tokens);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String tokens = data.getStringExtra("tokens");
                String msTokens = data.getStringExtra("msTokens");
                if(!tokens.isEmpty()) {
                    save("tokens",tokens);
                    save("msTokens", msTokens);
                    Intent intent = new Intent(this, NewsActivity.class);
                    this.openNewsView(intent);
                }
            }
            else if (resultCode == 500) {
                Log.d("Auth", "Failed to authenticate?");
            }
        }
    }

}