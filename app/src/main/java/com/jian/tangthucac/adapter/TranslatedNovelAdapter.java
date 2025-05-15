package com.jian.tangthucac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.tangthucac.R;
import com.jian.tangthucac.API.StorageManager;
import com.jian.tangthucac.model.OriginalStory;

import java.util.List;

/**
 * Adapter hiển thị danh sách truyện Trung Quốc đã dịch
 */
public class TranslatedNovelAdapter extends RecyclerView.Adapter<TranslatedNovelAdapter.ViewHolder> {

    private Context context;
    private List<OriginalStory> translatedNovels;
    private OnItemClickListener listener;
    private StorageManager storageManager;

    /**
     * Interface để xử lý sự kiện click
     */
    public interface OnItemClickListener {
        void onItemClick(OriginalStory story);
        void onResumeTranslationClick(OriginalStory story);
        void onDeleteClick(OriginalStory story);
    }

    public TranslatedNovelAdapter(Context context, List<OriginalStory> translatedNovels) {
        this.context = context;
        this.translatedNovels = translatedNovels;
        this.storageManager = new StorageManager();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_translated_novel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OriginalStory story = translatedNovels.get(position);

        // Thiết lập dữ liệu cho view
        holder.tvTitle.setText(story.getTitle());
        holder.tvAuthor.setText(story.getAuthor());

        // Hiển thị số chương đã dịch
        int translatedChapters = story.getTranslatedChaptersCount();
        int totalChapters = story.getTotalChapters();
        holder.tvProgress.setText(translatedChapters + "/" + totalChapters + " chương");

        // Hiển thị tiến độ dịch
        int progressPercent = totalChapters > 0 ? (translatedChapters * 100 / totalChapters) : 0;
        holder.tvProgressPercent.setText(progressPercent + "%");

        // Tải ảnh bìa truyện
        if (story.getCoverImage() != null && !story.getCoverImage().isEmpty()) {
            storageManager.loadImage(context, story.getCoverImage(), holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.placeholder_book);
        }

        // Thiết lập sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(story);
            }
        });

        holder.btnResume.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResumeTranslationClick(story);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(story);
            }
        });
    }

    @Override
    public int getItemCount() {
        return translatedNovels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvAuthor, tvProgress, tvProgressPercent;
        View btnResume, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            btnResume = itemView.findViewById(R.id.btnResume);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
