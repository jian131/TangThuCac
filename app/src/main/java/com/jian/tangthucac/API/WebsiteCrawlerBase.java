package com.jian.tangthucac.API;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.TranslatedChapter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Lớp cơ sở cho các web crawler, triển khai các chức năng chung
 */
public abstract class WebsiteCrawlerBase implements NovelCrawler {
    protected static final String TAG = "WebsiteCrawlerBase";

    protected final Context context;
    protected final Executor executor;
    protected final Handler mainHandler;

    public WebsiteCrawlerBase(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Helper method để tạo ID duy nhất cho truyện từ nguồn
     * @param sourceId ID của truyện từ nguồn gốc
     * @return ID được tạo theo định dạng: [tên nguồn]_[sourceId]
     */
    protected String generateNovelId(String sourceId) {
        return getSourceName().toLowerCase() + "_" + sourceId;
    }

    /**
     * Helper method để tạo ID duy nhất cho chương
     * @param novelId ID của truyện
     * @param chapterNumber Số chương
     * @return ID được tạo theo định dạng: [novelId]_[chapterNumber]
     */
    protected String generateChapterId(String novelId, int chapterNumber) {
        return novelId + "_" + chapterNumber;
    }

    /**
     * Helper method để log thông tin
     * @param message Thông điệp cần log
     */
    protected void logInfo(String message) {
        Log.i(TAG, getSourceName() + ": " + message);
    }

    /**
     * Helper method để log lỗi
     * @param message Thông điệp lỗi
     * @param e Exception
     */
    protected void logError(String message, Exception e) {
        Log.e(TAG, getSourceName() + ": " + message, e);
    }

    /**
     * Xử lý các lỗi từ crawler và gọi callback với exception
     * @param listener Listener để nhận thông báo lỗi
     * @param errorMessage Thông điệp lỗi
     * @param e Exception (có thể null)
     */
    protected void handleError(OnSearchResultListener listener, String errorMessage, Exception e) {
        Exception exception = (e != null) ? e : new Exception(errorMessage);
        logError(errorMessage, exception);
        mainHandler.post(() -> listener.onError(exception));
    }

    /**
     * Xử lý các lỗi từ crawler và gọi callback với exception
     * @param listener Listener để nhận thông báo lỗi
     * @param errorMessage Thông điệp lỗi
     * @param e Exception (có thể null)
     */
    protected void handleError(OnNovelDetailListener listener, String errorMessage, Exception e) {
        Exception exception = (e != null) ? e : new Exception(errorMessage);
        logError(errorMessage, exception);
        mainHandler.post(() -> listener.onError(exception));
    }

    /**
     * Xử lý các lỗi từ crawler và gọi callback với exception
     * @param listener Listener để nhận thông báo lỗi
     * @param errorMessage Thông điệp lỗi
     * @param e Exception (có thể null)
     */
    protected void handleError(OnChapterContentListener listener, String errorMessage, Exception e) {
        Exception exception = (e != null) ? e : new Exception(errorMessage);
        logError(errorMessage, exception);
        mainHandler.post(() -> listener.onError(exception));
    }
}
