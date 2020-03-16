package com.example.tibsolg.ethiotaxi;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;

public class CommentFb extends AppCompatActivity {

    // This url contains the content of the article excluding web page's
    // header, footer, title, comments
//    private static String url = "https://api.androidhive.info/facebook/firebase_analytics.html";

    private WebView webView;
    private ImageButton telegramButton;
    private ImageButton faceBookButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_fb);

        telegramButton = (ImageButton) findViewById(R.id.telegramButton);
        faceBookButton = (ImageButton)findViewById(R.id.FacebookButton);

    }
    public void telegramOnClick(View view){
        String url = "https://t.me/Weraj_ale";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void facebookOnclick(View view){
        String url = "https://facebook.com";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
