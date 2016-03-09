package com.sports.unity.scores.controller.fragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by madmachines on 8/3/16.
 */
public class MatchListWrapperDTO  implements  Comparable<MatchListWrapperDTO>{
    private String day;
    private ArrayList<JSONObject> list;
    private Long epochTime;
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public ArrayList<JSONObject> getList() {
        return list;
    }

    public void setList(ArrayList<JSONObject> list) {
        this.list = list;
    }

    public Long getEpochTime() {
        return epochTime;
    }

    public void setEpochTime(Long epochTime) {
        this.epochTime = epochTime;
    }

    @Override
    public int compareTo(MatchListWrapperDTO another) {
        return this.epochTime.compareTo(another.epochTime);
    }
}
