
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

public class HotStoryAdapter extends RecyclerView.Adapter<HotStoryAdapter.HotStoryViewHolder> {

    private Context context;
    private List<Story> hotStories;

    public HotStoryAdapter(Context context, List<Story> hotStories) {
        this.context = context;
        this.hotStories = hotStories;
    }

    @NonNull
    @Override
    public HotStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hot_story, parent, false);
        return new HotStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotStoryViewHolder holder, int position) {
        Story story = hotStories.get(position);

        holder.hotStoryTitle.setText(story.getTitle());

        Glide.with(context)
            .load(story.getImage())
            .placeholder(R.drawable.loading_placeholder)
            .error(R.drawable.error_image)
            .into(holder.hotStoryImage);

        holder.hotStoryAuthor.setText(story.getAuthor());
        holder.hotStoryViews.setText(String.valueOf(story.getViews()));

        // Hot badge is shown for hot stories by default in layout

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoryDetailActivity.class);
            intent.putExtra("story", story);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotStories.size();
    }

    static class HotStoryViewHolder extends RecyclerView.ViewHolder {
        ImageView hotStoryImage;
        TextView hotStoryTitle, hotStoryAuthor, hotStoryViews;

        public HotStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            hotStoryImage = itemView.findViewById(R.id.hotStoryImage);
            hotStoryTitle = itemView.findViewById(R.id.hotStoryTitle);
            hotStoryAuthor = itemView.findViewById(R.id.hotStoryAuthor);
            hotStoryViews = itemView.findViewById(R.id.hotStoryViews);
        }
    }
}
