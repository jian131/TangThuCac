package com.jian.tangthucac.adapters;

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
import com.jian.tangthucac.activities.NovelDetailActivity;
import com.jian.tangthucac.models.Novel;

import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelViewHolder> {

    private Context context;
    private List<Novel> novels;
    private boolean isGridLayout;

    public NovelAdapter(Context context, List<Novel> novels, boolean isGridLayout) {
        this.context = context;
        this.novels = novels;
        this.isGridLayout = isGridLayout;
    }

    @NonNull
    @Override
    public NovelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isGridLayout) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_novel_grid, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_novel, parent, false);
        }
        return new NovelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelViewHolder holder, int position) {
        Novel novel = novels.get(position);

        holder.tvTitle.setText(novel.getTitle());
        holder.tvAuthor.setText(novel.getAuthor());

        if (!isGridLayout) {
            holder.tvGenre.setText(novel.getGenre());
            holder.tvChapterCount.setText(context.getString(R.string.chapter_count, novel.getChapterCount()));
        }

        // Tải ảnh bìa truyện
        if (novel.getCoverUrl() != null && !novel.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(novel.getCoverUrl())
                    .placeholder(R.drawable.placeholder_cover)
                    .error(R.drawable.error_cover)
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.placeholder_cover);
        }

        // Xử lý khi nhấn vào truyện
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NovelDetailActivity.class);
            intent.putExtra("novel_id", novel.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return novels != null ? novels.size() : 0;
    }

    public void updateData(List<Novel> newNovels) {
        this.novels = newNovels;
        notifyDataSetChanged();
    }

    public static class NovelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvGenre;
        TextView tvChapterCount;

        public NovelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);

            // Các view chỉ có trong layout ngang
            if (itemView.findViewById(R.id.tv_genre) != null) {
                tvGenre = itemView.findViewById(R.id.tv_genre);
                tvChapterCount = itemView.findViewById(R.id.tv_chapter_count);
            }
        }
    }
}
