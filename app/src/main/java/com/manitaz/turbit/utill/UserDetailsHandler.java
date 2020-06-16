package com.manitaz.turbit.utill;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.manitaz.turbit.model.UserDetails;

public class UserDetailsHandler {
    private Context context;
    public UserDetailsHandler(Context context){
        this.context = context;
    }

    public void saveUserDetails(UserDetails userDetails){
        SharedPreferences.Editor editor = context.getSharedPreferences(AppConstants.PREF_PACKAGE, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(userDetails);
        editor.putString(AppConstants.PREF_USER_DETAILS , json);
        editor.commit();
    }

    public UserDetails getUserDetails(){
        SharedPreferences shardPref = context.getSharedPreferences(AppConstants.PREF_PACKAGE, Context.MODE_PRIVATE);
        String json = shardPref.getString(AppConstants.PREF_USER_DETAILS, "");
        Gson gson = new Gson();
        return gson.fromJson(json, UserDetails.class);
    }
}
