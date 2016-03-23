package com.sports.unity.messages.controller.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.sports.unity.Database.SportsUnityDBHelper;
import com.sports.unity.R;
import com.sports.unity.XMPPManager.XMPPClient;
import com.sports.unity.common.controller.CustomAppCompatActivity;
import com.sports.unity.common.controller.SettingsActivity;
import com.sports.unity.common.model.ContactsHandler;
import com.sports.unity.common.model.FontTypeface;
import com.sports.unity.common.model.TinyDB;
import com.sports.unity.common.model.UserUtil;
import com.sports.unity.messages.controller.model.Contacts;
import com.sports.unity.messages.controller.model.NearByUserJsonCaller;
import com.sports.unity.messages.controller.model.PeoplesNearMe;
import com.sports.unity.messages.controller.model.Person;
import com.sports.unity.scores.model.ScoresContentHandler;
import com.sports.unity.scores.model.ScoresJsonParser;
import com.sports.unity.util.Constants;
import com.sports.unity.util.network.LocManager;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;
import static com.sports.unity.common.model.TinyDB.KEY_CURRENT_LATITUDE;
import static com.sports.unity.common.model.TinyDB.KEY_CURRENT_LONGITUDE;
import static com.sports.unity.common.model.TinyDB.KEY_PASSWORD;
import static com.sports.unity.common.model.TinyDB.KEY_USER_JID;
import static com.sports.unity.common.model.TinyDB.getInstance;
import static com.sports.unity.scores.model.ScoresContentHandler.CALL_NAME_NEAR_BY_USERS;
import static com.sports.unity.scores.model.ScoresContentHandler.PARAM_LATITUDE;
import static com.sports.unity.scores.model.ScoresContentHandler.PARAM_LONGITUDE;
import static com.sports.unity.scores.model.ScoresContentHandler.PARAM_PASSWORD;
import static com.sports.unity.scores.model.ScoresContentHandler.PARAM_RADIUS;
import static com.sports.unity.scores.model.ScoresContentHandler.PARAM_USERNAME;
import static com.sports.unity.util.CommonUtil.getBuildConfig;
import static com.sports.unity.util.CommonUtil.getDeviceId;
import static com.sports.unity.util.Constants.REQUEST_PARAMETER_KEY_APK_VERSION;
import static com.sports.unity.util.Constants.REQUEST_PARAMETER_KEY_UDID;

public class PeopleAroundMeMap extends CustomAppCompatActivity implements PeopleService, ClusterManager.OnClusterClickListener<Person>, ClusterManager.OnClusterItemClickListener<Person> {

    private static final String REQUEST_LISTENER_KEY = "nearby_key";
    private static final String REQUEST_TAG = "nearby_tag";
    private static final String SPORT_SELECTION_FOOTBALL = "football";
    private static final String SPORT_SELECTION_CRICKET = "cricket";

    private IconGenerator clusterIconGenerator;
    private ImageView icon;
    private Dialog aDialog = null;

    private NearByUserJsonCaller nearByUserJsonCaller = new NearByUserJsonCaller();

    private GoogleMap map;
    private LatLng latLong;
    private String sportSelection = SPORT_SELECTION_FOOTBALL;

    private Toolbar toolbar;
    private TextView titleAddress;
    private TextView titleCity;
    private boolean customLocation;
    private LocManager locManager;
    private int radius = 1000;

    private int stepRange = 25;

    private boolean profilefetching = false;
    private ProgressDialog dialog = null;

