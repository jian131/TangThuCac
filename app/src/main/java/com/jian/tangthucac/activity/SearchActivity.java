package com.jian.tangthucac.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jian.tangthucac.activity.china.SearchActivity;

/**
 * Activity điều hướng đến trang tìm kiếm mới
 * Được giữ lại để tương thích với các thành phần cũ
 */
public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chuyển hướng đến trang tìm kiếm mới
        Intent intent = new Intent(this, com.jian.tangthucac.activity.china.SearchActivity.class);
        // Giữ lại các tham số tìm kiếm nếu có
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }

        startActivity(intent);
        finish();
    }
}
