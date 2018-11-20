package com.a2.group.mtr2018md2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.a2.group.mtr2018md2.R.id.audioButtonContainer;

public class GitHub extends AppCompatActivity {

    private static final String TAG = "GitHub API";

    private static final String GITHUB_API_URL = "https://api.github.com/users/Sacristan/repos";
    private static final int MAX_LIST_ITEMS = 50;

    ProgressDialog pd;
    ListView repoListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_hub);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        repoListView = findViewById(R.id.githubList);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fetchGithubData();
    }

    private void fetchGithubData(){
        new JsonTask().execute(GITHUB_API_URL);
    }

    private void populateList(String rawJSON){
        Log.i(TAG, "RAW JSON: "+rawJSON);

        try {
//            JSONObject jObject = new JSONObject(rawJSON);
            JSONArray jsonArray = new JSONArray(rawJSON);

            for (int i=0; i < jsonArray.length(); i++)
            {
                if(i >= MAX_LIST_ITEMS) break;

                JSONObject item = jsonArray.getJSONObject(i);
                String itemName = item.getString("full_name");
                String itemUrl = item.getString("html_url");
                addNewRecordItem(itemName, itemUrl);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void addNewRecordItem(String name, final String url){
        Button itemButton = new Button(this);
        itemButton.setText(name);

        itemButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        //FIX: https://stackoverflow.com/questions/4576219/logcat-error-addviewview-layoutparams-is-not-supported-in-adapterview-in-a
        //repoListView.addView(itemButton, 0);
        LinearLayout layout = new LinearLayout(this);
        layout.addView(itemButton,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,0));
    }

    protected class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);
                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result!="") {//possibly no connection here
                populateList(result);
            }
        }
    }


}
