package com.jian.tangthucac.activity;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.NovelCrawler;
import com.jian.tangthucac.R;
import com.jian.tangthucac.adapter.ChapterAdapter;
import com.jian.tangthucac.databinding.ActivityStoryDetailBinding;
import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.TranslatedChapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity hiển thị chi tiết truyện và danh sách chương
 */
public class StoryDetailActivity extends AppCompatActivity {

    private ActivityStoryDetailBinding binding;
    private ChapterAdapter chapterAdapter;
    private final List<TranslatedChapter> chapterList = new ArrayList<>();

    // Các manager
    private ChineseNovelManager novelManager;

    // Dữ liệu truyện
    private OriginalStory story;
    private String storyId;

    /**
     * Phương thức khởi động Activity từ bên ngoài
     * @param context Context gọi activity
     * @param storyId ID của truyện cần xem chi tiết
     */
    public static void start(Context context, String storyId) {
        Intent intent = new Intent(context, StoryDetailActivity.class);
        intent.putExtra("story_id", storyId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Khởi tạo manager
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(getApplicationContext());

        // Lấy storyId từ intent
        storyId = getIntent().getStringExtra("story_id");
        if (storyId == null) {
            Toast.makeText(this, "Không tìm thấy ID truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập RecyclerView cho danh sách chương
        setupRecyclerView();

        // Thiết lập listeners
        setupListeners();

        // Tải thông tin truyện
        loadStoryDetails();
    }

    private void setupRecyclerView() {
        chapterAdapter = new ChapterAdapter(this, chapterList);
        binding.chaptersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chaptersRecyclerView.setAdapter(chapterAdapter);

        // Thiết lập click listener
        chapterAdapter.setOnItemClickListener(chapter -> openChapter(chapter));
    }

    private void setupListeners() {
        // Nút tải xuống
        binding.downloadButton.setOnClickListener(v -> {
            downloadAllChapters();
        });

        // Nút đọc truyện
        binding.readButton.setOnClickListener(v -> {
            if (chapterList.isEmpty()) {
                Toast.makeText(this, "Chưa có chương nào", Toast.LENGTH_SHORT).show();
            } else {
                openChapter(chapterList.get(0));
            }
        });
    }

    private void loadStoryDetails() {
        // Hiển thị loading
        binding.downloadProgressBar.setVisibility(View.VISIBLE);

        // Lấy thông tin truyện từ cache hoặc Firebase
        novelManager.getOriginalStoryById(storyId, new ChineseNovelManager.OnStoryLoadedListener() {
            @Override
            public void onStoryLoaded(OriginalStory loadedStory) {
                story = loadedStory;

                // Hiển thị thông tin truyện
                displayStoryDetails();

                // Lấy danh sách chương
                loadChapters();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(StoryDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.downloadProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void displayStoryDetails() {
        // Hiển thị thông tin cơ bản
        binding.titleText.setText(story.getTitle());

        if (story.getTitleVi() != null && !story.getTitleVi().isEmpty()) {
            binding.titleViText.setText(story.getTitleVi());
            binding.titleViText.setVisibility(View.VISIBLE);
        } else {
            binding.titleViText.setVisibility(View.GONE);
        }

        binding.authorText.setText("Tác giả: " + story.getAuthor());

        // Hiển thị trạng thái
        String status = "Trạng thái: " + (story.isCompleted() ? "Hoàn thành" : "Đang tiến hành");
        status += " • " + story.getChapterCount() + " chương";
        binding.statusText.setText(status);

        // Hiển thị thể loại
        if (story.getGenres() != null && !story.getGenres().isEmpty()) {
            binding.genresText.setText("Thể loại: " + String.join(", ", story.getGenres()));
        } else {
            binding.genresText.setText("Thể loại: Không xác định");
        }

        // Hiển thị mô tả
        if (story.getDescription() != null) {
            binding.descriptionText.setText(story.getDescription());
        }

        if (story.getDescriptionVi() != null && !story.getDescriptionVi().isEmpty()) {
            binding.originalDescriptionText.setText(story.getDescriptionVi());
            binding.originalDescriptionText.setVisibility(View.VISIBLE);
        } else {
            binding.originalDescriptionText.setVisibility(View.GONE);
        }

        // Hiển thị nguồn
        binding.sourceText.setText("Nguồn: " + story.getSource());

        // Tải ảnh bìa
        if (story.getImageUrl() != null && !story.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(story.getImageUrl())
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book)
                    .into(binding.storyImage);
        }
    }

    private void loadChapters() {
        // Hiển thị thông tin chương
        if (story.getTranslatedChapters() != null) {
            Map<String, TranslatedChapter> chapterMap = story.getTranslatedChapters();
            int translatedCount = chapterMap.size();

            String chapterStatus = "Đã tải: " + translatedCount + "/" + story.getChapterCount() + " chương";
            chapterStatus += " • Đã dịch: " + translatedCount + " chương";
            binding.chapterStatusText.setText(chapterStatus);

            // Cập nhật danh sách chương
            chapterList.clear();
            chapterList.addAll(chapterMap.values());
            chapterAdapter.notifyDataSetChanged();
        } else {
            binding.chapterStatusText.setText("Đã tải: 0/" + story.getChapterCount() + " chương • Đã dịch: 0 chương");
        }

        binding.downloadProgressBar.setVisibility(View.GONE);
    }

    private void downloadAllChapters() {
        // Hiển thị thông báo tải xuống
        Toast.makeText(this, "Bắt đầu tải " + story.getChapterCount() + " chương", Toast.LENGTH_SHORT).show();

        // TODO: Triển khai tải xuống tất cả các chương bằng WorkManager
        // Đây là một tác vụ nặng cần được thực hiện trong background
    }

    private void openChapter(TranslatedChapter chapter) {
        // Mở activity đọc chương
        Intent intent = new Intent(this, ChapterReaderActivity.class);
        intent.putExtra("story_id", story.getId());
        intent.putExtra("chapter_id", chapter.getId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
