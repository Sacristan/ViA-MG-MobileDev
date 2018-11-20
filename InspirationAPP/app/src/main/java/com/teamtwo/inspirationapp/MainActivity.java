package com.teamtwo.inspirationapp;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String QUOTE_CACHE_FILENAME = "qoutes.cache";

    private static final String QUOTES_JSON_DATA_URI = "https://gist.githubusercontent.com/Sacristan/3cdc5db13184df250349467e7a568e28/raw/inspirational_quotes.json";
    private static final int PICTURE_COUNT = 24;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private static List<String> quotesList = new ArrayList<String>();
    private MediaPlayer mediaPlayer;

    private static FirebaseAnalytics mFirebaseAnalytics;

    ProgressDialog pd = null;

    private static int currentPageCounter = 0;
    private static int prevPicId = -1;
    private static int prevQuouteId = -1;

    private static void logAnalyticsEvent(String event){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, event);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void startInspirationalExperience(){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playAudio(R.raw.inspirational_background);
        fetchQuoutes();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        logAnalyticsEvent("launch_application");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        quotesList = null;
        mFirebaseAnalytics = null;
        currentPageCounter = 0;

        logAnalyticsEvent("exit_application");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class InpirationalContentFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public InpirationalContentFragment() {
        }

        public static InpirationalContentFragment newInstance(int sectionNumber) {
            InpirationalContentFragment fragment = new InpirationalContentFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.inspirationalBackground);

            prevPicId = getRandomId(PICTURE_COUNT, prevPicId) + 1;

            String imgName = "img_" + prevPicId;
            int id = getResources().getIdentifier(imgName, "drawable", this.getContext().getPackageName());
            imageView.setImageResource(id);

            TextView textView = (TextView) rootView.findViewById(R.id.inspirationalText);

            prevQuouteId = getRandomId(quotesList.size(), prevQuouteId);
            String inspirationalText = quotesList.get(prevQuouteId);

            textView.setText(inspirationalText);

            Log.i(TAG, "GENERATED IMGID: " + prevPicId+ " QUOUTEID: "+ prevQuouteId);

            currentPageCounter++;
            logAnalyticsEvent("look_page_"+currentPageCounter);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return InpirationalContentFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE; //Gl, with that
        }
    }

    private static int getRandomId(int barrierValue, int prevValue){
        Random rand = new Random();

        int rnd = rand.nextInt(barrierValue);

        while(rnd==prevValue){
            rnd = rand.nextInt(barrierValue);
        }

        return rnd;
    }

    private void playAudio(int id){
        mediaPlayer = MediaPlayer.create(getApplicationContext(), id);

        if (!mediaPlayer.isPlaying())
        {
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    }

    private void fetchQuoutes(){
        new JsonTask().execute(QUOTES_JSON_DATA_URI);
    }

    private void parseQuotesJSON(String rawJSON){
        final String ParseQuotesTag = "ParseQuotesJSON";

        JSONObject jObject = null;
        JSONObject jCacheObject = loadJSONFromCache();

        boolean emptyData = rawJSON == "";

        try {

            if(emptyData) {
                Log.i(ParseQuotesTag, "Received empty JSON data - trying to load from cache...");
                jObject = jCacheObject;
            }
            else{
                jObject = new JSONObject(rawJSON);
                int version = jObject.getInt("version");

                if (jCacheObject != null) {
                    int cacheVersion = jCacheObject.getInt("version");

                    if (version == cacheVersion) {
                        Log.i(ParseQuotesTag, "EXT and CACHE JSON versions the same. Nothing to do here!");
                    }
                    else{
                        Log.i(ParseQuotesTag, "EXT and CACHE JSON versions differ. Creating cache file...");
                        createCacheFile(rawJSON);
                    }
                }
                else{
                    Log.i(ParseQuotesTag, "No cache JSON present - creating one...");
                    createCacheFile(rawJSON);
                }
            }

            if(jObject!=null) {
                JSONArray jArray = jObject.getJSONArray("quotes");
                populateQuoutes(jArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private JSONObject loadJSONFromCache(){
        File directory = getApplicationContext().getFilesDir();
        File file = new File(directory, QUOTE_CACHE_FILENAME);

        if(!file.exists()) return null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            line =  br.lines().collect(Collectors.joining());
            Log.i(TAG, "READ CACHE JSON: "+line);

            br.close();
            try {
                JSONObject jObject = new JSONObject(line);
                return jObject;
            }

            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

    private void createCacheFile(String data){
        try {

//            File file = File.createTempFile(QUOTE_CACHE_FILENAME, null, getApplicationContext().getFilesDir());
            File file = new File(getApplicationContext().getFilesDir(), QUOTE_CACHE_FILENAME);

            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateQuoutes(JSONArray jArray){
        for (int i=0; i < jArray.length(); i++){
            try {
                String qoute = jArray.getString(i);
                quotesList.add(qoute);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
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

            parseQuotesJSON(result);

            if (pd.isShowing()){
                pd.dismiss();
            }

            startInspirationalExperience();

        }
    }
}
