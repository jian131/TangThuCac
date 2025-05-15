package com.jian.tangthucac.activity;

import android.app.Dialog;
import android.graphics.Typeface;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.TranslationService;
import com.jian.tangthucac.R;
import com.jian.tangthucac.model.TranslatedChapter;

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
}
