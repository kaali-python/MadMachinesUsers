package com.sports.unity.common.controller.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sports.unity.R;
import com.sports.unity.common.controller.AdvancedFilterActivity;
import com.sports.unity.common.controller.MainActivity;
import com.sports.unity.common.model.FavouriteContentHandler;
import com.sports.unity.common.model.FavouriteItem;
import com.sports.unity.common.model.FontTypeface;
import com.sports.unity.common.model.UserUtil;
import com.sports.unity.util.Constants;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by Mad on 12/28/2015.
 */
public class FilterByTagFragment extends Fragment implements AdvancedFilterActivity.onSearchListener, FavouriteContentHandler.ListPreparedListener {
    private RecyclerView filterRecyclerView;
    private ArrayList<FavouriteItem> itemDataSet;
    private ArrayList<Boolean> checkBoxState;
    private Bundle bundle;
    private String SPORTS_FILTER_TYPE, SPORTS_TYPE;
    private FavouriteContentHandler favouriteContentHandler;
    private FilterRecycleAdapter itemAdapter;
    ArrayList<FavouriteItem> searchList;
    private boolean isSearchRequested;
    private LinearLayout errorLayout;
    private ProgressBar progressBar;
    private TextView messageView;
    private final String errorMessage = "Something went wrong";
    private final String noResultMessage = "No result found";
    private boolean isFilterCompleted;
    private boolean isFromNav;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();

        favouriteContentHandler = FavouriteContentHandler.getInstance(getActivity());

        SPORTS_FILTER_TYPE = bundle.getString(Constants.SPORTS_FILTER_TYPE);
        SPORTS_TYPE = bundle.getString(Constants.SPORTS_TYPE);
        isFilterCompleted = UserUtil.isFilterCompleted();
        isFromNav = ((AdvancedFilterActivity) getActivity()).isFromNav;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((AdvancedFilterActivity) getActivity()).removeEditClickListener(this);
        favouriteContentHandler.searchNum = 0;
        favouriteContentHandler.onPause();
        favouriteContentHandler.removePreparedListener(this);
        hideProgress();
        hideErrorLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        favouriteContentHandler.onResume();
        ((AdvancedFilterActivity) getActivity()).addEditClickListener(this);
        favouriteContentHandler.addPreparedListener(this);
        if (!favouriteContentHandler.isDisplay) {
            showProgress();
            favouriteContentHandler.makeRequest();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.filterbytag_fargment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setUpRecyclerView(view);
    }

