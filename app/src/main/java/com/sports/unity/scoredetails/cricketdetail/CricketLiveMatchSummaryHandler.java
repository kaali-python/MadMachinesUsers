package com.sports.unity.scoredetails.cricketdetail;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.sports.unity.scoredetails.model.CricketScoreCard;

import java.util.HashSet;

/**
 * Created by madmachines on 16/2/16.
 */
public class CricketLiveMatchSummaryHandler {

    private static final String REQUEST_LISTENER_KEY = "COMPLETED_MATCH_SCREEN_LISTENER";
    private static final String REQUEST_TAG = "SUMMARY_TAG";
    private Context context;
    private String matchId = null;
    private String url = "http://52.74.75.79:8080/get_cricket_match_scorecard?match_key=";

    private ContentListener contentListener = null;
    private HashSet<String> requestInProcess = new HashSet<>();

    public static CricketLiveMatchSummaryHandler getInstance(Context context) {
        CricketLiveMatchSummaryHandler completedMatchScoreCardHandler = null;
        completedMatchScoreCardHandler = new CricketLiveMatchSummaryHandler();
        return completedMatchScoreCardHandler;
    }
    private interface ResponseListener extends Response.Listener<String>, Response.ErrorListener {

    }
    public interface ContentListener {

        void handleContent(int responseCode);

    }
    private ResponseListener responseListener_ForLoadContent = new ResponseListener() {

        @Override
        public void onResponse(String s) {
            requestInProcess.remove(REQUEST_TAG);
            CricketLiveMatchSummaryHandler.this.handleResponse(s);
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            requestInProcess.remove(REQUEST_TAG);
            CricketLiveMatchSummaryHandler.this.handleErrorResponse(volleyError);
        }
    };

    public void requestScoreCardDetail() {
        Log.i("Score Detail", "Request Score Details");

        url = url+matchId;
        StringRequest stringRequest = null;
        RequestQueue queue = Volley.newRequestQueue(context);
        stringRequest = new StringRequest(Request.Method.GET, url, responseListener_ForLoadContent,responseListener_ForLoadContent);
        queue.add(stringRequest);

        requestInProcess.add(REQUEST_TAG);
    }
    private void handleResponse(String response) {
        Gson gson = new Gson();
        try{
            CricketScoreCard scoreCardModel = gson.fromJson(response,CricketScoreCard.class);
            Log.i("Score Card", "handleResponse: ");
            if(scoreCardModel.isSuccess()){
                Log.i("Score Card",scoreCardModel.toString());
            }



        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    private void handleErrorResponse(VolleyError volleyError) {
        Log.i("News Content Handler", "Error Response " + volleyError.getMessage());
        if(contentListener != null) {
            contentListener.handleContent(0);
        }
    }

}
