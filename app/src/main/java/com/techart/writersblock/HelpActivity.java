package com.techart.writersblock;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Contains information about how to
 * Created by Kelvin on 10/08/2017.
 */

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);
        setTitle("Help");

        String information = getString(R.string.help);
        WebView webView = findViewById(R.id.wv_help);
        webView.loadDataWithBaseURL(null,information,"text/html","utf-8",null);
    }
}
