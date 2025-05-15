package com.jian.tangthucac.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jian.tangthucac.API.WikidichScraper;
import com.jian.tangthucac.R;
import com.jian.tangthucac.adapter.ChapterAdapter;
import com.jian.tangthucac.model.Chapter;
import com.jian.tangthucac.model.Story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikidichBrowserActivity extends AppCompatActivity {
    private EditText etUrl;
    private Button btnLoad, btnSaveToLibrary;
    private ProgressBar progressBar;
    private TextView tvStatus, tvTitle, tvAuthor, tvGenres, tvChapters, tvDescription, tvChapterTitle;
    private ImageView ivCover;
    private CardView storyInfoCard;
    private RecyclerView rvChapters;

    private Story currentStory;
    private List<Chapter> chapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikidich_browser);

        initViews();
        setupListeners();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etUrl = findViewById(R.id.etUrl);
        btnLoad = findViewById(R.id.btnLoad);
        btnSaveToLibrary = findViewById(R.id.btnSaveToLibrary);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvGenres = findViewById(R.id.tvGenres);
        tvChapters = findViewById(R.id.tvChapters);
        tvDescription = findViewById(R.id.tvDescription);
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        ivCover = findViewById(R.id.ivCover);
        storyInfoCard = findViewById(R.id.storyInfoCard);
        rvChapters = findViewById(R.id.rvChapters);

        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        chapterList = new ArrayList<>();
    }

    private void setupListeners() {
        btnLoad.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập URL", Toast.LENGTH_SHORT).show();
                return;
            }

            loadStoryFromUrl(url);
        });

        btnSaveToLibrary.setOnClickListener(v -> {
            if (currentStory != null) {
                saveStoryToLibrary();
            }
        });
    }

    private void loadStoryFromUrl(String url) {
        showLoading(true);
        tvStatus.setText("Đang tải thông tin truyện...");

        WikidichScraper.scrapeStory(url, new WikidichScraper.OnScrapingCompleteListener() {
            @Override
            public void onStoryScraped(Story story) {
                showLoading(false);
                if (story != null) {
                    currentStory = story;
                    displayStoryInfo(story);
                    loadChapters(story);
                } else {
                    tvStatus.setText("Không thể tải thông tin truyện");
                }
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                tvStatus.setText("Lỗi: " + e.getMessage());
            }
        });
    }

    private void displayStoryInfo(Story story) {
        storyInfoCard.setVisibility(View.VISIBLE);

        tvTitle.setText(story.getTitle());
        tvAuthor.setText("Tác giả: " + story.getAuthor());

        if (story.getGenres() != null && !story.getGenres().isEmpty()) {
            tvGenres.setText("Thể loại: " + String.join(", ", story.getGenres()));
        } else {
            tvGenres.setText("Thể loại: Không xác định");
        }

        tvChapters.setText("Số chương: " + story.getTotalChapters());
        tvDescription.setText(story.getDescription());

        if (story.getImage() != null && !story.getImage().isEmpty()) {
            Glide.with(this)
                    .load(story.getImage())
                    .placeholder(R.drawable.loading_placeholder)
                    .error(R.drawable.default_cover)
                    .into(ivCover);
        }
    }

    private void loadChapters(Story story) {
        tvChapterTitle.setVisibility(View.VISIBLE);
        tvChapterTitle.setText("Danh sách chương");

        Map<String, Chapter> chapters = story.getChapters();
        if (chapters != null && !chapters.isEmpty()) {
            chapterList.clear();
            chapterList.addAll(chapters.values());

            ChapterAdapter adapter = new ChapterAdapter(this, chapterList, position -> {
                Chapter chapter = chapterList.get(position);
                loadChapterContent(chapter);
            });

            rvChapters.setAdapter(adapter);
            rvChapters.setVisibility(View.VISIBLE);
        } else {
            tvChapterTitle.setText("Không có chương nào");
        }
    }

    private void loadChapterContent(Chapter chapter) {
        Intent intent = new Intent(this, ChapterReaderActivity.class);
        intent.putExtra("chapter", chapter);
        intent.putExtra("storyTitle", currentStory.getTitle());
        startActivity(intent);
    }

    private void saveStoryToLibrary() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để lưu truyện", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Library")
                .child(currentStory.getTitle());

        Map<String, Object> storyData = new HashMap<>();
        storyData.put("title", currentStory.getTitle());
        storyData.put("author", currentStory.getAuthor());
        storyData.put("image", currentStory.getImage());
        storyData.put("description", currentStory.getDescription());
        storyData.put("source", "wikidich");
        storyData.put("url", etUrl.getText().toString().trim());

        ref.setValue(storyData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(WikidichBrowserActivity.this, "Đã lưu vào thư viện", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(WikidichBrowserActivity.this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLoad.setEnabled(!isLoading);
    }
}
