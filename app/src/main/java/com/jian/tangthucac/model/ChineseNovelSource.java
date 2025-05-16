package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model đại diện cho các nguồn truyện Trung Quốc
 */
public class ChineseNovelSource implements Serializable {
    public static final String QIDIAN = "qidian";        // 起点中文网
    public static final String ZONGHENG = "zongheng";    // 纵横中文网
    public static final String UUKANSHU = "uukanshu";    // UU看书
    public static final String FANQIE = "fanqie";        // 番茄小说
    public static final String HONGXIU = "hongxiu";      // 红袖添香
    public static final String JJWXC = "jjwxc";          // 晋江文学城
    public static final String SFACG = "sfacg";          // SF轻小说
    public static final String SHUQI = "shuqi";          // 书旗小说
    public static final String CIWEIMAO = "ciweimao";    // 刺猬猫
    public static final String SEVENTEENK = "17k";       // 17K小说网

    private String id;              // ID nguồn (qidian, zongheng, ...)
    private String name;            // Tên nguồn (起点中文网, 纵横中文网, ...)
    private String nameVi;          // Tên tiếng Việt (Khởi Điểm, Tung Hoành, ...)
    private String baseUrl;         // URL cơ sở (https://book.qidian.com)
    private String logoUrl;         // URL logo
    private boolean enabled;        // Trạng thái kích hoạt
    private int priority;           // Ưu tiên (số thấp = ưu tiên cao hơn)
    private Map<String, String> endpoints; // Các endpoint API của nguồn

    public ChineseNovelSource() {
        // Constructor mặc định cho Firebase
        this.enabled = true;
        this.endpoints = new HashMap<>();
    }

    // Constructor đầy đủ
    public ChineseNovelSource(String id, String name, String nameVi, String baseUrl) {
        this();
        this.id = id;
        this.name = name;
        this.nameVi = nameVi;
        this.baseUrl = baseUrl;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameVi() {
        return nameVi;
    }

    public void setNameVi(String nameVi) {
        this.nameVi = nameVi;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints = endpoints;
    }

    // Phương thức tiện ích
    /**
     * Thêm endpoint cho nguồn
     * @param name Tên endpoint (search, detail, chapter, ...)
     * @param path Đường dẫn endpoint
     */
    public void addEndpoint(String name, String path) {
        if (endpoints == null) {
            endpoints = new HashMap<>();
        }
        endpoints.put(name, path);
    }

    /**
     * Lấy URL đầy đủ cho endpoint cụ thể
     * @param name Tên endpoint
     * @return URL đầy đủ hoặc null nếu không tồn tại
     */
    public String getEndpointUrl(String name) {
        if (endpoints != null && endpoints.containsKey(name)) {
            return baseUrl + endpoints.get(name);
        }
        return null;
    }

    /**
     * Tạo nguồn mặc định Qidian
     * @return Nguồn Qidian
     */
    public static ChineseNovelSource createQidianSource() {
        ChineseNovelSource source = new ChineseNovelSource(
                QIDIAN,
                "起点中文网",
                "Khởi Điểm",
                "https://book.qidian.com");
        source.setLogoUrl("https://qidian.gtimg.com/qd/images/logo.png");
        source.setPriority(1);
        source.addEndpoint("search", "/search?kw=%s");
        source.addEndpoint("detail", "/info/%s");
        source.addEndpoint("chapter", "/read/%s/%s");
        source.addEndpoint("catalog", "/ajax/book/category?bookId=%s");
        return source;
    }

    @Override
    public String toString() {
        return "ChineseNovelSource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nameVi='" + nameVi + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
