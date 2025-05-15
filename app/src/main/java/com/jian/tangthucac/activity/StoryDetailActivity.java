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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.NovelCrawler;
import com.jian.tangthucac.R;
import com.jian.tangthucac.adapter.ChapterAdapter;
import com.jian.tangthucac.databinding.ActivityStoryDetailBinding;
import com.jian.tangthucac.model.Chapter;
import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.Story;
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

        // Thiết lập RecyclerView cho danh sách chương
        setupRecyclerView();

        // Thiết lập listeners
        setupListeners();

        // Kiểm tra dữ liệu truyện từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Ưu tiên kiểm tra nếu có truyền trực tiếp đối tượng Story
            OriginalStory storyFromIntent = (OriginalStory) intent.getSerializableExtra("story");
            if (storyFromIntent != null) {
                // Lấy thông tin từ đối tượng Story được truyền vào
                storyId = storyFromIntent.getId();

                // Chuyển đổi từ Story sang OriginalStory nếu cần thiết
                story = new OriginalStory();
                story.setId(storyFromIntent.getId());
                story.setTitle(storyFromIntent.getTitle());
                story.setAuthor(storyFromIntent.getAuthor());
                story.setDescription(storyFromIntent.getDescription());
                story.setImageUrl(storyFromIntent.getImageUrl());

                // Hiển thị thông tin và tải chương sách
                displayStoryDetails();
                loadChapters();
                return;
            }

            // Nếu không có đối tượng Story, thử lấy storyId
            storyId = intent.getStringExtra("story_id");
            if (storyId == null || storyId.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy ID truyện", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Tải thông tin truyện từ id
            loadStoryDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin truyện", Toast.LENGTH_SHORT).show();
            finish();
        }
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
                // Kiểm tra nếu loadedStory là null
                if (loadedStory == null) {
                    Toast.makeText(StoryDetailActivity.this, "Không tìm thấy thông tin truyện", Toast.LENGTH_SHORT).show();
                    binding.downloadProgressBar.setVisibility(View.GONE);
                    finish();
                    return;
                }

                story = loadedStory;

                // Hiển thị thông tin truyện
                displayStoryDetails();

                // Lấy danh sách chương
                loadChapters();
            }

            @Override
            public void onError(Exception e) {
                // Kiểm tra nếu activity chưa bị destroy
                if (!isFinishing()) {
                    Toast.makeText(StoryDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.downloadProgressBar.setVisibility(View.GONE);

                    // Kiểm tra nếu là lỗi "Story not found", thử lấy thông tin từ Firebase Realtime Database
                    if (e.getMessage() != null && e.getMessage().contains("Story not found")) {
                        loadStoryFromStandardDatabase();
                    } else {
                        finish();
                    }
                }
            }
        });
    }

    /**
     * Tải thông tin truyện từ Firebase Realtime Database tiêu chuẩn
     * trong trường hợp không tìm thấy ở ChineseNovelManager
     */
    private void loadStoryFromStandardDatabase() {
        // Hiển thị loading
        binding.downloadProgressBar.setVisibility(View.VISIBLE);

        DatabaseReference storiesRef = FirebaseDatabase.getInstance().getReference("stories").child(storyId);
        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Story standardStory = snapshot.getValue(Story.class);
                    if (standardStory != null) {
                        standardStory.setId(snapshot.getKey());

                        // Chuyển đổi từ Story sang OriginalStory
                        story = new OriginalStory();
                        story.setId(standardStory.getId());
                        story.setTitle(standardStory.getTitle());
                        story.setAuthor(standardStory.getAuthor());
                        story.setDescription(standardStory.getDescription());
                        story.setImageUrl(standardStory.getImageUrl() != null ?
                                standardStory.getImageUrl() : standardStory.getImage());
                        story.setTotalChapters(standardStory.getTotalChapters());

                        // Hiển thị thông tin
                        displayStoryDetails();

                        // Lấy danh sách chương nếu có
                        if (standardStory.getChapters() != null) {
                            convertAndDisplayStandardChapters(standardStory.getChapters());
                        } else {
                            binding.downloadProgressBar.setVisibility(View.GONE);
                            binding.chapterStatusText.setText("Đã tải: 0/0 chương • Đã dịch: 0 chương");
                        }
                    } else {
                        binding.downloadProgressBar.setVisibility(View.GONE);
                        Toast.makeText(StoryDetailActivity.this, "Không thể đọc dữ liệu truyện", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    binding.downloadProgressBar.setVisibility(View.GONE);
                    Toast.makeText(StoryDetailActivity.this, "Không tìm thấy truyện trong hệ thống", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.downloadProgressBar.setVisibility(View.GONE);
                Toast.makeText(StoryDetailActivity.this, "Lỗi truy vấn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Chuyển đổi các chương từ Story tiêu chuẩn sang dạng TranslatedChapter
     */
    private void convertAndDisplayStandardChapters(Map<String, Chapter> chaptersMap) {
        chapterList.clear();

        if (chaptersMap != null) {
            for (Map.Entry<String, Chapter> entry : chaptersMap.entrySet()) {
                Chapter standardChapter = entry.getValue();
                TranslatedChapter translatedChapter = new TranslatedChapter();

                translatedChapter.setId(entry.getKey());
                translatedChapter.setTitle(standardChapter.getTitle());
                translatedChapter.setContent(standardChapter.getContent());
                translatedChapter.setIndex(standardChapter.getIndex());
                translatedChapter.setStoryId(storyId);

                chapterList.add(translatedChapter);
            }
        }

        // Cập nhật adapter
        chapterAdapter.notifyDataSetChanged();

        // Cập nhật UI
        String chapterStatus = "Đã tải: " + chapterList.size() + "/" +
                (story.getTotalChapters() > 0 ? story.getTotalChapters() : chapterList.size()) +
                " chương • Đã dịch: " + chapterList.size() + " chương";
        binding.chapterStatusText.setText(chapterStatus);

        binding.downloadProgressBar.setVisibility(View.GONE);
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
        // Kiểm tra biến story để tránh NullPointerException
        if (story == null) {
            Toast.makeText(this, "Thông tin truyện chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy số lượng chương và kiểm tra hợp lệ
        int chapterCount = story.getChapterCount();
        if (chapterCount <= 0) {
            Toast.makeText(this, "Không có chương nào để tải xuống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị thông báo tải xuống
        Toast.makeText(this, "Bắt đầu tải " + chapterCount + " chương", Toast.LENGTH_SHORT).show();

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
