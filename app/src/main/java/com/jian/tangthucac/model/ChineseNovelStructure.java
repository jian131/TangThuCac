package com.jian.tangthucac.model;

/**
 * Cấu trúc của database Firebase cho truyện tiếng Trung
 */
public class ChineseNovelStructure {
    // Đường dẫn chính
    public static final String USERS = "Users";
    public static final String CHINESE_NOVELS = "chinese_novels";
    public static final String CHINESE_NOVEL_CHAPTERS = "chinese_novel_chapters";
    public static final String CHINESE_GENRES = "chinese_genres";
    public static final String CHINESE_NOVELS_BY_GENRE = "chinese_novels_by_genre";
    public static final String CHINESE_NOVELS_BY_AUTHOR = "chinese_novels_by_author";
    public static final String FEATURED_CHINESE_NOVELS = "featured_chinese_novels";
    public static final String AI_CHAT_HISTORIES = "ai_chat_histories";

    // Thuộc tính người dùng
    public static final String USER_ID = "id";
    public static final String USER_EMAIL = "email";
    public static final String USER_USERNAME = "username";
    public static final String USER_API_KEYS = "apiKeys";
    public static final String USER_SETTINGS = "settings";
    public static final String USER_LIBRARY = "library";

    // API keys
    public static final String API_OPENAI = "openai";
    public static final String API_GEMINI = "gemini";

    // Cài đặt người dùng
    public static final String SETTING_DARK_MODE = "darkMode";
    public static final String SETTING_FONT_SIZE = "fontSize";
    public static final String SETTING_ENABLE_TRANSLATION = "enableTranslation";

    // Thư viện người dùng
    public static final String LIBRARY_NOVELS = "novels";
    public static final String LIBRARY_FAVORITES = "favorites";
    public static final String LIBRARY_LAST_READ = "lastRead";
    public static final String LIBRARY_READING_HISTORY = "reading_history";

    // Thuộc tính truyện Trung Quốc
    public static final String NOVEL_ID = "id";
    public static final String NOVEL_TITLE = "title";
    public static final String NOVEL_TITLE_VI = "titleVi";
    public static final String NOVEL_AUTHOR = "author";
    public static final String NOVEL_DESCRIPTION = "description";
    public static final String NOVEL_DESCRIPTION_VI = "descriptionVi";
    public static final String NOVEL_COVER_IMAGE_URL = "coverImageUrl";
    public static final String NOVEL_GENRES = "genres";
    public static final String NOVEL_TAGS = "tags";
    public static final String NOVEL_SOURCE = "source";
    public static final String NOVEL_SOURCE_URL = "sourceUrl";
    public static final String NOVEL_LANGUAGE = "language";
    public static final String NOVEL_CHAPTER_COUNT = "chapterCount";
    public static final String NOVEL_COMPLETED = "completed";
    public static final String NOVEL_UPDATE_TIME = "updateTime";
    public static final String NOVEL_VIEWS = "views";
    public static final String NOVEL_TRANSLATED_CHAPTERS_COUNT = "translatedChaptersCount";
    public static final String NOVEL_HOT = "hot";

    // Thuộc tính chương
    public static final String CHAPTER_NOVEL_ID = "novelId";
    public static final String CHAPTER_ID = "id";
    public static final String CHAPTER_TITLE = "title";
    public static final String CHAPTER_TITLE_VI = "titleVi";
    public static final String CHAPTER_INDEX = "index";
    public static final String CHAPTER_CONTENT = "content";
    public static final String CHAPTER_TRANSLATED_CONTENT = "translatedContent";
    public static final String CHAPTER_HAS_TRANSLATION = "hasTranslation";
    public static final String CHAPTER_SOURCE = "source";
    public static final String CHAPTER_SOURCE_URL = "sourceUrl";
    public static final String CHAPTER_TRANSLATED_AT = "translatedAt";
    public static final String CHAPTER_UPLOADED_AT = "uploadedAt";

    // Thuộc tính thể loại
    public static final String GENRE_ID = "id";
    public static final String GENRE_NAME = "name";
    public static final String GENRE_DESCRIPTION = "description";
    public static final String GENRE_IMAGE_URL = "imageUrl";
    public static final String GENRE_COUNT = "count";

    // Thuộc tính nổi bật
    public static final String FEATURED_HOT = "hot";
    public static final String FEATURED_NEW = "new";
    public static final String FEATURED_RECOMMENDED = "recommended";

    // Thuộc tính AI chat
    public static final String CHAT_ID = "id";
    public static final String CHAT_TITLE = "title";
    public static final String CHAT_CREATED_AT = "createdAt";
    public static final String CHAT_MESSAGES = "messages";

    // Thuộc tính tin nhắn chat
    public static final String MESSAGE_ID = "id";
    public static final String MESSAGE_SENDER = "sender";
    public static final String MESSAGE_CONTENT = "content";
    public static final String MESSAGE_TIMESTAMP = "timestamp";

    // Phương thức tiện ích - Đường dẫn người dùng
    public static String userPath(String userId) {
        return USERS + "/" + userId;
    }

    // Phương thức tiện ích - Đường dẫn thư viện người dùng
    public static String userLibraryPath(String userId) {
        return userPath(userId) + "/" + USER_LIBRARY;
    }

    // Phương thức tiện ích - Đường dẫn truyện
    public static String novelPath(String novelId) {
        return CHINESE_NOVELS + "/" + novelId;
    }

    // Phương thức tiện ích - Đường dẫn chương
    public static String chapterPath(String chapterId) {
        return CHINESE_NOVEL_CHAPTERS + "/" + chapterId;
    }

    // Phương thức tiện ích - Đường dẫn thể loại
    public static String genrePath(String genreId) {
        return CHINESE_GENRES + "/" + genreId;
    }

    // Phương thức tiện ích - Đường dẫn truyện theo thể loại
    public static String novelsByGenrePath(String genreId) {
        return CHINESE_NOVELS_BY_GENRE + "/" + genreId;
    }

    // Phương thức tiện ích - Đường dẫn truyện theo tác giả
    public static String novelsByAuthorPath(String author) {
        return CHINESE_NOVELS_BY_AUTHOR + "/" + author;
    }

    // Phương thức tiện ích - Đường dẫn lịch sử chat
    public static String chatHistoryPath(String userId) {
        return AI_CHAT_HISTORIES + "/" + userId;
    }

    // Phương thức tiện ích - Đường dẫn cuộc trò chuyện
    public static String chatPath(String userId, String chatId) {
        return chatHistoryPath(userId) + "/" + chatId;
    }
}
