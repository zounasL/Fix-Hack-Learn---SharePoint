package com.example.hackhaton_multiple_client_id;

import android.content.Context;
import android.os.AsyncTask;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.webkit.ClientCertRequest;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

final class KeyChainLookup extends AsyncTask<Void, Void, Void> {
    private final Context mContext;
    private final ClientCertRequest mHandler;
    private final String mAlias;
    KeyChainLookup(Context context, ClientCertRequest handler, String alias) {
        mContext = context.getApplicationContext();
        mHandler = handler;
        mAlias = alias;
    }
    @Override
    protected Void doInBackground(Void... params) {
        PrivateKey privateKey;
        X509Certificate[] certificateChain;
        try {
            privateKey = KeyChain.getPrivateKey(mContext, mAlias);
            certificateChain = KeyChain.getCertificateChain(mContext, mAlias);
        } catch (InterruptedException e) {
            mHandler.ignore();
            return null;
        } catch (KeyChainException e) {
            mHandler.ignore();
            return null;
        }
        mHandler.proceed(privateKey, certificateChain);
        return null;
    }
}