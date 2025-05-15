package com.jian.tangthucac.crawler;

import com.jian.tangthucac.model.OriginalStory;

import java.util.List;

/**
 * Interface cho các crawler thu thập truyện từ các nguồn khác nhau
 */
public interface NovelCrawler {
    /**
     * Tìm kiếm truyện theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     * @param page Trang kết quả (bắt đầu từ 1)
     * @param listener Callback khi hoàn thành
     */
    void searchNovels(String keyword, int page, OnSearchCompleteListener listener);

    /**
     * Lấy thông tin chi tiết của truyện
     * @param storyId ID của truyện
     * @param listener Callback khi hoàn thành
     */
    void getNovelDetails(String storyId, OnNovelDetailListener listener);

    /**
     * Lấy danh sách chương của truyện
     * @param storyId ID của truyện
     * @param listener Callback khi hoàn thành
     */
    void getChapterList(String storyId, OnChapterListListener listener);

    /**
     * Lấy nội dung của một chương
     * @param storyId ID của truyện
     * @param chapterId ID của chương
     * @param listener Callback khi hoàn thành
     */
    void getChapterContent(String storyId, String chapterId, OnChapterContentListener listener);

    /**
     * Interface lắng nghe kết quả tìm kiếm
     */
    interface OnSearchCompleteListener {
        void onSearchCompleted(List<OriginalStory> results);
        void onError(Exception e);
    }

    /**
     * Interface lắng nghe kết quả chi tiết truyện
     */
    interface OnNovelDetailListener {
        void onNovelDetailLoaded(OriginalStory novel);
        void onError(Exception e);
    }

    /**
     * Interface lắng nghe kết quả danh sách chương
     */
    interface OnChapterListListener {
        void onChapterListLoaded(List<String> chapterIds, List<String> chapterTitles);
        void onError(Exception e);
    }

    /**
     * Interface lắng nghe kết quả nội dung chương
     */
    interface OnChapterContentListener {
        void onChapterContentLoaded(String chapterId, String title, String content);
        void onError(Exception e);
    }

    /**
     * Interface callback khi hoàn thành tìm kiếm truyện (tương thích ngược)
     */
    interface OnSearchResultListener {
        void onSearchCompleted(List<OriginalStory> results);
        void onSearchError(Exception e);
    }

    /**
     * Phương thức tìm kiếm truyện (tương thích ngược)
     * @param keyword Từ khóa tìm kiếm
     * @param listener Callback khi hoàn thành
     */
    default void searchNovel(String keyword, OnSearchResultListener listener) {
        // Gọi phương thức mới với trang mặc định là 1
        searchNovels(keyword, 1, new OnSearchCompleteListener() {
            @Override
            public void onSearchCompleted(List<OriginalStory> results) {
                if (listener != null) {
                    listener.onSearchCompleted(results);
                }
            }

            @Override
            public void onError(Exception e) {
                if (listener != null) {
                    listener.onSearchError(e);
                }
            }
        });
    }
}
