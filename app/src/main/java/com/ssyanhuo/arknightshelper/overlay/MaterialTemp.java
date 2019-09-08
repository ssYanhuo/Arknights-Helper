package com.ssyanhuo.arknightshelper.overlay;

import android.content.Context;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ssyanhuo.arknightshelper.R;

public class MaterialTemp {
    private WebView webView;
    private ProgressBar progressBar;
    public void init(Context context, View view){
        progressBar = view.findViewById(R.id.webview_progress);
        webView = view.findViewById(R.id.webview_tmp);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.requestFocus();
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadUrl("https://penguin-stats.io/result/item");
    }
}
