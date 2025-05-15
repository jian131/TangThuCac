package com.jian.tangthucac.API;

import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.TranslatedChapter;

import java.util.List;

/**
 * Interface định nghĩa các phương thức cần thiết cho việc crawl dữ liệu truyện
 * từ các trang web Trung Quốc khác nhau.
 */
public interface NovelCrawler {

    /**
     * Callback được gọi sau khi tìm kiếm hoàn tất
     */
    interface OnSearchResultListener {
        void onSearchCompleted(List<OriginalStory> results);
        void onError(Exception e);
    }

    /**
     * Callback được gọi sau khi lấy chi tiết truyện hoàn tất
     */
    interface OnNovelDetailListener {
        void onNovelDetailLoaded(OriginalStory story);
        void onError(Exception e);
    }

    /**
     * Callback được gọi sau khi lấy nội dung chương hoàn tất
     */
    interface OnChapterContentListener {
        void onChapterLoaded(TranslatedChapter chapter);
        void onError(Exception e);
    }

    /**
     * Tìm kiếm truyện bằng từ khóa
     * @param keyword Từ khóa tìm kiếm (tiếng Trung)
     * @param listener Callback để nhận kết quả
     */
    void searchNovel(String keyword, OnSearchResultListener listener);

    /**
     * Lấy thông tin chi tiết của truyện
     * @param novelId ID của truyện
     * @param listener Callback để nhận kết quả
     */
    void getNovelDetails(String novelId, OnNovelDetailListener listener);

    /**
     * Lấy nội dung của một chương
     * @param chapterId ID hoặc URL của chương
     * @param listener Callback để nhận kết quả
     */
    void getChapterContent(String chapterId, OnChapterContentListener listener);

    /**
     * Lấy danh sách chương của truyện
     * @param novelId ID của truyện
     * @param listener Callback để nhận kết quả
     */
    void getChapterList(String novelId, OnNovelDetailListener listener);

    /**
     * Trả về tên của nguồn truyện
     * @return Tên của nguồn (Webnovel, Qidian, JJWXC, etc.)
     */
    String getSourceName();
}
