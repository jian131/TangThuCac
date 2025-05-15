package com.jian.tangthucac.API;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory để tạo và quản lý các crawler cho các trang web khác nhau
 */
public class CrawlerFactory {
    private static CrawlerFactory instance;
    private final Context context;
    private final Map<String, NovelCrawler> crawlers = new HashMap<>();

    // Các nguồn truyện hỗ trợ
    public static final String SOURCE_WEBNOVEL = "Webnovel";
    public static final String SOURCE_QIDIAN = "Qidian";
    public static final String SOURCE_JJWXC = "JJWXC";
    public static final String SOURCE_NOVELFULL = "NovelFull";
    public static final String SOURCE_WIKIDICH_CN = "WikidichCN";

    private CrawlerFactory(Context context) {
        this.context = context.getApplicationContext();
        initializeCrawlers();
    }

    public static synchronized CrawlerFactory getInstance(Context context) {
        if (instance == null) {
            instance = new CrawlerFactory(context);
        }
        return instance;
    }

    private void initializeCrawlers() {
        // Khởi tạo các crawler
        crawlers.put(SOURCE_WEBNOVEL, new WebnovelCrawler(context));
        crawlers.put(SOURCE_NOVELFULL, new NovelFullScraper(context));
        crawlers.put(SOURCE_WIKIDICH_CN, new ChineseSiteAdapter(context));

        // TODO: Triển khai các crawler khác
        // crawlers.put(SOURCE_QIDIAN, new QidianCrawler(context));
        // crawlers.put(SOURCE_JJWXC, new JJWXCCrawler(context));
    }

    /**
     * Lấy crawler theo tên nguồn
     * @param source Tên nguồn (Webnovel, Qidian, JJWXC, etc.)
     * @return NovelCrawler tương ứng hoặc null nếu không tìm thấy
     */
    public NovelCrawler getCrawler(String source) {
        return crawlers.get(source);
    }

    /**
     * Kiểm tra xem nguồn có được hỗ trợ hay không
     * @param source Tên nguồn
     * @return true nếu nguồn được hỗ trợ
     */
    public boolean isSourceSupported(String source) {
        return crawlers.containsKey(source);
    }

    /**
     * Lấy danh sách tên các nguồn được hỗ trợ
     * @return Mảng các tên nguồn
     */
    public String[] getSupportedSources() {
        return crawlers.keySet().toArray(new String[0]);
    }
}
