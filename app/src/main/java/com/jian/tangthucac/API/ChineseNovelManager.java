package com.jian.tangthucac.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.SearchKeywordMap;
import com.jian.tangthucac.model.TranslatedChapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lớp quản lý dữ liệu truyện Trung Quốc và cache
 */
public class ChineseNovelManager {
    private static final String TAG = "ChineseNovelManager";
    private static ChineseNovelManager instance;

    // Firebase references
    private final DatabaseReference originalStoriesRef;
    private final DatabaseReference translatedChaptersRef;
    private final DatabaseReference keywordMapsRef;

    // Cache for stories and translations
    private final Map<String, OriginalStory> storiesCache;
    private final Map<String, TranslatedChapter> chaptersCache;
    private final Map<String, SearchKeywordMap> keywordCache;

    // LRU cache cho tối ưu hiệu suất
    private final Map<String, Long> cacheAccessTime;

    // Gson for serialization/deserialization
    private final Gson gson;

    // Executor for background tasks
    private final Executor executor;
    private final ScheduledExecutorService scheduledExecutor;

    // Context for sharedPreferences
    private Context context;
    private SharedPreferences preferences;

    // Cache constants
    private static final String PREF_NAME = "chinese_novel_cache";
    private static final String STORIES_CACHE_KEY = "stories_cache";
    private static final String CHAPTERS_CACHE_KEY = "chapters_cache";
    private static final String KEYWORDS_CACHE_KEY = "keywords_cache";

    // Kích thước tối đa cho cache
    private static final int MAX_STORY_CACHE_SIZE = 100;
    private static final int MAX_CHAPTER_CACHE_SIZE = 200;
    private static final int MAX_KEYWORD_CACHE_SIZE = 300;

    // Thời gian tự động lưu cache (mili giây)
    private static final long CACHE_AUTO_SAVE_INTERVAL = 30 * 60 * 1000; // 30 phút

    // Interface for callbacks
    public interface OnStoryLoadedListener {
        void onStoryLoaded(OriginalStory story);
        void onError(Exception e);
    }

    public interface OnStoriesLoadedListener {
        void onStoriesLoaded(List<OriginalStory> stories);
        void onError(Exception e);
    }

    public interface OnChapterLoadedListener {
        void onChapterLoaded(TranslatedChapter chapter);
        void onError(Exception e);
    }

    public interface OnKeywordMappedListener {
        void onKeywordMapped(SearchKeywordMap keywordMap);
        void onError(Exception e);
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // Thêm interface cho việc xử lý empty state
    public interface EmptyStateListener {
        void onEmptyState(boolean isEmpty);
    }

    private ChineseNovelManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        originalStoriesRef = database.getReference("chinese_novels");
        translatedChaptersRef = database.getReference("translated_chapters");
        keywordMapsRef = database.getReference("keyword_maps");

        storiesCache = new HashMap<>();
        chaptersCache = new HashMap<>();
        keywordCache = new HashMap<>();
        cacheAccessTime = new HashMap<>();

        gson = new Gson();
        executor = Executors.newCachedThreadPool();
        scheduledExecutor = Executors.newScheduledThreadPool(1);
    }

