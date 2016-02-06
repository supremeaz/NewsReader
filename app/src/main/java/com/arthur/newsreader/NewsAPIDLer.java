package com.arthur.newsreader;

import android.os.AsyncTask;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Arthur on 04/02/2016.
 */
public class NewsAPIDLer extends AsyncTask<String,Void,String> {
    URL url;
    String content="";
    HttpURLConnection httpURLConnection=null;
    @Override
    protected String doInBackground(String... urls) {
        try{
            URL url=new URL(urls[0]);
            httpURLConnection=(HttpURLConnection)url.openConnection();
            InputStream in=httpURLConnection.getInputStream();
            InputStreamReader inReader=new InputStreamReader(in);
            int t=inReader.read();
            while(t!=-1){
                content+=(char)t;
                t=inReader.read();
            }
            return content;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
