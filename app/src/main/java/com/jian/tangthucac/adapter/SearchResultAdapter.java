package com.jian.tangthucac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.jian.tangthucac.R;
import com.jian.tangthucac.model.OriginalStory;

import java.util.List;

/**
 * Adapter để hiển thị kết quả tìm kiếm truyện
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final Context context;
    private final List<OriginalStory> stories;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onViewDetailClick(OriginalStory story);
        void onDownloadClick(OriginalStory story);
    }

    public SearchResultAdapter(Context context, List<OriginalStory> stories) {
        this.context = context;
        this.stories = stories;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OriginalStory story = stories.get(position);

        // Hiển thị thông tin truyện
        holder.titleText.setText(story.getTitle());

        // Hiển thị tiêu đề tiếng Việt nếu có
        if (story.getTitleVi() != null && !story.getTitleVi().isEmpty()) {
            holder.titleViText.setText(story.getTitleVi());
            holder.titleViText.setVisibility(View.VISIBLE);
        } else {
            holder.titleViText.setVisibility(View.GONE);
        }

        // Hiển thị tác giả
        holder.authorText.setText(story.getAuthor());

        // Hiển thị thể loại
        if (story.getGenres() != null && !story.getGenres().isEmpty()) {
            holder.genresText.setText(String.join(", ", story.getGenres()));
        } else {
            holder.genresText.setText("Không xác định");
        }

        // Hiển thị mô tả
        holder.descriptionText.setText(story.getDescription());

        // Hiển thị nguồn
        holder.sourceText.setText("Nguồn: " + story.getSource());

        // Tải ảnh bìa
        Glide.with(context)
                .load(story.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_book)
                        .error(R.drawable.placeholder_book))
                .into(holder.coverImageView);

        // Set click listeners
        holder.viewDetailButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailClick(story);
            }
        });

        holder.downloadButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadClick(story);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView coverImageView;
        public TextView titleText;
        public TextView titleViText;
        public TextView authorText;
        public TextView genresText;
        public TextView descriptionText;
        public TextView sourceText;
        public MaterialButton viewDetailButton;
        public MaterialButton downloadButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.coverImageView);
            titleText = itemView.findViewById(R.id.titleText);
            titleViText = itemView.findViewById(R.id.titleViText);
            authorText = itemView.findViewById(R.id.authorText);
            genresText = itemView.findViewById(R.id.genresText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            sourceText = itemView.findViewById(R.id.sourceText);
            viewDetailButton = itemView.findViewById(R.id.viewDetailButton);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}
