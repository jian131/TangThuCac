package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Model đại diện cho thể loại truyện Trung Quốc
 */
public class ChineseGenre implements Serializable {
    // Các thể loại phổ biến
    public static final String XUAN_HUAN = "xuanhuan";      // 玄幻小说 - Huyền Huyễn
    public static final String XIAN_XIA = "xianxia";        // 仙侠小说 - Tiên Hiệp
    public static final String WU_XIA = "wuxia";            // 武侠小说 - Võ Hiệp
    public static final String URBAN = "urban";             // 都市小说 - Đô Thị
    public static final String HISTORICAL = "historical";   // 历史小说 - Lịch Sử
    public static final String ROMANCE = "romance";         // 言情小说 - Ngôn Tình
    public static final String GAME = "game";               // 游戏小说 - Game
    public static final String SCIFI = "scifi";             // 科幻小说 - Khoa Huyễn
    public static final String MILITARY = "military";       // 军事小说 - Quân Sự
    public static final String SPORTS = "sports";           // 体育小说 - Thể Thao
    public static final String LIGHT_NOVEL = "lightnovel";  // 轻小说 - Light Novel

    private String id;          // ID thể loại (xuanhuan, xianxia, ...)
    private String name;        // Tên thể loại tiếng Trung (玄幻小说, 仙侠小说, ...)
    private String nameVi;      // Tên thể loại tiếng Việt (Huyền Huyễn, Tiên Hiệp, ...)
    private String description; // Mô tả tiếng Việt
    private String imageUrl;    // URL hình ảnh đại diện
    private int novelCount;     // Số lượng truyện thuộc thể loại này

    public ChineseGenre() {
        // Constructor mặc định cho Firebase
        this.novelCount = 0;
    }

    // Constructor đầy đủ
    public ChineseGenre(String id, String name, String nameVi, String description) {
        this();
        this.id = id;
        this.name = name;
        this.nameVi = nameVi;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getNovelCount() {
        return novelCount;
    }

    public void setNovelCount(int novelCount) {
        this.novelCount = novelCount;
    }

    /**
     * Tăng số lượng truyện
     */
    public void incrementNovelCount() {
        this.novelCount++;
    }

    /**
     * Giảm số lượng truyện
     */
    public void decrementNovelCount() {
        if (this.novelCount > 0) {
            this.novelCount--;
        }
    }

    /**
     * Tạo các thể loại mặc định
     * @param id ID thể loại
     * @return Thể loại mặc định tương ứng hoặc null nếu không tìm thấy
     */
    public static ChineseGenre createDefaultGenre(String id) {
        switch (id) {
            case XUAN_HUAN:
                return new ChineseGenre(XUAN_HUAN, "玄幻小说", "Huyền Huyễn",
                        "Tiểu thuyết huyền huyễn là một thế giới có quy luật hoàn toàn khác với thế giới thực tại, các nhân vật có năng lực siêu nhiên.");
            case XIAN_XIA:
                return new ChineseGenre(XIAN_XIA, "仙侠小说", "Tiên Hiệp",
                        "Tiểu thuyết tiên hiệp kể về các tu tiên, đạo sĩ có pháp lực, có thọ mệnh dài lâu, tu luyện để đạt tới cảnh giới cao nhất.");
            case WU_XIA:
                return new ChineseGenre(WU_XIA, "武侠小说", "Võ Hiệp",
                        "Tiểu thuyết võ hiệp lấy bối cảnh thời phong kiến, kể về các nhân vật võ công cao cường, hiệp nghĩa giang hồ.");
            case URBAN:
                return new ChineseGenre(URBAN, "都市小说", "Đô Thị",
                        "Tiểu thuyết đô thị thường kể về các nhân vật trong thế giới hiện đại với các mối quan hệ xã hội, tình cảm, công việc.");
            case HISTORICAL:
                return new ChineseGenre(HISTORICAL, "历史小说", "Lịch Sử",
                        "Tiểu thuyết lịch sử lấy bối cảnh các thời kỳ lịch sử, có thể là tái hiện hoặc hư cấu dựa trên nền tảng lịch sử.");
            case ROMANCE:
                return new ChineseGenre(ROMANCE, "言情小说", "Ngôn Tình",
                        "Tiểu thuyết ngôn tình tập trung vào các mối quan hệ tình cảm lãng mạn giữa nam và nữ chính.");
            case GAME:
                return new ChineseGenre(GAME, "游戏小说", "Game",
                        "Tiểu thuyết game kể về các nhân vật trong thế giới game hoặc bị lôi vào thế giới game với các nhiệm vụ, phiêu lưu.");
            case SCIFI:
                return new ChineseGenre(SCIFI, "科幻小说", "Khoa Huyễn",
                        "Tiểu thuyết khoa huyễn đưa ra các giả thuyết khoa học, công nghệ tương lai hoặc không gian, thời gian.");
            case MILITARY:
                return new ChineseGenre(MILITARY, "军事小说", "Quân Sự",
                        "Tiểu thuyết quân sự kể về đời sống quân ngũ, chiến tranh, binh pháp, các trận đánh và tướng lĩnh.");
            case SPORTS:
                return new ChineseGenre(SPORTS, "体育小说", "Thể Thao",
                        "Tiểu thuyết thể thao kể về các nhân vật trong thể thao, các tài năng hay quá trình phấn đấu đạt thành tích.");
            case LIGHT_NOVEL:
                return new ChineseGenre(LIGHT_NOVEL, "轻小说", "Light Novel",
                        "Light Novel là tiểu thuyết nhẹ nhàng, thường có minh họa màu, hướng đến đối tượng thanh thiếu niên.");
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "ChineseGenre{" +
                "id='" + id + '\'' +
                ", nameVi='" + nameVi + '\'' +
                ", novelCount=" + novelCount +
                '}';
    }
}
