package com.jian.tangthucac.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jian.tangthucac.model.ChineseChapter;
import com.jian.tangthucac.model.ChineseNovel;
import com.jian.tangthucac.model.ChineseNovelStructure;
import com.jian.tangthucac.model.SearchKeywordMap;

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
    private final DatabaseReference chineseNovelsRef;
    private final DatabaseReference chineseChaptersRef;
    private final DatabaseReference keywordMapsRef;

    // Cache for novels and translations
    private final Map<String, ChineseNovel> novelsCache;
    private final Map<String, ChineseChapter> chaptersCache;
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
    private static final String NOVELS_CACHE_KEY = "novels_cache";
    private static final String CHAPTERS_CACHE_KEY = "chapters_cache";
    private static final String KEYWORDS_CACHE_KEY = "keywords_cache";

    // Kích thước tối đa cho cache
    private static final int MAX_NOVEL_CACHE_SIZE = 100;
    private static final int MAX_CHAPTER_CACHE_SIZE = 200;
    private static final int MAX_KEYWORD_CACHE_SIZE = 300;

    // Thời gian tự động lưu cache (mili giây)
    private static final long CACHE_AUTO_SAVE_INTERVAL = 30 * 60 * 1000; // 30 phút

    // Interface for callbacks
    public interface OnNovelLoadedListener {
        void onNovelLoaded(ChineseNovel novel);
        void onError(Exception e);
    }

    public interface OnNovelsLoadedListener {
        void onNovelsLoaded(List<ChineseNovel> novels);
        void onError(Exception e);
    }

    public interface OnChapterLoadedListener {
        void onChapterLoaded(ChineseChapter chapter);
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
        chineseNovelsRef = database.getReference(ChineseNovelStructure.CHINESE_NOVELS);
        chineseChaptersRef = database.getReference(ChineseNovelStructure.CHINESE_NOVEL_CHAPTERS);
        keywordMapsRef = database.getReference("keyword_maps"); // Giữ lại từ phiên bản cũ

        novelsCache = new HashMap<>();
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
                // Load novels cache
                String novelsCacheJson = preferences.getString(NOVELS_CACHE_KEY, null);
                if (novelsCacheJson != null) {
                    Type novelsMapType = new TypeToken<Map<String, ChineseNovel>>(){}.getType();
                    Map<String, ChineseNovel> loadedNovelsCache = gson.fromJson(novelsCacheJson, novelsMapType);
                    if (loadedNovelsCache != null) {
                        novelsCache.putAll(loadedNovelsCache);
                        Log.d(TAG, "Loaded " + novelsCache.size() + " novels from cache");
                    }
                }

                // Load chapters cache
                String chaptersCacheJson = preferences.getString(CHAPTERS_CACHE_KEY, null);
                if (chaptersCacheJson != null) {
                    Type chaptersMapType = new TypeToken<Map<String, ChineseChapter>>(){}.getType();
                    Map<String, ChineseChapter> loadedChaptersCache = gson.fromJson(chaptersCacheJson, chaptersMapType);
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
                for (String key : novelsCache.keySet()) {
                    cacheAccessTime.put("novel_" + key, currentTime);
                }
                for (String key : chaptersCache.keySet()) {
                    cacheAccessTime.put("chapter_" + key, currentTime);
                }
                for (String key : keywordCache.keySet()) {
                    cacheAccessTime.put("keyword_" + key, currentTime);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading cache from preferences", e);
            }
        });
    }

    /**
     * Lưu cache vào SharedPreferences
     */
    private void saveCacheToPreferences() {
        executor.execute(() -> {
            try {
                trimCacheIfNeeded();

                SharedPreferences.Editor editor = preferences.edit();

                // Save novels cache
                String novelsCacheJson = gson.toJson(novelsCache);
                editor.putString(NOVELS_CACHE_KEY, novelsCacheJson);

                // Save chapters cache
                String chaptersCacheJson = gson.toJson(chaptersCache);
                editor.putString(CHAPTERS_CACHE_KEY, chaptersCacheJson);

                // Save keywords cache
                String keywordsCacheJson = gson.toJson(keywordCache);
                editor.putString(KEYWORDS_CACHE_KEY, keywordsCacheJson);

                editor.apply();
                Log.d(TAG, "Cache saved to preferences");
            } catch (Exception e) {
                Log.e(TAG, "Error saving cache to preferences", e);
            }
        });
    }

    /**
     * Cắt giảm cache nếu vượt quá kích thước tối đa
     */
    private void trimCacheIfNeeded() {
        // Trim novels cache
        if (novelsCache.size() > MAX_NOVEL_CACHE_SIZE) {
            List<Map.Entry<String, Long>> novelEntries = new ArrayList<>();
            for (String key : novelsCache.keySet()) {
                Long accessTime = cacheAccessTime.get("novel_" + key);
                if (accessTime != null) {
                    novelEntries.add(new HashMap.SimpleEntry<>(key, accessTime));
                }
            }

            // Sắp xếp theo thời gian truy cập (cũ nhất -> mới nhất)
            Collections.sort(novelEntries, Comparator.comparing(Map.Entry::getValue));

            // Xóa các mục cũ nhất
            int removeCount = novelsCache.size() - MAX_NOVEL_CACHE_SIZE;
            for (int i = 0; i < removeCount; i++) {
                String key = novelEntries.get(i).getKey();
                novelsCache.remove(key);
                cacheAccessTime.remove("novel_" + key);
            }
            Log.d(TAG, "Trimmed " + removeCount + " novels from cache");
        }

        // Trim chapters cache
        if (chaptersCache.size() > MAX_CHAPTER_CACHE_SIZE) {
            List<Map.Entry<String, Long>> chapterEntries = new ArrayList<>();
            for (String key : chaptersCache.keySet()) {
                Long accessTime = cacheAccessTime.get("chapter_" + key);
                if (accessTime != null) {
                    chapterEntries.add(new HashMap.SimpleEntry<>(key, accessTime));
                }
            }

            Collections.sort(chapterEntries, Comparator.comparing(Map.Entry::getValue));

            int removeCount = chaptersCache.size() - MAX_CHAPTER_CACHE_SIZE;
            for (int i = 0; i < removeCount; i++) {
                String key = chapterEntries.get(i).getKey();
                chaptersCache.remove(key);
                cacheAccessTime.remove("chapter_" + key);
            }
            Log.d(TAG, "Trimmed " + removeCount + " chapters from cache");
        }

        // Trim keywords cache
        if (keywordCache.size() > MAX_KEYWORD_CACHE_SIZE) {
            List<Map.Entry<String, Long>> keywordEntries = new ArrayList<>();
            for (String key : keywordCache.keySet()) {
                Long accessTime = cacheAccessTime.get("keyword_" + key);
                if (accessTime != null) {
                    keywordEntries.add(new HashMap.SimpleEntry<>(key, accessTime));
                }
            }

            Collections.sort(keywordEntries, Comparator.comparing(Map.Entry::getValue));

            int removeCount = keywordCache.size() - MAX_KEYWORD_CACHE_SIZE;
            for (int i = 0; i < removeCount; i++) {
                String key = keywordEntries.get(i).getKey();
                keywordCache.remove(key);
                cacheAccessTime.remove("keyword_" + key);
            }
            Log.d(TAG, "Trimmed " + removeCount + " keywords from cache");
        }
    }

    /**
     * Lấy danh sách truyện Trung Quốc
     */
    public void getChineseNovels(int limit, String lastNovelId, OnNovelsLoadedListener listener) {
        Query query = chineseNovelsRef.orderByKey();
        if (lastNovelId != null && !lastNovelId.isEmpty()) {
            query = query.startAfter(lastNovelId);
        }
        query = query.limitToFirst(limit);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ChineseNovel> novels = new ArrayList<>();
                for (DataSnapshot novelSnapshot : snapshot.getChildren()) {
                    ChineseNovel novel = novelSnapshot.getValue(ChineseNovel.class);
                    if (novel != null) {
                        novel.setId(novelSnapshot.getKey());
                        novels.add(novel);

                        // Cập nhật cache
                        novelsCache.put(novel.getId(), novel);
                        cacheAccessTime.put("novel_" + novel.getId(), System.currentTimeMillis());
                    }
                }

                listener.onNovelsLoaded(novels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Lấy tất cả truyện Trung Quốc
     */
    public void getChineseNovels(OnNovelsLoadedListener listener) {
        getChineseNovels(100, null, listener);
    }

    /**
     * Lấy truyện theo ID
     */
    public void getChineseNovelById(String novelId, OnNovelLoadedListener listener) {
        // Kiểm tra cache trước
        if (novelsCache.containsKey(novelId)) {
            ChineseNovel cachedNovel = novelsCache.get(novelId);
            cacheAccessTime.put("novel_" + novelId, System.currentTimeMillis());
            listener.onNovelLoaded(cachedNovel);
            return;
        }

        // Nếu không có trong cache, lấy từ Firebase
        chineseNovelsRef.child(novelId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChineseNovel novel = snapshot.getValue(ChineseNovel.class);
                if (novel != null) {
                    novel.setId(snapshot.getKey());

                    // Cập nhật cache
                    novelsCache.put(novel.getId(), novel);
                    cacheAccessTime.put("novel_" + novel.getId(), System.currentTimeMillis());

                    listener.onNovelLoaded(novel);
                } else {
                    listener.onError(new Exception("Novel not found with ID: " + novelId));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Lưu truyện Trung Quốc
     */
    public void saveChineseNovel(ChineseNovel novel, OnNovelLoadedListener listener) {
        if (novel.getId() == null || novel.getId().isEmpty()) {
            listener.onError(new Exception("Novel ID cannot be empty"));
            return;
        }

        chineseNovelsRef.child(novel.getId()).setValue(novel)
            .addOnSuccessListener(aVoid -> {
                // Cập nhật cache
                novelsCache.put(novel.getId(), novel);
                cacheAccessTime.put("novel_" + novel.getId(), System.currentTimeMillis());

                listener.onNovelLoaded(novel);
            })
            .addOnFailureListener(e -> listener.onError(e));
    }

    /**
     * Lấy chương truyện theo ID
     */
    public void getChineseChapter(String chapterId, OnChapterLoadedListener listener) {
        // Kiểm tra cache trước
        if (chaptersCache.containsKey(chapterId)) {
            ChineseChapter cachedChapter = chaptersCache.get(chapterId);
            cacheAccessTime.put("chapter_" + chapterId, System.currentTimeMillis());
            listener.onChapterLoaded(cachedChapter);
            return;
        }

        // Nếu không có trong cache, lấy từ Firebase
        chineseChaptersRef.child(chapterId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChineseChapter chapter = snapshot.getValue(ChineseChapter.class);
                if (chapter != null) {
                    chapter.setId(snapshot.getKey());

                    // Cập nhật cache
                    chaptersCache.put(chapter.getId(), chapter);
                    cacheAccessTime.put("chapter_" + chapter.getId(), System.currentTimeMillis());

                    listener.onChapterLoaded(chapter);
                } else {
                    listener.onError(new Exception("Chapter not found with ID: " + chapterId));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Lưu chương truyện
     */
    public void saveChineseChapter(ChineseChapter chapter, OnChapterLoadedListener listener) {
        if (chapter.getId() == null || chapter.getId().isEmpty()) {
            listener.onError(new Exception("Chapter ID cannot be empty"));
            return;
        }

        chineseChaptersRef.child(chapter.getId()).setValue(chapter)
            .addOnSuccessListener(aVoid -> {
                // Cập nhật cache
                chaptersCache.put(chapter.getId(), chapter);
                cacheAccessTime.put("chapter_" + chapter.getId(), System.currentTimeMillis());

                listener.onChapterLoaded(chapter);
            })
            .addOnFailureListener(e -> listener.onError(e));
    }

    /**
     * Tìm kiếm chương theo ID
     */
    public void getChapterById(String novelId, String chapterId, OnChapterLoadedListener listener) {
        // Tạo ID đầy đủ cho chương
        String fullChapterId = novelId + "_" + chapterId;
        getChineseChapter(fullChapterId, listener);
    }

    /**
     * Lấy chương kế tiếp hoặc trước đó
     */
    public void getAdjacentChapter(String novelId, String currentChapterId, boolean isPrevious, OnChapterLoadedListener listener) {
        getChineseNovelById(novelId, new OnNovelLoadedListener() {
            @Override
            public void onNovelLoaded(ChineseNovel novel) {
                if (novel.getChapterIds() == null || novel.getChapterIds().isEmpty()) {
                    listener.onError(new Exception("Novel has no chapters"));
                    return;
                }

                List<String> chapterIds = novel.getChapterIds();
                int currentIndex = -1;

                // Tìm vị trí của chương hiện tại
                for (int i = 0; i < chapterIds.size(); i++) {
                    if (chapterIds.get(i).equals(currentChapterId)) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex == -1) {
                    listener.onError(new Exception("Current chapter not found in novel"));
                    return;
                }

                // Tính toán chương kế tiếp hoặc trước đó
                int targetIndex = isPrevious ? currentIndex - 1 : currentIndex + 1;

                // Kiểm tra giới hạn
                if (targetIndex < 0 || targetIndex >= chapterIds.size()) {
                    listener.onError(new Exception("No " + (isPrevious ? "previous" : "next") + " chapter available"));
                    return;
                }

                // Lấy chương mục tiêu
                String targetChapterId = chapterIds.get(targetIndex);
                getChineseChapter(targetChapterId, listener);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    /**
     * Xóa tất cả cache
     */
    public void clearCache() {
        novelsCache.clear();
        chaptersCache.clear();
        keywordCache.clear();
        cacheAccessTime.clear();

        // Xóa cache từ SharedPreferences
        preferences.edit()
            .remove(NOVELS_CACHE_KEY)
            .remove(CHAPTERS_CACHE_KEY)
            .remove(KEYWORDS_CACHE_KEY)
            .apply();

        Log.d(TAG, "Cache cleared");
    }

    /**
     * Đóng và giải phóng tài nguyên
     */
    public void shutdown() {
        saveCacheToPreferences();
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tìm kiếm truyện Trung Quốc
     */
    public void searchChineseNovels(String keyword, int limit, OnNovelsLoadedListener listener) {
        // Tìm kiếm theo từ khóa trong tựa đề hoặc mô tả
        chineseNovelsRef.orderByChild(ChineseNovelStructure.NOVEL_TITLE_VI)
            .startAt(keyword)
            .endAt(keyword + "\uf8ff")
            .limitToFirst(limit)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<ChineseNovel> novels = new ArrayList<>();
                    for (DataSnapshot novelSnapshot : snapshot.getChildren()) {
                        ChineseNovel novel = novelSnapshot.getValue(ChineseNovel.class);
                        if (novel != null) {
                            novel.setId(novelSnapshot.getKey());
                            novels.add(novel);

                            // Cập nhật cache
                            novelsCache.put(novel.getId(), novel);
                            cacheAccessTime.put("novel_" + novel.getId(), System.currentTimeMillis());
                        }
                    }

                    listener.onNovelsLoaded(novels);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onError(error.toException());
                }
            });
    }

    /**
     * Lấy truyện theo thể loại
     */
    public void getNovelsByGenre(String genreId, OnNovelsLoadedListener listener) {
        DatabaseReference novelsByGenreRef = FirebaseDatabase.getInstance()
            .getReference(ChineseNovelStructure.CHINESE_NOVELS_BY_GENRE)
            .child(genreId);

        novelsByGenreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onNovelsLoaded(new ArrayList<>());
                    return;
                }

                List<String> novelIds = new ArrayList<>();
                for (DataSnapshot novelSnapshot : snapshot.getChildren()) {
                    if (novelSnapshot.getValue(Boolean.class) == Boolean.TRUE) {
                        novelIds.add(novelSnapshot.getKey());
                    }
                }

                if (novelIds.isEmpty()) {
                    listener.onNovelsLoaded(new ArrayList<>());
                    return;
                }

                List<ChineseNovel> novels = new ArrayList<>();
                final int[] completedCount = {0};
                final int[] totalCount = {novelIds.size()};

                for (String novelId : novelIds) {
                    getChineseNovelById(novelId, new OnNovelLoadedListener() {
                        @Override
                        public void onNovelLoaded(ChineseNovel novel) {
                            novels.add(novel);
                            completedCount[0]++;
                            checkCompletion();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error loading novel: " + e.getMessage());
                            completedCount[0]++;
                            checkCompletion();
                        }

                        private void checkCompletion() {
                            if (completedCount[0] >= totalCount[0]) {
                                listener.onNovelsLoaded(novels);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }
}
