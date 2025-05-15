package com.jian.tangthucac.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;
import com.jian.tangthucac.R;
import com.jian.tangthucac.databinding.ActivityApiSettingsBinding;

/**
 * Activity để quản lý cài đặt API keys
 */
public class ApiSettingsActivity extends AppCompatActivity {

    private ActivityApiSettingsBinding binding;
    private SharedPreferences sharedPreferences;

    // URLs để đăng ký API keys
    private static final String CLAUDE_SIGNUP_URL = "https://www.anthropic.com/";
    private static final String DEEPL_SIGNUP_URL = "https://www.deepl.com/pro-api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApiSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Khởi tạo SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Tải API keys đã lưu
        loadSavedApiKeys();

        // Thiết lập listeners
        setupListeners();
    }

    private void loadSavedApiKeys() {
        String claudeApiKey = sharedPreferences.getString("claude_api_key", "");
        String deeplApiKey = sharedPreferences.getString("deepl_api_key", "");

        binding.claudeApiKeyEditText.setText(claudeApiKey);
        binding.deeplApiKeyEditText.setText(deeplApiKey);

        updateApiStatus();
    }

    private void setupListeners() {
        // Button đăng ký Claude API
        binding.getClaudeApiKeyButton.setOnClickListener(v -> {
            openWebPage(CLAUDE_SIGNUP_URL);
        });

        // Button đăng ký DeepL API
        binding.getDeeplApiKeyButton.setOnClickListener(v -> {
            openWebPage(DEEPL_SIGNUP_URL);
        });

        // Button lưu API keys
        binding.saveApiKeysButton.setOnClickListener(v -> {
            saveApiKeys();
        });
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không thể mở trang web", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveApiKeys() {
        String claudeApiKey = binding.claudeApiKeyEditText.getText().toString().trim();
        String deeplApiKey = binding.deeplApiKeyEditText.getText().toString().trim();

        // Lưu vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("claude_api_key", claudeApiKey);
        editor.putString("deepl_api_key", deeplApiKey);
        editor.apply();

        // Hiển thị thông báo
        Toast.makeText(this, "Đã lưu API keys", Toast.LENGTH_SHORT).show();

        // Cập nhật trạng thái
        updateApiStatus();
    }

    private void updateApiStatus() {
        String claudeApiKey = binding.claudeApiKeyEditText.getText().toString().trim();
        String deeplApiKey = binding.deeplApiKeyEditText.getText().toString().trim();

        StringBuilder status = new StringBuilder();

        if (!TextUtils.isEmpty(claudeApiKey)) {
            status.append("✅ Claude API: Đã thiết lập");
        } else {
            status.append("❌ Claude API: Chưa thiết lập");
        }

        status.append("\n");

        if (!TextUtils.isEmpty(deeplApiKey)) {
            status.append("✅ DeepL API: Đã thiết lập");
        } else {
            status.append("❌ DeepL API: Chưa thiết lập");
        }

        binding.apiStatusTextView.setText(status.toString());

        // Hiển thị trạng thái API
        binding.apiStatusTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
