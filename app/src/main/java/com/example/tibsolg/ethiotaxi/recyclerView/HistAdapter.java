package com.example.tibsolg.ethiotaxi.recyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tibsolg.ethiotaxi.R;

import java.util.List;

/**
 * Created by manel on 03/04/2017.
 */

public class HistAdapter extends RecyclerView.Adapter<HistViewHolders> {

    private List<HistObject> itemList;
    private Context context;

    public HistAdapter(List<HistObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public HistViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        HistViewHolders rcv = new HistViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(HistViewHolders holder, final int position) {
        holder.rideId.setText(itemList.get(position).getRideId());
        if(itemList.get(position).getTime()!=null){
            holder.time.setText(itemList.get(position).getTime());
        }
    }
    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

}