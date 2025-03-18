package com.rlc.onms.Adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rlc.onms.R;
import java.util.ArrayList;
import java.util.List;

public class KmzAdapter extends RecyclerView.Adapter<KmzAdapter.ViewHolder> {
    private final List<String> kmzList;
    private final List<String> filteredList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String fileName);
    }

    public KmzAdapter(List<String> kmzList, OnItemClickListener listener) {
        this.kmzList = kmzList;
        this.filteredList = new ArrayList<>(kmzList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kmz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String fileName = filteredList.get(position);
        holder.textView.setText(fileName);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(fileName));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }



    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(kmzList);
        } else {
            for (String file : kmzList) {
                if (file.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(file);
                }
            }
        }
        Log.d("FILTER", "Filtered List boyut: " + filteredList.size());
        Log.d("FILTER", "Filtered List içerik: " + filteredList);

        notifyDataSetChanged();  // ahmet bak düşme ha !!!
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewFileName);
            textView.setTextSize(12);
        }
    }


}

