package com.manitaz.turbit.ui.fragements;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.model.BankModel;
import com.manitaz.turbit.model.UserDetails;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.ui.custom.ExpandableHeightGridView;
import com.manitaz.turbit.utill.AppConstants;
import com.manitaz.turbit.utill.DatabaseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BanksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BanksFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ExpandableHeightGridView imagegrid;
    private Button proceed;
    private CheckBox checkBox;
    private List<BankModel> bankItems = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private ProgressDialog pDialog;
    private boolean isNewUser = false;

    public BanksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BanksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BanksFragment newInstance(String param1, String param2) {
        BanksFragment fragment = new BanksFragment();
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

        View inflate = inflater.inflate(R.layout.fragment_banks, container, false);
        isNewUser = (boolean) getArguments().getSerializable("new_user_account");
        initUI(inflate);
        getBankData();
        return inflate;
    }

    private void initUI(View inflate){
        proceed = inflate.findViewById(R.id.proceed);
        imagegrid = inflate.findViewById(R.id.PhoneImageGrid);

        imagegrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                checkBox = ((ViewHolder) view.getTag()).checkbox;
                checkBox.setChecked(!checkBox.isChecked());
                if (checkBox.isChecked()) {
                    checkBox.setChecked(true);
                    bankItems.get(i).setIs_selected(1);
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    checkBox.setChecked(false);
                    checkBox.setVisibility(View.INVISIBLE);
                    bankItems.get(i).setIs_selected(0);
                }
            }
        });

        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);
        imagegrid.setExpanded(true);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog = new ProgressDialog(getContext());
                pDialog.show();
                pDialog.setCancelable(true);
                pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                pDialog.setContentView(R.layout.progress_spinner);
               updateBankList();
            }
        });

    }

    private void updateBankList(){
        ArrayList<BankModel> selectedBanks = getSelectedIndexes();
        DatabaseHandler.getInstance(getContext()).changeSelectedStatusBanks(selectedBanks);

        String url = BuildConfig.SERVICE_BASE_URL+"auth/user-profile";
        UserDetails userDetails = new UserDetails();
        SharedPreferences sharedPreferences= getContext().getSharedPreferences( AppConstants.PREF_PACKAGE , Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(AppConstants.PREF_USER_ID, 0);
        String userToken = sharedPreferences.getString(AppConstants.PREF_USER_TOKEN, "");
        userDetails.setUserId(userId);

        int[] selectedBanksArray = new int[selectedBanks.size()];
        for (int x = 0; x<selectedBanks.size(); x++){
            selectedBanksArray[x] = selectedBanks.get(x).getId();
        }

        userDetails.setBank_id_list(selectedBanksArray);

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
                Fragment newFragment;
                String tag = "MyOfferFeedFragment";
                if(isNewUser){
                    newFragment= new PreferenceFragment();
                    tag = "PreferenceFragment";
                }else{
                    newFragment = new MyOfferFeedFragment();
                }
                Bundle args = new Bundle();
                args.putSerializable("new_user_account", isNewUser);
                newFragment.setArguments(args);

                FragmentManager fManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fManager.beginTransaction();
                fragmentTransaction.add(R.id.content_frame, newFragment, tag).addToBackStack("backstack");
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
                headers.put("Authorization", "Bearer"+ " "+ userToken);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        VolleySingleton.getInstance().addToRequestQueue(jsonObjectReq);

    }

    public ArrayList<BankModel> getSelectedIndexes() {
        ArrayList<BankModel> items = new ArrayList<>();
        for (BankModel bank : bankItems) {
            if (bank.getIs_selected() == 1) {
                items.add(bank);
            }
        }
        return items;
    }

    public void getBankData() {
        bankItems = DatabaseHandler.getInstance(getContext()).getAllBanks();
        Log.d("manitaz", "banks items "+ bankItems.size());
        imageAdapter.notifyDataSetChanged();
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return bankItems.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.bank, null);
                holder.imageview = convertView.findViewById(R.id.thumbImage);
                holder.checkbox = convertView.findViewById(R.id.itemCheckBox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(bankItems.get(position).getId());
            holder.imageview.setId(bankItems.get(position).getId());
            if (!bankItems.get(position).getSelected()) {
                bankItems.get(position).setIs_selected(0);
                holder.checkbox.setVisibility(View.INVISIBLE);
                holder.checkbox.setChecked(false);
            } else {
                holder.checkbox.setChecked(true);
                holder.checkbox.setVisibility(View.VISIBLE);
                bankItems.get(position).setIs_selected(1);
            }

            Uri uri = Uri.parse(bankItems.get(position).getImage());

            Picasso.with(getContext())
                    .load(uri)
                    .placeholder(R.drawable.turbit_loader)
                    .error(R.drawable.turbit_loader)
                    .into(holder.imageview);

            holder.id = bankItems.get(position).getId();
            return convertView;
        }


    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        int id;
        TextView header;
    }

}
