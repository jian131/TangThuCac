
package com.jian.tangthucac.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jian.tangthucac.R;
import com.jian.tangthucac.activity.StoryDetailActivity;
import com.jian.tangthucac.model.Story;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<Story> bannerStories;

    public BannerAdapter(Context context, List<Story> bannerStories) {
        this.context = context;
        this.bannerStories = bannerStories;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Story story = bannerStories.get(position);

        Glide.with(context)
            .load(story.getImage())
            .placeholder(R.drawable.banner_placeholder)
            .error(R.drawable.banner_error)
            .into(holder.bannerImage);

        holder.bannerTitle.setText(story.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoryDetailActivity.class);
            intent.putExtra("story", story);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bannerStories.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView bannerTitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
            bannerTitle = itemView.findViewById(R.id.bannerTitle);
        }
    }
}
