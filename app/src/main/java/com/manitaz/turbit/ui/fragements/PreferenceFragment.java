package com.manitaz.turbit.ui.fragements;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.adaptor.PreferenceAdaptor;
import com.manitaz.turbit.model.BankModel;
import com.manitaz.turbit.model.PreferenceModel;
import com.manitaz.turbit.model.UserDetails;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.utill.AppConstants;
import com.manitaz.turbit.utill.DatabaseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceFragment extends Fragment implements PreferenceAdaptor.ItemClickCallback{

    private PreferenceAdaptor adapter;
    private RecyclerView recyclerView;

    private List<PreferenceModel> list;
    private ProgressDialog pDialog;
    private Button updateButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_preference, container, false);
        list = new ArrayList<>();
        recyclerView = inflate.findViewById(R.id.recyclerview);
        updateButton = inflate.findViewById(R.id.button);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(layoutManager);

        adapter = new PreferenceAdaptor(list, getContext());
        recyclerView.setAdapter(adapter);
        adapter.setItemClickCallback(this);
        getBankData();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<PreferenceModel> checked = adapter.getCheckedItemsIds();
                Log.d("manitaz", "checked items : "+ checked.size() );
                DatabaseHandler.getInstance(getContext()).changeSelectedStatusPreference(checked);
                updatePreference(checked);
            }
        });

        return inflate;
    }

    @Override
    public void onItemClick(int p) {
        int isSelected = list.get(p).getIs_selected();
        if(isSelected == 0){
            list.get(p).setIs_selected(1);
        }else{
            list.get(p).setIs_selected(0);
        }
        adapter.notifyDataSetChanged();
    }

    public void getBankData() {
        List<PreferenceModel> datapreference = DatabaseHandler.getInstance(getContext()).getAllPreference();
        for (PreferenceModel data : datapreference) {
            list.add(data);
        }
        Log.d("manitaz", "Preference length notify : "+list.size());
        adapter.notifyDataSetChanged();
    }

    private void updatePreference(ArrayList<PreferenceModel> selectedPreferences){
        pDialog = new ProgressDialog(getContext());
        pDialog.show();
        pDialog.setCancelable(true);
        pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        pDialog.setContentView(R.layout.progress_spinner);
        DatabaseHandler.getInstance(getContext()).changeSelectedStatusPreference(selectedPreferences);

        String url = BuildConfig.SERVICE_BASE_URL+"auth/user-profile";
        UserDetails userDetails = new UserDetails();
        SharedPreferences sharedPreferences= getContext().getSharedPreferences( AppConstants.PREF_PACKAGE , Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.PREF_USER_ID, 0);
        userDetails.setUserId(userId);

        int[] selectedPreferencesArray = new int[selectedPreferences.size()];
        for (int x = 0; x<selectedPreferences.size(); x++){
            selectedPreferencesArray[x] = selectedPreferences.get(x).getId();
        }

        userDetails.setAdvertisement_cat_id(selectedPreferencesArray);

        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject((new Gson().toJson(userDetails)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();
                Log.d("manitaz", "Response : "+response);
                Fragment newFragment = new MyOfferFeedFragment();
                FragmentManager fManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fManager.beginTransaction();
                fragmentTransaction.add(R.id.content_frame, newFragment, "MyOfferFeedFragment").addToBackStack("backstack");
                fragmentTransaction.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
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

}
