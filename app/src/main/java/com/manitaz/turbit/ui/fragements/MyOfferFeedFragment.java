package com.manitaz.turbit.ui.fragements;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.adaptor.OfferAdaptor;
import com.manitaz.turbit.model.NoResultFound;
import com.manitaz.turbit.model.OfferModel;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.utill.AppConstants;
import com.manitaz.turbit.utill.GPSTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOfferFeedFragment extends Fragment implements OfferAdaptor.ItemClickCallback{
    ProgressDialog progressDialog;
    public List<Object> offers = new ArrayList<Object>();
    private OfferAdaptor offerAdaptor;
    private RecyclerView recView;

    private boolean networkRequest = false;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public MyOfferFeedFragment() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Offers");
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("onQueryTextChange", newText);

                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("manitaz", "OnQuery submit");
                    if(!networkRequest){
                        networkRequest = true;
                        progressDialog = new ProgressDialog(getContext());
                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        progressDialog.setContentView(R.layout.progress_layout);

                        getOfferSearchData(query);
                    }
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_my_offer_feed, container, false);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress_layout);

        recView = inflate.findViewById(R.id.rec_list);

        recView.setLayoutManager(new LinearLayoutManager(getContext()));
        offerAdaptor = new OfferAdaptor(offers, getContext());
        recView.setAdapter(offerAdaptor);

        offerAdaptor.setItemClickCallback(this);

        boolean locationEnabled = checkLocationPermission();
        if(locationEnabled){
            getOfferData();
        }

        return inflate;
    }

    public void getOfferSearchData(String queryText){
        offers.clear();
        SharedPreferences pref = getContext().getSharedPreferences(AppConstants.PREF_PACKAGE, 0);
        int userId = pref.getInt(AppConstants.PREF_USER_ID, 0);
        String userToken = pref.getString(AppConstants.PREF_USER_TOKEN, "");

        String initUrl = BuildConfig.SERVICE_BASE_URL+ "auth/search";
        Log.d("manitaz", "url : "+ initUrl);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", userId);
            jsonObject.put("searchText", queryText);

            Log.d("manitaz", "data created" + jsonObject.toString());
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    initUrl,
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("manitaz", "Response :"+ response);
                            try {
                                progressDialog.dismiss();
                                networkRequest = false;
                                JSONArray offerData = response.getJSONArray("offers");
                                for (int i = 0; i < offerData.length(); i++) {
                                    JSONObject offerObj = offerData.getJSONObject(i);
                                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                                    OfferModel offer = gson.fromJson(String.valueOf(offerObj), OfferModel.class);
                                    offers.add(offer);
                                }

                                if (offers.size() == 0) {
                                    Log.d("manitaz", "offers 0 ");
                                    offers.add(new NoResultFound());
                                }

                                offerAdaptor.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            networkRequest = false;
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", "Bearer"+ " "+ userToken);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance().addToRequestQueue(req);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getOfferData(){
        GPSTracker gps = new GPSTracker(getContext());
        // Check if GPS enabled
        if(gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            offers.clear();
            SharedPreferences pref = getContext().getSharedPreferences(AppConstants.PREF_PACKAGE, 0);
            int userId = pref.getInt(AppConstants.PREF_USER_ID, 0);
            String userToken = pref.getString(AppConstants.PREF_USER_TOKEN, "");

            String initUrl = BuildConfig.SERVICE_BASE_URL+ "auth/pull";
            Log.d("manitaz", "url : "+ initUrl);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", userId);
                JSONObject latLon = new JSONObject();
                latLon.put("lat", latitude);
                latLon.put("lon", longitude);

//                latLon.put("lat", 6.9946);
//                latLon.put("lon", 79.8954);
                jsonObject.putOpt("location", latLon);

                Log.d("manitaz", "data created" + jsonObject.toString());
                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
                        initUrl,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("manitaz", "Response :"+ response);
                                try {
                                    progressDialog.dismiss();
                                    JSONArray offerData = response.getJSONArray("offers");
                                    for (int i = 0; i < offerData.length(); i++) {
                                        JSONObject offerObj = offerData.getJSONObject(i);
                                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                                        OfferModel offer = gson.fromJson(String.valueOf(offerObj), OfferModel.class);
                                        offers.add(offer);
                                    }

                                    if (offers.size() == 0) {
                                        Log.d("manitaz", "offers 0 ");
                                        offers.add(new NoResultFound());
                                    }

                                    Log.d("manitaz", "Offers count : "+ offers.size() );

                                    offerAdaptor.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Authorization", "Bearer"+ " "+ userToken);
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                VolleySingleton.getInstance().addToRequestQueue(req);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }
    }

    @Override
    public void onItemClick(int p) {

    }

    @Override
    public void onChangeWishListStatus(int p) {
        offerAdaptor.notifyDataSetChanged();
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Required")
                        .setMessage("Turbit needs your location to give optimal results.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        getOfferData();
                    }

                } else {

                }
                return;
            }

        }
    }
}
