package com.sports.unity.common.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sports.unity.BuildConfig;
import com.sports.unity.R;
import com.sports.unity.XMPPManager.XMPPClient;
import com.sports.unity.common.model.FontTypeface;
import com.sports.unity.common.model.TinyDB;
import com.sports.unity.common.model.UserUtil;
import com.sports.unity.util.Constants;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SelectSportsActivity extends CustomAppCompatActivity {

    private ArrayList<String> sports = new ArrayList<String>();
    private boolean isResultRequired;
    private Thread sendInterestsThread = null;
    private String base_url = "http://" + BuildConfig.XMPP_SERVER_API_BASE_URL + "/set_user_interests?username=";
    private String urlToRequest = "";
    /*For future use: to add all the sports
     in sports selection screen*/
    /*private Integer[] mThumbIds = {
            R.drawable.btn_basketball_disabled,
            R.drawable.btn_cricket_disabled,
            R.drawable.btn_football_disabled,
            R.drawable.btn_tennis_disabled,
            R.drawable.btn_f1_disabled,
    };
    private Integer[] mThumbIdsSelected = {
            R.drawable.btn_basketball_selected,
            R.drawable.btn_cricket_selected,
            R.drawable.btn_football_selected,
            R.drawable.btn_tennis_selected,
            R.drawable.btn_f1_selected,
    };
    private String[] mSports = {
            Constants.GAME_KEY_BASKETBALL,
            Constants.GAME_KEY_CRICKET,
            Constants.GAME_KEY_FOOTBALL,
            Constants.GAME_KEY_TENNIS,
            Constants.GAME_KEY_F1,
    };*/

    private Integer[] mThumbIds = {
            R.drawable.btn_cricket_disabled,
            R.drawable.btn_football_disabled
    };
    private Integer[] mThumbIdsSelected = {
            R.drawable.btn_cricket_selected,
            R.drawable.btn_football_selected
    };
    private String[] mSports = {
            Constants.GAME_KEY_CRICKET,
            Constants.GAME_KEY_FOOTBALL
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            isResultRequired = getIntent().getExtras().getBoolean(Constants.RESULT_REQUIRED);
        } catch (NullPointerException booleanNull) {

        }

        setContentView(R.layout.activity_select_sports);
        sports = new ArrayList<>(UserUtil.getSportsSelected());
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        initView();
    }


    ImageView.OnClickListener imageViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView view = (ImageView) v;
            if (v.getId() == R.id.cricket) {
                if (sports.contains(Constants.GAME_KEY_CRICKET)) {
                    view.setImageResource(R.drawable.btn_cricket_disabled);
                    sports.remove(Constants.GAME_KEY_CRICKET);
                } else {
                    view.setImageResource(R.drawable.btn_cricket_selected);
                    sports.add(Constants.GAME_KEY_CRICKET);
                }
            } else {
                if (sports.contains(Constants.GAME_KEY_FOOTBALL)) {
                    view.setImageResource(R.drawable.btn_football_disabled);
                    sports.remove(Constants.GAME_KEY_FOOTBALL);
                } else {
                    view.setImageResource(R.drawable.btn_football_selected);
                    sports.add(Constants.GAME_KEY_FOOTBALL);
                }
            }
        }
    };

    private void initView() {

        /*
         * initialise tool bar
         */
        {
            Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            mTitle.setText(R.string.select_your_favourite_sports);
            mTitle.setTypeface(FontTypeface.getInstance(this).getRobotoCondensedRegular());
        }

//        GridView gridview = (GridView) findViewById(R.id.gridview);
//        gridview.setAdapter(new SportsGridViewAdapter(this));
//        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (!sports.contains(mSports[position])) {
//                    ImageView imageView = (ImageView) view;
//                    imageView.setImageResource(mThumbIdsSelected[position]);
//                    sports.add(mSports[position]);
//                } else {
//                    ImageView imageView = (ImageView) view;
//                    imageView.setImageResource(mThumbIds[position]);
//                    sports.remove(mSports[position]);
//                }
//            }
//        });

        ImageView cricket = (ImageView) findViewById(R.id.cricket);
        ImageView football = (ImageView) findViewById(R.id.football);
        cricket.setOnClickListener(imageViewClickListener);
        football.setOnClickListener(imageViewClickListener);
        if (sports.contains(Constants.GAME_KEY_FOOTBALL)) {
            football.setImageResource(R.drawable.btn_football_selected);
        }
        if (sports.contains(Constants.GAME_KEY_CRICKET)) {
            cricket.setImageResource(R.drawable.btn_cricket_selected);
        }


        Button next = (Button) findViewById(R.id.toLeagueSelect);
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (sports.isEmpty()) {
                    Toast.makeText(SelectSportsActivity.this, R.string.select_atleast_one_sport_message, Toast.LENGTH_SHORT).show();
                } else {
                    moveOn();
                }
            }
        });

    }


    private void moveOn() {
        executeThreadToUpdateInterests();
        UserUtil.setSportsSelected(SelectSportsActivity.this, sports);
        UserUtil.setScoreFilterSportsSelected(SelectSportsActivity.this, new ArrayList<String>(sports));
        UserUtil.setNewsFilterSportsSelected(SelectSportsActivity.this, new ArrayList<String>(sports));
        Intent intent = new Intent(getIntent());
        intent.setClass(this, AdvancedFilterActivity.class);
        intent.putExtra(Constants.SPORTS_TYPE, UserUtil.getSportsSelected().get(0));
        if (!isResultRequired) {
            startActivity(intent);
            finish();
        } else {
            startActivityForResult(intent, Constants.REQUEST_CODE_ADD_SPORT);
        }

    }

    @Override
    public void onBackPressed() {
        if(isResultRequired){
            setResult(RESULT_CANCELED);
            finish();
        }else{
            super.onBackPressed();
        }

    }

    private void executeThreadToUpdateInterests() {
        if (sendInterestsThread != null && sendInterestsThread.isAlive()) {

        } else {
            sendInterestsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sendInterests();
                }
            });
            sendInterestsThread.start();
        }
    }

    private void sendInterests() {
        urlToRequest = "";
        HttpURLConnection httpURLConnection = null;
        String interests = "";
        if (sports != null || sports.size() > 0)
            for (String sport :
                    sports) {
                interests += "&interests=" + sport.toLowerCase();
            }
        urlToRequest = base_url + TinyDB.getInstance(getApplicationContext()).getString(TinyDB.KEY_USERNAME) + interests;
        Log.i("urltorequest", urlToRequest);
        try {
            URL sendInterests = new URL(urlToRequest);
            httpURLConnection = (HttpURLConnection) sendInterests.openConnection();
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setDoInput(false);
            httpURLConnection.setRequestMethod("GET");

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.i("Interest Sent", " true ");
            } else {
                Log.i("Interest Sent", " false ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpURLConnection.disconnect();
            } catch (Exception ex) {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.REQUEST_CODE_ADD_SPORT) {
            setResult(resultCode);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }

    }
}

