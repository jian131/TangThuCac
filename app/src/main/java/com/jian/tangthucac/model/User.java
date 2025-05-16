package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model đại diện cho người dùng trong ứng dụng
 */
public class User implements Serializable {
    private String id;                  // ID người dùng (Firebase UID)
    private String username;            // Tên người dùng
    private String email;               // Email
    private String avatarUrl;           // URL ảnh đại diện
    private Map<String, String> apiKeys; // Các API key (DeepL, OpenAI, ...)
    private Map<String, Object> settings; // Cài đặt người dùng
    private long createdAt;             // Thời gian tạo tài khoản
    private long lastLogin;             // Thời gian đăng nhập cuối
    private int translationCredits;     // Số credits dùng để dịch
    private boolean isPremium;          // Người dùng premium
    private UserLibrary library;        // Thư viện của người dùng

    public User() {
        // Constructor mặc định cho Firebase
        this.apiKeys = new HashMap<>();
        this.settings = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.translationCredits = 100; // Tặng 100 credits khi đăng ký
        this.isPremium = false;
    }

    // Constructor với ID và email
    public User(String id, String email) {
        this();
        this.id = id;
        this.email = email;
        this.username = email.split("@")[0]; // Mặc định username là phần trước @ của email
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Map<String, String> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(Map<String, String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getTranslationCredits() {
        return translationCredits;
    }

    public void setTranslationCredits(int translationCredits) {
        this.translationCredits = translationCredits;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public UserLibrary getLibrary() {
        return library;
    }

    public void setLibrary(UserLibrary library) {
        this.library = library;
    }

    // Phương thức tiện ích
    /**
     * Thêm API key
     * @param apiName Tên API (deepl, openai, ...)
     * @param apiKey API key
     */
    public void addApiKey(String apiName, String apiKey) {
        if (apiKeys == null) {
            apiKeys = new HashMap<>();
        }
        apiKeys.put(apiName, apiKey);
    }

    /**
     * Lấy API key
     * @param apiName Tên API
     * @return API key hoặc null nếu không tồn tại
     */
    public String getApiKey(String apiName) {
        if (apiKeys != null && apiKeys.containsKey(apiName)) {
            return apiKeys.get(apiName);
        }
        return null;
    }

    /**
     * Thêm cài đặt
     * @param key Khóa cài đặt
     * @param value Giá trị cài đặt
     */
    public void addSetting(String key, Object value) {
        if (settings == null) {
            settings = new HashMap<>();
        }
        settings.put(key, value);
    }

    /**
     * Lấy cài đặt
     * @param key Khóa cài đặt
     * @return Giá trị cài đặt hoặc null nếu không tồn tại
     */
    public Object getSetting(String key) {
        if (settings != null && settings.containsKey(key)) {
            return settings.get(key);
        }
        return null;
    }

    /**
     * Tiêu thụ credits dịch thuật
     * @param credits Số credits cần tiêu thụ
     * @return true nếu đủ credits, false nếu không đủ
     */
    public boolean useTranslationCredits(int credits) {
        if (isPremium) return true; // Người dùng premium không giới hạn credits

        if (translationCredits >= credits) {
            translationCredits -= credits;
            return true;
        }
        return false;
    }

    /**
     * Cập nhật thời gian đăng nhập
     */
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    /**
     * Khởi tạo thư viện nếu chưa có
     */
    public void initializeLibrary() {
        if (library == null) {
            library = new UserLibrary(id);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isPremium=" + isPremium +
                ", translationCredits=" + translationCredits +
                '}';
    }
}
