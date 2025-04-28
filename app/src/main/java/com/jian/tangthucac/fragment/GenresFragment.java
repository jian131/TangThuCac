
package com.jian.tangthucac.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.tangthucac.R;
import com.jian.tangthucac.activity.ChatActivity;
import com.jian.tangthucac.activity.GenreStoriesActivity;
import com.jian.tangthucac.activity.SearchActivity;
import com.jian.tangthucac.adapter.GenreAdapter;
import com.jian.tangthucac.model.Genre;

import java.util.ArrayList;
import java.util.List;

public class GenresFragment extends Fragment implements GenreAdapter.OnGenreClickListener {

    private RecyclerView rvGenres;
    private ImageView btnSearch, btnChat;
    private List<Genre> genres;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genres, container, false);

        // Initialize UI components
        rvGenres = view.findViewById(R.id.rvGenres);
        btnSearch = view.findViewById(R.id.btn_search);
        btnChat = view.findViewById(R.id.btn_chat);

        // Prepare data for Genres
        prepareGenreData();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up buttons
        btnSearch.setOnClickListener(v -> startActivity(new Intent(getContext(), SearchActivity.class)));
        btnChat.setOnClickListener(v -> startActivity(new Intent(getContext(), ChatActivity.class)));

        return view;
    }

    private void prepareGenreData() {
        genres = new ArrayList<>();

        // Add sample genres - in a real app, this could come from Firebase
        genres.add(new Genre("Tiên Hiệp", R.drawable.img_genre_tienhiep));
        genres.add(new Genre("Kiếm Hiệp", R.drawable.img_genre_kiemhiep));
        genres.add(new Genre("Ngôn Tình", R.drawable.img_genre_ngontinh));
        genres.add(new Genre("Đô Thị", R.drawable.img_genre_dothi));
        genres.add(new Genre("Huyền Huyễn", R.drawable.img_genre_huyenhuyen));
        genres.add(new Genre("Xuyên Không", R.drawable.img_genre_xuyenkhong));
        genres.add(new Genre("Truyện Teen", R.drawable.img_genre_truyenteen));
        genres.add(new Genre("Trinh Thám", R.drawable.img_genre_trinhtham));
        genres.add(new Genre("Lịch Sử", R.drawable.img_genre_lichsu));
        genres.add(new Genre("Huyền Nghi", R.drawable.img_genre_huyennghi));
    }

    private void setupRecyclerView() {
        rvGenres.setLayoutManager(new GridLayoutManager(getContext(), 2));
        GenreAdapter adapter = new GenreAdapter(getContext(), genres, this);
        rvGenres.setAdapter(adapter);
    }

    @Override
    public void onGenreClick(int position) {
        Genre selectedGenre = genres.get(position);
        Intent intent = new Intent(getContext(), GenreStoriesActivity.class);
        intent.putExtra("GENRE", selectedGenre.getName());
        startActivity(intent);
    }

    // Simple Genre model for encapsulation
    public static class Genre {
        private String name;
        private int imageResource;

        public Genre(String name, int imageResource) {
            this.name = name;
            this.imageResource = imageResource;
        }

        public String getName() {
            return name;
        }

        public int getImageResource() {
            return imageResource;
        }
    }
}