    public static synchronized ChineseNovelManager getInstance() {
        if (instance == null) {
            instance = new ChineseNovelManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadCacheFromPreferences();

        // Lên lịch tự động lưu cache định kỳ
        scheduledExecutor.scheduleAtFixedRate(
            this::saveCacheToPreferences,
            CACHE_AUTO_SAVE_INTERVAL,
            CACHE_AUTO_SAVE_INTERVAL,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Tải cache từ SharedPreferences
     */
    private void loadCacheFromPreferences() {
        executor.execute(() -> {
            try {
                // Load stories cache
                String storiesCacheJson = preferences.getString(STORIES_CACHE_KEY, null);
                if (storiesCacheJson != null) {
                    Type storiesMapType = new TypeToken<Map<String, OriginalStory>>(){}.getType();
                    Map<String, OriginalStory> loadedStoriesCache = gson.fromJson(storiesCacheJson, storiesMapType);
                    if (loadedStoriesCache != null) {
                        storiesCache.putAll(loadedStoriesCache);
                        Log.d(TAG, "Loaded " + storiesCache.size() + " stories from cache");
                    }
                }

                // Load chapters cache
                String chaptersCacheJson = preferences.getString(CHAPTERS_CACHE_KEY, null);
                if (chaptersCacheJson != null) {
                    Type chaptersMapType = new TypeToken<Map<String, TranslatedChapter>>(){}.getType();
                    Map<String, TranslatedChapter> loadedChaptersCache = gson.fromJson(chaptersCacheJson, chaptersMapType);
                    if (loadedChaptersCache != null) {
                        chaptersCache.putAll(loadedChaptersCache);
                        Log.d(TAG, "Loaded " + chaptersCache.size() + " chapters from cache");
                    }
                }

                // Load keywords cache
                String keywordsCacheJson = preferences.getString(KEYWORDS_CACHE_KEY, null);
                if (keywordsCacheJson != null) {
                    Type keywordsMapType = new TypeToken<Map<String, SearchKeywordMap>>(){}.getType();
                    Map<String, SearchKeywordMap> loadedKeywordsCache = gson.fromJson(keywordsCacheJson, keywordsMapType);
                    if (loadedKeywordsCache != null) {
                        keywordCache.putAll(loadedKeywordsCache);
                        Log.d(TAG, "Loaded " + keywordCache.size() + " keywords from cache");
                    }
                }

                // Khởi tạo thời gian truy cập cache
                long currentTime = System.currentTimeMillis();
                for (String key : storiesCache.keySet()) {
                    cacheAccessTime.put("story_" + key, currentTime);
                }
                for (String key : chaptersCache.keySet()) {
                    cacheAccessTime.put("chapter_" + key, currentTime);
                }
                for (String key : keywordCache.keySet()) {
                    cacheAccessTime.put("keyword_" + key, currentTime);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading cache: " + e.getMessage());
            }
        });
    }

    /**
     * Lưu cache vào SharedPreferences
     */
    private void saveCacheToPreferences() {
        if (preferences == null) return;

        executor.execute(() -> {
            try {
                SharedPreferences.Editor editor = preferences.edit();

                // Giới hạn kích thước cache trước khi lưu
                trimCacheIfNeeded();

                // Save stories cache
                String storiesCacheJson = gson.toJson(storiesCache);
                editor.putString(STORIES_CACHE_KEY, storiesCacheJson);

                // Save chapters cache
                String chaptersCacheJson = gson.toJson(chaptersCache);
                editor.putString(CHAPTERS_CACHE_KEY, chaptersCacheJson);

                // Save keywords cache
                String keywordsCacheJson = gson.toJson(keywordCache);
                editor.putString(KEYWORDS_CACHE_KEY, keywordsCacheJson);

                editor.apply();
                Log.d(TAG, "Cache saved to preferences");
            } catch (Exception e) {
                Log.e(TAG, "Error saving cache: " + e.getMessage());
            }
        });
    }

    /**
     * Giới hạn kích thước cache khi quá lớn
     */
    private void trimCacheIfNeeded() {
        // Giới hạn cache truyện
        if (storiesCache.size() > MAX_STORY_CACHE_SIZE) {
            List<Map.Entry<String, Long>> entries = new ArrayList<>();
            for (String key : storiesCache.keySet()) {
                entries.add(new HashMap.SimpleEntry<>(key, cacheAccessTime.getOrDefault("story_" + key, 0L)));
            }

            // Sắp xếp theo thời gian truy cập (LRU)
            Collections.sort(entries, Comparator.comparingLong(Map.Entry::getValue));

            // Xóa các mục cũ nhất
            int toRemove = storiesCache.size() - MAX_STORY_CACHE_SIZE;
            for (int i = 0; i < toRemove; i++) {
                String key = entries.get(i).getKey();
                storiesCache.remove(key);
                cacheAccessTime.remove("story_" + key);
            }

            Log.d(TAG, "Trimmed story cache from " + (MAX_STORY_CACHE_SIZE + toRemove) + " to " + MAX_STORY_CACHE_SIZE);
        }

        // Giới hạn cache chương
        if (chaptersCache.size() > MAX_CHAPTER_CACHE_SIZE) {
            List<Map.Entry<String, Long>> entries = new ArrayList<>();
            for (String key : chaptersCache.keySet()) {
                entries.add(new HashMap.SimpleEntry<>(key, cacheAccessTime.getOrDefault("chapter_" + key, 0L)));
            }

            Collections.sort(entries, Comparator.comparingLong(Map.Entry::getValue));

            int toRemove = chaptersCache.size() - MAX_CHAPTER_CACHE_SIZE;
            for (int i = 0; i < toRemove; i++) {
                String key = entries.get(i).getKey();
                chaptersCache.remove(key);
                cacheAccessTime.remove("chapter_" + key);
            }

            Log.d(TAG, "Trimmed chapter cache from " + (MAX_CHAPTER_CACHE_SIZE + toRemove) + " to " + MAX_CHAPTER_CACHE_SIZE);
        }

        // Giới hạn cache từ khóa
        if (keywordCache.size() > MAX_KEYWORD_CACHE_SIZE) {
            List<Map.Entry<String, Long>> entries = new ArrayList<>();
            for (String key : keywordCache.keySet()) {
                entries.add(new HashMap.SimpleEntry<>(key, cacheAccessTime.getOrDefault("keyword_" + key, 0L)));
            }

            Collections.sort(entries, Comparator.comparingLong(Map.Entry::getValue));

            int toRemove = keywordCache.size() - MAX_KEYWORD_CACHE_SIZE;
            for (int i = 0; i < toRemove; i++) {
                String key = entries.get(i).getKey();
                keywordCache.remove(key);
                cacheAccessTime.remove("keyword_" + key);
            }

            Log.d(TAG, "Trimmed keyword cache from " + (MAX_KEYWORD_CACHE_SIZE + toRemove) + " to " + MAX_KEYWORD_CACHE_SIZE);
        }
    }

    /**
     * Lấy danh sách truyện từ Firebase với phân trang
     * @param limit Số lượng truyện tối đa cần lấy
     * @param lastStoryId ID truyện cuối cùng của trang trước (null nếu là trang đầu tiên)
     */
    public void getOriginalStories(int limit, String lastStoryId, OnStoriesLoadedListener listener) {
        Query query;
        if (lastStoryId == null) {
            // Trang đầu tiên
            query = originalStoriesRef.orderByKey().limitToFirst(limit);
        } else {
            // Trang tiếp theo, bắt đầu sau lastStoryId
            query = originalStoriesRef.orderByKey().startAfter(lastStoryId).limitToFirst(limit);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OriginalStory> stories = new ArrayList<>();
                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    OriginalStory story = storySnapshot.getValue(OriginalStory.class);
                    if (story != null) {
                        story.setId(storySnapshot.getKey());
                        stories.add(story);
                        // Cập nhật cache
                        storiesCache.put(story.getId(), story);
                        cacheAccessTime.put("story_" + story.getId(), System.currentTimeMillis());
                    }
                }

                // Lên lịch lưu cache
                if (!stories.isEmpty()) {
                    scheduledExecutor.schedule(
                        ChineseNovelManager.this::saveCacheToPreferences,
                        5,
                        TimeUnit.SECONDS
                    );
                }

                listener.onStoriesLoaded(stories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Phương thức wrapper cho khả năng tương thích
     */
    public void getOriginalStories(OnStoriesLoadedListener listener) {
        getOriginalStories(50, null, listener);
    }

    /**
     * Lấy thông tin truyện theo ID
     */
    public void getOriginalStoryById(String storyId, OnStoryLoadedListener listener) {
        // Kiểm tra cache trước
        if (storiesCache.containsKey(storyId)) {
            OriginalStory cachedStory = storiesCache.get(storyId);
            cacheAccessTime.put("story_" + storyId, System.currentTimeMillis());
            listener.onStoryLoaded(cachedStory);
            return;
        }

        // Nếu không có trong cache, lấy từ Firebase
        originalStoriesRef.child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OriginalStory story = snapshot.getValue(OriginalStory.class);
                if (story != null) {
                    story.setId(snapshot.getKey());
                    // Cập nhật cache
                    storiesCache.put(story.getId(), story);
                    cacheAccessTime.put("story_" + storyId, System.currentTimeMillis());

                    // Lên lịch lưu cache
                    scheduledExecutor.schedule(
                        ChineseNovelManager.this::saveCacheToPreferences,
                        5,
                        TimeUnit.SECONDS
                    );

                    listener.onStoryLoaded(story);
                } else {
                    listener.onError(new Exception("Story not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Lưu truyện mới vào Firebase
     */
    public void saveOriginalStory(OriginalStory story, OnStoryLoadedListener listener) {
        if (story.getId() == null || story.getId().isEmpty()) {
            // Tạo ID mới nếu chưa có
            String newId = originalStoriesRef.push().getKey();
            story.setId(newId);
        }

        originalStoriesRef.child(story.getId()).setValue(story)
            .addOnSuccessListener(aVoid -> {
                // Cập nhật cache
                storiesCache.put(story.getId(), story);
                cacheAccessTime.put("story_" + story.getId(), System.currentTimeMillis());

                // Lên lịch lưu cache
                scheduledExecutor.schedule(
                    this::saveCacheToPreferences,
                    5,
                    TimeUnit.SECONDS
                );

                listener.onStoryLoaded(story);
            })
            .addOnFailureListener(e -> listener.onError(e));
    }

    /**
     * Lấy chương đã dịch theo ID
     */
    public void getTranslatedChapter(String chapterId, OnChapterLoadedListener listener) {
        // Kiểm tra cache trước
        if (chaptersCache.containsKey(chapterId)) {
            TranslatedChapter cachedChapter = chaptersCache.get(chapterId);
            cacheAccessTime.put("chapter_" + chapterId, System.currentTimeMillis());
            listener.onChapterLoaded(cachedChapter);
            return;
        }

        // Nếu không có trong cache, lấy từ Firebase
        translatedChaptersRef.child(chapterId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TranslatedChapter chapter = snapshot.getValue(TranslatedChapter.class);
                if (chapter != null) {
                    chapter.setId(snapshot.getKey());
                    // Cập nhật cache
                    chaptersCache.put(chapter.getId(), chapter);
                    cacheAccessTime.put("chapter_" + chapterId, System.currentTimeMillis());

                    // Lên lịch lưu cache
                    scheduledExecutor.schedule(
                        ChineseNovelManager.this::saveCacheToPreferences,
                        5,
                        TimeUnit.SECONDS
                    );

                    listener.onChapterLoaded(chapter);
                } else {
                    listener.onError(new Exception("Chapter not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Lưu chương đã dịch vào Firebase
     */
    public void saveTranslatedChapter(TranslatedChapter chapter, OnChapterLoadedListener listener) {
        if (chapter.getId() == null || chapter.getId().isEmpty()) {
            // Tạo ID mới nếu chưa có
            String newId = translatedChaptersRef.push().getKey();
            chapter.setId(newId);
        }

        translatedChaptersRef.child(chapter.getId()).setValue(chapter)
            .addOnSuccessListener(aVoid -> {
                // Cập nhật cache
                chaptersCache.put(chapter.getId(), chapter);
                cacheAccessTime.put("chapter_" + chapter.getId(), System.currentTimeMillis());

                // Lên lịch lưu cache
                scheduledExecutor.schedule(
                    this::saveCacheToPreferences,
                    5,
                    TimeUnit.SECONDS
                );

                listener.onChapterLoaded(chapter);
            })
            .addOnFailureListener(e -> listener.onError(e));
    }

    /**
     * Tìm ánh xạ từ khóa tiếng Việt sang tiếng Trung
     */
    public void findKeywordMapping(String vietnameseKeyword, OnKeywordMappedListener listener) {
        // Kiểm tra cache trước
        for (SearchKeywordMap keywordMap : keywordCache.values()) {
            if (keywordMap.getVietnameseKeyword().equalsIgnoreCase(vietnameseKeyword)) {
                keywordMap.incrementUseCount();
                listener.onKeywordMapped(keywordMap);
                saveCacheToPreferences();
                return;
            }
        }

        // Nếu không có trong cache, tìm trong Firebase
        keywordMapsRef.orderByChild("vietnameseKeyword").equalTo(vietnameseKeyword)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot keywordSnapshot : snapshot.getChildren()) {
                            SearchKeywordMap keywordMap = keywordSnapshot.getValue(SearchKeywordMap.class);
                            if (keywordMap != null) {
                                keywordMap.setId(keywordSnapshot.getKey());
                                keywordMap.incrementUseCount();

                                // Cập nhật Firebase
                                keywordMapsRef.child(keywordMap.getId()).child("useCount").setValue(keywordMap.getUseCount());
                                keywordMapsRef.child(keywordMap.getId()).child("lastUsedTime").setValue(keywordMap.getLastUsedTime());

                                // Cập nhật cache
                                keywordCache.put(keywordMap.getId(), keywordMap);
                                saveCacheToPreferences();

                                listener.onKeywordMapped(keywordMap);
                                return;
                            }
                        }
                    }

                    // Không tìm thấy, báo lỗi
                    listener.onError(new Exception("Keyword mapping not found"));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onError(error.toException());
                }
            });
    }

    /**
     * Lưu ánh xạ từ khóa mới
     */
    public void saveKeywordMapping(SearchKeywordMap keywordMap, OnKeywordMappedListener listener) {
        if (keywordMap.getId() == null || keywordMap.getId().isEmpty()) {
            // Tạo ID mới nếu chưa có
            String newId = keywordMapsRef.push().getKey();
            keywordMap.setId(newId);
        }

        keywordMapsRef.child(keywordMap.getId()).setValue(keywordMap)
            .addOnSuccessListener(aVoid -> {
                // Cập nhật cache
                keywordCache.put(keywordMap.getId(), keywordMap);
                saveCacheToPreferences();

                listener.onKeywordMapped(keywordMap);
            })
            .addOnFailureListener(e -> listener.onError(e));
    }

    /**
     * Xóa cache
     */
    public void clearCache() {
        storiesCache.clear();
        chaptersCache.clear();
        keywordCache.clear();
        if (preferences != null) {
            preferences.edit().clear().apply();
        }
    }

    /**
     * Lấy thông tin chương theo ID
     */
    public void getChapterById(String storyId, String chapterId, OnChapterLoadedListener listener) {
        // Check cache first
        if (translatedChaptersRef != null) {
            translatedChaptersRef
                .child(storyId)
                .child("chapters")
                .child(chapterId)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        TranslatedChapter chapter = dataSnapshot.getValue(TranslatedChapter.class);
                        if (chapter != null) {
                            // Đảm bảo chapter có ID
                            chapter.setId(dataSnapshot.getKey());
                            listener.onChapterLoaded(chapter);
                        } else {
                            listener.onError(new Exception("Không thể parse dữ liệu chương"));
                        }
                    } else {
                        listener.onError(new Exception("Không tìm thấy chương"));
                    }
                })
                .addOnFailureListener(e -> listener.onError(e));
        } else {
            listener.onError(new Exception("Firebase chưa được khởi tạo"));
        }
    }

    /**
     * Lấy chương liền kề (trước hoặc sau)
     * @param isPrevious true để lấy chương trước, false để lấy chương sau
     */
    public void getAdjacentChapter(String storyId, String currentChapterId, boolean isPrevious, OnChapterLoadedListener listener) {
        // Lấy danh sách chương của truyện
        if (translatedChaptersRef != null) {
            translatedChaptersRef
                .child(storyId)
                .child("chapters")
                .orderByKey()
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        List<String> chapterIds = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            chapterIds.add(snapshot.getKey());
                        }

                        // Tìm vị trí chương hiện tại
                        int currentIndex = chapterIds.indexOf(currentChapterId);
                        if (currentIndex == -1) {
                            listener.onError(new Exception("Không tìm thấy chương hiện tại"));
                            return;
                        }

                        // Tính vị trí chương mới
                        int newIndex = isPrevious ? currentIndex - 1 : currentIndex + 1;
                        if (newIndex < 0 || newIndex >= chapterIds.size()) {
                            // Không có chương trước/sau
                            listener.onChapterLoaded(null);
                            return;
                        }

                        // Lấy thông tin chương mới
                        String newChapterId = chapterIds.get(newIndex);
                        getChapterById(storyId, newChapterId, listener);
                    } else {
                        listener.onError(new Exception("Không tìm thấy danh sách chương"));
                    }
                })
                .addOnFailureListener(e -> listener.onError(e));
        } else {
            listener.onError(new Exception("Firebase chưa được khởi tạo"));
        }
    }

    private void notifyEmptyState(EmptyStateListener listener) {
        if (listener != null) {
            listener.onEmptyState(true);
        }
    }

    /**
     * Lấy danh sách truyện đã dịch của người dùng
     * @param userId ID của người dùng
     * @param listener callback khi tải xong
     */
    public void getUserTranslatedNovels(String userId, OnStoriesLoadedListener listener) {
        if (userId == null || userId.isEmpty()) {
            listener.onError(new IllegalArgumentException("userId không được null hoặc rỗng"));
            return;
        }

        if (FirebaseDatabase.getInstance() == null) {
            FirebaseDatabase.getInstance();
        }

        DatabaseReference userTranslationsRef = FirebaseDatabase.getInstance().getReference("user_translations").child(userId);
        userTranslationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<OriginalStory> userStories = new ArrayList<>();
                List<String> storyIds = new ArrayList<>();

                // Lấy danh sách ID truyện đã dịch
                for (DataSnapshot storySnapshot : dataSnapshot.getChildren()) {
                    String storyId = storySnapshot.getKey();
                    if (storyId != null) {
                        storyIds.add(storyId);
                    }
                }

                if (storyIds.isEmpty()) {
                    // Người dùng chưa có truyện nào đã dịch
                    listener.onStoriesLoaded(userStories);
                    return;
                }

                // Đếm số lượng truyện đã xử lý
                final int[] storiesProcessed = {0};
                final int totalStories = storyIds.size();

                // Lấy thông tin chi tiết cho từng truyện
                for (String storyId : storyIds) {
                    getOriginalStoryById(storyId, new OnStoryLoadedListener() {
                        @Override
                        public void onStoryLoaded(OriginalStory story) {
                            // Cập nhật số lượng chương đã dịch
                            if (dataSnapshot.child(storyId).hasChild("translatedChapters")) {
                                int translatedCount = (int) dataSnapshot.child(storyId).child("translatedChapters").getChildrenCount();
                                story.setTranslatedChaptersCount(translatedCount);
                            }

                            userStories.add(story);
                            checkCompletion();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error loading story " + storyId + ": " + e.getMessage());
                            checkCompletion();
                        }

                        private void checkCompletion() {
                            storiesProcessed[0]++;
                            if (storiesProcessed[0] >= totalStories) {
                                // Đã xử lý tất cả truyện
                                listener.onStoriesLoaded(userStories);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.toException());
            }
        });
    }

    /**
     * Xóa truyện khỏi danh sách đã dịch của người dùng
     * @param userId ID của người dùng
     * @param storyId ID của truyện cần xóa
     * @param callback callback khi xong
     */
    public void removeUserTranslatedNovel(String userId, String storyId, FirebaseCallback<Boolean> callback) {
        if (userId == null || userId.isEmpty() || storyId == null || storyId.isEmpty()) {
            callback.onError(new IllegalArgumentException("userId và storyId không được null hoặc rỗng"));
            return;
        }

        if (FirebaseDatabase.getInstance() == null) {
            FirebaseDatabase.getInstance();
        }

        DatabaseReference userStoryRef = FirebaseDatabase.getInstance().getReference("user_translations")
                .child(userId)
                .child(storyId);

        userStoryRef.removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError(e));
    }

    /**
     * Đóng manager và giải phóng tài nguyên
     */
    public void shutdown() {
        // Lưu cache trước khi đóng
        saveCacheToPreferences();

        // Dừng executor đã lên lịch
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
        }
    }

    /**
     * Nhận kết quả có phân trang khi tìm kiếm truyện
     */
    public void searchChineseNovels(String keyword, int limit, String lastStoryId, OnStoriesLoadedListener listener) {
        // Tạo query với phân trang
        Query query;
        if (lastStoryId == null) {
            // Trang đầu tiên
            query = originalStoriesRef.orderByChild("title").startAt(keyword).endAt(keyword + "\uf8ff").limitToFirst(limit);
        } else {
            // Trang tiếp theo
            query = originalStoriesRef.orderByChild("title").startAt(keyword).endAt(keyword + "\uf8ff")
                    .startAfter(lastStoryId).limitToFirst(limit);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OriginalStory> stories = new ArrayList<>();
                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    OriginalStory story = storySnapshot.getValue(OriginalStory.class);
                    if (story != null) {
                        story.setId(storySnapshot.getKey());
                        stories.add(story);
                        // Cập nhật cache
                        storiesCache.put(story.getId(), story);
                        cacheAccessTime.put("story_" + story.getId(), System.currentTimeMillis());
                    }
                }

                if (!stories.isEmpty()) {
                    scheduledExecutor.schedule(
                        ChineseNovelManager.this::saveCacheToPreferences,
                        5,
                        TimeUnit.SECONDS
                    );
                }

                listener.onStoriesLoaded(stories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }
}
