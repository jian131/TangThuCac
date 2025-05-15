package com.jian.tangthucac.API;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jian.tangthucac.model.OriginalStory;
import com.jian.tangthucac.model.TranslatedChapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter cho trang truyện Trung Quốc đã dịch như Wikidich
 */
public class ChineseSiteAdapter extends WebsiteCrawlerBase {
    private static final String TAG = "ChineseSiteAdapter";
    private static final String BASE_URL = "https://truyenwikidich.net";

    public ChineseSiteAdapter(Context context) {
        super(context);
    }

    @Override
    public String getSourceName() {
        return "WikidichCN";
    }

    @Override
    public void searchNovel(String keyword, OnSearchResultListener listener) {
        executor.execute(() -> {
            try {
                String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
                String searchUrl = BASE_URL + "/tim-kiem?tu-khoa=" + encodedKeyword;

                Document doc = Jsoup.connect(searchUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                List<OriginalStory> results = parseSearchResults(doc);

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
                String url = BASE_URL + "/truyen/" + novelId;

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                OriginalStory story = parseNovelDetails(doc, novelId);

                mainHandler.post(() -> listener.onNovelDetailLoaded(story));
            } catch (Exception e) {
                handleError(listener, "Lỗi khi lấy chi tiết truyện: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void getChapterList(String novelId, OnNovelDetailListener listener) {
        getNovelDetails(novelId, listener);
    }

    @Override
    public void getChapterContent(String chapterId, OnChapterContentListener listener) {
        executor.execute(() -> {
            try {
                String url = chapterId.startsWith("http") ? chapterId : BASE_URL + chapterId;

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                TranslatedChapter chapter = parseChapterContent(doc, chapterId);

                mainHandler.post(() -> listener.onChapterLoaded(chapter));
            } catch (Exception e) {
                handleError(listener, "Lỗi khi lấy nội dung chương: " + e.getMessage(), e);
            }
        });
    }

    private List<OriginalStory> parseSearchResults(Document doc) {
        List<OriginalStory> results = new ArrayList<>();

        try {
            Elements bookItems = doc.select(".list-stories .book-item");

            for (Element bookItem : bookItems) {
                Element titleElement = bookItem.selectFirst("h3.book-title a");
                if (titleElement == null) continue;

                String title = titleElement.text();
                String href = titleElement.attr("href");
                String id = extractIdFromUrl(href);

                Element authorElement = bookItem.selectFirst(".book-author");
                String author = authorElement != null ? authorElement.text() : "Không rõ";

                Element imageElement = bookItem.selectFirst(".book-img img");
                String imageUrl = imageElement != null ? imageElement.attr("src") : "";
                if (!imageUrl.startsWith("http")) {
                    imageUrl = BASE_URL + imageUrl;
                }

                Element descElement = bookItem.selectFirst(".book-desc");
                String desc = descElement != null ? descElement.text() : "";

                OriginalStory story = new OriginalStory();
                story.setId(generateNovelId(id));
                story.setTitle(title);
                story.setAuthor(author);
                story.setImageUrl(imageUrl);
                story.setDescription(desc);
                story.setSource(getSourceName());
                story.setSourceUrl(BASE_URL + href);
                story.setLanguage("zh-vi"); // Wikidich thường có truyện Trung đã dịch sang tiếng Việt

                // Lấy thể loại
                Elements genreElements = bookItem.select(".book-tags a");
                ArrayList<String> genres = new ArrayList<>();
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
            // Lấy tiêu đề
            Element titleElement = doc.selectFirst("h1.book-title");
            String title = titleElement != null ? titleElement.text() : "";

            // Lấy tác giả
            Element authorElement = doc.selectFirst(".book-info .author span");
            String author = authorElement != null ? authorElement.text() : "Không rõ";

            // Lấy hình ảnh
            Element imageElement = doc.selectFirst(".book-img img");
            String imageUrl = imageElement != null ? imageElement.attr("src") : "";
            if (!imageUrl.startsWith("http")) {
                imageUrl = BASE_URL + imageUrl;
            }

            // Lấy mô tả
            Element descElement = doc.selectFirst(".desc-text");
            String desc = descElement != null ? descElement.html() : "";

            // Lấy thể loại
            Elements genreElements = doc.select(".book-info .tag a");
            ArrayList<String> genres = new ArrayList<>();
            for (Element genreElement : genreElements) {
                genres.add(genreElement.text());
            }

            // Thiết lập story
            story.setId(novelId);
            story.setTitle(title);
            // Thêm tiêu đề tiếng Việt (trong trường hợp có dịch)
            story.setTitleVi(title);
            story.setAuthor(author);
            story.setImageUrl(imageUrl);
            story.setDescription(desc);
            // Thêm mô tả tiếng Việt (trong trường hợp có dịch)
            story.setDescriptionVi(desc);
            story.setSource(getSourceName());
            story.setSourceUrl(BASE_URL + "/truyen/" + novelId);
            story.setGenres(genres);
            story.setLanguage("zh-vi");

            // Lấy danh sách chương
            Elements chapterElements = doc.select(".list-chapter li a");
            int chapterCount = chapterElements.size();
            story.setChapterCount(chapterCount);

            // Lấy tags
            ArrayList<String> tags = new ArrayList<>(genres);
            story.setTags(tags);

            // Thiết lập thời gian cập nhật
            story.setUpdateTime(System.currentTimeMillis());

            // Kiểm tra tình trạng hoàn thành
            boolean completed = doc.select(".book-info .complete").size() > 0;
            story.setCompleted(completed);

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
            String content = "";
            if (contentElement != null) {
                // Loại bỏ các phần tử không mong muốn
                contentElement.select("script, ins, iframe").remove();
                content = contentElement.html();
            }

            // Lấy số chương (từ tiêu đề)
            int chapterNumber = 0;
            Pattern pattern = Pattern.compile("Chương (\\d+)");
            Matcher matcher = pattern.matcher(title);
            if (matcher.find()) {
                chapterNumber = Integer.parseInt(matcher.group(1));
            }

            // Thiết lập chapter
            chapter.setId(chapterId);
            chapter.setTitle(title);
            chapter.setTitleVi(title); // Wikidich đã dịch tiêu đề sang tiếng Việt
            chapter.setContent(content);
            chapter.setContentVi(content); // Wikidich đã dịch nội dung sang tiếng Việt
            chapter.setChapterNumber(chapterNumber);
            chapter.setSourceUrl(chapterId);
            chapter.setTranslatedTime(System.currentTimeMillis());
            chapter.setTranslationEngine("human"); // Dịch bởi con người
            chapter.setTranslationQuality(1.0); // Chất lượng cao do con người dịch

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi parse nội dung chương: " + e.getMessage(), e);
        }

        return chapter;
    }

    private String extractIdFromUrl(String url) {
        // Extract ID từ URL, ví dụ: /truyen/ten-truyen/ -> ten-truyen
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("truyen") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return url;
    }
}
