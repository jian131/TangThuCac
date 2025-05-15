package com.jian.tangthucac.activity;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.SharedPreferences;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.TranslationService;
import com.jian.tangthucac.R;
import com.jian.tangthucac.model.TranslatedChapter;
import com.jian.tangthucac.utils.ContentNormalizer;
import com.jian.tangthucac.worker.ChapterPreloadWorker;

import java.util.Locale;

/**
 * Activity hiển thị nội dung chương với chức năng đọc song ngữ Trung-Việt
 */
public class ChapterReaderActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final String TAG = "ChapterReaderActivity";

    // UI elements
    private TextView chapterTitle;
    private TextView chapterContent;
    private Button btnPrev, btnNext, btnTTSSettings, btnPlay, btnStop;
    private ToggleButton toggleLanguage;

    // Data
    private String storyId;
    private String chapterId;
    private String title;
    private TranslatedChapter chapter;
    private ChineseNovelManager novelManager;

    // Text-to-Speech
    private TextToSpeech textToSpeech;
    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;
    private float currentPitch = 1.0f;
    private float currentFontSize = 18f;
    private Typeface currentTypeface = Typeface.DEFAULT;

    // Language mode
    private boolean showingVietnamese = true;

    private WebView contentWebView;
    private boolean showChinese = true;
    private boolean showVietnamese = true;
    private int chineseFontSize = 18;
    private int vietFontSize = 18;
    private boolean isDarkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reader);

        // Ánh xạ view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        chapterTitle = findViewById(R.id.chapterTitle);
        chapterContent = findViewById(R.id.chapterContent);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnTTSSettings = findViewById(R.id.btnTTSSettings);
        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        toggleLanguage = findViewById(R.id.toggleLanguage);

        // Khởi tạo TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

        // Khởi tạo manager
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(getApplicationContext());

        // Nhận dữ liệu từ Intent
        storyId = getIntent().getStringExtra("story_id");
        chapterId = getIntent().getStringExtra("chapter_id");

        if (storyId == null || chapterId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin chương", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Thiết lập listeners
        setupListeners();

        // Tải nội dung chương
        loadChapter();
    }

    private void setupListeners() {
        // Thiết lập toggle language
        toggleLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showingVietnamese = isChecked;
            updateContent();
        });

        // Xử lý nút chuyển chương
        btnPrev.setOnClickListener(v -> {
            loadPreviousChapter();
        });

        btnNext.setOnClickListener(v -> {
            loadNextChapter();
        });

        // Xử lý nút Play
        btnPlay.setOnClickListener(v -> {
            if (isPlaying) {
                pauseSpeaking();
                btnPlay.setText("Đọc");
            } else {
                startSpeaking();
                btnPlay.setText("Tạm dừng");
            }
            isPlaying = !isPlaying;
        });

        // Xử lý nút Stop
        btnStop.setOnClickListener(v -> {
            stopSpeaking();
            btnPlay.setText("Đọc");
            isPlaying = false;
        });

        // Xử lý nút TTS Settings
        btnTTSSettings.setOnClickListener(v -> showTTSControlDialog());
    }

    private void loadChapter() {
        novelManager.getChapterById(storyId, chapterId, new ChineseNovelManager.OnChapterLoadedListener() {
            @Override
            public void onChapterLoaded(TranslatedChapter loadedChapter) {
                chapter = loadedChapter;

                if (chapter != null) {
                    // Mặc định hiển thị phiên bản tiếng Việt
                    showingVietnamese = true;
                    toggleLanguage.setChecked(true);

                    // Hiển thị nội dung
                    updateContent();
                } else {
                    Toast.makeText(ChapterReaderActivity.this, "Không thể tải nội dung chương", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChapterReaderActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateContent() {
        if (chapter == null) return;

        // Hiển thị tiêu đề
        String title = showingVietnamese ? chapter.getTitleVi() : chapter.getTitle();
        if (title == null || title.isEmpty()) {
            title = chapter.getTitle(); // Fallback to original title
        }
        chapterTitle.setText(title);

        // Hiển thị nội dung
        String content = showingVietnamese ? chapter.getContentVi() : chapter.getContent();
        if (content == null || content.isEmpty()) {
            content = chapter.getContent(); // Fallback to original content
            Toast.makeText(this, "Chưa có bản dịch", Toast.LENGTH_SHORT).show();
        }
        chapterContent.setText(content);

        // Áp dụng cài đặt hiển thị
        chapterContent.setTextSize(currentFontSize);
        chapterContent.setTypeface(currentTypeface);

        // Lên lịch tải trước các chương tiếp theo
        scheduleChapterPreloading();
    }

    private void loadPreviousChapter() {
        if (chapter == null) return;

        novelManager.getAdjacentChapter(storyId, chapterId, true, new ChineseNovelManager.OnChapterLoadedListener() {
            @Override
            public void onChapterLoaded(TranslatedChapter previousChapter) {
                if (previousChapter != null) {
                    chapter = previousChapter;
                    chapterId = previousChapter.getId();
                    updateContent();
                    stopSpeaking();
                } else {
                    Toast.makeText(ChapterReaderActivity.this, "Đây là chương đầu tiên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChapterReaderActivity.this, "Không tìm thấy chương trước", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNextChapter() {
        if (chapter == null) return;

        novelManager.getAdjacentChapter(storyId, chapterId, false, new ChineseNovelManager.OnChapterLoadedListener() {
            @Override
            public void onChapterLoaded(TranslatedChapter nextChapter) {
                if (nextChapter != null) {
                    chapter = nextChapter;
                    chapterId = nextChapter.getId();
                    updateContent();
                    stopSpeaking();
                } else {
                    Toast.makeText(ChapterReaderActivity.this, "Đây là chương cuối cùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChapterReaderActivity.this, "Không tìm thấy chương tiếp theo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTTSControlDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_tts_control);

        SeekBar speedSeekBar = dialog.findViewById(R.id.seekBarSpeed);
        SeekBar pitchSeekBar = dialog.findViewById(R.id.seekBarPitch);
        SeekBar fontSizeSeekBar = dialog.findViewById(R.id.seekBarFontSize);
        Spinner spinnerFont = dialog.findViewById(R.id.spinnerFont);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        // Thiết lập giá trị hiện tại
        speedSeekBar.setProgress((int)((currentSpeed - 0.5f) * 100));
        pitchSeekBar.setProgress((int)((currentPitch - 0.5f) * 100));
        fontSizeSeekBar.setProgress((int)currentFontSize - 10); // Giả sử cỡ chữ từ 10-40

        // Thiết lập Spinner cho phông chữ
        String[] fontOptions = {"Mặc định", "Serif", "Sans Serif", "Monospace"};
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fontOptions);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(fontAdapter);

        // Đặt lựa chọn phông chữ hiện tại
        if (currentTypeface == Typeface.DEFAULT) spinnerFont.setSelection(0);
        else if (currentTypeface == Typeface.SERIF) spinnerFont.setSelection(1);
        else if (currentTypeface == Typeface.SANS_SERIF) spinnerFont.setSelection(2);
        else if (currentTypeface == Typeface.MONOSPACE) spinnerFont.setSelection(3);

        // Xử lý thay đổi tốc độ
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSpeed = 0.5f + (progress / 100f);
                if (textToSpeech != null) {
                    textToSpeech.setSpeechRate(currentSpeed);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Xử lý thay đổi cao độ
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentPitch = 0.5f + (progress / 100f);
                if (textToSpeech != null) {
                    textToSpeech.setPitch(currentPitch);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Xử lý thay đổi cỡ chữ
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFontSize = progress + 10; // Cỡ chữ từ 10sp đến 40sp
                chapterContent.setTextSize(currentFontSize);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Xử lý thay đổi phông chữ
        spinnerFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentTypeface = Typeface.DEFAULT;
                        break;
                    case 1:
                        currentTypeface = Typeface.SERIF;
                        break;
                    case 2:
                        currentTypeface = Typeface.SANS_SERIF;
                        break;
                    case 3:
                        currentTypeface = Typeface.MONOSPACE;
                        break;
                }
                chapterContent.setTypeface(currentTypeface);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Xử lý nút đóng
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void startSpeaking() {
        if (textToSpeech != null && chapter != null) {
            textToSpeech.setSpeechRate(currentSpeed);
            textToSpeech.setPitch(currentPitch);

            // Đọc theo ngôn ngữ đang hiển thị
            String content = showingVietnamese ? chapter.getContentVi() : chapter.getContent();
            if (content != null && !content.isEmpty()) {
                // Thiết lập ngôn ngữ phù hợp
                if (showingVietnamese) {
                    textToSpeech.setLanguage(new Locale("vi", "VN"));
                } else {
                    textToSpeech.setLanguage(Locale.CHINESE);
                }

                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Toast.makeText(this, "Không có nội dung để đọc", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pauseSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    private void stopSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Ngôn ngữ sẽ được đặt khi bắt đầu đọc dựa vào chế độ hiển thị
            if (showingVietnamese) {
                int result = textToSpeech.setLanguage(new Locale("vi", "VN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Tiếng Việt không được hỗ trợ", Toast.LENGTH_SHORT).show();
                }
            } else {
                int result = textToSpeech.setLanguage(Locale.CHINESE);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Tiếng Trung không được hỗ trợ", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Không thể khởi tạo Text-to-Speech", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Cải thiện hiệu suất hiển thị nội dung chương song ngữ
     */
    private void setupBilingualContent(TranslatedChapter chapter) {
        if (chapter == null) {
            showToast("Lỗi: không thể hiển thị chương");
            return;
        }

        // Tối ưu hiệu suất hiển thị nội dung song ngữ
        if (contentWebView != null) {
            // Sử dụng AsyncTask để tải và xử lý nội dung ở background
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    // Chuẩn hóa nội dung chương
                    String chineseContent = chapter.getContent();
                    String vietnameseContent = chapter.getContentVi();

                    // Kiểm tra và chuẩn hóa nội dung nếu cần
                    if (chineseContent != null && !chineseContent.isEmpty()) {
                        chineseContent = ContentNormalizer.normalizeChapterContent(chineseContent);
                    }

                    if (vietnameseContent != null && !vietnameseContent.isEmpty()) {
                        vietnameseContent = ContentNormalizer.normalizeChapterContent(vietnameseContent);
                    }

                    // Tạo HTML song ngữ
                    return formatBilingualContent(chineseContent, vietnameseContent);
                }

                @Override
                protected void onPostExecute(String htmlContent) {
                    // Tải HTML vào WebView
                    contentWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

                    // Cập nhật số lượt xem
                    updateViewCount(chapter);
                }
            }.execute();
        }
    }

    /**
     * Tạo HTML định dạng cho nội dung song ngữ với hiệu suất cao
     */
    private String formatBilingualContent(String chineseContent, String vietnameseContent) {
        StringBuilder htmlBuilder = new StringBuilder();

        // Sử dụng StringBuilder thay vì ghép chuỗi để tối ưu hiệu suất
        htmlBuilder.append("<!DOCTYPE html><html><head>")
                   .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                   .append("<style>")
                   .append("body { font-family: 'Noto Sans', 'Roboto', sans-serif; font-size: 18px; line-height: 1.8; padding: 16px; background-color: ")
                   .append(isDarkMode ? "#1a1a1a" : "#f8f8f8")
                   .append("; color: ")
                   .append(isDarkMode ? "#f0f0f0" : "#333333")
                   .append("; }")
                   .append(".bilingual-pair { margin-bottom: 20px; border-bottom: 1px solid ")
                   .append(isDarkMode ? "#444" : "#ddd")
                   .append("; padding-bottom: 10px; }")
                   .append(".chinese { font-size: ")
                   .append(chineseFontSize)
                   .append("px; ")
                   .append(showChinese ? "display: block;" : "display: none;")
                   .append(" margin-bottom: 8px; color: ")
                   .append(isDarkMode ? "#ffcc80" : "#d66000")
                   .append("; }")
                   .append(".vietnamese { font-size: ")
                   .append(vietFontSize)
                   .append("px; ")
                   .append(showVietnamese ? "display: block;" : "display: none;")
                   .append(" margin-bottom: 8px; color: ")
                   .append(isDarkMode ? "#a5d6a7" : "#2e7d32")
                   .append("; }")
                   .append("</style></head><body>");

        // Chia từng đoạn văn và hiển thị song song
        if (chineseContent != null && !chineseContent.isEmpty() &&
            vietnameseContent != null && !vietnameseContent.isEmpty()) {

            // Tách nội dung thành các đoạn
            String[] chineseParagraphs = chineseContent.split("(?<=<br/>)|(?<=</p>)");
            String[] vietnameseParagraphs = vietnameseContent.split("(?<=<br/>)|(?<=</p>)");

            // Xác định số đoạn cần hiển thị
            int paragraphCount = Math.min(chineseParagraphs.length, vietnameseParagraphs.length);

            // Hiển thị từng cặp đoạn song ngữ
            for (int i = 0; i < paragraphCount; i++) {
                String chinesePara = chineseParagraphs[i].trim();
                String vietnamesePara = vietnameseParagraphs[i].trim();

                if (!chinesePara.isEmpty() || !vietnamesePara.isEmpty()) {
                    htmlBuilder.append("<div class='bilingual-pair'>")
                               .append("<div class='chinese'>").append(chinesePara).append("</div>")
                               .append("<div class='vietnamese'>").append(vietnamesePara).append("</div>")
                               .append("</div>");
                }
            }

            // Nếu một ngôn ngữ có nhiều đoạn hơn, hiển thị phần còn lại
            if (chineseParagraphs.length > vietnameseParagraphs.length) {
                for (int i = paragraphCount; i < chineseParagraphs.length; i++) {
                    String para = chineseParagraphs[i].trim();
                    if (!para.isEmpty()) {
                        htmlBuilder.append("<div class='bilingual-pair'>")
                                   .append("<div class='chinese'>").append(para).append("</div>")
                                   .append("<div class='vietnamese'><i>Chưa có bản dịch</i></div>")
                                   .append("</div>");
                    }
                }
            } else if (vietnameseParagraphs.length > chineseParagraphs.length) {
                for (int i = paragraphCount; i < vietnameseParagraphs.length; i++) {
                    String para = vietnameseParagraphs[i].trim();
                    if (!para.isEmpty()) {
                        htmlBuilder.append("<div class='bilingual-pair'>")
                                   .append("<div class='chinese'><i>Không có nội dung gốc</i></div>")
                                   .append("<div class='vietnamese'>").append(para).append("</div>")
                                   .append("</div>");
                    }
                }
            }
        } else if (chineseContent != null && !chineseContent.isEmpty()) {
            // Chỉ có nội dung tiếng Trung
            htmlBuilder.append("<div class='chinese'>").append(chineseContent).append("</div>");
        } else if (vietnameseContent != null && !vietnameseContent.isEmpty()) {
            // Chỉ có nội dung tiếng Việt
            htmlBuilder.append("<div class='vietnamese'>").append(vietnameseContent).append("</div>");
        } else {
            // Không có nội dung
            htmlBuilder.append("<p>Không có nội dung khả dụng.</p>");
        }

        // Thêm JavaScript để cải thiện tương tác
        htmlBuilder.append("<script>")
                   .append("function toggleChinese() { ")
                   .append("  var elements = document.getElementsByClassName('chinese'); ")
                   .append("  for (var i = 0; i < elements.length; i++) { ")
                   .append("    elements[i].style.display = elements[i].style.display === 'none' ? 'block' : 'none'; ")
                   .append("  } ")
                   .append("} ")
                   .append("function toggleVietnamese() { ")
                   .append("  var elements = document.getElementsByClassName('vietnamese'); ")
                   .append("  for (var i = 0; i < elements.length; i++) { ")
                   .append("    elements[i].style.display = elements[i].style.display === 'none' ? 'block' : 'none'; ")
                   .append("  } ")
                   .append("} ")
                   .append("</script>")
                   .append("</body></html>");

        return htmlBuilder.toString();
    }

    /**
     * Cập nhật trạng thái chế độ đọc, lưu vào SharedPreferences
     */
    private void updateReadingPreferences() {
        SharedPreferences prefs = getSharedPreferences("reading_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("show_chinese", showChinese);
        editor.putBoolean("show_vietnamese", showVietnamese);
        editor.putInt("chinese_font_size", chineseFontSize);
        editor.putInt("viet_font_size", vietFontSize);
        editor.putBoolean("dark_mode", isDarkMode);

        editor.apply();
    }

    /**
     * Tải trạng thái chế độ đọc từ SharedPreferences
     */
    private void loadReadingPreferences() {
        SharedPreferences prefs = getSharedPreferences("reading_preferences", MODE_PRIVATE);

        showChinese = prefs.getBoolean("show_chinese", true);
        showVietnamese = prefs.getBoolean("show_vietnamese", true);
        chineseFontSize = prefs.getInt("chinese_font_size", 18);
        vietFontSize = prefs.getInt("viet_font_size", 18);
        isDarkMode = prefs.getBoolean("dark_mode", false);
    }

    /**
     * Lên lịch tải trước các chương tiếp theo
     */
    private void scheduleChapterPreloading() {
        if (storyId == null || chapterId == null) return;

        // Tạo ràng buộc - chỉ tải khi có kết nối mạng
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Tạo dữ liệu đầu vào cho worker
        Data inputData = new Data.Builder()
                .putString(ChapterPreloadWorker.KEY_STORY_ID, storyId)
                .putString(ChapterPreloadWorker.KEY_CURRENT_CHAPTER_ID, chapterId)
                .putInt(ChapterPreloadWorker.KEY_NUMBER_OF_CHAPTERS, 3) // Tải trước 3 chương
                .putBoolean(ChapterPreloadWorker.KEY_AUTO_TRANSLATE, true)
                .build();

        // Tạo request
        OneTimeWorkRequest preloadRequest = new OneTimeWorkRequest.Builder(ChapterPreloadWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();

        // Đặt lịch thực hiện
        WorkManager.getInstance(getApplicationContext()).enqueue(preloadRequest);

        Log.d(TAG, "Đã lên lịch tải trước các chương tiếp theo");
    }

    /**
     * Hiển thị thông báo Toast
     * @param message Nội dung thông báo
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cập nhật số lần xem cho chương
     * @param chapter Chương cần cập nhật
     */
    private void updateViewCount(TranslatedChapter chapter) {
        // Implementation của việc cập nhật view count
        // (có thể là trống trong trường hợp này vì không còn cần thiết)
    }
}
