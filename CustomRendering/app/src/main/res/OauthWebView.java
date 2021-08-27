package com.example.hackhaton_multiple_client_id;

import android.app.Activity;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.webkit.ClientCertRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class OauthWebView extends WebViewClient {
    private Activity activity = null;

    public OauthWebView(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("WebView", url);

        if(url.contains("code=")) {
            return true;
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


    /*
    public void closeAuth(){
        if(!this.rtFa.isEmpty() && !this.fedAuth.isEmpty()) {
            Intent returnIntent = new Intent(this.activity, MainActivity.class);
            returnIntent.putExtra("rtFa", this.rtFa);
            returnIntent.putExtra("fedAuth", this.fedAuth);
            returnIntent.putExtra("estsAuthPersistent", this.estsAuthPersistent);
            this.activity.setResult(Activity.RESULT_OK, returnIntent);
            this.activity.finish();
        } else {
            Intent returnIntent = new Intent(this.activity, MainActivity.class);
            this.activity.setResult(500, returnIntent);
            this.activity.finish();
        }
    }
    */

}
