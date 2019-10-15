package com.example.cliforcast.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cliforcast.R;

import java.util.List;

public class DialogAdapter extends RecyclerView.Adapter<ListItemViewHolder> {
    private Context context;
    private List<City> cities;
    private OnListItemSelected onListItemSelected;

    public DialogAdapter(Context context, List<City> cities, OnListItemSelected onListItemSelected) {
        this.context = context;
        this.cities = cities;
        this.onListItemSelected = onListItemSelected;
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return  new ListItemViewHolder(LayoutInflater.from(context).inflate(R.layout.current_weather_search_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        holder.currentWeatherSearchItemTextView.setText(cities.get(position).name);
        holder.currentWeatherSearchItemTextView.setOnClickListener(v -> {
            onListItemSelected.onSelect(cities.get(position).id);
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }
}
