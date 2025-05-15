package com.jian.tangthucac.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jian.tangthucac.R;
import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.adapter.TranslatedNovelAdapter;
import com.jian.tangthucac.model.OriginalStory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách truyện Trung Quốc đã dịch của người dùng
 */
public class MyTranslatedNovelsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TranslatedNovelAdapter adapter;
    private List<OriginalStory> translatedNovels;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private View loadingView;

    private ChineseNovelManager novelManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_translated_novels);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Truyện đã dịch của tôi");
        }

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo ChineseNovelManager
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(getApplicationContext());

        // Khởi tạo views
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        loadingView = findViewById(R.id.loadingView);

        // Thiết lập RecyclerView
        translatedNovels = new ArrayList<>();
        adapter = new TranslatedNovelAdapter(this, translatedNovels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Thiết lập adapter OnItemClickListener
        adapter.setOnItemClickListener(new TranslatedNovelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(OriginalStory story) {
                // Mở màn hình chi tiết truyện
                openStoryDetail(story);
            }

            @Override
            public void onResumeTranslationClick(OriginalStory story) {
                // Mở màn hình dịch tiếp truyện
                resumeTranslation(story);
            }

            @Override
            public void onDeleteClick(OriginalStory story) {
                // Xóa truyện
                deleteTranslatedNovel(story);
            }
        });

        // Thiết lập SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadTranslatedNovels);

        // Tải danh sách truyện đã dịch
        loadTranslatedNovels();
    }

    private void loadTranslatedNovels() {
        // Kiểm tra đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem truyện đã dịch", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị loading
        showLoading();

        // Lấy danh sách truyện đã dịch của người dùng
        String userId = currentUser.getUid();
        novelManager.getUserTranslatedNovels(userId, new ChineseNovelManager.OnStoriesLoadedListener() {
            @Override
            public void onStoriesLoaded(List<OriginalStory> stories) {
                runOnUiThread(() -> {
                    // Cập nhật danh sách truyện
                    translatedNovels.clear();
                    translatedNovels.addAll(stories);
                    adapter.notifyDataSetChanged();

                    // Ẩn refresh indicator
                    swipeRefreshLayout.setRefreshing(false);

                    // Kiểm tra danh sách trống
                    if (translatedNovels.isEmpty()) {
                        showEmptyState();
                    } else {
                        showContent();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyTranslatedNovelsActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    showEmptyState();
                });
            }
        });
    }

    private void openStoryDetail(OriginalStory story) {
        // Mở màn hình chi tiết truyện
        StoryDetailActivity.start(this, story.getId());
    }

    private void resumeTranslation(OriginalStory story) {
        // Mở màn hình dịch tiếp truyện
        Toast.makeText(this, "Đang chuẩn bị dịch tiếp " + story.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Triển khai chức năng dịch tiếp
    }

    private void deleteTranslatedNovel(OriginalStory story) {
        // Hiển thị dialog xác nhận trước khi xóa
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa truyện \"" + story.getTitle() + "\" khỏi danh sách truyện đã dịch?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa truyện khỏi danh sách đã dịch
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        novelManager.removeUserTranslatedNovel(userId, story.getId(), new ChineseNovelManager.FirebaseCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                runOnUiThread(() -> {
                                    // Xóa khỏi danh sách và cập nhật adapter
                                    translatedNovels.remove(story);
                                    adapter.notifyDataSetChanged();

                                    // Hiển thị empty state nếu danh sách trống
                                    if (translatedNovels.isEmpty()) {
                                        showEmptyState();
                                    }

                                    Toast.makeText(MyTranslatedNovelsActivity.this, "Đã xóa truyện", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MyTranslatedNovelsActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
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
