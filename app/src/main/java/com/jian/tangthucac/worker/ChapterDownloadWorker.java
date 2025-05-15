package com.jian.tangthucac.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.CrawlerFactory;
import com.jian.tangthucac.API.NovelCrawler;
import com.jian.tangthucac.API.TranslationService;
import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.TranslatedChapter;

/**
 * Worker để tải và dịch chương truyện trong background
 */
public class ChapterDownloadWorker extends Worker {
    private static final String TAG = "ChapterDownloadWorker";

    // Keys cho input data
    public static final String KEY_STORY_ID = "story_id";
    public static final String KEY_CHAPTER_ID = "chapter_id";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_SHOULD_TRANSLATE = "should_translate";

    // Keys cho output data
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR_MESSAGE = "error_message";

    private final ChineseNovelManager novelManager;
    private final TranslationService translationService;
    private final CrawlerFactory crawlerFactory;

    public ChapterDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(context.getApplicationContext());

        translationService = TranslationService.getInstance();
        // Lấy API keys từ SharedPreferences trong initialize của TranslationService
        translationService.initialize(context.getApplicationContext(), null, null);

        crawlerFactory = CrawlerFactory.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Lấy dữ liệu đầu vào
        String storyId = getInputData().getString(KEY_STORY_ID);
        String chapterId = getInputData().getString(KEY_CHAPTER_ID);
        String source = getInputData().getString(KEY_SOURCE);
        boolean shouldTranslate = getInputData().getBoolean(KEY_SHOULD_TRANSLATE, true);

        if (storyId == null || chapterId == null || source == null) {
            Log.e(TAG, "Missing required parameters");
            return createFailureResult("Missing required parameters");
        }

        try {
            // Lấy crawler tương ứng với nguồn
            NovelCrawler crawler = crawlerFactory.getCrawler(source);
            if (crawler == null) {
                Log.e(TAG, "Unsupported source: " + source);
                return createFailureResult("Unsupported source: " + source);
            }

            // Tải nội dung chương
            final Result[] result = {null};
            final boolean[] completed = {false};

            crawler.getChapterContent(chapterId, new NovelCrawler.OnChapterContentListener() {
                @Override
                public void onChapterLoaded(TranslatedChapter chapter) {
                    // Lưu chương vào database
                    novelManager.saveTranslatedChapter(chapter, new ChineseNovelManager.OnChapterLoadedListener() {
                        @Override
                        public void onChapterLoaded(TranslatedChapter savedChapter) {
                            if (shouldTranslate && savedChapter.getContentVi() == null) {
                                // Dịch nội dung chương
                                translationService.translateChapter(
                                        savedChapter,
                                        TranslationService.LANGUAGE_ZH,
                                        TranslationService.LANGUAGE_VI,
                                        new TranslationService.OnContentTranslationListener() {
                                            @Override
                                            public void onContentTranslated(TranslatedChapter translatedChapter) {
                                                // Cập nhật chương đã dịch vào database
                                                novelManager.saveTranslatedChapter(translatedChapter, new ChineseNovelManager.OnChapterLoadedListener() {
                                                    @Override
                                                    public void onChapterLoaded(TranslatedChapter finalChapter) {
                                                        // Cập nhật thông tin chương trong story
                                                        updateStoryWithChapter(storyId, finalChapter);
                                                        result[0] = Result.success();
                                                        completed[0] = true;
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Log.e(TAG, "Error saving translated chapter", e);
                                                        result[0] = createFailureResult("Error saving translated chapter: " + e.getMessage());
                                                        completed[0] = true;
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.e(TAG, "Error translating chapter", e);
                                                // Vẫn lưu chương không dịch
                                                updateStoryWithChapter(storyId, savedChapter);
                                                result[0] = Result.success(); // Coi như thành công dù không dịch được
                                                completed[0] = true;
                                            }
                                        });
                            } else {
                                // Nếu không cần dịch hoặc đã có bản dịch
                                updateStoryWithChapter(storyId, savedChapter);
                                result[0] = Result.success();
                                completed[0] = true;
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error saving chapter", e);
                            result[0] = createFailureResult("Error saving chapter: " + e.getMessage());
                            completed[0] = true;
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error loading chapter content", e);
                    result[0] = createFailureResult("Error loading chapter content: " + e.getMessage());
                    completed[0] = true;
                }
            });

            // Đợi cho đến khi hoàn thành
            int timeout = 0;
            while (!completed[0] && timeout < 120) { // Tối đa 2 phút
                try {
                    Thread.sleep(1000);
                    timeout++;
                } catch (InterruptedException e) {
                    Log.e(TAG, "Work interrupted", e);
                    return Result.failure();
                }
            }

            if (result[0] != null) {
                return result[0];
            } else {
                return createFailureResult("Operation timed out");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in ChapterDownloadWorker", e);
            return createFailureResult("Error: " + e.getMessage());
        }
    }

    private void updateStoryWithChapter(String storyId, TranslatedChapter chapter) {
        novelManager.getOriginalStoryById(storyId, new ChineseNovelManager.OnStoryLoadedListener() {
            @Override
            public void onStoryLoaded(OriginalStory story) {
                if (story.getTranslatedChapters() == null) {
                    story.setTranslatedChapters(new java.util.HashMap<>());
                }
                story.getTranslatedChapters().put(chapter.getId(), chapter);

                // Cập nhật story
                novelManager.saveOriginalStory(story, new ChineseNovelManager.OnStoryLoadedListener() {
                    @Override
                    public void onStoryLoaded(OriginalStory updatedStory) {
                        Log.d(TAG, "Story updated with chapter: " + chapter.getId());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error updating story with chapter", e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading story for update", e);
            }
        });
    }

    private Result createFailureResult(String errorMessage) {
        Data outputData = new Data.Builder()
                .putBoolean(KEY_SUCCESS, false)
                .putString(KEY_ERROR_MESSAGE, errorMessage)
                .build();
        return Result.failure(outputData);
    }
}
