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
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.utill.AppConstants;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateNewAccount extends AppCompatActivity {

    private TextInputEditText nameText, emailText, passwordText;
    private TextInputLayout emailInputLayout, passwordInputLayout, nameInputLayout;
    private Button registerBtn, haveAccountBtn;
    private String nameHolder, emailHolder, passwordHoledr;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);

        initUI();
    }

    private void initUI(){
        nameText = findViewById(R.id.register_name);
        emailText = findViewById(R.id.register_email);
        passwordText = findViewById(R.id.register_password);
        registerBtn = findViewById(R.id.register_btn);
        haveAccountBtn = findViewById(R.id.create_account_btn);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        nameInputLayout = findViewById(R.id.nameInputLayout);

        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                nameText.setError(null);
            }
        });
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

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Button", Toast.LENGTH_SHORT);
                if(validateInputs()){
                    pDialog = new ProgressDialog(CreateNewAccount.this);
                    pDialog.show();
                    pDialog.setCancelable(true);
                    pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    pDialog.setContentView(R.layout.progress_spinner);
                    createAccountRequest();
                }else{

                }
            }
        });

        haveAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public boolean validateInputs(){
        boolean inputValid = true;
        nameHolder = nameText.getText().toString();
        emailHolder = emailText.getText().toString();
        passwordHoledr = passwordText.getText().toString();

        if(TextUtils.isEmpty(nameHolder)){
            inputValid = false;
            nameInputLayout.setError("Name is required");
        }

        if(TextUtils.isEmpty(passwordHoledr) || passwordHoledr.length() < 4){
            inputValid = false;
            passwordInputLayout.setError("Enter strong password");
        }

        if((!TextUtils.isEmpty(emailHolder) && Patterns.EMAIL_ADDRESS.matcher(emailHolder).matches()) == false){
            inputValid =false;
            emailInputLayout.setError("Enter valid email address");
        }
        return inputValid;
    }

    private void createAccountRequest(){
        String url = BuildConfig.SERVICE_BASE_URL+"register";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", emailHolder);
            jsonBody.put("password", passwordHoledr);
            jsonBody.put("register", nameHolder);
            JsonObjectRequest jsonObjectReq = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    pDialog.dismiss();
                    Log.d("manitaz", "Response : "+ response );
                    try {
                        if(response.has("id")){
                            SharedPreferences.Editor editor= getSharedPreferences( AppConstants.PREF_PACKAGE , MODE_PRIVATE).edit();
                            editor.putInt(AppConstants.PREF_USER_ID, response.getInt("id"));
                            editor.putString(AppConstants.PREF_USER_TOKEN, response.getString("token"));
                            editor.commit();
                            Intent intent = new Intent(CreateNewAccount.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("new_user_account", true);
                            startActivity(intent);
                            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                        }else{

                        }
                    } catch (JSONException e) {

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pDialog.dismiss();
                    emailInputLayout.setError("Email already have account");
                    Toast.makeText(getApplicationContext(), "Network request failed", Toast.LENGTH_SHORT);
                }
            });
            VolleySingleton.getInstance().addToRequestQueue(jsonObjectReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
