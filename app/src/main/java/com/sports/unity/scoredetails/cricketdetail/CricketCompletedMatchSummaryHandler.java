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

import org.json.JSONObject;

import java.util.HashSet;

/**
 * Created by madmachines on 22/2/16.
 */
public class CricketCompletedMatchSummaryHandler {

    private static final String REQUEST_TAG = "COMPLETED_SUMMARY_TAG";
    private static Context mContext;
    private String url = "http://52.74.75.79:8080/get_cricket_match_scorecard?match_key=";

    private CricketCompletedMatchSummaryContentListener mcontentListener;
    private HashSet<String> requestInProcess = new HashSet<>();

    public static CricketCompletedMatchSummaryHandler getInstance(Context context) {
        CricketCompletedMatchSummaryHandler cricketCompletedMatchSummaryHandler = null;
        cricketCompletedMatchSummaryHandler = new CricketCompletedMatchSummaryHandler();
        mContext = context;
        return cricketCompletedMatchSummaryHandler;
    }
    private interface ResponseListener extends Response.Listener<String>, Response.ErrorListener {

    }
    public interface CricketCompletedMatchSummaryContentListener {

        void handleContent(JSONObject jsonObject);

    }
    private ResponseListener responseListener_ForLoadContent = new ResponseListener() {

        @Override
        public void onResponse(String s) {
            requestInProcess.remove(REQUEST_TAG);
            CricketCompletedMatchSummaryHandler.this.handleResponse(s);
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            requestInProcess.remove(REQUEST_TAG);
            CricketCompletedMatchSummaryHandler.this.handleErrorResponse(volleyError);
        }
    };

    public void requestCompletedMatchSummary(String matchId) {
        Log.i("Score Detail", "Request Score Details");

        url = url+matchId;
        StringRequest stringRequest = null;
        RequestQueue queue = Volley.newRequestQueue(mContext);
        stringRequest = new StringRequest(Request.Method.GET, url, responseListener_ForLoadContent,responseListener_ForLoadContent);
        queue.add(stringRequest);

        requestInProcess.add(REQUEST_TAG);
    }
    private void handleResponse(String response) {
        try{
            JSONObject jsonObject = new JSONObject(response);
            Log.i("Score Card", "handleResponse: ");
            if(jsonObject.getBoolean("success")){
                mcontentListener.handleContent(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void handleErrorResponse(VolleyError volleyError) {
        Log.i("News Content Handler", "Error Response " + volleyError.getMessage());

    }
    public void addListener(CricketCompletedMatchSummaryContentListener contentListener) {
        mcontentListener = contentListener;
    }
}