    private PeoplesNearMe peoplesNearMe;
    private ClusterManager<Person> mClusterManager;
    private ScoresContentHandler.ContentListener contentListener = new ScoresContentHandler.ContentListener() {

        @Override
        public void handleContent(String tag, String content, int responseCode) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLong);
            icon.setImageResource(R.drawable.ic_marker_my_location);
            clusterIconGenerator.setBackground(icon.getDrawable());
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(clusterIconGenerator.makeIcon("")));
            map.addMarker(markerOptions);
            if (tag.equals(REQUEST_TAG)) {
                if (responseCode == 200) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    peoplesNearMe = ScoresJsonParser.parseListOfNearByUsers(content);
                    List<Person> people = peoplesNearMe.getPersons();
                    if (people.size() > 1) {
                        plotMarkers(people, sportSelection);
                    } else {
                        map.moveCamera(CameraUpdateFactory.zoomTo(calculateZoomLevel(radius)));
                        LayoutInflater inflater = PeopleAroundMeMap.this.getLayoutInflater();
                        View view = inflater.inflate(R.layout.chat_other_profile_layout, null);
//                        AlertDialog.Builder otherProfileBuilder = new AlertDialog.Builder(PeopleAroundMeMap.this);
//                        otherProfileBuilder.setView(view);
//                        aDialog = otherProfileBuilder.create();
//                        aDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                        aDialog.show();
                        aDialog.setContentView(R.layout.chat_other_profile_layout);
                        aDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        aDialog.show();
                        populateProfilePopup(null, view, null, 0, null, null);
                    }

                } else {
                    if (dialog != null) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                    makeText(getApplicationContext(), "Something went wrong please try again", LENGTH_LONG).show();
                }
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_around_me_map);
        this.customLocation = false;
        aDialog = new Dialog(PeopleAroundMeMap.this);
        aDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.clusterIconGenerator = new IconGenerator(getApplicationContext());
        View view = getLayoutInflater().inflate(R.layout.cluster_view, null);
        icon = (ImageView) view.findViewById(R.id.cluster_icon);
        this.clusterIconGenerator.setContentView(view);
        locManager = LocManager.getInstance(getApplicationContext());
        locManager.buildApiClient();
        hideSoftKeyboard();
        initToolbar();
        // openMap();
        InitSeekbar();
        setsportSelectionButtons();
        setCustomButtonsForNavigationAndUsers();
        bindAutoComplete();

    }

    private void bindAutoComplete() {
        PlaceAutocompleteFragment fragment = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.custom_location);
        fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                customLocation = true;
                findViewById(R.id.fl_custom_location).setVisibility(GONE);
                getInstance(getApplicationContext()).putDouble(KEY_CURRENT_LATITUDE, place.getLatLng().latitude);
                getInstance(getApplicationContext()).putDouble(KEY_CURRENT_LONGITUDE, place.getLatLng().longitude);
                titleAddress.setText(place.getAddress());
                titleCity.setText(place.getName());
                latLong = place.getLatLng();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, getcurrentZoom()));
               // getPeopleAroundMe(place.getLatLng().latitude, place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                customLocation = false;
                findViewById(R.id.fl_custom_location).setVisibility(GONE);
                Toast.makeText(getApplicationContext(), getText(R.string.oops_try_again), LENGTH_LONG).show();
            }
        });
    }


    private void setCustomButtonsForNavigationAndUsers() {
        ImageView myLocation = (ImageView) findViewById(R.id.myLocation);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customLocation = false;
                boolean success = checkIfGPSEnabled();
                if (success) {
                    getLocation();
                }
            }
        });
        ImageView refreshUsers = (ImageView) findViewById(R.id.refreshUsers);
        refreshUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPeopleAroundMe(latLong.latitude, latLong.longitude);
            }
        });
    }

    private void setsportSelectionButtons() {
        final Button cricket = (Button) findViewById(R.id.people_cricket_interest);
        final Button football = (Button) findViewById(R.id.people_football_interest);
        cricket.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoCondensedRegular());
        football.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoCondensedRegular());
        football.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                LinearLayout sportSwitchLAyout = (LinearLayout) findViewById(R.id.switch_sports);
                sportSwitchLAyout.setBackground(getResources().getDrawable(R.drawable.btn_s1_focused));
                sportSelection = SPORT_SELECTION_FOOTBALL;
                football.setTextColor(Color.WHITE);
                cricket.setTextColor(getResources().getColor(R.color.app_theme_blue));
                if (peoplesNearMe != null) {
                    plotMarkers(peoplesNearMe.getPersons(), sportSelection);
                }
            }
        });

        cricket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                LinearLayout sportSwitchLAyout = (LinearLayout) findViewById(R.id.switch_sports);
                sportSwitchLAyout.setBackground(getResources().getDrawable(R.drawable.btn_s2_focused));
                sportSelection = SPORT_SELECTION_CRICKET;
                cricket.setTextColor(Color.WHITE);
                football.setTextColor(getResources().getColor(R.color.app_theme_blue));
                if (peoplesNearMe != null) {
                    plotMarkers(peoplesNearMe.getPersons(), sportSelection);
                }
            }
        });
    }

    private void InitSeekbar() {
        TextView distanceText = (TextView) findViewById(R.id.distance_text);
        distanceText.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoCondensedBold());
        SeekBar seekDistance = (SeekBar) findViewById(R.id.distance_seekbar);
        seekDistance.setMax(100);
        seekDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress >= 0 && progress <= stepRange / 2) {
                    seekBar.setProgress(0);
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_distance_slider_01));
                    radius = 1000;

                    map.animateCamera(CameraUpdateFactory.zoomTo(calculateZoomLevel(radius)));
                    fetchUsersNearByWithNewRadius();

                } else if (progress >= 0 * stepRange + stepRange / 2 && progress <= 0 * stepRange + stepRange / 2 + stepRange) {
                    seekBar.setProgress(1 * stepRange);
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_distance_slider_05));
                    radius = 5000;
                    map.animateCamera(CameraUpdateFactory.zoomTo(calculateZoomLevel(radius)));
                    fetchUsersNearByWithNewRadius();
                } else if (progress >= 1 * stepRange + stepRange / 2 && progress <= 1 * stepRange + stepRange / 2 + stepRange) {
                    seekBar.setProgress(2 * stepRange);
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_distance_slider_20));
                    radius = 20000;
                    map.animateCamera(CameraUpdateFactory.zoomTo(calculateZoomLevel(radius)));
                    fetchUsersNearByWithNewRadius();
                } else if (progress >= 2 * stepRange + stepRange / 2 && progress <= 2 * stepRange + stepRange / 2 + stepRange) {
                    seekBar.setProgress(3 * stepRange);
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_distance_slider_30));
                    radius = 30000;
                    map.animateCamera(CameraUpdateFactory.zoomTo(calculateZoomLevel(radius)));
                    fetchUsersNearByWithNewRadius();
                } else {
                    seekBar.setProgress(4 * stepRange);
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_distance_slider_40));
                    radius = 40000;

                    map.animateCamera(CameraUpdateFactory.zoomTo(calculateZoomLevel(radius)));
                    fetchUsersNearByWithNewRadius();
                }

            }

        });
    }

    private int calculateZoomLevel(int radius) {
        int zoomLevel = 0;
        Circle circle = map.addCircle(new CircleOptions().center(latLong).radius(radius).strokeColor(Color.TRANSPARENT));
        circle.setVisible(true);
        if (circle != null) {
            double r = circle.getRadius();
            double scale = r / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        Log.i("zoomlevel", String.valueOf(zoomLevel));
        return zoomLevel;
    }

    private void fetchUsersNearByWithNewRadius() {
        getPeopleAroundMe(latLong.latitude, latLong.longitude);
    }

    private void plotMarkers(List<Person> persons, String sportSelection) {
        ArrayList<Marker> markers = new ArrayList<>();
        Log.i("plottingmarkers", "true");
        mClusterManager.clearItems();
        mClusterManager.addItems(persons);
        mClusterManager.cluster();
//        for (Person person : persons) {
//            MarkerOptions markerOption = new MarkerOptions();
//            markerOption.position(person.getPosition());
//            if (friend) {
//                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_my_friends));
//            } else {
//                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_su_users));
//            }
//            Marker marker = null;
//            if (person.getUsername().equalsIgnoreCase(getInstance(getApplicationContext()).getString(KEY_USERNAME))) {
//                if (sportSelection.equalsIgnoreCase(SPORT_SELECTION_FOOTBALL)) {
//                    markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mrkr_fball));
//                } else {
//                    markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mrkr_cri));
//                }
//                marker = map.addMarker(markerOption);
//                marker.setDraggable(true);
//            } else {
//                marker = map.addMarker(markerOption);
//            }
//
//            markers.add(marker);
//            mClusterManager.addItem(person);
//        }
//        for (JSONObject user : users) {
//            nearByUserJsonCaller.setJsonObject(user);
//            try {
//                String interests = nearByUserJsonCaller.getInterests();
//                if (interests.contains(sportSelection)) {
//                    double latitude = nearByUserJsonCaller.getLatitude();
//                    double longitude = nearByUserJsonCaller.getLongitude();
//                    MarkerOptions markerOption = new MarkerOptions().position(new LatLng(latitude, longitude));
//                    if (sportSelection.equals(SPORT_SELECTION_FOOTBALL)) {
//                        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mrkr_fball));
//                    } else {
//                        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mrkr_cri));
//                    }
//                    markerOption.snippet(nearByUserJsonCaller.getUsername() + "," + nearByUserJsonCaller.getDistance());
//                    if (nearByUserJsonCaller.getUsername().equals(getInstance(getApplicationContext()).getString(TinyDB.KEY_USERNAME))) {
//                        //nothing
//                    } else {
//                        Marker marker = map.addMarker(markerOption);
//                        markers.add(marker);
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
        animateCamera(markers);
    }

    private void animateCamera(ArrayList<Marker> markers) {
        if (markers != null) {
            int counter = 0;
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
                counter++;
            }
            if (counter > 0) {
                LatLngBounds bounds = builder.build();
                int padding = 10; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.animateCamera(cu);
                makeText(PeopleAroundMeMap.this, markers.size() + " people around you", Toast.LENGTH_SHORT).show();
            }
        }


    }


    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.tool_bar_map);
        titleAddress = (TextView) toolbar.findViewById(R.id.toolbar_title);
        titleAddress.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoRegular());
        titleCity = (TextView) toolbar.findViewById(R.id.secondary_title);
        titleCity.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoRegular());
        setCurrentAddressOnToolbar(titleAddress, titleCity);
        //toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.findViewById(R.id.close_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.findViewById(R.id.privacy_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getIntent();


              //  makeText(getApplicationContext(), "Privacy Policy work in progress", LENGTH_LONG).show();


                    checkAndEnableLocation();




            }
        });
        toolbar.findViewById(R.id.myaddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.fl_custom_location).setVisibility(VISIBLE);
            }
        });

    }

    private void checkAndEnableLocation() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra(Constants.ENABLE_LOCATION, Constants.CHECK_LOCATION);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("After Permission" + intent, "onActivityResult: " + requestCode);
        if (requestCode == Constants.REQUEST_CODE_LOCATION_PERMISSION) {
            if (resultCode == RESULT_OK) {
                Log.d("After Permission", "onActivityResult: ");
            }
        }
    }
    private void setCurrentAddressOnToolbar(TextView titleAddress, TextView titleCity) {
        titleAddress.setText(getInstance(getApplicationContext()).getString(TinyDB.KEY_ADDRESS_LOCATION));
        titleCity.setText(getInstance(getApplicationContext()).getString(TinyDB.KEY_ADDRESS_STATE));
    }

    private void loadMap() {
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {


                    openMap(googleMap);


            }
        });
    }

    private void openMap(final GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                getPeopleAroundMe(position.latitude, position.longitude);
            }
        });
        map.getUiSettings().setZoomGesturesEnabled(true);
        try {
            map.setMyLocationEnabled(false);
        } catch (SecurityException ex) {

        }
        mClusterManager = new ClusterManager<Person>(getApplicationContext(), map);
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(new CustomClusterRenderer(PeopleAroundMeMap.this, map, mClusterManager));
        mClusterManager.setOnClusterItemClickListener(this);
        hideLocationbutton();
        map.setTrafficEnabled(false);
        double latitude = getInstance(getApplicationContext()).getDouble(KEY_CURRENT_LATITUDE, 0.0);
        double longitude = getInstance(getApplicationContext()).getDouble(KEY_CURRENT_LONGITUDE, 0.0);
        latLong = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, calculateZoomLevel(radius)));
        if (checkIfGPSEnabled()) {
            getPeopleAroundMe(latitude, longitude);
        }
    }


    private void hideLocationbutton() {
        View mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getView();
        View btnMyLocation = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        btnMyLocation.setVisibility(View.INVISIBLE);
    }

    private void getPeopleAroundMe(double latitude, double longitude) {

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.app_theme_blue), android.graphics.PorterDuff.Mode.MULTIPLY);

        map.clear();
        mClusterManager.clearItems();
        dialog = ProgressDialog.show(PeopleAroundMeMap.this, "",
                "fetching...", true);
        dialog.setIndeterminateDrawable(progressBar.getIndeterminateDrawable());
        ScoresContentHandler.getInstance().addResponseListener(contentListener, REQUEST_LISTENER_KEY);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(PARAM_USERNAME, getInstance(getApplicationContext()).getString(KEY_USER_JID));
        parameters.put(PARAM_PASSWORD, getInstance(getApplicationContext()).getString(KEY_PASSWORD));
        parameters.put(PARAM_LATITUDE, String.valueOf(latitude));
        parameters.put(PARAM_LONGITUDE, String.valueOf(longitude));
        parameters.put(PARAM_RADIUS, String.valueOf(radius));
        parameters.put(REQUEST_PARAMETER_KEY_APK_VERSION, getBuildConfig());
        parameters.put(REQUEST_PARAMETER_KEY_UDID, getDeviceId(this));
        ScoresContentHandler.getInstance().requestCall(CALL_NAME_NEAR_BY_USERS, parameters, REQUEST_LISTENER_KEY, REQUEST_TAG);
    }

    private boolean createContact(String jid, Context context, VCard vCard) {
        boolean success = false;
        SportsUnityDBHelper.getInstance(context).addToContacts(vCard.getNickName(), null, jid, ContactsHandler.getInstance().defaultStatus, null, Contacts.AVAILABLE_BY_PEOPLE_AROUND_ME);
        SportsUnityDBHelper.getInstance(context).updateContacts(jid, vCard.getAvatar(), vCard.getMiddleName());
        return success;
    }

    private void renderProfile(final Person person) {
        LayoutInflater inflater = PeopleAroundMeMap.this.getLayoutInflater();
        View popupProfile = inflater.inflate(R.layout.chat_other_profile_layout, null);

//        AlertDialog.Builder otherProfileBuilder = new AlertDialog.Builder(PeopleAroundMeMap.this);
//        otherProfileBuilder.setView(popupProfile);
        aDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                profilefetching = false;
            }

        });
