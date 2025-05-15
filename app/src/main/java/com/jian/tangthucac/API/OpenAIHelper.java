package com.jian.tangthucac;

import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIHelper {
    private static final String TAG = "AIAssistantAPI";

    // Gemini API Constants
    private static final String GEMINI_API_KEY = "AIzaSyC3YdOPnUA1SFjgK_iuB3t79DvaNq5U3Go";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // Claude API Constants
    private static final String CLAUDE_API_KEY = ""; // Nhập API key ở đây
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL = "claude-3-haiku-20240307";

    // LLM Provider Options
    public enum Provider {
        GEMINI,
        CLAUDE
    }

    private static Provider currentProvider = Provider.GEMINI;

    // Thiết lập provider
    public static void setProvider(Provider provider) {
        currentProvider = provider;
    }

    public static Provider getCurrentProvider() {
        return currentProvider;
    }

    public static void sendQuestion(String message, Callback callback) {
        if (currentProvider == Provider.CLAUDE && !CLAUDE_API_KEY.isEmpty()) {
            sendToClaudeAPI(message, callback);
        } else {
            sendToGeminiAPI(message, callback);
        }
    }

    // Gửi câu hỏi đến Gemini API
    private static void sendToGeminiAPI(String message, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        try {
            JSONObject part = new JSONObject();
            part.put("text", message);

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(part));

            json.put("contents", new JSONArray().put(content));

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(GEMINI_API_URL + "?key=" + GEMINI_API_KEY)
                    .post(body)
                    .build();

            Log.d(TAG, "Gemini Request body: " + json.toString());

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo yêu cầu gửi Gemini", e);
        }
    }

    // Gửi câu hỏi đến Claude API
    private static void sendToClaudeAPI(String message, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        try {
            // Xây dựng yêu cầu cho Anthropic API
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Bạn là trợ lý AI giúp đỡ người dùng tìm hiểu và khám phá truyện chữ. Bạn có kiến thức về văn học, tiểu thuyết, và có thể gợi ý tác phẩm phù hợp với sở thích của người dùng.");

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);

            JSONArray messages = new JSONArray();
            messages.put(systemMessage);
            messages.put(userMessage);

            json.put("model", CLAUDE_MODEL);
            json.put("messages", messages);
            json.put("max_tokens", 1024);
            json.put("temperature", 0.7);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(CLAUDE_API_URL)
                    .post(body)
                    .header("x-api-key", CLAUDE_API_KEY)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .build();

            Log.d(TAG, "Claude Request body: " + json.toString());

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo yêu cầu gửi Claude", e);
        }
    }

    // Parse Gemini response
    public static String parseGeminiResponse(String jsonResponse) {
        try {
            Log.d(TAG, "Gemini Raw response: " + jsonResponse);
            JSONObject obj = new JSONObject(jsonResponse);

            if (obj.has("error")) {
                JSONObject err = obj.getJSONObject("error");
                return "Lỗi từ Gemini: " + err.getString("message");
            }

            JSONArray candidates = obj.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text");
                }
            }

            return "Không có nội dung phản hồi từ Gemini.";
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi phân tích phản hồi từ Gemini", e);
            return "Lỗi khi phân tích phản hồi từ Gemini.";
        }
    }

    // Parse Claude response
    public static String parseClaudeResponse(String jsonResponse) {
        try {
            Log.d(TAG, "Claude Raw response: " + jsonResponse);
            JSONObject obj = new JSONObject(jsonResponse);

            if (obj.has("error")) {
                return "Lỗi từ Claude: " + obj.getJSONObject("error").getString("message");
            }

            // Trích xuất văn bản từ phản hồi
            JSONObject content = obj.getJSONArray("content").getJSONObject(0);
            if (content.getString("type").equals("text")) {
                return content.getString("text");
            }

            return "Không có nội dung phản hồi từ Claude.";
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi phân tích phản hồi từ Claude", e);
            return "Lỗi khi phân tích phản hồi từ Claude.";
        }
    }

    // Parse response based on provider
    public static String parseResponse(String jsonResponse) {
        if (currentProvider == Provider.CLAUDE) {
            return parseClaudeResponse(jsonResponse);
        } else {
            return parseGeminiResponse(jsonResponse);
        }
    }
}
