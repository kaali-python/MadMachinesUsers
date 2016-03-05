package com.sports.unity.scoredetails;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sports.unity.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpcommingMatchCommentaryFragment extends Fragment {
    private TextView textView;
    public UpcommingMatchCommentaryFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_empty_view, container, false);
        initView(view);
        return view;
    }


    private void initView(View view) {
        textView = (TextView) view.findViewById(R.id.tv_empty_view);
        textView.setText(R.string.commentary_not_exist);
    }


    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();
    }

}
