package com.arthur.newsreader;


import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {

    ActionBar aBar;
    Intent parentI;
    WebView webDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        parentI=getIntent();
        setUp();
    }
    private void setUp(){
        webDisplay=(WebView)findViewById(R.id.webView);
        webDisplay.getSettings().setJavaScriptEnabled(true);
        if(parentI.getStringExtra("url")!=null){
            webDisplay.loadUrl(parentI.getStringExtra("url"));
        }
        webDisplay.setWebViewClient(new WebViewClient());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Menu should display the WebPage Title.
        aBar=getSupportActionBar();
        parentI=getIntent();
        String siteTitle=parentI.getStringExtra("title");
        if(siteTitle!=null){
            aBar.setTitle(siteTitle);
        }
        aBar.setDisplayHomeAsUpEnabled(true);
        aBar.setHomeButtonEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
