package com.sports.unity.scoredetails.footballdetail.fooballadaptersanddto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.sports.unity.R;

import java.text.Format;
import java.util.List;

/**
 * Created by cfeindia on 28/2/16.
 */
public class CompleteFootballTimeLineAdapter extends RecyclerView.Adapter<CompleteFootballTimeLineAdapter.ViewHolder> {

    private final List<CompleteFootballTimeLineDTO> mValues;
    private Context context;

    public CompleteFootballTimeLineAdapter(List<CompleteFootballTimeLineDTO> mValues,Context context) {
        this.mValues = mValues;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        try{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.completed_football_match_timeline_card,parent,false);

        }catch (Exception e){e.printStackTrace();}
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try{
            // context.getResources().getDimensionPixelSize(R.dimen.);

            TextDrawable drawable = null;
            holder.dto = mValues.get(position);


            if(position==0){
                holder.tvTimeInterval.setVisibility(View.VISIBLE);
                holder.upperDotView.setVisibility(View.VISIBLE);
            }else {
                holder.tvTimeInterval.setVisibility(View.GONE);
                holder.upperDotView.setVisibility(View.GONE);
            }

            if(getItemCount()-1== position){
                holder.gameStartImage.setVisibility(View.VISIBLE);
                holder.gameStartImage.setImageResource(R.drawable.ic_match_start);
            }else {
                holder.gameStartImage.setVisibility(View.GONE);
            }

            if(holder.dto.getTeamName().equalsIgnoreCase(context.getString(R.string.home_team_name))) {
                if(holder.dto.getTvTeamFirstOffPlayer()!=null){

                }
                holder.tvTeamFirstTime.setText(holder.dto.getTvTeamFirstTime());
                holder.tvTeamFirstOnPlayer.setText(holder.dto.getTvTeamFirstOnPlayer());
                holder.tvTeamFirstOffPlayer.setText(holder.dto.getTvTeamFirstOffPlayer());
                holder.tvTeamFirstTime.setVisibility(View.VISIBLE);
                holder.tvTeamFirstOnPlayer.setVisibility(View.VISIBLE);
                holder.tvTeamFirstOffPlayer.setVisibility(View.VISIBLE);
                holder.teamFirstView.setVisibility(View.VISIBLE);
                holder.tvTeamSecondTime.setVisibility(View.INVISIBLE);
                holder.teamSecondView.setVisibility(View.INVISIBLE);
            }else if(holder.dto.getTeamName().equalsIgnoreCase(context.getString(R.string.away_team_name)))
            {
                holder.tvTeamSecondTime.setText(holder.dto.getTvTeamSecondTime());
                holder.tvTeamSecondOnPlayer.setText(holder.dto.getTvTeamSecondOnPlayer());
                holder.tvTeamSecondOffPlayer.setText(holder.dto.getTvTeamSecondOffPlayer());
                holder.tvTeamSecondTime.setVisibility(View.VISIBLE);
                holder.tvTeamSecondOnPlayer.setVisibility(View.VISIBLE);
                holder.tvTeamSecondOffPlayer.setVisibility(View.VISIBLE);
                holder.teamSecondView.setVisibility(View.VISIBLE);
                holder.teamFirstView.setVisibility(View.INVISIBLE);
                holder.tvTeamFirstTime.setVisibility(View.INVISIBLE);
            }
            holder.centralCircularImage.setImageDrawable(holder.dto.getDrwDrawable());

        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;

        private TextView tvTeamFirstTime;
        private TextView tvTeamSecondTime;
        private TextView tvTeamFirstOnPlayer;
        private TextView tvTeamSecondOnPlayer;
        private TextView tvTeamFirstOffPlayer;
        private TextView tvTeamSecondOffPlayer;
        private ImageView keyImage;
        private View teamFirstView;
        private View teamSecondView;
        private View centralLineImage;
        private ImageView centralCircularImage;
        private ImageView gameStartImage;
        private TextView tvTimeInterval;
        private View upperDotView;
        public CompleteFootballTimeLineDTO dto;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvTeamFirstTime = (TextView) view.findViewById(R.id.tv_team_first_time);
            tvTeamSecondTime = (TextView) view.findViewById(R.id.tv_team_second_time);
            tvTeamFirstOnPlayer = (TextView) view.findViewById(R.id.tv_team_first_on);
            tvTeamSecondOnPlayer = (TextView) view.findViewById(R.id.tv_team_second_on);
            tvTeamFirstOffPlayer = (TextView) view.findViewById(R.id.tv_team_first_off);
            tvTeamSecondOffPlayer = (TextView) view.findViewById(R.id.tv_team_second_off);
            keyImage = (ImageView) view.findViewById(R.id.iv_time_line_image);
            teamFirstView = view.findViewById(R.id.ll_first_player_info);
            teamSecondView = view.findViewById(R.id.ll_second_player_info);
            centralLineImage = view.findViewById(R.id.ll_imv_center);
            centralCircularImage = (ImageView) view.findViewById(R.id.iv_time_line_image);
            gameStartImage = (ImageView) view.findViewById(R.id.iv_centre_image);
            tvTimeInterval = (TextView) view.findViewById(R.id.tv_time_interval);
            upperDotView = view.findViewById(R.id.iv_dot_upper);
        }
    }
}