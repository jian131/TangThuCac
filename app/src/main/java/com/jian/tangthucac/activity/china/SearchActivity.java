package com.jian.tangthucac.activity.china;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.R;
import com.jian.tangthucac.adapter.SearchResultAdapter;
import com.jian.tangthucac.model.ChineseNovel;
import com.jian.tangthucac.model.OriginalStory;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity tìm kiếm truyện Trung Quốc
 */
public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    private EditText etSearch;
    private ImageButton btnSearch;
    private RecyclerView recyclerViewResults;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    private SearchResultAdapter searchAdapter;
    private ChineseNovelManager novelManager;
    private List<OriginalStory> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Khởi tạo ChineseNovelManager
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(this);

        // Ánh xạ view
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);

        // Thiết lập adapter
        setupRecyclerView();

        // Xử lý sự kiện tìm kiếm
        btnSearch.setOnClickListener(v -> performSearch());

        // Xử lý intent từ thông báo hoặc từ màn hình khác
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String query = intent.getStringExtra("SEARCH_QUERY");
            if (query != null && !query.isEmpty()) {
                etSearch.setText(query);
                performSearch();
            }
        }
    }

    private void setupRecyclerView() {
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchResultAdapter(this, searchResults);
        searchAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onViewDetailClick(OriginalStory story) {
                openStoryDetail(story);
            }

            @Override
            public void onDownloadClick(OriginalStory story) {
                downloadStory(story);
            }
        });
        recyclerViewResults.setAdapter(searchAdapter);
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();

        // Thực hiện tìm kiếm
        novelManager.searchChineseNovels(query, 20, new ChineseNovelManager.OnNovelsLoadedListener() {
            @Override
            public void onNovelsLoaded(List<ChineseNovel> novels) {
                progressBar.setVisibility(View.GONE);

                if (novels.isEmpty()) {
                    tvNoResults.setVisibility(View.VISIBLE);
                } else {
                    // Chuyển đổi ChineseNovel sang OriginalStory
                    for (ChineseNovel novel : novels) {
                        searchResults.add(new OriginalStory(novel));
                    }
                    searchAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText("Lỗi: " + e.getMessage());
            }
        });
    }

    private void openStoryDetail(OriginalStory story) {
        Intent intent = new Intent(this, ChineseNovelDetailActivity.class);
        intent.putExtra("NOVEL_ID", story.getId());
        startActivity(intent);
    }

    private void downloadStory(OriginalStory story) {
        Toast.makeText(this, "Đang tải xuống: " + story.getTranslatedTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Thực hiện tải xuống truyện

        // Chuyển đổi thành ChineseNovel
        ChineseNovel novel = story.toChineseNovel();
        novel.setDownloaded(true);

        // Lưu vào cơ sở dữ liệu
        novelManager.saveChineseNovel(novel, new ChineseNovelManager.OnNovelLoadedListener() {
            @Override
            public void onNovelLoaded(ChineseNovel updatedNovel) {
                Toast.makeText(SearchActivity.this,
                    "Đã tải xuống: " + updatedNovel.getTitleVi(),
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SearchActivity.this,
                    "Lỗi khi tải xuống: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}