    private void initViews(View view) {
        LinearLayout textPlayer = (LinearLayout) view.findViewById(R.id.text);

        if(!SPORTS_FILTER_TYPE.equals(Constants.FILTER_TYPE_PLAYER)) {
            textPlayer.setVisibility(View.GONE);
        }else {
            //nothing
        }

        errorLayout = (LinearLayout) view.findViewById(R.id.error);
        TextView oops = (TextView) errorLayout.findViewById(R.id.oops);
        oops.setTypeface(FontTypeface.getInstance(getActivity()).getRobotoLight());

        messageView = (TextView) errorLayout.findViewById(R.id.something_wrong);
        messageView.setTypeface(FontTypeface.getInstance(getActivity()).getRobotoLight());
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBar.getIndeterminateDrawable().setColorFilter(getActivity().getResources().getColor(R.color.app_theme_blue), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void setUpRecyclerView(View view) {
        filterRecyclerView = (RecyclerView) view.findViewById(R.id.recycle_view);
        filterRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        filterRecyclerView.setLayoutManager(mLayoutManager);
        progressBar.setVisibility(View.VISIBLE);
        if (favouriteContentHandler.isDisplay) {
            hideProgress();
            prepareList();
        }
    }

    /**
     * prepare the corresponding favourite list according to the filter type e.g. League, Team or Player.
     */
    private void prepareList() {


        if (SPORTS_FILTER_TYPE.equals(Constants.FILTER_TYPE_TEAM)) {

            if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_CRICKET)) {
                itemDataSet = favouriteContentHandler.getFavCricketTeams();

            } else if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_FOOTBALL)) {
                itemDataSet = favouriteContentHandler.getFavFootballTeams();
            }

        } else if (SPORTS_FILTER_TYPE.equals(Constants.FILTER_TYPE_PLAYER)) {

            if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_CRICKET)) {
                itemDataSet = favouriteContentHandler.getFavCricketPlayers();

            } else if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_FOOTBALL)) {
                itemDataSet = favouriteContentHandler.getFavFootballPlayers();

            }

        } else if (SPORTS_FILTER_TYPE.equals(Constants.FILTER_TYPE_LEAGUE)) {
            itemDataSet = favouriteContentHandler.getFavFootballLeagues();

        }
        if (itemDataSet == null || itemDataSet.size() <= 0) {
            hideProgress();
            showErrorLayout(errorMessage);
        } else {
            displayContent();
        }
    }


    /**
     * update the filter list in filter recycle adapter and display.
     */
    private void displayContent() {
        hideErrorLayout();
        filterRecyclerView.setVisibility(View.VISIBLE);
        itemAdapter = new FilterRecycleAdapter(getActivity(), itemDataSet);
        filterRecyclerView.setAdapter(itemAdapter);
    }

    /**
     * handle the search query from the user.
     * marge previous search result.
     * and request current query to network.
     *
     * @param isSearchInitiated true if user searches for a favourite false when user closes the search.
     */
    private void requestSearch(boolean isSearchInitiated, String SearchString) {
        if (isSearchInitiated) {
            showProgress();
            /**
             * Handle previous search result if search is
             not closed by the user and add data to item data set */
            if (isSearchRequested) {
                try {
                    searchList = itemAdapter.getItemDataSet();
                    if (searchList.size() > 0) {
                        try {
                            for (FavouriteItem f : searchList) {
                                if (f.isChecked()) {
                                    for (int index = 0; index < itemDataSet.size(); index++) {
                                        FavouriteItem f1 = itemDataSet.get(index);
                                        if (f1.getName().equals(f.getName())) {
                                            itemDataSet.remove(index);
                                        }
                                    }
                                    itemDataSet.add(0, f);
                                } else if (!f.isChecked()) {
                                    for (int index = 0; index < itemDataSet.size(); index++) {
                                        FavouriteItem f1 = itemDataSet.get(index);
                                        if (f1.getName().equals(f.getName())) {
                                            if (f1.isChecked()) {
                                                f1.setChecked(false);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (ConcurrentModificationException e) {
                            hideErrorLayout();
                        }
                    }
                } catch (NullPointerException e) {
                    showErrorLayout(errorMessage);
                }

            }
            isSearchRequested = true;
            favouriteContentHandler.requestFavSearch(SPORTS_FILTER_TYPE, SPORTS_TYPE, SearchString);

        } else if (isSearchRequested) {
            searchList = itemAdapter.getItemDataSet();
            try {
                if (searchList.size() > 0) {
                    try {
                        for (FavouriteItem f : searchList) {
                            if (f.isChecked()) {
                                for (int index = 0; index < itemDataSet.size(); index++) {
                                    FavouriteItem f1 = itemDataSet.get(index);
                                    if (f1.getName().equals(f.getName())) {
                                        itemDataSet.remove(index);
                                    }
                                }
                                itemDataSet.add(0, f);
                            } else if (!f.isChecked()) {
                                for (int index = 0; index < itemDataSet.size(); index++) {
                                    FavouriteItem f1 = itemDataSet.get(index);
                                    if (f1.getName().equals(f.getName())) {
                                        if (f1.isChecked()) {
                                            f1.setChecked(false);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ConcurrentModificationException e) {
                        filterRecyclerView.setVisibility(View.VISIBLE);
                        hideErrorLayout();
                    }
                    isSearchRequested = false;
                    hideErrorLayout();
                    filterRecyclerView.setVisibility(View.VISIBLE);
                    itemAdapter.setItemDataSet(itemDataSet);
                }
            } catch (NullPointerException e) {
                showErrorLayout(errorMessage);
            }
        } else {
            displayContent();
        }
    }

    /**
     * handles the search result and get the corresponding list from FavouriteContentHandler
     * and updates the filter recycler adapter on success otherwise enables the error layout.
     */
    public void displaySearchResult() {
        searchList = new ArrayList<FavouriteItem>();
        if (SPORTS_FILTER_TYPE.equals(Constants.FILTER_TYPE_TEAM)) {
            if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_CRICKET)) {
                searchList = favouriteContentHandler.getSearchedCricketTeam();

            } else if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_FOOTBALL)) {
                searchList = favouriteContentHandler.getSearchedFootballTeam();
            }

        } else if (SPORTS_FILTER_TYPE.equals(Constants.FILTER_TYPE_PLAYER)) {

            if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_CRICKET)) {
                searchList = favouriteContentHandler.getSearchedCricketPlayer();

            } else if (SPORTS_TYPE.equals(Constants.SPORTS_TYPE_FOOTBALL)) {
                searchList = favouriteContentHandler.getSearchedFootballPlayer();

            }

        } else if (SPORTS_FILTER_TYPE.equals(Constants.FILTER_TYPE_LEAGUE)) {
            searchList = favouriteContentHandler.getSearchedFootballLeague();

        }
        try {
            if (searchList.size() <= 0) {
                showErrorLayout(noResultMessage);
            } else {
                for (FavouriteItem f : ((AdvancedFilterActivity) getActivity()).favList) {
                    if (f.isChecked() && searchList.contains(f)) {
                        for (int index = 0; index < searchList.size(); index++) {
                            FavouriteItem f1 = searchList.get(index);
                            if (f1.getName().equals(f.getName())) {
                                searchList.remove(index);
                            }
                        }
                        searchList.add(0, f);
                    }
                }
                itemAdapter.setItemDataSet(searchList);
                filterRecyclerView.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e) {
            showErrorLayout(noResultMessage);
        }
    }

    /**
     * display the error layout.
     *
     * @param message error message.
     */
    private void showErrorLayout(String message) {
        filterRecyclerView.setVisibility(View.INVISIBLE);
        errorLayout.setVisibility(View.VISIBLE);
        messageView.setText(message);
        errorLayout.requestLayout();
    }

    /**
     * hides error layout.
     */
    private void hideErrorLayout() {
        filterRecyclerView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    /**
     * show progress dialog.
     */
    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);

    }

    /**
     * hide progress dialog.
     */
    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    /**
     * callback for search fired from MainActivity.
     *
     * @param isSearchInitiated True if user searches for a favourite. False when user closes the search.
     */
    @Override
    public void onSearch(boolean isSearchInitiated, String searchString) {
        requestSearch(isSearchInitiated, searchString);

    }

    /**
     * Implementation of ListPrepared listener callback.
     * callback from List Prepared listener attached to FavouriteContentHandler.
     *
     * @param isPrepared success
     * @param message    error message.
     */
    @Override
    public void onListPrepared(Boolean isPrepared, String message) {
        if (!isSearchRequested) {
            if (isPrepared) {
                prepareList();
                hideErrorLayout();
                hideProgress();
            } else {
                hideProgress();
                showErrorLayout(message);
            }
        } else {
            if (isPrepared) {
                hideErrorLayout();
                hideProgress();
                displaySearchResult();
            } else {
                hideProgress();
                showErrorLayout(message);
            }
        }
    }
}
