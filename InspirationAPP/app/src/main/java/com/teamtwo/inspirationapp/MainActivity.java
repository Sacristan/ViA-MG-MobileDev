package com.teamtwo.inspirationapp;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int PICTURE_COUNT = 24;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private static List<String> quotesList = new ArrayList<String>();
    private MediaPlayer mediaPlayer;

    private static FirebaseAnalytics mFirebaseAnalytics;

    private static int currentPageCounter = 0;
    private static int prevPicId = -1;
    private static int prevQuouteId= -1;

    private static void logAnalyticsEvent(String event){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, event);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        playAudio(R.raw.inspirational_background);
        loadQuotes();

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

            prevPicId = getRandomId(PICTURE_COUNT+1, prevPicId);

            String imgName = "img_" + prevPicId;
            int id = getResources().getIdentifier(imgName, "drawable", this.getContext().getPackageName());
            imageView.setImageResource(id);

            TextView textView = (TextView) rootView.findViewById(R.id.inspirationalText);

            prevQuouteId = getRandomId(quotesList.size(), prevQuouteId);
            String inspirationalText = quotesList.get(prevQuouteId);

            textView.setText(inspirationalText);

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


    private void loadQuotes(){
        BufferedReader reader;

        try{
            final InputStream file = getResources().openRawResource(R.raw.inspirational_quotes);
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while(line != null){
                line = reader.readLine();
                if(line!=null && line!="") quotesList.add(line);
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
