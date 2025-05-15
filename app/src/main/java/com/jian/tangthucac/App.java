package com.jian.tangthucac;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.API.ContentNormalizer;
import com.jian.tangthucac.API.TranslationService;

/**
 * Lớp Application khởi tạo các dịch vụ và cấu hình toàn cục
 */
public class App extends Application implements Configuration.Provider {
    private static final String TAG = "TangThuCacApp";

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        // Khởi tạo Work Manager
        try {
            WorkManager.initialize(
                this,
                getWorkManagerConfiguration()
            );
            Log.d(TAG, "WorkManager đã được khởi tạo thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo WorkManager: " + e.getMessage());
        }

        // Khởi tạo các managers và services
        initializeServices();
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build();
    }

    /**
     * Khởi tạo các managers và services
     */
    private void initializeServices() {
        // Khởi tạo ChineseNovelManager
        ChineseNovelManager novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(this);

        // Khởi tạo TranslationService với các API keys mặc định
        // API keys thực sẽ được cập nhật khi người dùng đăng nhập
        TranslationService translationService = TranslationService.getInstance();
        translationService.initialize(this, null, null);

        Log.d(TAG, "Các services đã được khởi tạo");
    }

    /**
     * Phương thức để lấy Application Context từ bất kỳ đâu
     */
    public static Context getAppContext() {
        return appContext;
    }

    /**
     * Giải phóng tài nguyên khi ứng dụng thoát
     */
    @Override
    public void onTerminate() {
        // Lưu cache và đóng các managers
        ChineseNovelManager.getInstance().shutdown();

        // Xóa cache để giải phóng bộ nhớ
        ContentNormalizer.clearCache();

        super.onTerminate();
    }

    /**
     * Xử lý khi bộ nhớ thấp
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();

        // Giải phóng tài nguyên không cần thiết
        ContentNormalizer.manageMemory(true);

        Log.d(TAG, "Đã giải phóng bộ nhớ do onLowMemory");
    }

    /**
     * Xử lý khi bộ nhớ sắp hết
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level >= TRIM_MEMORY_MODERATE) {
            // Giải phóng bộ nhớ khi cần thiết
            ContentNormalizer.manageMemory(false);
            Log.d(TAG, "Đã giải phóng một phần bộ nhớ - level: " + level);
        }
    }
}
