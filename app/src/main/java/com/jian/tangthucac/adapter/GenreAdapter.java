
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
import com.jian.tangthucac.fragment.GenresFragment.Genre;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
    private final Context context;
    private final List<Genre> genres;
    private final OnGenreClickListener listener;

    public interface OnGenreClickListener {
        void onGenreClick(int position);
    }

    public GenreAdapter(Context context, List<Genre> genres, OnGenreClickListener listener) {
        this.context = context;
        this.genres = genres;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genres.get(position);
        holder.tvGenreName.setText(genre.getName());
        holder.ivGenreImage.setImageResource(genre.getImageResource());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGenreClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    static class GenreViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGenreImage;
        TextView tvGenreName;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGenreImage = itemView.findViewById(R.id.ivGenreImage);
            tvGenreName = itemView.findViewById(R.id.tvGenreName);
        }
    }
}
