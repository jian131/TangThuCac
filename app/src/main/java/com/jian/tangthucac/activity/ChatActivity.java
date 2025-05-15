package com.jian.tangthucac.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.tangthucac.OpenAIHelper;
import com.jian.tangthucac.R;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.jian.tangthucac.adapter.ChatAdapter;
import com.jian.tangthucac.model.Message;

import okhttp3.*;
import org.jetbrains.annotations.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecycler;
    private EditText edtMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecycler = findViewById(R.id.chatRecycler);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        adapter = new ChatAdapter(messageList);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(adapter);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Hiển thị model đang dùng
        updateProviderTitle();

        btnSend.setOnClickListener(v -> {
            String question = edtMessage.getText().toString();
            if (!question.isEmpty()) {
                addMessage(new Message(question, "user"));
                progressBar.setVisibility(View.VISIBLE);

                OpenAIHelper.sendQuestion(question, new Callback() {
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            String answer = OpenAIHelper.parseResponse(response.body().string());
                            runOnUiThread(() -> {
                                addMessage(new Message(answer, "bot"));
                                progressBar.setVisibility(View.GONE);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                addMessage(new Message("Lỗi kết nối: " + e.getMessage(), "bot"));
                                progressBar.setVisibility(View.GONE);
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            addMessage(new Message("Lỗi kết nối: " + e.getMessage(), "bot"));
                            progressBar.setVisibility(View.GONE);
                        });
                    }
                });
                edtMessage.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_change_model) {
            showChangeModelDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChangeModelDialog() {
        String[] models = {"Gemini 2.0 Flash", "Claude 3 Haiku"};
        int currentSelected = OpenAIHelper.getCurrentProvider() == OpenAIHelper.Provider.GEMINI ? 0 : 1;

        new AlertDialog.Builder(this)
            .setTitle("Chọn mô hình AI")
            .setSingleChoiceItems(models, currentSelected, (dialog, which) -> {
                if (which == 0) {
                    OpenAIHelper.setProvider(OpenAIHelper.Provider.GEMINI);
                    addMessage(new Message("Đã chuyển sang sử dụng Gemini 2.0 Flash", "bot"));
                } else {
                    if (OpenAIHelper.getCurrentProvider() == OpenAIHelper.Provider.CLAUDE) {
                        OpenAIHelper.setProvider(OpenAIHelper.Provider.CLAUDE);
                        addMessage(new Message("Đã chuyển sang sử dụng Claude 3 Haiku", "bot"));
                    } else {
                        new AlertDialog.Builder(this)
                            .setTitle("Claude API Key")
                            .setMessage("Bạn cần thêm API key cho Claude trong OpenAIHelper.java trước khi sử dụng. Hiện tại sẽ tiếp tục sử dụng Gemini.")
                            .setPositiveButton("OK", null)
                            .show();
                    }
                }
                updateProviderTitle();
                dialog.dismiss();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void updateProviderTitle() {
        String model = OpenAIHelper.getCurrentProvider() == OpenAIHelper.Provider.GEMINI ?
            "Gemini 2.0 Flash" : "Claude 3 Haiku";
        if (getSupportActionBar() != null) {
            TextView titleView = findViewById(R.id.tvChatTitle);
            if (titleView != null) {
