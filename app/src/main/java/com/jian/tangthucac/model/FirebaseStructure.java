package com.jian.tangthucac.model;

/**
 * Class định nghĩa cấu trúc dữ liệu trên Firebase
 * Lớp này không phải là một model nhưng chứa các hằng số và đường dẫn
 * để các lớp khác tham chiếu đến cấu trúc trong Firebase Realtime Database
 */
public class FirebaseStructure {

    // Root nodes
    public static final String USERS = "users";
    public static final String NOVELS = "novels";
    public static final String CHAPTERS = "chapters";
    public static final String GENRES = "genres";
    public static final String SOURCES = "sources";
    public static final String TRANSLATED = "translated";
    public static final String LIBRARIES = "libraries";
    public static final String STATISTICS = "statistics";
    public static final String POPULAR = "popular";

    // Users sub-nodes
    public static final String USER_API_KEYS = "apiKeys";
    public static final String USER_SETTINGS = "settings";
    public static final String USER_LIBRARY = "library";

    // API Keys fields
    public static final String API_DEEPL = "deepl";
    public static final String API_OPENAI = "openai";
    public static final String API_GOOGLE = "google";

    // Settings fields
    public static final String SETTING_DARK_MODE = "darkMode";
    public static final String SETTING_FONT_SIZE = "fontSize";
    public static final String SETTING_TRANSLATE_ENGINE = "translateEngine";

    // Novels sub-nodes
    public static final String NOVEL_DETAILS = "details";
    public static final String NOVEL_CHAPTERS = "chapters";

    // Genre queries
    public static final String GENRE_NOVELS = "genre_novels";

    // User Library nodes
    public static final String LIBRARY_NOVELS = "novels";
    public static final String LIBRARY_RECENTLY_READ = "recentlyRead";
    public static final String LIBRARY_FAVORITES = "favorites";
    public static final String LIBRARY_LAST_READ = "lastRead";

    // Translated nodes
    public static final String TRANSLATED_BY_USER = "byUser";
    public static final String TRANSLATED_BY_NOVEL = "byNovel";

    // Popular nodes
    public static final String POPULAR_DAILY = "daily";
    public static final String POPULAR_WEEKLY = "weekly";
    public static final String POPULAR_MONTHLY = "monthly";
    public static final String POPULAR_ALL_TIME = "allTime";

    /**
     * Tạo đường dẫn đến thông tin chi tiết của người dùng
     * @param userId ID người dùng
     * @return Đường dẫn trong DB
     */
    public static String userPath(String userId) {
        return USERS + "/" + userId;
    }

    /**
     * Tạo đường dẫn đến thông tin API keys của người dùng
     * @param userId ID người dùng
     * @return Đường dẫn trong DB
     */
    public static String userApiKeysPath(String userId) {
        return userPath(userId) + "/" + USER_API_KEYS;
    }

    /**
     * Tạo đường dẫn đến thông tin một API key cụ thể của người dùng
     * @param userId ID người dùng
     * @param apiName Tên API (deepl, openai, ...)
     * @return Đường dẫn trong DB
     */
    public static String userApiKeyPath(String userId, String apiName) {
        return userApiKeysPath(userId) + "/" + apiName;
    }

    /**
     * Tạo đường dẫn đến thông tin chi tiết của truyện
     * @param novelId ID truyện
     * @return Đường dẫn trong DB
     */
    public static String novelDetailsPath(String novelId) {
        return NOVELS + "/" + novelId + "/" + NOVEL_DETAILS;
    }

    /**
     * Tạo đường dẫn đến danh sách chương của truyện
     * @param novelId ID truyện
     * @return Đường dẫn trong DB
     */
    public static String novelChaptersPath(String novelId) {
        return NOVELS + "/" + novelId + "/" + NOVEL_CHAPTERS;
    }

    /**
     * Tạo đường dẫn đến một chương cụ thể của truyện
     * @param novelId ID truyện
     * @param chapterId ID chương
     * @return Đường dẫn trong DB
     */
    public static String chapterPath(String novelId, String chapterId) {
        return CHAPTERS + "/" + novelId + "/" + chapterId;
    }

    /**
     * Tạo đường dẫn đến thư viện của người dùng
     * @param userId ID người dùng
     * @return Đường dẫn trong DB
     */
    public static String userLibraryPath(String userId) {
        return LIBRARIES + "/" + userId;
    }

    /**
     * Tạo đường dẫn đến danh sách truyện trong thư viện của người dùng
     * @param userId ID người dùng
     * @return Đường dẫn trong DB
     */
    public static String userLibraryNovelsPath(String userId) {
        return userLibraryPath(userId) + "/" + LIBRARY_NOVELS;
    }

    /**
     * Tạo đường dẫn đến các chương được dịch của một truyện
     * @param novelId ID truyện
     * @return Đường dẫn trong DB
     */
    public static String translatedChaptersPath(String novelId) {
        return TRANSLATED + "/" + TRANSLATED_BY_NOVEL + "/" + novelId;
    }

    /**
     * Tạo đường dẫn đến các chương được dịch bởi một người dùng
     * @param userId ID người dùng
     * @return Đường dẫn trong DB
     */
    public static String userTranslatedPath(String userId) {
        return TRANSLATED + "/" + TRANSLATED_BY_USER + "/" + userId;
    }

    /**
     * Tạo đường dẫn đến danh sách truyện theo thể loại
     * @param genreId ID thể loại
     * @return Đường dẫn trong DB
     */
    public static String genreNovelsPath(String genreId) {
        return GENRE_NOVELS + "/" + genreId;
    }

    /**
     * Tạo đường dẫn đến danh sách truyện phổ biến nhất trong ngày
     * @return Đường dẫn trong DB
     */
    public static String popularDailyPath() {
        return POPULAR + "/" + POPULAR_DAILY;
    }

    /**
     * Tạo đường dẫn đến danh sách truyện phổ biến nhất trong tuần
     * @return Đường dẫn trong DB
     */
    public static String popularWeeklyPath() {
        return POPULAR + "/" + POPULAR_WEEKLY;
    }

    /**
     * Tạo đường dẫn đến danh sách truyện phổ biến nhất trong tháng
     * @return Đường dẫn trong DB
     */
    public static String popularMonthlyPath() {
        return POPULAR + "/" + POPULAR_MONTHLY;
    }

    /**
     * Tạo đường dẫn đến danh sách truyện phổ biến nhất mọi thời đại
     * @return Đường dẫn trong DB
     */
    public static String popularAllTimePath() {
        return POPULAR + "/" + POPULAR_ALL_TIME;
    }
}
