package com.arthur.newsreader;



import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ListView siteList;
    private ArrayList<String>siteTitles=new ArrayList<String>();
    private ArrayAdapter<String>arrayAdapter;
    private TextView titleText;
    private ArrayList<String>siteTypes=new ArrayList<String>();
    private ArrayList<String>siteURLs=new ArrayList<String>();
    SQLiteDatabase webDataBase;
    SharedPreferences sharedPreferences;
    private void setUp(){
        siteList=(ListView)findViewById(R.id.siteList);
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,siteTitles);
        siteList.setAdapter(arrayAdapter);

        sharedPreferences=getSharedPreferences("com.arthur.newsreader", Context.MODE_PRIVATE);
        siteTitles.add("Google");
        webDataBase=this.openOrCreateDatabase("links",MODE_PRIVATE,null);
        if(updateRequired()){            //Checks if data is too old!
            updateDataBase(webDataBase);
        }
        updateList(webDataBase);
        titleText=(TextView)findViewById(R.id.titleText);
        titleText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Refresh")
                        .setMessage("Do you wish to refresh?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateDataBase(webDataBase);
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
                return true;
            }
        });
        //update Adapter when contents updated.
        siteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=new Intent(getApplicationContext(),WebActivity.class);
                i.putExtra("index",position);
                i.putExtra("title",siteTitles.get(position));
                i.putExtra("url",siteURLs.get(position));
                startActivity(i);
            }
        });
    }

    private void updateList(SQLiteDatabase database){
        Log.i("updatingList","Updating...");
        try{
            //Need to Read from SQLite Database,  upDate arrayList, and refresh arrayAdapter.
            Cursor c=database.rawQuery("SELECT * FROM weblinks",null);
            int titleIndex=c.getColumnIndex("title"); int typeIndex=c.getColumnIndex("type"); int urlIndex=c.getColumnIndex("url");
            c.moveToFirst();
            siteTitles.clear();siteTypes.clear();siteURLs.clear();
            while(c!=null){
                Log.i("ListProgress", "Current");
                Log.i("infoSite",c.getString(titleIndex)+","+c.getString(typeIndex)+","+c.getString(urlIndex));
                siteTitles.add(c.getString(titleIndex));
                siteTypes.add(c.getString(typeIndex));
                siteURLs.add(c.getString(urlIndex));
                c.moveToNext();
            }
            arrayAdapter.notifyDataSetChanged();
            Log.i("ListProgress","Done");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private boolean updateRequired(){
        long lastDateS=sharedPreferences.getLong("time",-1);
        long currentTime=System.currentTimeMillis();
        if(lastDateS==-1){
            Log.i("Time","First time running");
            sharedPreferences.edit().putLong("time",currentTime).apply();
            return true;
        }
        else {
            if(currentTime-lastDateS>=3600000){     //if last log was more than 1 hour ago, update
                Log.i("Time-Update",String.valueOf(currentTime-lastDateS)+" ms since last log");
                sharedPreferences.edit().putLong("time",currentTime).apply();
                return true;
            }
            //Else don't update time...
            Log.i("Time-NoUpdate",String.valueOf(currentTime-lastDateS)+"ms since Last, Update not needed");
            return false;
        }
    }
    private void updateDataBase(SQLiteDatabase database){
        //Gotta first get the info from the API...

        //Update the DataBase;
        try{
            Log.i("StartingDatabaseUpdate", "Database Updating");
            database.execSQL("CREATE TABLE IF NOT EXISTS weblinks(title VARCHAR,type VARCHAR,url VARCHAR)");
            database.execSQL("DELETE FROM weblinks");   //First Create Database..

            Log.i("UpdatingDatabase", "Database created" );
            //Download APi.
            NewsAPIDLer newsAPIDLer=new NewsAPIDLer();
            //Got in Json Array a list of top 500 stories.... Now need to get each one and add to the list. for top 20...
            String apiInString=newsAPIDLer.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            JSONArray apiJ=new JSONArray(apiInString);

            for(int i=0;i<20;i++){
                Log.i("UpdatingDatabase","Updating row "+i);
                NewsAPIDLer individualDLer=new NewsAPIDLer();
                JSONObject newsAPIContent=new JSONObject(individualDLer.execute("https://hacker-news.firebaseio.com/v0/item/"+apiJ.get(i)+".json?print=pretty").get());
                String newsTitle=newsAPIContent.getString("title");
                String newsType=newsAPIContent.getString("type");
                String newsURL=newsAPIContent.getString("url");
                database.execSQL("INSERT INTO weblinks(title,type,url) VALUES ('"+newsTitle+"','"+newsType+"','"+newsURL+"')");
            }
            Log.i("FinishDatabaseUpdata","Database Updated");
            updateList(database);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        Log.i("Time-UpdateNow","updating");

        //At the end have to update ArrayList and ArrayAdapter.
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();
    }
}
