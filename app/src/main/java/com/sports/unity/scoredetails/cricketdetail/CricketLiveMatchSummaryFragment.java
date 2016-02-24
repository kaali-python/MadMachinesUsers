package com.sports.unity.scoredetails.cricketdetail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sports.unity.R;
import com.sports.unity.scoredetails.BallDetail;
import com.sports.unity.scores.ScoreDetailActivity;
import com.sports.unity.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CricketLiveMatchSummaryFragment extends Fragment implements  CricketLiveMatchSummaryHandler.LiveCricketMatchSummaryContentListener {

    private ImageView ivFirstBall;
    private ImageView ivSecondBall;
    private ImageView ivThirdBall;
    private ImageView ivFourthBall;
    private ImageView ivFifthBall;
    private ImageView ivSixthBall;
    private ImageView ivFirstPlayer;
    private TextView tvFirstPlayerName;
    private TextView tvFirstPlayerRunRate;
    private TextView tvFirstPlayerRunOnBall;
    private TextView tvPartnershipRecord;
    private TextView tvSecondPlayerName;
    private TextView tvSecondPlayerRunRate;
    private TextView tvSecondPlayerRunOnBall;
    private ImageView ivPlayerSecond;
    private ImageView ivUppComingPlayerFirst;
    private ImageView ivUppComingPlayerSecond;
    private ImageView ivUppComingPlayerThird;
    private TextView tvSecondUpComingPlayerName;
    private TextView tvThirdUpComingPlayerName;
    private TextView tvFirstUpComingPlayerName;
    private TextView tvFirstUpComingPlayerRunRate;
    private TextView tvSecondUpComingPlayerRunRate;
    private TextView tvThirdUpComingPlayerRunRate;
    private ImageView ivBowlerProfile;
    private TextView tvBowlerName;
    private TextView tvBowlerOverlabel;
    private TextView tvBowlerWRun;
    private TextView tvBowlerEcon;
    private TextView tvBowlerOver;
    private TextView tvBowlerWr;
   public CricketLiveMatchSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        String matchId =  getActivity().getIntent().getStringExtra(Constants.INTENT_KEY_ID);
       CricketLiveMatchSummaryHandler cricketLiveMatchSummaryHandler = CricketLiveMatchSummaryHandler.getInstance(context);
        cricketLiveMatchSummaryHandler.addListener(this);
        cricketLiveMatchSummaryHandler.requestLiveMatchSummary(matchId);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cricket_live_match_summery, container, false);
        initView(view);
        return view;
    }
    private void initView(View view) {
        ivFirstBall = (ImageView) view.findViewById(R.id.iv_first_ball);
        ivSecondBall = (ImageView) view.findViewById(R.id.iv_second_ball);
        ivThirdBall = (ImageView) view.findViewById(R.id.iv_third_ball);
        ivFourthBall = (ImageView) view.findViewById(R.id.iv_fourth_ball);
        ivFifthBall = (ImageView) view.findViewById(R.id.iv_fifth_ball);
        ivSixthBall = (ImageView) view.findViewById(R.id.iv_sixth_ball);
        ivFirstPlayer = (ImageView) view.findViewById(R.id.iv_player_first);
        tvFirstPlayerName = (TextView) view.findViewById(R.id.tv_first_player_name);
        tvFirstPlayerRunRate = (TextView) view.findViewById(R.id.tv_first_player_run_rate);
        tvFirstPlayerRunOnBall = (TextView) view.findViewById(R.id.tv_first_player_run_on_ball);
        tvPartnershipRecord = (TextView) view.findViewById(R.id.tv_partnership_record);
        tvSecondPlayerName = (TextView) view.findViewById(R.id.tv_second_player_name);
        tvSecondPlayerRunRate = (TextView) view.findViewById(R.id.tv_first_player_run_rate);
        tvSecondPlayerRunOnBall = (TextView) view.findViewById(R.id.tv_second_player_run_on_ball);
        ivPlayerSecond = (ImageView) view.findViewById(R.id.iv_player_first);
        ivUppComingPlayerFirst = (ImageView) view.findViewById(R.id.iv_upp_coming_player_first);
        ivUppComingPlayerSecond = (ImageView) view.findViewById(R.id.iv_up_coming_player_second);
        ivUppComingPlayerThird = (ImageView) view.findViewById(R.id.iv_up_coming_player_third);
        tvSecondUpComingPlayerName = (TextView) view.findViewById(R.id.tv_second_up_coming_player_name);
        tvThirdUpComingPlayerName = (TextView) view.findViewById(R.id.tv_third_up_coming_player_name);
        tvFirstUpComingPlayerName = (TextView) view.findViewById(R.id.tv_first_up_coming_player_name);
        tvFirstUpComingPlayerRunRate = (TextView) view.findViewById(R.id.tv_first_up_coming_player_run_rate);
        tvSecondUpComingPlayerRunRate = (TextView) view.findViewById(R.id.tv_second_up_coming_player_run_rate);
        tvThirdUpComingPlayerRunRate = (TextView) view.findViewById(R.id.tv_third_up_coming_player_run_rate);
        ivBowlerProfile = (ImageView) view.findViewById(R.id.iv_bowler_profile);
        tvBowlerName = (TextView) view.findViewById(R.id.tv_bowler_name);
        tvBowlerOverlabel = (TextView) view.findViewById(R.id.tv_bowler_over_label);
        tvBowlerWRun = (TextView) view.findViewById(R.id.tv_bowler_W_Run);
        tvBowlerEcon = (TextView) view.findViewById(R.id.tv_bowler_econ);
        tvBowlerOver = (TextView) view.findViewById(R.id.tv_bowler_over);
        tvBowlerWr = (TextView) view.findViewById(R.id.tv_bowler_wr);

        initErrorLayout(view);

    }

    @Override
    public void handleContent(String content) {

        try {
            JSONObject object = new JSONObject(content);
            boolean success = object.getBoolean("success");
            boolean error = object.getBoolean("error");

            if (success) {

                renderDisplay(object);

            } else {
                Toast.makeText(getActivity(), R.string.match_not_exist, Toast.LENGTH_SHORT).show();
                showErrorLayout(getView());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getActivity(), R.string.oops_try_again, Toast.LENGTH_SHORT).show();
        }

    }
    private void initErrorLayout(View view) {
        LinearLayout errorLayout = (LinearLayout) view.findViewById(R.id.error);
        errorLayout.setVisibility(View.GONE);

    }

    private void showErrorLayout(View view) {

        LinearLayout errorLayout = (LinearLayout) view.findViewById(R.id.error);
        errorLayout.setVisibility(View.VISIBLE);

    }

    private void renderDisplay(final JSONObject jsonObject) throws JSONException {

        ScoreDetailActivity activity = (ScoreDetailActivity) getActivity();
        JSONArray dataArray= jsonObject.getJSONArray("data");
        JSONObject matchObject = dataArray.getJSONObject(0);
        JSONArray recentOverArray = matchObject.getJSONArray("recent_overs");
        final JSONObject currentPartnershipDetails = matchObject.getJSONObject("current_partnership_details");
        BallDetail defb = new BallDetail();
        BallDetail []balls = new BallDetail[]{defb,defb,defb,defb,defb,defb,defb};
        int ballIndex = 6;
        for(int i =0; i<= recentOverArray.length();i++){
              JSONArray ballsArray = recentOverArray.getJSONArray(i);
               JSONArray over = ballsArray.getJSONArray(1);
                for (int j=over.length()-1; j>=0; j--){
                    if(ballIndex<0)
                    {
                        break;
                    }
                            balls[ballIndex] = getResolveBall(over.getString(j));
                            ballIndex--;
              }
        }


         if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        tvFirstPlayerName.setText(currentPartnershipDetails.getString("player_a"));
                        tvSecondPlayerName.setText(currentPartnershipDetails.getString("player_b"));
                        tvFirstPlayerRunRate.setText(currentPartnershipDetails.getString("player_a_strikerate"));
                        tvSecondPlayerRunRate.setText(currentPartnershipDetails.getString("player_b_strikerate"));
                        tvFirstPlayerRunOnBall.setText(currentPartnershipDetails.getString("player_a_runs")+"("+currentPartnershipDetails.getString("player_a_balls")+")");
                        tvSecondPlayerRunOnBall.setText(currentPartnershipDetails.getString("player_b_runs")+"("+currentPartnershipDetails.getString("player_b_balls")+")");
                        tvPartnershipRecord.setText(currentPartnershipDetails.getString("partnership_runs")+"("+currentPartnershipDetails.getString("partnership_balls")+")");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showErrorLayout(getView());
                    }
                }
            });
        }

    }

    private BallDetail getResolveBall(String value){
        BallDetail ballDetail = new BallDetail();
        switch (value){
            case "r1":
                ballDetail.setValue("1");
                break;
            case "r2":
                ballDetail.setValue("2");
                break;
            case "r3":
                ballDetail.setValue("3");
                break;
            case "r4":
                ballDetail.setValue("4");
                break;
            case "r5":
                ballDetail.setValue("5");
                break;
            case "r6":
                ballDetail.setValue("6");
                break;
        }
      return    ballDetail;
    }
}
