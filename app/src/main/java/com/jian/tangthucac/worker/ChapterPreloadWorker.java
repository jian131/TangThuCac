package com.jian.tangthucac.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.ContentNormalizer;
import com.jian.tangthucac.API.NovelCrawler;
import com.jian.tangthucac.API.NovelFullScraper;
import com.jian.tangthucac.API.TranslationService;
import com.jian.tangthucac.model.TranslatedChapter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Worker để tải trước và lưu cache các chương kế tiếp
 */
public class ChapterPreloadWorker extends Worker {
    private static final String TAG = "ChapterPreloadWorker";

    // Tham số input
    public static final String KEY_STORY_ID = "story_id";
    public static final String KEY_CURRENT_CHAPTER_ID = "current_chapter_id";
    public static final String KEY_NUMBER_OF_CHAPTERS = "number_of_chapters";
    public static final String KEY_AUTO_TRANSLATE = "auto_translate";

    // Tham số output
    public static final String KEY_RESULT_MESSAGE = "result_message";
    public static final String KEY_PRELOADED_CHAPTERS = "preloaded_chapters";

    // Số chương mặc định sẽ tải trước
    private static final int DEFAULT_PRELOAD_CHAPTERS = 3;

    // Các managers và services
    private ChineseNovelManager novelManager;
    private TranslationService translationService;

    public ChapterPreloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(context);
        translationService = TranslationService.getInstance();
        translationService.initialize(context, null, null); // API keys sẽ được tải từ tài khoản người dùng
    }

    @NonNull
    @Override
    public Result doWork() {
        // Lấy tham số
        String storyId = getInputData().getString(KEY_STORY_ID);
        String currentChapterId = getInputData().getString(KEY_CURRENT_CHAPTER_ID);
        int numberOfChapters = getInputData().getInt(KEY_NUMBER_OF_CHAPTERS, DEFAULT_PRELOAD_CHAPTERS);
        boolean autoTranslate = getInputData().getBoolean(KEY_AUTO_TRANSLATE, true);

        // Kiểm tra tham số đầu vào
        if (storyId == null || currentChapterId == null) {
            return createFailureResult("Thiếu thông tin story ID hoặc chapter ID");
        }

        Log.d(TAG, "Bắt đầu tải trước " + numberOfChapters + " chương tiếp theo cho truyện " + storyId);

        // Tạo biến để đếm số chương đã tải
        final int[] preloadedCount = {0};

        // Tạo CountDownLatch để đồng bộ hóa các tác vụ bất đồng bộ
        final CountDownLatch latch = new CountDownLatch(1);

        // Bắt đầu tải các chương tiếp theo
        preloadNextChapters(storyId, currentChapterId, numberOfChapters, autoTranslate, preloadedCount, latch);

        try {
            // Đợi tối đa 10 phút cho việc tải trước kết thúc
            boolean completed = latch.await(10, TimeUnit.MINUTES);

            if (!completed) {
                Log.w(TAG, "Tải trước chương bị timeout");
                return createSuccessResult("Tải trước bị timeout, đã tải " + preloadedCount[0] + " chương", preloadedCount[0]);
            }

            return createSuccessResult("Đã tải trước " + preloadedCount[0] + " chương thành công", preloadedCount[0]);
        } catch (InterruptedException e) {
            Log.e(TAG, "Tải trước chương bị gián đoạn", e);
            return createFailureResult("Tải trước bị gián đoạn: " + e.getMessage());
        }
    }

    /**
     * Tải trước nhiều chương kế tiếp
     */
    private void preloadNextChapters(String storyId, String currentChapterId, int count,
                                    boolean autoTranslate, final int[] preloadedCount,
                                    final CountDownLatch latch) {
        // Nếu đã tải đủ số lượng chương hoặc không còn chương tiếp theo
        if (count <= 0) {
            latch.countDown();
            return;
        }

        // Lấy chương tiếp theo
        novelManager.getAdjacentChapter(storyId, currentChapterId, false, new ChineseNovelManager.OnChapterLoadedListener() {
            @Override
            public void onChapterLoaded(TranslatedChapter nextChapter) {
                if (nextChapter == null) {
                    // Không còn chương tiếp theo
                    latch.countDown();
                    return;
                }

                // Đánh dấu đã tải thêm 1 chương
                preloadedCount[0]++;

                // Chuẩn hóa nội dung chương
                if (nextChapter.getContent() != null) {
                    String normalizedContent = ContentNormalizer.normalizeChapterContent(nextChapter.getContent());
                    nextChapter.setContent(normalizedContent);
                }

                // Kiểm tra nếu cần dịch tự động
                if (autoTranslate && nextChapter.getContentVi() == null && nextChapter.getContent() != null) {
                    // Dịch nội dung
                    translationService.translateChapter(nextChapter,
                        TranslationService.LANGUAGE_ZH,
                        TranslationService.LANGUAGE_VI,
                        new TranslationService.OnContentTranslationListener() {
                            @Override
                            public void onContentTranslated(TranslatedChapter translatedChapter) {
                                // Lưu chương đã dịch vào database
                                novelManager.saveTranslatedChapter(translatedChapter, new ChineseNovelManager.OnChapterLoadedListener() {
                                    @Override
                                    public void onChapterLoaded(TranslatedChapter savedChapter) {
                                        // Tiếp tục tải các chương tiếp theo
                                        preloadNextChapters(storyId, nextChapter.getId(), count - 1, autoTranslate, preloadedCount, latch);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "Lỗi khi lưu chương đã dịch: " + e.getMessage(), e);
                                        // Vẫn tiếp tục tải các chương khác
                                        preloadNextChapters(storyId, nextChapter.getId(), count - 1, autoTranslate, preloadedCount, latch);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Lỗi khi dịch chương: " + e.getMessage(), e);
                                // Vẫn tiếp tục tải các chương khác
                                preloadNextChapters(storyId, nextChapter.getId(), count - 1, autoTranslate, preloadedCount, latch);
                            }
                        });
                } else {
                    // Không cần dịch, tiếp tục tải chương tiếp theo
                    preloadNextChapters(storyId, nextChapter.getId(), count - 1, autoTranslate, preloadedCount, latch);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi khi tải chương kế tiếp: " + e.getMessage(), e);
                // Kết thúc tiến trình
                latch.countDown();
            }
        });
    }

    /**
     * Tạo kết quả thành công
     */
    private Result createSuccessResult(String message, int preloadedCount) {
        Data outputData = new Data.Builder()
                .putString(KEY_RESULT_MESSAGE, message)
                .putInt(KEY_PRELOADED_CHAPTERS, preloadedCount)
                .build();
        return Result.success(outputData);
    }

    /**
     * Tạo kết quả thất bại
     */
    private Result createFailureResult(String message) {
        Data outputData = new Data.Builder()
                .putString(KEY_RESULT_MESSAGE, message)
                .putInt(KEY_PRELOADED_CHAPTERS, 0)
                .build();
        return Result.failure(outputData);
    }
}
