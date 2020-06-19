package com.OpenNAC.openauth.Services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenNAC.openauth.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    ArrayList<FeedItem> feedItems;
    Context context;
    public MyAdapter(Context context,ArrayList<FeedItem>feedItems){
        this.context = context;
        this.feedItems = feedItems;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_row_news_item,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FeedItem current = feedItems.get(position);
        holder.Title.setText(current.getTitle());
        holder.Description.setText(current.getDescription());
        holder.Date.setText(current.getPubDate());
        Picasso.get().load(current.getThumbnailUrl()).into(holder.Thumbnail);
        holder.Title.setOnClickListener(v -> {
            Uri webaddress = Uri.parse(current.getLink());
            Intent goToSite = new Intent(Intent.ACTION_VIEW, webaddress);
            if (goToSite.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(goToSite);
            }
        });
        holder.Description.setOnClickListener(v -> {
            Uri webaddress = Uri.parse(current.getLink());
            Intent goToSite = new Intent(Intent.ACTION_VIEW, webaddress);
            if (goToSite.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(goToSite);
            }
        });
        holder.Thumbnail.setOnClickListener(v -> {
            Uri webaddress = Uri.parse(current.getLink());
            Intent goToSite = new Intent(Intent.ACTION_VIEW, webaddress);
            if (goToSite.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(goToSite);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Title,Description,Date;
        ImageView Thumbnail;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.title_text);
            Description = itemView.findViewById(R.id.description_text);
            Date = itemView.findViewById(R.id.date_text);
            Thumbnail = itemView.findViewById(R.id.thumb_img);
        }
    }
}
