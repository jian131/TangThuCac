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
import com.jian.tangthucac.model.Story;
import com.jian.tangthucac.activity.StoryDetailActivity;

import java.util.List;

/**
 * Adapter để hiển thị kết quả tìm kiếm truyện
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final Context context;
    private List<OriginalStory> stories;
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

    // Thêm phương thức để cập nhật dữ liệu mới
    public void updateData(List<Story> newStories) {
        // Chuyển đổi từ Story sang OriginalStory nếu cần
        this.stories.clear();
        for (Story story : newStories) {
            OriginalStory originalStory = new OriginalStory();
            originalStory.setId(story.getId());
            originalStory.setTitle(story.getTitle());
            originalStory.setAuthor(story.getAuthor());
            originalStory.setDescription(story.getDescription());
            originalStory.setImageUrl(story.getImageUrl());
            this.stories.add(originalStory);
        }
        notifyDataSetChanged();
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

        // Hiển thị thông tin
        holder.titleText.setText(story.getTitle());
        holder.authorText.setText("Tác giả: " + story.getAuthor());

        if (story.getDescription() != null && !story.getDescription().isEmpty()) {
            holder.descriptionText.setText(story.getDescription());
            holder.descriptionText.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionText.setVisibility(View.GONE);
        }

        // Tải hình ảnh
        if (story.getImageUrl() != null && !story.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(story.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_book)
                            .error(R.drawable.error_image))
                    .into(holder.coverImage);
        } else {
            holder.coverImage.setImageResource(R.drawable.placeholder_book);
        }

        // Thiết lập sự kiện click
        holder.viewDetailButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailClick(story);
            } else if (story != null && story.getId() != null) {
                // Fallback khi không có listener
                StoryDetailActivity.start(context, story.getId());
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
        public ImageView coverImage;
        public TextView titleText;
        public TextView authorText;
        public TextView descriptionText;
        public MaterialButton viewDetailButton;
        public MaterialButton downloadButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.coverImageView);
            titleText = itemView.findViewById(R.id.titleText);
            authorText = itemView.findViewById(R.id.authorText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            viewDetailButton = itemView.findViewById(R.id.viewDetailButton);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}