//        otherProfileBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                profilefetching = false;
//            }
//
//        });

        aDialog.setContentView(R.layout.chat_other_profile_layout);
        aDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        aDialog.show();

        aDialog.findViewById(R.id.progressBarProfile).setVisibility(VISIBLE);

//        int distance = (int) Math.round(Double.parseDouble(marker.getSnippet().substring(marker.getSnippet().indexOf(",") + 1, marker.getSnippet().length())));
        int distance = (int) (person.getDistance());
        new GetVcardForUser(popupProfile, distance, person).execute(person.getUsername());

    }

    @Override
    public boolean showProfile(Person person) {
        if (profilefetching == true) {
            //nothing
        } else {
            profilefetching = true;
            renderProfile(person);
        }
        return false;
    }

    @Override
    public void showCluster(Cluster<Person> cluster) {

    }

    private void populateProfilePopup(final VCard vCard, View popupProfile, final String jid, int distance, String info, Person person) {

        CircleImageView imageview = (CircleImageView) aDialog.findViewById(R.id.user_pic);

        TextView distancefromUser = (TextView) aDialog.findViewById(R.id.distanceFromMe);
        distancefromUser.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoMedium());

        TextView sport = (TextView) aDialog.findViewById(R.id.sport);
        sport.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoRegular());

        TextView name = (TextView) aDialog.findViewById(R.id.username);
        name.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoCondensedBold());

        Button sayHello = (Button) aDialog.findViewById(R.id.start_chat);
        sayHello.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoRegular());

        ImageView dismissDialog = (ImageView) aDialog.findViewById(R.id.close_icon);
        dismissDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
            }
        });
        aDialog.findViewById(R.id.progressBarProfile).setVisibility(GONE);

        if (vCard == null) {
            if (info == null) {
                sayHello.setVisibility(GONE);
                aDialog.findViewById(R.id.dot).setVisibility(GONE);
                sport.setVisibility(GONE);

                name.setText(R.string.no_users_nearby);
                distancefromUser.setText(R.string.try_broadening_search_increasing_radius);
                imageview.setImageResource(R.drawable.img_no_frnd_found);
            } else {
                sayHello.setVisibility(GONE);
                aDialog.findViewById(R.id.dot).setVisibility(GONE);
                sport.setVisibility(GONE);

                name.setText("Whoops!!");
                distancefromUser.setText(info);
                imageview.setImageResource(R.drawable.img_no_frnd_found);
            }

        } else {
            name.setText(vCard.getNickName());
            byte[] image = vCard.getAvatar();
            if (image != null) {
                imageview.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            } else {
                // nothing
            }
            if (distance > 1000) {
                float dist = distance /= 1000;
                distancefromUser.setText(String.valueOf(dist) + " kms ");
            } else {
                distancefromUser.setText(String.valueOf(distance) + " mts ");
            }


            sport.setText(" " + sportSelection);

            sayHello.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contacts contact = SportsUnityDBHelper.getInstance(getApplicationContext()).getContactByJid(jid);
                    if (contact == null) {
                        createContact(jid, getApplicationContext(), vCard);
                        contact = SportsUnityDBHelper.getInstance(getApplicationContext()).getContactByJid(jid);
                        moveToChatActivity(contact, false);
                    } else {
                        moveToChatActivity(contact, true);
                    }
                }
            });
        }
    }

    private void moveToChatActivity(Contacts contact, boolean contactAvailable) {
        String number = contact.jid;
        String name = contact.name;
        long contactId = contact.id;
        byte[] userPicture = contact.image;
        boolean nearbyChat = false;

        String groupServerId = SportsUnityDBHelper.DEFAULT_GROUP_SERVER_ID;
        long chatId = SportsUnityDBHelper.getInstance(getApplicationContext()).getChatEntryID(contactId, groupServerId);
        if (chatId == SportsUnityDBHelper.DEFAULT_ENTRY_ID) {
            if (contactAvailable) {
                //create chat entry inside ChatScreenActivity only
            } else {
                nearbyChat = true;
                chatId = SportsUnityDBHelper.getInstance(getApplicationContext()).createChatEntry(name, contactId, nearbyChat);
            }

        }
        boolean blockStatus = SportsUnityDBHelper.getInstance(getApplicationContext()).isChatBlocked(contactId);
        boolean othersChat = contact.isOthers();

        Intent intent = ChatScreenActivity.createChatScreenIntent(this, number, name, contactId, chatId, groupServerId, userPicture, blockStatus, othersChat);
        startActivity(intent);
    }

    private boolean checkIfGPSEnabled() {
        boolean succcess = false;
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            showDialogToPromptEnableGps();
        } else {
            succcess = true;
        }
        return succcess;
    }

    private void showDialogToPromptEnableGps() {
        LayoutInflater inflater = PeopleAroundMeMap.this.getLayoutInflater();
        View enableGps = inflater.inflate(R.layout.dialog_enable_gps, null);
        setCustomFont(enableGps);

        aDialog.setContentView(R.layout.dialog_enable_gps);
//        aDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        aDialog.show();

//        AlertDialog.Builder builder = new AlertDialog.Builder(PeopleAroundMeMap.this);
//        builder.setView(enableGps);
//
//        aDialog = builder.create();
//        aDialog.show();

        aDialog.findViewById(R.id.enable_gps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.cancel();
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
    }

    private void setCustomFont(View enableGps) {
        TextView titleGps = (TextView) enableGps.findViewById(R.id.title_gps);
        titleGps.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoCondensedBold());

        TextView gpsStatus = (TextView) enableGps.findViewById(R.id.gps_status);
        gpsStatus.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoRegular());

        TextView turnOnGps = (TextView) enableGps.findViewById(R.id.turn_on_gps);
        turnOnGps.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoRegular());

        Button goToLocationSettings = (Button) enableGps.findViewById(R.id.enable_gps);
        goToLocationSettings.setTypeface(FontTypeface.getInstance(getApplicationContext()).getRobotoRegular());

    }

    private void getLocation() {
        Log.i("gettingLocation", "true");
        Location location = locManager.getLocation();
        if (location != null) {
            LocManager.getInstance(getApplicationContext()).sendLatituteAndLongitude(location, true);
            getInstance(getApplicationContext()).putDouble(KEY_CURRENT_LATITUDE, location.getLatitude());
            getInstance(getApplicationContext()).putDouble(KEY_CURRENT_LONGITUDE, location.getLongitude());
            latLong = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, getcurrentZoom()));
            new FetchAndDisplayCurrentAddress(location).execute();
            getPeopleAroundMe(latLong.latitude, latLong.longitude);
        }

    }

    private float getcurrentZoom() {
        return map.getCameraPosition().zoom;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onResume", "called");
        ScoresContentHandler.getInstance().addResponseListener(contentListener, REQUEST_LISTENER_KEY);

        if (checkIfGPSEnabled()) {

            Location location = LocManager.getInstance(getApplicationContext()).getLocation();
            if (location != null) {
                LocManager.getInstance(getApplicationContext()).sendLatituteAndLongitude(location, true);
            }
        } else {
            //nothing
        }

        loadMap();
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void dismissDialog() {
        if (aDialog != null && aDialog.isShowing()) {
            aDialog.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("onPaused", "called");
        dismissDialog();
        ScoresContentHandler.getInstance().removeResponseListener(REQUEST_LISTENER_KEY);
    }

    private void onUnSuccessfulVcardRetrieval(View view, Person person) {
        String info = "Something went wrong";
        populateProfilePopup(null, view, null, 0, info, person);
        //TODO
    }

    private void onSuccessfulVcardRetrieval(View view, VCard vCard, String jid, int distance, Person person) {
        populateProfilePopup(vCard, view, jid, distance, null, person);
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {

        return false;
    }

    @Override
    public boolean onClusterItemClick(Person person) {
        return showProfile(person);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locManager.connect();
    }

    class FetchAndDisplayCurrentAddress extends AsyncTask<Void, Void, Void> {

        Location location = null;

        public FetchAndDisplayCurrentAddress(Location location) {
            this.location = location;
        }

        @Override
        protected Void doInBackground(Void... params) {
            LocManager.getInstance(getApplicationContext()).saveLocation(location);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            titleAddress.setText(getInstance(getApplicationContext()).getString(TinyDB.KEY_ADDRESS_LOCATION));
            titleCity.setText(getInstance(getApplicationContext()).getString(TinyDB.KEY_ADDRESS_STATE));
            super.onPostExecute(aVoid);
        }
    }

    private class GetVcardForUser extends AsyncTask<String, Void, VCard> {
        private boolean success = false;
        private String jid = null;

        private View view = null;
        private int distance = 0;
        private Person person;

        public GetVcardForUser(View view, int distance, Person person) {
            this.view = view;
            this.distance = distance;
            this.person = person;
        }

        @Override
        protected VCard doInBackground(String... param) {
            XMPPTCPConnection connection = XMPPClient.getInstance().getConnection();
            VCard card = new VCard();
            try {
                jid = param[0];
                if (connection.isAuthenticated()) {
                    card.load(connection, jid + "@mm.io");
                    success = true;
                } else {
                    success = false;
                }
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            }
            return card;
        }

        @Override
        protected void onPostExecute(VCard vCard) {
            if (success) {
                onSuccessfulVcardRetrieval(view, vCard, jid, distance, person);
            } else {
                onUnSuccessfulVcardRetrieval(view, person);
            }
        }

    }

}
