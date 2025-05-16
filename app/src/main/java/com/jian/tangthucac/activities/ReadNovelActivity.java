package com.jian.tangthucac.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jian.tangthucac.R;
import com.jian.tangthucac.databinding.ActivityReadNovelBinding;
import com.jian.tangthucac.models.Chapter;
import com.jian.tangthucac.models.Novel;

public class ReadNovelActivity extends AppCompatActivity {

    private ActivityReadNovelBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private String novelId;
    private int chapterNumber = 1;
    private Novel novel;
    private Chapter currentChapter;

    // Thiết lập đọc
    private int fontSize = 16; // Cỡ chữ mặc định
    private boolean isDarkMode = false; // Chế độ đọc sáng mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadNovelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Lấy dữ liệu từ Intent
        novelId = getIntent().getStringExtra("novel_id");
        chapterNumber = getIntent().getIntExtra("chapter_number", 1);

        if (novelId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Tải thiết lập đọc truyện từ preferences
        loadReadingSettings();

        // Thiết lập sự kiện
        setupListeners();

        // Tải dữ liệu truyện và chương
        loadNovelData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reading, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_font_size) {
            showFontSizeDialog();
            return true;
        } else if (id == R.id.action_background) {
            toggleDarkMode();
            return true;
        } else if (id == R.id.action_chapters) {
            showChaptersDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupListeners() {
        // Nút chương trước
        binding.btnPrevious.setOnClickListener(v -> {
            if (chapterNumber > 1) {
                loadChapter(chapterNumber - 1);
            } else {
                Toast.makeText(this, "Đây là chương đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút chương sau
        binding.btnNext.setOnClickListener(v -> {
            if (novel != null && chapterNumber < novel.getChapterCount()) {
                loadChapter(chapterNumber + 1);
            } else {
                Toast.makeText(this, "Đây là chương mới nhất", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNovelData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // TODO: Tải thông tin truyện từ Firebase hoặc API

        // Sử dụng dữ liệu mẫu cho giai đoạn phát triển
        mockNovelData();
    }

    private void mockNovelData() {
        // Dữ liệu truyện giả lập
        novel = new Novel(
                novelId,
                "Đấu La Đại Lục",
                "Đường Gia Tam Thiếu",
                "https://example.com/cover.jpg",
                "Huyền Huyễn",
                4.8f,
                200,
                "",
                false,
                null,
                "zh",
                "",
                0,
                0,
                System.currentTimeMillis()
        );

        // Tải chương hiện tại
        loadChapter(chapterNumber);
    }

    private void loadChapter(int number) {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Cập nhật số chương hiện tại
        chapterNumber = number;

        // TODO: Tải nội dung chương từ Firebase hoặc API

        // Sử dụng dữ liệu mẫu cho giai đoạn phát triển
        mockChapterData(number);
    }

    private void mockChapterData(int number) {
        // Dữ liệu chương giả lập
        currentChapter = new Chapter(
                "chapter_" + number,
                novelId,
                number,
                "Chương " + number + ": Đấu La Đại Lục",
                "这是原始中文内容。这只是一个示例。这应该被翻译成越南语。",
                "Đây là nội dung tiếng Việt đã được dịch. Đây chỉ là ví dụ. Đấu La Đại Lục là câu chuyện về một cậu bé tên Đường Tam bị ép phải chạy trốn khỏi nơi sinh sống vì lý do bí ẩn. Khi người ta định giết cậu, cậu đã nhảy xuống từ vách núi nhưng không chết, ngược lại được một dị hồn bí ẩn phụ thể. Linh hồn này đã thay đổi vận mệnh của Đường Tam. Từ một thợ rèn thành một thiên tài Võ hồn. Và rồi trên con đường trở thành một trong số 7 đại đấu hồn gia đứng đầu đại lục, Đường Tam đã trải qua không ít gian truân, luôn đối mặt với các thế lực, tổ chức luôn muốn giết hại cậu. Đường Tam có vượt qua mọi chông gai, cạm bẫy hay không? Ai là kẻ đứng đằng sau tất cả? Kết thúc sẽ ra sao?",
                true,
                "",
                System.currentTimeMillis() - 86400000, // 1 ngày trước
                System.currentTimeMillis()
        );

        // Cập nhật UI
        updateChapterUI();

        // Lưu tiến độ đọc
        saveReadingProgress();
    }

    private void updateChapterUI() {
        if (currentChapter == null) return;

        binding.progressBar.setVisibility(View.GONE);
        binding.scrollView.setVisibility(View.VISIBLE);

        // Hiển thị tiêu đề chương
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentChapter.getDisplayTitle());
        }

        // Hiển thị nội dung chương
        binding.tvContent.setText(currentChapter.getDisplayContent());

        // Cập nhật trạng thái nút
        binding.btnPrevious.setEnabled(chapterNumber > 1);
        binding.btnNext.setEnabled(novel != null && chapterNumber < novel.getChapterCount());
    }

    private void saveReadingProgress() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Lưu tiến độ đọc vào Firestore
        mFirestore.collection("users")
                .document(user.getUid())
                .update("readingHistory", com.google.firebase.firestore.FieldValue.arrayUnion(novelId))
                .addOnFailureListener(e -> {
                    // Không cần hiển thị lỗi cho người dùng
                });

        // Lưu chương đang đọc
        mFirestore.collection("reading_progress")
                .document(user.getUid() + "_" + novelId)
                .set(new java.util.HashMap<String, Object>() {{
                    put("userId", user.getUid());
                    put("novelId", novelId);
                    put("chapterNumber", chapterNumber);
                    put("timestamp", System.currentTimeMillis());
                }})
                .addOnFailureListener(e -> {
                    // Không cần hiển thị lỗi cho người dùng
                });
    }

    private void loadReadingSettings() {
        // TODO: Tải thiết lập từ SharedPreferences

        // Áp dụng thiết lập
        applyFontSize();
        applyBackgroundColor();
    }

    private void applyFontSize() {
        binding.tvContent.setTextSize(fontSize);
    }

    private void applyBackgroundColor() {
        if (isDarkMode) {
            binding.scrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.reading_background_dark));
            binding.tvContent.setTextColor(ContextCompat.getColor(this, R.color.reading_text_dark));
        } else {
            binding.scrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.reading_background_light));
            binding.tvContent.setTextColor(ContextCompat.getColor(this, R.color.reading_text_light));
        }
    }

    private void showFontSizeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_font_size, null);
        SeekBar seekBar = dialogView.findViewById(R.id.seekbar_font_size);

        // Thiết lập giá trị hiện tại
        seekBar.setProgress(fontSize - 12); // Giả sử cỡ chữ nhỏ nhất là 12

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.font_size)
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    fontSize = seekBar.getProgress() + 12;
                    applyFontSize();
                    // TODO: Lưu thiết lập vào SharedPreferences
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyBackgroundColor();
        // TODO: Lưu thiết lập vào SharedPreferences
    }

    private void showChaptersDialog() {
        // TODO: Hiển thị danh sách chương
        Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
    }
}
