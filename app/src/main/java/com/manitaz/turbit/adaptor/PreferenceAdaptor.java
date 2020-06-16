package com.manitaz.turbit.adaptor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manitaz.turbit.R;
import com.manitaz.turbit.model.BankModel;
import com.manitaz.turbit.model.PreferenceModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PreferenceAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<PreferenceModel> list;
    private LayoutInflater inflater;
    private Context mCtx;
    private ItemClickCallback itemClickCallback;

    public PreferenceAdaptor(List<PreferenceModel> list, Context mCtx){
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
        View view = inflater.inflate(R.layout.list_item_preference, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ViewHolder preferenceHolder = (ViewHolder) holder;
        final PreferenceModel preferenceModel = list.get(position);
        Uri uri = Uri.parse(preferenceModel.getImage());

        if(preferenceModel.getIs_selected() == 0){
            preferenceHolder.lstCheckBox.setChecked(false);
        }else{
            preferenceHolder.lstCheckBox.setChecked(true);
        }
        Picasso.with(mCtx)
                .load(uri)
                .placeholder(R.drawable.turbit_loader)
                .error(R.drawable.turbit_loader)
                .into(preferenceHolder.imBankIcon);

        preferenceHolder.lblListItem.setText(preferenceModel.getCategory_name());
    }

    @Override
    public int getItemCount() {
        Log.d("manitaz", "Preference length : "+list.size());
        return list.size();
    }

    public ArrayList<PreferenceModel> getCheckedItemsIds() {
        ArrayList<PreferenceModel> selectedIds = new ArrayList<>();
        for (PreferenceModel preferenceModel : list) {
            if (preferenceModel.getIs_selected() == 1) {
                selectedIds.add(preferenceModel);
            }
        }
        return selectedIds;
    }

    public interface ItemClickCallback {
        void onItemClick(int p);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView lblListItem;
        ImageView imBankIcon;
        CheckBox lstCheckBox;


        ViewHolder(View itemView) {
            super(itemView);
            lblListItem = itemView.findViewById(R.id.lblListItem);
            lstCheckBox = itemView.findViewById(R.id.lstCheckBox);
            imBankIcon = itemView.findViewById(R.id.imBankIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClickCallback.onItemClick(getAdapterPosition());
        }
    }
}
