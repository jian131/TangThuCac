package com.jian.tangthucac.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jian.tangthucac.R;
import com.jian.tangthucac.databinding.ActivityNovelDetailBinding;
import com.jian.tangthucac.models.Novel;

public class NovelDetailActivity extends AppCompatActivity {

    private ActivityNovelDetailBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private Novel novel;
    private String novelId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNovelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Lấy ID truyện từ Intent
        novelId = getIntent().getStringExtra("novel_id");
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

        // Thiết lập sự kiện
        setupListeners();

        // Tải thông tin truyện
        loadNovelDetails();
    }

    private void setupListeners() {
        // Nút thêm/xóa khỏi yêu thích
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Nút bắt đầu đọc truyện
        binding.btnStartReading.setOnClickListener(v -> startReading(1)); // Bắt đầu từ chương 1

        // Nút tiếp tục đọc
        binding.btnContinueReading.setOnClickListener(v -> {
            // TODO: Lấy chương đọc gần nhất
            startReading(1); // Tạm thời bắt đầu từ chương 1
        });
    }

    private void loadNovelDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // TODO: Tải thông tin truyện từ Firestore hoặc API

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
                2000,
                "Đấu La Đại Lục là câu chuyện về một cậu bé tên Đường Tam bị ép phải chạy trốn khỏi nơi sinh sống vì lý do bí ẩn. Khi người ta định giết cậu, cậu đã nhảy xuống từ vách núi nhưng không chết, ngược lại được một dị hồn bí ẩn phụ thể. Linh hồn này đã thay đổi vận mệnh của Đường Tam. Từ một thợ rèn thành một thiên tài Võ hồn. Và rồi trên con đường trở thành một trong số 7 đại đấu hồn gia đứng đầu đại lục, Đường Tam đã trải qua không ít gian truân, luôn đối mặt với các thế lực, tổ chức luôn muốn giết hại cậu. Đường Tam có vượt qua mọi chông gai, cạm bẫy hay không? Ai là kẻ đứng đằng sau tất cả? Kết thúc sẽ ra sao?",
                false,
                null,
                "zh",
                "https://example.com/novel/1",
                15000,
                500,
                System.currentTimeMillis()
        );

        // Hiển thị dữ liệu
        updateUI();

        // Kiểm tra trạng thái yêu thích
        checkFavoriteStatus();
    }

    private void updateUI() {
        if (novel == null) return;

        binding.progressBar.setVisibility(View.GONE);
        binding.scrollView.setVisibility(View.VISIBLE);

        // Hiển thị tiêu đề
        binding.tvTitle.setText(novel.getTitle());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(novel.getTitle());
        }

        // Hiển thị thông tin tác giả
        binding.tvAuthor.setText(getString(R.string.author, novel.getAuthor()));

        // Hiển thị thể loại
        binding.tvGenre.setText(getString(R.string.category, novel.getGenre()));

        // Hiển thị số chương
        binding.tvChapterCount.setText(getString(R.string.chapters, novel.getChapterCount()));

        // Hiển thị trạng thái hoàn thành
        binding.tvStatus.setText(getString(R.string.status, novel.getCompletionStatus()));

        // Hiển thị mô tả
        binding.tvDescription.setText(novel.getDescription());

        // Tải ảnh bìa
        if (novel.getCoverUrl() != null && !novel.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(novel.getCoverUrl())
                    .placeholder(R.drawable.placeholder_cover)
                    .error(R.drawable.error_cover)
                    .into(binding.ivCover);
        } else {
            binding.ivCover.setImageResource(R.drawable.placeholder_cover);
        }

        // Hiển thị nút tiếp tục đọc nếu đã đọc truyện này
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // TODO: Kiểm tra xem người dùng đã đọc truyện này chưa
            binding.btnContinueReading.setVisibility(View.GONE); // Tạm thời ẩn
        } else {
            binding.btnContinueReading.setVisibility(View.GONE);
        }
    }

    private void checkFavoriteStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            updateFavoriteButton(false);
            return;
        }

        // Kiểm tra xem truyện có trong danh sách yêu thích không
        mFirestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy danh sách truyện yêu thích
                        java.util.List<String> favorites = (java.util.List<String>) documentSnapshot.get("favoriteNovels");
                        isFavorite = favorites != null && favorites.contains(novelId);
                        updateFavoriteButton(isFavorite);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra trạng thái yêu thích", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleFavorite() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, getString(R.string.login_required), Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = mFirestore.collection("users").document(user.getUid());

        if (isFavorite) {
            // Xóa khỏi yêu thích
            userRef.update("favoriteNovels", com.google.firebase.firestore.FieldValue.arrayRemove(novelId))
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateFavoriteButton(false);
                        Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Thêm vào yêu thích
            userRef.update("favoriteNovels", com.google.firebase.firestore.FieldValue.arrayUnion(novelId))
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateFavoriteButton(true);
                        Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            binding.btnFavorite.setText(R.string.remove_from_favorites);
            binding.btnFavorite.setBackgroundResource(R.drawable.bg_button_remove_favorite);
        } else {
            binding.btnFavorite.setText(R.string.add_to_favorites);
            binding.btnFavorite.setBackgroundResource(R.drawable.bg_button_add_favorite);
        }
    }

    private void startReading(int chapterNumber) {
        Intent intent = new Intent(this, ReadNovelActivity.class);
        intent.putExtra("novel_id", novelId);
        intent.putExtra("chapter_number", chapterNumber);
        startActivity(intent);
    }
}
