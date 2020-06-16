package com.manitaz.turbit.ui.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.model.BankModel;
import com.manitaz.turbit.model.PreferenceModel;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailText, passwordText;
    private TextInputLayout passwordInputLayout, emailInputLayout;
    private Button loginBtn, createAccountBtn;
    private String emailHolder, passwordHoledr;
    private ProgressDialog pDialog;

    private boolean isNetworkRequestFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();
    }

    private void initUI(){
        emailText = findViewById(R.id.login_email);
        passwordText = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);
        createAccountBtn = findViewById(R.id.create_account_btn);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);

        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                emailInputLayout.setError(null);
            }
        });
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                passwordInputLayout.setError(null);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInputs()){
                    //send network request valid inputs entered
                    pDialog = new ProgressDialog(LoginActivity.this);
                    pDialog.show();
                    pDialog.setCancelable(true);
                    pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    pDialog.setContentView(R.layout.progress_spinner);
                    loginRequest();
                }else{

                }
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateNewAccount.class );
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs(){
        boolean inputValid = true;
        emailHolder = emailText.getText().toString();
        passwordHoledr = passwordText.getText().toString();

        if(TextUtils.isEmpty(passwordHoledr)){
            inputValid = false;
            passwordInputLayout.setError("Enter password");
        }

        if((!TextUtils.isEmpty(emailHolder) && Patterns.EMAIL_ADDRESS.matcher(emailHolder).matches()) == false){
            inputValid =false;
            emailInputLayout.setError("Enter valid email");
        }
        return inputValid;
    }

    private void loginRequest(){
        String url = BuildConfig.SERVICE_BASE_URL+"login";
        Log.d("manitaz", url);
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", emailHolder);
            jsonBody.put("password", passwordHoledr);
            Log.d("manitaz", "Json body :"+jsonBody);
            JsonObjectRequest jsonObjectReq = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("manitaz", "Response : "+ response );
                    pDialog.dismiss();
                    try {
                        if(response.has("Code")){
                            passwordInputLayout.setError("Invalid credentials");
                        }else{
                            SharedPreferences.Editor editor= getSharedPreferences( AppConstants.PREF_PACKAGE , MODE_PRIVATE).edit();
                            editor.putInt(AppConstants.PREF_USER_ID, response.getInt("id"));
                            editor.putString(AppConstants.PREF_USER_TOKEN, response.getString("token"));
                            editor.commit();

                            getInitialUserDetails(response.getInt("id"), response.getString("token"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pDialog.dismiss();
                    passwordInputLayout.setError("Invalid credentials");
                    Log.d("manitaz", "Error response : "+ error);
                }
            }){
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            VolleySingleton.getInstance().addToRequestQueue(jsonObjectReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getInitialUserDetails(int userId, String userToken){

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
                        try {
                            List<BankModel> bankList = new ArrayList<BankModel>();
                            for(int x = 0; x< response.length(); x++ ) {
                                JSONObject bankObject = response.getJSONObject(x);
                                Gson gson = new GsonBuilder().create();
                                BankModel bankItem = gson.fromJson(String.valueOf(bankObject), BankModel.class);
                                bankList.add(bankItem);
                            }
                            DatabaseHandler.getInstance(LoginActivity.this).addBanksDetails(bankList);
                        }catch (JSONException e){

                        }

                        if(isNetworkRequestFinished){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        isNetworkRequestFinished = true;
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
                        Log.d("manitaz", "Json array : "+ response );
                        try {
                            List<PreferenceModel> preferenceList = new ArrayList<PreferenceModel>();
                            for(int x = 0; x< response.length(); x++ ) {
                                JSONObject referenceObject = response.getJSONObject(x);
                                Gson gson = new GsonBuilder().create();
                                PreferenceModel preferenceModel = gson.fromJson(String.valueOf(referenceObject), PreferenceModel.class);
                                preferenceList.add(preferenceModel);
                            }
                            DatabaseHandler.getInstance(LoginActivity.this).addPreferenceDetails(preferenceList);
                        }catch (JSONException e){

                        }

                        if(isNetworkRequestFinished){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        isNetworkRequestFinished = true;
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




}
