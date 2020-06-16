package com.manitaz.turbit.ui.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.model.UserDetails;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.ui.fragements.BanksFragment;
import com.manitaz.turbit.ui.fragements.MyOfferFeedFragment;
import com.manitaz.turbit.ui.fragements.PreferenceFragment;
import com.manitaz.turbit.ui.fragements.ProfileFragment;
import com.manitaz.turbit.utill.AppConstants;
import com.manitaz.turbit.utill.UserDetailsHandler;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView emailView, nameView;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);

        emailView = hView.findViewById(R.id.emailTextView);
        nameView = hView.findViewById(R.id.nameView);
        profileImage = hView.findViewById(R.id.profileImageVIew);

        navigationView.setNavigationItemSelectedListener(this);

        if (getIntent().hasExtra("new_user_account")) {
            displaySelectedScreen(R.id.nav_my_profile, true);
        } else {
            displaySelectedScreen(R.id.nav_offer_feed, false);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportFragmentManager().findFragmentById(R.id.content_frame).onResume();
            }
        });

        UserDetailsHandler userDetailsHandler = new UserDetailsHandler(getApplicationContext());
        updateNameProfile(userDetailsHandler.getUserDetails());

        getUserProfileData();
    }


    public void updateNameProfile(UserDetails userDetails){
        if(userDetails != null) {
            if (userDetails.getGender().equalsIgnoreCase("m")) {
                profileImage.setImageResource(R.drawable.male_img);
            } else {
                profileImage.setImageResource(R.drawable.female_img);
            }
            nameView.setText(userDetails.getName());
            emailView.setText(userDetails.getEmail());
        }
    }

    private void getUserProfileData(){
        String url = BuildConfig.SERVICE_BASE_URL+"auth/get-user-profile";
        Log.d("manitaz", url);
        SharedPreferences sharedPreferences= getSharedPreferences( AppConstants.PREF_PACKAGE , Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.PREF_USER_ID, 0);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("manitaz", "User details : "+ jsonBody);

        Log.d("manitaz", "Json : "+ jsonBody);
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject)jsonParser.parse(response.toString());
                Gson gson = new Gson();
                UserDetails userDetails = gson.fromJson(gsonObject, UserDetails.class);
                updateNameProfile(userDetails);
                UserDetailsHandler userDetailsHandler = new UserDetailsHandler(getApplicationContext());
                userDetailsHandler.saveUserDetails(userDetails);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", "Bearer"+ " "+ "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MH0.zGsP_8XSXNrF7G8jOebsVfdHfCvqnCXrmgcSJA9AHaU");
                return headers;
            }
        };
        VolleySingleton.getInstance().addToRequestQueue(jsonObjectReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        displaySelectedScreen(menuItem.getItemId(), false);
        return true;
    }

    private void displaySelectedScreen(int itemId, boolean newAccount) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        FragmentManager fManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fManager.beginTransaction();
        Fragment fragement;
        Bundle args = new Bundle();
        args.putSerializable("new_user_account", newAccount);
        switch (itemId) {
            case R.id.nav_offer_feed:
                fragement = new MyOfferFeedFragment();
                fragement.setArguments(args);
                fragmentTransaction.replace(R.id.content_frame, fragement);
                fragmentTransaction.commit();
                break;

            case R.id.nav_my_profile:
                fragement = new ProfileFragment();
                fragement.setArguments(args);
                fragmentTransaction.replace(R.id.content_frame, fragement);
                fragmentTransaction.commit();
                break;

            case R.id.nav_my_banks:
                fragement = new BanksFragment();
                fragement.setArguments(args);
                fragmentTransaction.replace(R.id.content_frame, fragement);
                fragmentTransaction.commit();
                break;

            case R.id.nav_my_preferences:
                fragement = new PreferenceFragment();
                fragement.setArguments(args);
                fragmentTransaction.replace(R.id.content_frame, fragement);
                fragmentTransaction.commit();
                break;
        }
    }


}
