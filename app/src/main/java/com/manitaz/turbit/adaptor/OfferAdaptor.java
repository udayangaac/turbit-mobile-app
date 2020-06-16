package com.manitaz.turbit.adaptor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.manitaz.turbit.BuildConfig;
import com.manitaz.turbit.R;
import com.manitaz.turbit.model.NoResultFound;
import com.manitaz.turbit.model.OfferModel;
import com.manitaz.turbit.network.VolleySingleton;
import com.manitaz.turbit.utill.AppConstants;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfferAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private final List<Object> list;
    private final Context mCtx;
    private LayoutInflater inflater;
    private ItemClickCallback itemClickCallback;

    public static final int TYPE_ITEM = 1;
    public static final int TYPE_NO_OFFERS = 2;

    public OfferAdaptor(List<Object> list, Context mCtx){
        this.list = list;
        this.mCtx = mCtx;
        inflater = LayoutInflater.from(mCtx);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = inflater.inflate(R.layout.offer_feed_item, parent, false);
            return new OfferViewHolder(view);
        } else if (viewType == TYPE_NO_OFFERS) {
            View view = inflater.inflate(R.layout.no_offers, parent, false);
            return new NoOfferViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position) instanceof OfferModel) {
            return TYPE_ITEM;
        } else if (list.get(position) instanceof NoResultFound) {
            return TYPE_NO_OFFERS;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OfferViewHolder) {
            final OfferViewHolder offerViewHolder = (OfferViewHolder) holder;
            OfferModel item = (OfferModel) list.get(position);
            if (item.getImage_publisher() != "") {
                Uri uri2 = Uri.parse(item.getImage_publisher());
                Picasso.with(mCtx)
                        .load(uri2)
                        .placeholder(R.drawable.turbit_loader)
                        .error(R.drawable.turbit_loader)
                        .into(offerViewHolder.thumbnail);
            } else {
                Picasso.with(mCtx)
                        .load(R.drawable.turbit_loader)
                        .placeholder(R.drawable.turbit_loader)
                        .error(R.drawable.turbit_loader)
                        .into(offerViewHolder.thumbnail);
            }

            if(item.getLogo_company() != ""){
                Uri uri = Uri.parse(item.getLogo_company());
                Picasso.with(mCtx)
                        .load(uri)
                        .placeholder(R.drawable.turbit_loader)
                        .error(R.drawable.turbit_loader)
                        .into(offerViewHolder.companyIcon);
            } else {
                Picasso.with(mCtx)
                        .load(R.drawable.turbit_loader)
                        .placeholder(R.drawable.turbit_loader)
                        .error(R.drawable.turbit_loader)
                        .into(offerViewHolder.companyIcon);
            }

            offerViewHolder.companyName.setText(item.getCompany_name());
            offerViewHolder.tagLine.setText(item.getContent());

            offerViewHolder.tagLine.setText(item.getContent());

            if(item.isBtnLike()){
               offerViewHolder.btnLike.setBackgroundResource(R.drawable.feed_button_selected);
               offerViewHolder.btnLike.setTextColor(Color.parseColor("#ffffff"));
            }

            if(item.isBtnDislike()){
                offerViewHolder.btnDislike.setBackgroundResource(R.drawable.feed_button_selected);
                offerViewHolder.btnDislike.setTextColor(Color.parseColor("#ffffff"));
            }

            if(item.isBtnUseful()){
                offerViewHolder.btnUseful.setBackgroundResource(R.drawable.feed_button_selected);
                offerViewHolder.btnUseful.setTextColor(Color.parseColor("#ffffff"));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM");
            String  startDate = sdf.format(item.getStart_time());
            String endDate = sdf.format(item.getEnd_date());
            offerViewHolder.offerPeriod.setText("Offer valid from "+startDate+" to "+endDate);

        } else if (holder instanceof NoOfferViewHolder) {
            NoOfferViewHolder noOfferViewHolder = (NoOfferViewHolder) holder;
            NoResultFound item = (NoResultFound) list.get(position);

            if (item.getTitle() != null) {
                noOfferViewHolder.textViewHeader.setText(item.getTitle());
                noOfferViewHolder.txtInfoMsg.setText(item.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
         return list.size();
    }

    public interface ItemClickCallback {
        void onItemClick(int p);
        void onChangeWishListStatus(int p);
    }

    class NoOfferViewHolder extends RecyclerView.ViewHolder {

        TextView textViewHeader;
        TextView txtInfoMsg;

        public NoOfferViewHolder(View itemView) {
            super(itemView);
            textViewHeader = itemView.findViewById(R.id.txtHeader);
            txtInfoMsg = itemView.findViewById(R.id.txtMessage);
        }
    }

    class OfferViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView companyName;
        ImageView companyIcon;
        ImageView thumbnail;
        TextView tagLine, offerPeriod;
        View container;
        Button btnLike, btnDislike, btnUseful;

        SharedPreferences sharedPref2 = mCtx.getSharedPreferences(AppConstants.PREF_PACKAGE, Context.MODE_PRIVATE);
        int userId = sharedPref2.getInt(AppConstants.PREF_USER_ID, 0);

        public OfferViewHolder(final View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.lbl_company_Name);
            companyIcon = itemView.findViewById(R.id.im_bank_icon);
            thumbnail = itemView.findViewById(R.id.im_item_icon);
            tagLine = itemView.findViewById(R.id.tagline);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnDislike = itemView.findViewById(R.id.btn_dislike);
            btnUseful = itemView.findViewById(R.id.btn_useful);
            offerPeriod = itemView.findViewById(R.id.offer_period);

            btnLike.setOnClickListener(this);
            btnDislike.setOnClickListener(this);
            btnUseful.setOnClickListener(this);

            container = itemView.findViewById(R.id.cont_item_root);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try{
                final OfferModel offerDetails = (OfferModel) list.get(getAdapterPosition());
                if (v.getId() == R.id.btn_like) {
                    if(!offerDetails.isBtnLike()){
                        ((OfferModel) list.get(getAdapterPosition())).setBtnLike(true);
                        addInteraction(offerDetails, 2);
                    }
                }else if(v.getId() == R.id.btn_dislike){
                    if(!offerDetails.isBtnDislike()){
                        ((OfferModel) list.get(getAdapterPosition())).setBtnDislike(true);
                        addInteraction(offerDetails, 1);
                    }
                } else if (v.getId() == R.id.btn_useful) {
                    if(!offerDetails.isBtnUseful()){
                        ((OfferModel) list.get(getAdapterPosition())).setBtnUseful(true);
                        addInteraction(offerDetails, 3);
                    }
                } else {
                    itemClickCallback.onItemClick(getAdapterPosition());
                }
            }catch(ArrayIndexOutOfBoundsException ex){

            }
        }


        public void addInteraction(OfferModel offerModel, int interaction){
            SharedPreferences pref = mCtx.getSharedPreferences(AppConstants.PREF_PACKAGE, 0);
            String userToken = pref.getString(AppConstants.PREF_USER_TOKEN, "");

            itemClickCallback.onChangeWishListStatus(getAdapterPosition());
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("userId", userId);
                jsonBody.put("notificationId", offerModel.getId());
                jsonBody.put("status", interaction);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url = BuildConfig.SERVICE_BASE_URL+"auth/user-reaction";
            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("manitaz", "EVent listent :"+ response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer"+ " "+ userToken);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            VolleySingleton.getInstance().addToRequestQueue(req);
        }
    }
}
