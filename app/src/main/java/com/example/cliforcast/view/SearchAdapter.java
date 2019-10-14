package com.example.cliforcast.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cliforcast.R;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {


    private Context context;
    private List<City> cities;
    private OnSearchListSelected onSearchListSelected;

    public SearchAdapter(Context context,List<City> cities,OnSearchListSelected onSearchListSelected) {
        this.context = context;
        this.cities = cities;
        this.onSearchListSelected = onSearchListSelected;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return  new SearchViewHolder(LayoutInflater.from(context).inflate(R.layout.current_weather_search_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.currentWeatherSearchItemTextView.setText(cities.get(position).name);
        holder.currentWeatherSearchItemTextView.setOnClickListener(v -> {
            onSearchListSelected.onSelect(cities.get(position).id);
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }
}