package com.example.cliforcast.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cliforcast.R;

public class SearchViewHolder extends RecyclerView.ViewHolder {
    TextView currentWeatherSearchItemTextView;
    public SearchViewHolder(@NonNull View itemView) {
        super(itemView);
        currentWeatherSearchItemTextView = itemView.findViewById(R.id.currentWeatherSearchItemTextView);
    }
}
