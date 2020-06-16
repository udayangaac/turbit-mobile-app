package com.manitaz.turbit.ui.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.interfaces.ThreadCompleteListener;
import com.manitaz.turbit.model.BankModel;
import com.manitaz.turbit.model.PreferenceModel;
import com.manitaz.turbit.network.NotifyingThread;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.utill.AppConstants;
import com.manitaz.turbit.utill.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashScreen extends AppCompatActivity implements ThreadCompleteListener {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private NotifyingThread progressThread;
    private Handler handler = new Handler();
    private boolean isNetworkRequestFinished = false;
    private int userId= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        progressBar = findViewById(R.id.progressBar1);
        showProgress();
        getInitialUserDetails();
    }

    private void showProgress() {
        progressStatus = 0;
        progressThread = new NotifyingThread() {
            @Override
            public void doRun() {
                while (progressStatus < 1000) {
                    progressStatus += 1;
                    // Update the progress bar and display the
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        progressThread.addListener(this); // add ourselves as a listener
        progressThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressThread.removeListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showProgress();
    }

    private void getInitialUserDetails(){
        SharedPreferences pref = getSharedPreferences(AppConstants.PREF_PACKAGE, 0);
        userId = pref.getInt(AppConstants.PREF_USER_ID, 0);
        String userToken = pref.getString(AppConstants.PREF_USER_TOKEN, "");

        String initUrl = BuildConfig.SERVICE_BASE_URL+ "bank-list/user/"+ userId;
        Log.d("manitaz", "url : "+ initUrl);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                initUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("manitaz", "Json array : "+ response );
                        isNetworkRequestFinished = true;
                        try {
                            List<BankModel> bankList = new ArrayList<BankModel>();
                            for(int x = 0; x< response.length(); x++ ) {
                                JSONObject bankObject = response.getJSONObject(x);
                                Gson gson = new GsonBuilder().create();
                                BankModel bankItem = gson.fromJson(String.valueOf(bankObject), BankModel.class);
                                bankList.add(bankItem);
                            }
                            DatabaseHandler.getInstance(SplashScreen.this).addBanksDetails(bankList);
                        }catch (JSONException e){

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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

        String reqCategoriestUrl = BuildConfig.SERVICE_BASE_URL+ "notification-type/user/"+ userId;
        JsonArrayRequest reqCategories = new JsonArrayRequest(
                Request.Method.GET,
                reqCategoriestUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        isNetworkRequestFinished = true;
                        Log.d("manitaz", "Json array : "+ response );
                        try {
                            List<PreferenceModel> preferenceList = new ArrayList<PreferenceModel>();
                            for(int x = 0; x< response.length(); x++ ) {
                                JSONObject referenceObject = response.getJSONObject(x);
                                Gson gson = new GsonBuilder().create();
                                PreferenceModel preferenceModel = gson.fromJson(String.valueOf(referenceObject), PreferenceModel.class);
                                preferenceList.add(preferenceModel);
                            }
                            DatabaseHandler.getInstance(SplashScreen.this).addPreferenceDetails(preferenceList);
                        }catch (JSONException e){

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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

        VolleySingleton.getInstance().addToRequestQueue(reqCategories);

    }




    @Override
    public void notifyOfThreadComplete(Thread thread) {
        if(isNetworkRequestFinished){
            finish();
            if(userId == 0){
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                //Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }else{
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                // Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        }else{
            showProgress();
        }
    }
}
