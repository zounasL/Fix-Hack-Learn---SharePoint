package com.example.customrendering;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.webkit.ClientCertRequest;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationActivity extends WebViewClient {
    private Activity activity = null;
    private String tokens = "";

    public AuthenticationActivity(Activity activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("WebView", url);
        String redirectUri = this.activity.getString(R.string.redirectUri);

        if(url.contains("code=") && url.startsWith(redirectUri)) {
            String code = this.codeCollector(url);
            this.getToken(code, false, view);
        }
        return false;
    }

    /**
     * Displays client certificate request to the user.
     */
    @Override
    public void onReceivedClientCertRequest(final WebView view, final ClientCertRequest request) {
        KeyChain.choosePrivateKeyAlias(activity, new KeyChainAliasCallback() {
                    @Override
                    public void alias(String alias) {
                        if (alias == null) {
                            request.cancel();
                            return;
                        }
                        new KeyChainLookup(activity.getApplication().getApplicationContext(), request, alias).execute();
                    }
                }, request.getKeyTypes(), request.getPrincipals(), request.getHost(),
                request.getPort(), null);
    }

    public String codeCollector(String url) {
        Uri uri= Uri.parse(url);
        String code = uri.getQueryParameter("code");
        Log.d("WebView", code);
        if(code.isEmpty()) { return ""; }
        return code;
    }

    public void getToken(final String code, final Boolean isMsAuth, WebView view) {
        RequestQueue requestQueue = Volley.newRequestQueue(this.activity);
        try {
            String clientId = this.activity.getString(R.string.clientId);
            String redirectUri = this.activity.getString(R.string.redirectUri);
            String tenant = this.activity.getString(R.string.tenant);
            String url = this.activity.getString(R.string.microsoftonline);

            final String clientIdBody = clientId;
            final String redirectUriBody = redirectUri;

            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST, url + "/token", new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            tokens = response;
                            closeAuth("");
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String body;
                            //get response body and parse with appropriate encoding
                            try {
                                body = new String(error.networkResponse.data,"UTF-8");
                                Log.d("Response", body);
                                closeAuth("");
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
                    params.put("code", code);
                    params.put("client_id", clientIdBody);
                    params.put("scope", tenant + "/.default");
                    params.put("redirect_uri", redirectUriBody);
                    params.put("grant_type", "authorization_code");

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

    public void closeAuth(String token){
        if(!this.tokens.isEmpty()) {
            Intent returnIntent = new Intent(this.activity, MainActivity.class);
            returnIntent.putExtra("tokens", this.tokens);
            this.activity.setResult(Activity.RESULT_OK, returnIntent);
            this.activity.finish();
        } else {
            Intent returnIntent = new Intent(this.activity, MainActivity.class);
            this.activity.setResult(500, returnIntent);
            this.activity.finish();
        }
    }
}
