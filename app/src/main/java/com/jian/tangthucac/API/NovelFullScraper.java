package com.jian.tangthucac.API;

import android.content.Context;
import android.util.Log;

import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.TranslatedChapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crawler cho trang NovelFull.com
 */
public class NovelFullScraper extends WebsiteCrawlerBase {
    private static final String TAG = "NovelFullScraper";

    // URL endpoints
    private static final String BASE_URL = "https://novelfull.com";
    private static final String SEARCH_URL = BASE_URL + "/search?keyword=";
    private static final String NOVEL_URL = BASE_URL + "/";

    public NovelFullScraper(Context context) {
        super(context);
    }

    @Override
    public String getSourceName() {
        return "NovelFull";
    }

    @Override
    public void searchNovel(String keyword, OnSearchResultListener listener) {
        executor.execute(() -> {
            try {
                String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
                String url = SEARCH_URL + encodedKeyword;

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                List<OriginalStory> results = parseSearchResults(doc);

                // Trả kết quả về main thread
                mainHandler.post(() -> listener.onSearchCompleted(results));

            } catch (Exception e) {
                handleError(listener, "Lỗi khi tìm kiếm truyện: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void getNovelDetails(String novelId, OnNovelDetailListener listener) {
        executor.execute(() -> {
            try {
                String url = NOVEL_URL + novelId;

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                OriginalStory story = parseNovelDetails(doc, novelId);

                // Trả kết quả về main thread
                mainHandler.post(() -> listener.onNovelDetailLoaded(story));

            } catch (Exception e) {
                handleError(listener, "Lỗi khi lấy chi tiết truyện: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void getChapterList(String novelId, OnNovelDetailListener listener) {
        // Lấy danh sách chương cùng với chi tiết truyện
        getNovelDetails(novelId, listener);
    }

    @Override
    public void getChapterContent(String chapterId, OnChapterContentListener listener) {
        executor.execute(() -> {
            try {
                // Giả sử chapterId là URL đầy đủ hoặc đường dẫn tương đối
                String url = chapterId.startsWith("http") ? chapterId : BASE_URL + chapterId;

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                TranslatedChapter chapter = parseChapterContent(doc, chapterId);

                // Trả kết quả về main thread
                mainHandler.post(() -> listener.onChapterLoaded(chapter));

            } catch (Exception e) {
                handleError(listener, "Lỗi khi lấy nội dung chương: " + e.getMessage(), e);
            }
        });
    }

    private List<OriginalStory> parseSearchResults(Document doc) {
        List<OriginalStory> results = new ArrayList<>();

        try {
            Elements novelElements = doc.select(".list-truyen .row");

            for (Element novelElement : novelElements) {
                Element titleElement = novelElement.selectFirst("h3.truyen-title a");

                if (titleElement == null) continue;

                String title = titleElement.text();
                String href = titleElement.attr("href");
                String id = extractIdFromUrl(href);

                Element authorElement = novelElement.selectFirst(".author");
                String author = authorElement != null ? authorElement.text().replace("Author:", "").trim() : "Không rõ";

                Element imageElement = novelElement.selectFirst("img");
                String imageUrl = imageElement != null ? imageElement.attr("src") : "";
                if (!imageUrl.startsWith("http")) {
                    imageUrl = BASE_URL + imageUrl;
                }

                Element descElement = novelElement.selectFirst(".excerpt");
                String desc = descElement != null ? descElement.text() : "";

                OriginalStory story = new OriginalStory();
                story.setId(generateNovelId(id));
                story.setTitle(title);
                story.setAuthor(author);
                story.setImageUrl(imageUrl);
                story.setDescription(desc);
                story.setSource(getSourceName());
                story.setSourceUrl(BASE_URL + href);
                story.setLanguage("en"); // NovelFull thường có truyện tiếng Anh

                // Thể loại thường được lấy từ trang chi tiết
                ArrayList<String> genres = new ArrayList<>();
                Elements genreElements = novelElement.select(".kind a");
                for (Element genreElement : genreElements) {
                    genres.add(genreElement.text());
                }
                story.setGenres(genres);

                results.add(story);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi parse kết quả tìm kiếm: " + e.getMessage(), e);
        }

        return results;
    }

    private OriginalStory parseNovelDetails(Document doc, String novelId) {
        OriginalStory story = new OriginalStory();

        try {
            Element infoElement = doc.selectFirst(".info-holder");
            if (infoElement == null) return story;

            // Lấy tiêu đề
            Element titleElement = doc.selectFirst("h3.title");
            String title = titleElement != null ? titleElement.text() : "";

            // Lấy tác giả
            Element authorElement = infoElement.selectFirst(".info a[href*=author]");
            String author = authorElement != null ? authorElement.text() : "Không rõ";

            // Lấy hình ảnh
            Element imageElement = doc.selectFirst(".book img");
            String imageUrl = imageElement != null ? imageElement.attr("src") : "";
            if (!imageUrl.startsWith("http")) {
                imageUrl = BASE_URL + imageUrl;
            }

            // Lấy mô tả
            Element descElement = doc.selectFirst(".desc-text");
            String desc = descElement != null ? descElement.html() : "";

            // Lấy thể loại
            Elements genreElements = infoElement.select(".info a[href*=genre]");
            ArrayList<String> genres = new ArrayList<>();
            for (Element genreElement : genreElements) {
                genres.add(genreElement.text());
            }

            // Lấy thông tin hoàn thành
            Element statusElement = infoElement.selectFirst(".info .text-success");
            boolean completed = statusElement != null && statusElement.text().contains("Completed");

            // Thiết lập story
            story.setId(novelId);
            story.setTitle(title);
            story.setAuthor(author);
            story.setImageUrl(imageUrl);
            story.setDescription(desc);
            story.setSource(getSourceName());
            story.setSourceUrl(BASE_URL + "/" + novelId);
            story.setGenres(genres);
            story.setCompleted(completed);
            story.setLanguage("en");

            // Lấy danh sách chương
            int totalChapters = getChapterCount(doc);
            story.setChapterCount(totalChapters);

            // Lấy tags
            ArrayList<String> tags = new ArrayList<>(genres); // Sử dụng genres làm tags mặc định
            story.setTags(tags);

            // Thiết lập thời gian cập nhật
            story.setUpdateTime(System.currentTimeMillis());

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi parse chi tiết truyện: " + e.getMessage(), e);
        }

        return story;
    }

    private TranslatedChapter parseChapterContent(Document doc, String chapterId) {
        TranslatedChapter chapter = new TranslatedChapter();

        try {
            // Lấy tiêu đề chương
            Element titleElement = doc.selectFirst(".chapter-title");
            String title = titleElement != null ? titleElement.text() : "";

            // Lấy nội dung chương
            Element contentElement = doc.selectFirst("#chapter-content");
            String content = contentElement != null ? contentElement.html() : "";

            // Lấy số chương (từ tiêu đề)
            int chapterNumber = 0;
            if (title.contains("Chapter")) {
                Pattern pattern = Pattern.compile("Chapter (\\d+)");
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    chapterNumber = Integer.parseInt(matcher.group(1));
                }
            }

            // Thiết lập chapter
            chapter.setId(chapterId);
            chapter.setTitle(title);
            chapter.setContent(content);
            chapter.setChapterNumber(chapterNumber);
            chapter.setSourceUrl(chapterId);
            chapter.setTranslatedTime(System.currentTimeMillis());

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi parse nội dung chương: " + e.getMessage(), e);
        }

        return chapter;
    }

    private int getChapterCount(Document doc) {
        try {
            // Tìm phần tử hiển thị tổng số chương
            Element lastPageElement = doc.selectFirst(".last a");
            if (lastPageElement != null) {
                String href = lastPageElement.attr("href");
                Pattern pattern = Pattern.compile("page=(\\d+)");
                Matcher matcher = pattern.matcher(href);
                if (matcher.find()) {
                    int lastPage = Integer.parseInt(matcher.group(1));
                    Elements chaptersPerPage = doc.select("ul.list-chapter li");
                    return lastPage * chaptersPerPage.size();
                }
            }

            // Nếu không tìm thấy phân trang, đếm trực tiếp số chương
            return doc.select("ul.list-chapter li").size();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đếm số chương: " + e.getMessage(), e);
            return 0;
        }
    }

    private String extractIdFromUrl(String url) {
        // Extract ID từ URL, ví dụ: /truyen/some-novel-name/ -> some-novel-name
        String[] parts = url.split("/");
        for (String part : parts) {
            if (!part.isEmpty() && !part.equals("truyen")) {
                return part;
            }
        }
        return url;
    }
}
