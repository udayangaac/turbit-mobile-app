package com.manitaz.turbit.ui.fragements;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.model.CompanyDetails;
import com.manitaz.turbit.model.UserDetails;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.utill.AppConstants;
import com.manitaz.turbit.utill.UserDetailsHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextInputLayout kidsInputLayout, companyInputLayout, companyLocationInputLayout, addressInputLayout, dobInputLayout;
    private TextInputEditText dobInput, kidsInput, companyNameInput, companyLocationInput, addressInput;
    private DatePickerDialog picker;
    private Button updateButton;
    private String addressHolder, genderHolder= "M", dobHolder, companyNameHolder, companyLocationHolder;
    private int employeeStatusHolder= 1, civilStatusHolder= 0, kidsHolder = 0;
    private ProgressDialog pDialog;
    private RadioGroup genderRadioGroup, maritualStatusGroup, employeeStatusRadioGroup;
    private boolean isNewUser = false;
    private RadioButton maleRadio, femaleRadio, jobTrue, jobFalse, marriedTrue, marriedFalse;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_profile, container, false);
        isNewUser = (boolean) getArguments().getSerializable("new_user_account");
        initUI(inflate);
        return inflate;
    }

    public void initUI(View inflate){
        maleRadio = inflate.findViewById(R.id.genderMaleRadio);
        femaleRadio = inflate.findViewById(R.id.genderFemaleRadio);
        jobTrue = inflate.findViewById(R.id.employeeYesRadio);
        jobFalse = inflate.findViewById(R.id.employeeNoRadio);
        marriedTrue = inflate.findViewById(R.id.maritalMarriedRadio);
        marriedFalse = inflate.findViewById(R.id.maritalSingleRadio);
        addressInputLayout = inflate.findViewById(R.id.addressInputLayout);
        dobInputLayout = inflate.findViewById(R.id.dobInputLayout);
        kidsInputLayout = inflate.findViewById(R.id.kidsInputLayout);
        companyInputLayout = inflate.findViewById(R.id.companyInputLayout);
        companyLocationInputLayout = inflate.findViewById(R.id.companyLocationInputLayout);

        genderRadioGroup= inflate.findViewById(R.id.radio_group_gender);
        employeeStatusRadioGroup = inflate.findViewById(R.id.radio_group_employee);
        maritualStatusGroup = inflate.findViewById(R.id.radio_group_marital_status);

        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                onRadioButtonClicked(i);
            }
        });
        employeeStatusRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                onRadioButtonClicked(i);
            }
        });
        maritualStatusGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                onRadioButtonClicked(i);
            }
        });

        addressInput = inflate.findViewById(R.id.addressInput);
        kidsInput = inflate.findViewById(R.id.kidsInput);
        companyNameInput = inflate.findViewById(R.id.companyInput);
        companyLocationInput = inflate.findViewById(R.id.companyLocationInput);
        dobInput = inflate.findViewById(R.id.dobInput);
        dobInput.setInputType(InputType.TYPE_NULL);
        dobInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dobInput.setText(year + "-" + (monthOfYear + 1) + "-" +dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        updateButton = inflate.findViewById(R.id.updateDetailsButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    //send network request valid inputs entered
                    pDialog = new ProgressDialog(getContext());
                    pDialog.show();
                    pDialog.setCancelable(true);
                    pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    pDialog.setContentView(R.layout.progress_spinner);
                    sendDataToServer();
                }
            }
        });


        if(isNewUser == false){
            UserDetailsHandler userDetailsHandler = new UserDetailsHandler(getContext());
            UserDetails userDetails = userDetailsHandler.getUserDetails();
            addressInput.setText(userDetails.getAddress());
            dobInput.setText(userDetails.getDob());
            if(userDetails.getGender() == "M"){
                maleRadio.setChecked(true);
            }else{
                femaleRadio.setChecked(true);
            }
            if(userDetails.getEmployee_status() == 1){
                jobTrue.setChecked(true);
            }else{
                jobFalse.setChecked(true);
            }
            companyNameInput.setText(userDetails.getJob_details().getName());
            companyLocationInput.setText(userDetails.getJob_details().getAddress());
            if(userDetails.getCivil_status() == 1 ){
                marriedTrue.setChecked(true);
            }else{
                marriedFalse.setChecked(true);
            }
            kidsInput.setText(Integer.toString(userDetails.getKids()));
        }
    }

    public boolean validateInputs(){
        boolean inputValid = true;
        dobHolder = dobInput.getText().toString();
        addressHolder = addressInput.getText().toString();
        companyNameHolder = companyNameInput.getText().toString();
        companyLocationHolder = companyLocationInput.getText().toString();
        kidsHolder = Integer.parseInt(kidsInput.getText().toString());

        if(TextUtils.isEmpty(addressHolder)){
            inputValid = false;
            addressInputLayout.setError("Address is required");
        }

        if(TextUtils.isEmpty(dobHolder)){
            inputValid = false;
            dobInputLayout.setError("DOB is required");
        }

        if(employeeStatusHolder == 1){
            if(TextUtils.isEmpty(companyNameHolder)){
                inputValid = false;
                companyInputLayout.setError("Enter company name");
            }

            if(TextUtils.isEmpty(companyLocationHolder)){
                inputValid = false;
                companyLocationInputLayout.setError("Enter company location");
            }
        }

        if(civilStatusHolder == 1){
            if(TextUtils.isEmpty(dobHolder)){
                inputValid = false;
                addressInputLayout.setError("Enter number of kids");
            }
        }
        return inputValid;
    }

    private void sendDataToServer(){
        String url = BuildConfig.SERVICE_BASE_URL+"auth/user-profile";
        UserDetails userDetails = new UserDetails();
        SharedPreferences sharedPreferences= getContext().getSharedPreferences( AppConstants.PREF_PACKAGE , Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.PREF_USER_ID, 0);
        userDetails.setUserId(userId);
        userDetails.setAddress(addressHolder);
        userDetails.setDob(dobHolder);
        userDetails.setGender(genderHolder);
        userDetails.setEmployee_status(employeeStatusHolder);
        if (employeeStatusHolder == 1) {
            CompanyDetails companyDetails = new CompanyDetails();
            companyDetails.setName(companyNameHolder);
            companyDetails.setAddress(companyLocationHolder);
            userDetails.setJob_details(companyDetails);
        }
        userDetails.setCivil_status(civilStatusHolder);
        if(civilStatusHolder == 1){
            userDetails.setKids(kidsHolder);
        }

        UserDetailsHandler userDetailsHandler = new UserDetailsHandler(getContext());
        userDetailsHandler.saveUserDetails(userDetails);

        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject((new Gson().toJson(userDetails)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("manitaz1", "Json "+ jsonBody);

        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();
                Log.d("manitaz", "Response : "+response);
                Fragment newFragment;
                String tag = "MyOfferFeedFragment";
                if(isNewUser){
                    newFragment= new BanksFragment();
                    tag = "BanksFragment";
                }else{
                    newFragment = new MyOfferFeedFragment();
                }
                Bundle args = new Bundle();
                args.putSerializable("new_user_account", isNewUser);
                newFragment.setArguments(args);
                FragmentManager fManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fManager.beginTransaction();
                fragmentTransaction.add(R.id.content_frame, newFragment, "tag").addToBackStack("backstack");
                fragmentTransaction.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                Log.d("manitaz", "Error: " + error
                        + "\nStatus Code " + error.networkResponse.statusCode
                        + "\nCause " + error.getCause()
                        + "\nnetworkResponse " + error.networkResponse.data.toString()
                        + "\nmessage" + error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer"+ " "+ "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoidXNlciIsInVzZXJJZCI6MH0.zGsP_8XSXNrF7G8jOebsVfdHfCvqnCXrmgcSJA9AHaU");
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        VolleySingleton.getInstance().addToRequestQueue(jsonObjectReq);
    }

    public void onRadioButtonClicked(int id){
        switch (id){
            case R.id.genderMaleRadio:
                genderHolder = "M";
                break;

            case R.id.genderFemaleRadio:
                genderHolder = "F";
                break;

            case R.id.employeeYesRadio:
                employeeStatusHolder = 1;
                companyInputLayout.setVisibility(View.VISIBLE);
                companyLocationInputLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.employeeNoRadio:
                employeeStatusHolder = 0;
                companyInputLayout.setVisibility(View.GONE);
                companyLocationInputLayout.setVisibility(View.GONE);
                break;

            case R.id.maritalMarriedRadio:
                civilStatusHolder = 1;
                kidsInputLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.maritalSingleRadio:
                civilStatusHolder = 0;
                kidsInputLayout.setVisibility(View.GONE);
                break;
        }
    }

}
