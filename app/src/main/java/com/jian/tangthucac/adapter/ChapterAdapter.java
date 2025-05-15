package com.jian.tangthucac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.tangthucac.model.TranslatedChapter;
import com.jian.tangthucac.R;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
    private Context context;
    private List<TranslatedChapter> chapterList;
    private OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(TranslatedChapter chapter);
    }

    public ChapterAdapter(Context context, List<TranslatedChapter> chapterList) {
        this.context = context;
        this.chapterList = chapterList;
    }

    public void setOnItemClickListener(OnChapterClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        TranslatedChapter chapter = chapterList.get(position);
        holder.chapterTitleTextView.setText(chapter.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChapterClick(chapter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView chapterTitleTextView;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            chapterTitleTextView = itemView.findViewById(R.id.chapterTitleTextView);
        }
    }
}
