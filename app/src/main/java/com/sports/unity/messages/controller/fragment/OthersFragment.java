package com.sports.unity.messages.controller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sports.unity.R;
import com.sports.unity.messages.controller.viewhelper.OnSearchViewQueryListener;

/**
 * Created by madmachines on 23/9/15.
 */
public class OthersFragment extends Fragment implements OnSearchViewQueryListener {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_others, container, false);
        return v;
    }

    @Override
    public void onSearchQuery(String filterText) {

    }
}
