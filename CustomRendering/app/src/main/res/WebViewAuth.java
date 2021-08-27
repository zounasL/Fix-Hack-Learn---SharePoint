package com.example.hackhaton_multiple_client_id;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.webkit.ClientCertRequest;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;


public class WebViewAuth extends WebViewClient {
    private Activity activity = null;
    private String tenantUrl = "";

    private String rtFa = "";
    private String fedAuth = "";
    private String code = "";
    // private String estsAuthPersistent = "";
    // private spOauthNativeClientProvider oauthNative;

    public WebViewAuth(Activity activity, String tenantUrl) {
        this.activity = activity;
        this.tenantUrl = tenantUrl;
        // this.oauthNative = new spOauthNativeClientProvider(this.activity);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(request.getUrl().toString());
        if(cookies != null) {
            this.cookieCollector(request.getUrl().toString());
            Log.d("WebView", cookies);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("WebView", url);

        if(this.checkSharepointAuthCookies(url)) {
            this.cookieCollector(url);
            // view.loadUrl(this.oauthNative.getAuthorizeCodeUrl(this.tenantUrl));
            return true;
        }
        if(url.contains("code=")) {
            this.codeCollector(url);
            this.closeAuth();
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

    protected boolean checkSharepointAuthCookies(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        if(cookies != null) {
            return cookies.contains("rtFa=") && cookies.contains("FedAuth=");
        }
        return false;
    }

    public void codeCollector(String url) {
        Uri uri= Uri.parse(url);
        this.code = uri.getQueryParameter("code");
        Log.d("WebView", this.code);
    }

    public void cookieCollector(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        if(cookies != null) {
            String[] urlCookies = cookies.split(";");
            /*
            if(cookies.contains("ESTSAUTHPERSISTENT=")) {
                this.estsAuthPersistent = cookies;
            }
            */
            if(cookies.contains("rtFa=")) {
                this.rtFa = this.getCookieFromArray(urlCookies, "rtFa=");
            }
            if(cookies.contains("FedAuth=")) {
                this.fedAuth = this.getCookieFromArray(urlCookies, "FedAuth=");
            }
        }
    }

    private String getCookieFromArray(String[] cookies, String key) {
        for (String cookie : cookies ){
            if(cookie.contains(key)){
                return cookie;
            }
        }
        return "";
    }

    public void closeAuth(){
        if(!this.rtFa.isEmpty() && !this.fedAuth.isEmpty()) {
            Intent returnIntent = new Intent(this.activity, MainActivity.class);
            returnIntent.putExtra("rtFa", this.rtFa);
            returnIntent.putExtra("fedAuth", this.fedAuth);
            returnIntent.putExtra("code", this.code);
            // returnIntent.putExtra("estsAuthPersistent", this.estsAuthPersistent);
            this.activity.setResult(Activity.RESULT_OK, returnIntent);
            this.activity.finish();
        } else {
            Intent returnIntent = new Intent(this.activity, MainActivity.class);
            this.activity.setResult(500, returnIntent);
            this.activity.finish();
        }
    }

}
