package com.jian.tangthucac.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.TranslationService;
import com.jian.tangthucac.R;
import com.jian.tangthucac.adapter.SearchResultAdapter;
import com.jian.tangthucac.databinding.ActivityChineseNovelSearchBinding;
import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.SearchKeywordMap;
import com.jian.tangthucac.crawler.CrawlerFactory;
import com.jian.tangthucac.crawler.NovelCrawler;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity cho phép tìm kiếm truyện tiếng Trung với khả năng dịch từ khóa tự động
 */
public class ChineseNovelSearchActivity extends AppCompatActivity {

    private static final String TAG = "ChineseNovelSearch";
    private ActivityChineseNovelSearchBinding binding;
    private SearchResultAdapter adapter;
    private final List<OriginalStory> searchResults = new ArrayList<>();

    // Services
    private ChineseNovelManager novelManager;
    private TranslationService translationService;

    // Tracking state
    private String currentKeyword = "";
    private String detectedLanguage = "";
    private String translatedKeyword = "";
    private boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChineseNovelSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Khởi tạo managers và services
        initializeManagers();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập listeners
        setupListeners();

        // Kiểm tra có intent search không
        handleSearchIntent(getIntent());
    }

    private void initializeManagers() {
        // Khởi tạo ChineseNovelManager
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(getApplicationContext());

        // Khởi tạo TranslationService với API keys từ SharedPreferences
        translationService = TranslationService.getInstance();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String claudeApiKey = prefs.getString("claude_api_key", "");
        String deeplApiKey = prefs.getString("deepl_api_key", "");

        translationService.initialize(getApplicationContext(), claudeApiKey, deeplApiKey);
    }

    private void setupRecyclerView() {
        adapter = new SearchResultAdapter(this, searchResults);
        binding.resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.resultsRecyclerView.setAdapter(adapter);

        // Thiết lập click listener cho adapter
        adapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onViewDetailClick(OriginalStory story) {
                openStoryDetail(story);
            }

            @Override
            public void onDownloadClick(OriginalStory story) {
                downloadStory(story);
            }
        });
    }

    private void setupListeners() {
        // Thiết lập search button
        binding.searchButton.setOnClickListener(v -> performSearch());

        // Thiết lập IME action cho search EditText
        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void handleSearchIntent(Intent intent) {
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra("query");
            if (!TextUtils.isEmpty(query)) {
                binding.searchEditText.setText(query);
                performSearch();
            }
        }
    }

    private void performSearch() {
        String keyword = binding.searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu keyword hiện tại
        currentKeyword = keyword;

        // Reset trạng thái
        searchResults.clear();
        adapter.notifyDataSetChanged();
        binding.searchStatusText.setText("Đang phân tích từ khóa...");
        binding.searchStatusText.setVisibility(View.VISIBLE);
        binding.detectedLanguageText.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        isSearching = true;

        // Phát hiện ngôn ngữ
        detectedLanguage = translationService.detectLanguage(keyword);
        processKeywordAndSearch(keyword, detectedLanguage);
    }

    private void processKeywordAndSearch(String keyword, String detectedLanguage) {
        // Nếu từ khóa là tiếng Trung, tìm kiếm trực tiếp
        if (detectedLanguage.equals(TranslationService.LANGUAGE_ZH)) {
            binding.detectedLanguageText.setText("Đã nhận diện: Tiếng Trung");
            binding.detectedLanguageText.setVisibility(View.VISIBLE);
            binding.searchStatusText.setText("Đang tìm kiếm truyện...");

            // Tìm truyện với từ khóa tiếng Trung
            searchNovelsByKeyword(keyword);
        }
        // Nếu từ khóa là tiếng Việt, cần dịch sang tiếng Trung
        else if (detectedLanguage.equals(TranslationService.LANGUAGE_VI)) {
            binding.searchStatusText.setText("Đang dịch từ khóa sang tiếng Trung...");

            // Kiểm tra DeepL API key trước khi dịch
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String deeplApiKey = prefs.getString("deepl_api_key", "");

            if (deeplApiKey.isEmpty()) {
                // Nếu không có API key, hiển thị thông báo nhưng vẫn tiếp tục tìm kiếm với từ khóa tiếng Việt
                Toast.makeText(this, "DeepL API chưa được cấu hình. Kết quả tìm kiếm có thể không chính xác.", Toast.LENGTH_LONG).show();
                searchWithKeyword(keyword, getSelectedSource());
                return;
            }

            // Tiếp tục với quy trình dịch và tìm kiếm bình thường
            translationService.initialize(this, null, deeplApiKey);

            // Dịch từ khóa sang tiếng Trung
            translationService.translateKeyword(keyword, TranslationService.LANGUAGE_VI, TranslationService.LANGUAGE_ZH,
                    new TranslationService.OnKeywordTranslationListener() {
                        @Override
                        public void onKeywordTranslated(SearchKeywordMap keywordMap) {
                            // Lưu từ khóa tìm kiếm
                            novelManager.saveSearchKeyword(keywordMap);

                            // Thực hiện tìm kiếm với từ khóa đã dịch
                            String chineseKeyword = keywordMap.getChineseKeyword();
                            searchWithKeyword(chineseKeyword, getSelectedSource());
                        }

                        @Override
                        public void onError(Exception e) {
                            // Xử lý lỗi: tìm kiếm với từ khóa tiếng Việt
                            Log.e(TAG, "Lỗi dịch từ khóa: " + e.getMessage());
                            searchWithKeyword(keyword, getSelectedSource());
                        }
                    });
        }
        // Ngôn ngữ khác (ví dụ tiếng Anh), cũng dịch sang tiếng Trung
        else {
            binding.searchStatusText.setText("Đang dịch từ khóa sang tiếng Trung...");

            translationService.translateKeyword(keyword, detectedLanguage,
                    TranslationService.LANGUAGE_ZH, new TranslationService.OnKeywordTranslationListener() {
                @Override
                public void onKeywordTranslated(SearchKeywordMap keywordMap) {
                    runOnUiThread(() -> {
                        translatedKeyword = keywordMap.getChineseKeyword();
                        binding.detectedLanguageText.setText("Đã nhận diện: " + getLanguageName(detectedLanguage) +
                                " → đã dịch sang Tiếng Trung: " + translatedKeyword);
                        binding.detectedLanguageText.setVisibility(View.VISIBLE);
                        binding.searchStatusText.setText("Đang tìm kiếm truyện...");

                        // Tìm truyện với từ khóa đã dịch
                        searchNovelsByKeyword(translatedKeyword);
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.searchStatusText.setText("Lỗi dịch từ khóa: " + e.getMessage());
                        isSearching = false;
                    });
                }
            });
        }
    }

    private void searchNovelsByKeyword(String keyword) {
        // Xác định nguồn tìm kiếm
        String source = getSelectedSource();

        // Lấy crawler tương ứng với nguồn đã chọn
        CrawlerFactory crawlerFactory = CrawlerFactory.getInstance(this);

        if (crawlerFactory.isSourceSupported(source)) {
            NovelCrawler crawler = crawlerFactory.getCrawler(source);

            // Gửi yêu cầu tìm kiếm đến crawler
            crawler.searchNovel(keyword, new NovelCrawler.OnSearchResultListener() {
                @Override
                public void onSearchCompleted(List<OriginalStory> results) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);

                        if (results.isEmpty()) {
                            binding.searchStatusText.setText("Không tìm thấy kết quả nào cho \"" + keyword + "\"");
                        } else {
                            binding.searchStatusText.setText("Tìm thấy " + results.size() + " kết quả cho \"" + keyword + "\"");
                            searchResults.clear();
                            searchResults.addAll(results);
                            adapter.notifyDataSetChanged();
                        }

                        isSearching = false;
                    });
                }

                @Override
                public void onSearchError(Exception e) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.searchStatusText.setText("Lỗi tìm kiếm: " + e.getMessage());
                        isSearching = false;
                    });
                }
            });
        } else {
            // Nếu nguồn chưa được hỗ trợ, sử dụng dữ liệu mẫu
            generateDemoResults(keyword, source);
        }
    }

    // Demo function, thay thế bằng API thực tế
    private void generateDemoResults(String keyword, String source) {
        // Tạo dữ liệu mẫu để kiểm thử
        List<OriginalStory> demoResults = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            OriginalStory story = new OriginalStory();
            story.setId("story_" + i);
            story.setTitle(keyword + " " + i + " - 小说");
            story.setTitleVi("Truyện " + keyword + " " + i);
            story.setAuthor("作者 " + i);
            story.setDescription("这是一个关于" + keyword + "的精彩故事...");
            story.setDescriptionVi("Đây là một câu chuyện tuyệt vời về " + keyword + "...");
            story.setSource(source);
            story.setImageUrl("https://picsum.photos/200/300?random=" + i);

            ArrayList<String> genres = new ArrayList<>();
            genres.add("玄幻");
            genres.add("武侠");
            story.setGenres(genres);

            story.setChapterCount(100 + i * 10);
            story.setCompleted(i % 2 == 0);

            demoResults.add(story);
        }

        // Cập nhật UI với kết quả
        runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);

            if (demoResults.isEmpty()) {
                binding.searchStatusText.setText("Không tìm thấy kết quả nào cho \"" + keyword + "\"");
            } else {
                binding.searchStatusText.setText("Tìm thấy " + demoResults.size() + " kết quả cho \"" + keyword + "\"");
                searchResults.clear();
                searchResults.addAll(demoResults);
                adapter.notifyDataSetChanged();
            }

            isSearching = false;
        });
    }

    private String getSelectedSource() {
        int selectedId = binding.sourceRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        return radioButton.getText().toString();
    }

    private void openStoryDetail(OriginalStory story) {
        Intent intent = new Intent(this, StoryDetailActivity.class);
        intent.putExtra("story_id", story.getId());
        startActivity(intent);
    }

    private void downloadStory(OriginalStory story) {
        Toast.makeText(this, "Bắt đầu tải " + story.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Implement download functionality
    }

    private String getLanguageName(String langCode) {
        switch (langCode) {
            case TranslationService.LANGUAGE_ZH:
                return "Tiếng Trung";
            case TranslationService.LANGUAGE_VI:
                return "Tiếng Việt";
            case TranslationService.LANGUAGE_EN:
                return "Tiếng Anh";
            default:
                return langCode;
        }
    }

    /**
     * Tìm kiếm truyện với từ khóa đã chỉ định và nguồn
     */
    private void searchWithKeyword(String keyword, String source) {
        runOnUiThread(() -> {
            binding.detectedLanguageText.setText("Tìm kiếm với từ khóa: " + keyword);
            binding.detectedLanguageText.setVisibility(View.VISIBLE);
            binding.searchStatusText.setText("Đang tìm kiếm truyện...");
            searchNovelsByKeyword(keyword);
        });
    }

    /**
     * Tìm kiếm truyện với từ khóa đã chỉ định
     */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
