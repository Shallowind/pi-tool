package com.zmmxkid.pitool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class gonglue extends AppCompatActivity {

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gonglue);


        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());

        webView.getSettings().setJavaScriptEnabled(true); // 允许JavaScript执行
        webView.loadUrl("https://view.xdocin.com/view?src=http://101.42.49.54:8888/down/QLPqnzm68HCV"); // 加载网页
    }
}