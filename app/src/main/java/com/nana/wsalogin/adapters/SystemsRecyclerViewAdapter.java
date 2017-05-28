package com.nana.wsalogin.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nana.wsalogin.R;
import com.nana.wsalogin.SystemsActivity;
import com.nana.wsalogin.helpers.Systems;

import java.util.List;

/**
 * Created by NanaYaw on 5/24/2017.
 */

public class SystemsRecyclerViewAdapter extends RecyclerView.Adapter<SystemsRecyclerViewAdapter.ViewHolder> {
    List<Systems> systemsList;
    SystemsListClickListener listener;
    Context context;

    public SystemsRecyclerViewAdapter(Context context, List<Systems> systemsList,
                                      SystemsListClickListener listener) {
        this.context = context;
        this.systemsList = systemsList;
        this.listener = listener;
    }

    @Override
    public SystemsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.systems_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(systemsList.get(position).isStatus()) {
            holder.locText.setText(systemsList.get(position).getLocation());
            holder.plName.setText(systemsList.get(position).getPlant());
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.green_circle));
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onSystemClick(systemsList.get(position));
                }
            });
        }else{
            holder.locText.setText(context.getString(R.string.no_activate));
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.red_circle));
        }
    }


    @Override
    public int getItemCount() {
        return systemsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView locText;
        public final TextView plName;
        public final ImageView statusImg;
        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            locText = (TextView) itemView.findViewById(R.id.loc_name_tv);
            plName = (TextView) itemView.findViewById(R.id.pl_name_tv);
            statusImg = (ImageView) itemView.findViewById(R.id.status_iv);
        }
    }
}
