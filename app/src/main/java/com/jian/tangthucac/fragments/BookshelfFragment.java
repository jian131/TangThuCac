package com.jian.tangthucac.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jian.tangthucac.R;
import com.jian.tangthucac.adapters.NovelAdapter;
import com.jian.tangthucac.databinding.FragmentBookshelfBinding;
import com.jian.tangthucac.models.Novel;
import com.jian.tangthucac.models.User;

import java.util.ArrayList;
import java.util.List;

public class BookshelfFragment extends Fragment {

    private FragmentBookshelfBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private List<Novel> favoriteNovels = new ArrayList<>();
    private List<Novel> historyNovels = new ArrayList<>();
    private NovelAdapter novelAdapter;

    // Trạng thái tab hiện tại
    private int currentTab = 0; // 0: Yêu thích, 1: Lịch sử đọc

    public BookshelfFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookshelfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Thiết lập TabLayout
        setupTabLayout();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Tải dữ liệu
        loadUserData();

        // Thiết lập SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateBookshelfDisplay();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
    }

    private void setupRecyclerView() {
        binding.rvNovels.setLayoutManager(new GridLayoutManager(getContext(), 2));
        novelAdapter = new NovelAdapter(getContext(), new ArrayList<>(), true);
        binding.rvNovels.setAdapter(novelAdapter);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Người dùng chưa đăng nhập
            binding.tvEmptyBookshelf.setVisibility(View.VISIBLE);
            binding.tvEmptyBookshelf.setText(R.string.login_required);
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyBookshelf.setVisibility(View.GONE);

        // Tải thông tin người dùng từ Firestore
        mFirestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        // Tải danh sách truyện yêu thích
                        loadFavoriteNovels(user.getFavoriteNovels());

                        // Tải lịch sử đọc truyện
                        loadHistoryNovels(user.getReadingHistory());
                    } else {
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Tải danh sách truyện yêu thích dựa trên danh sách ID
     */
    private void loadFavoriteNovels(List<String> favoriteIds) {
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            updateBookshelfDisplay();
            return;
        }

        // Xóa danh sách cũ
        favoriteNovels.clear();

        // TODO: Tải thông tin truyện từ Firestore hoặc API dựa trên danh sách ID

        // Giả lập dữ liệu cho giai đoạn phát triển
        mockFavoriteNovels();

        updateBookshelfDisplay();
    }

    /**
     * Tải lịch sử đọc truyện dựa trên danh sách ID
     */
    private void loadHistoryNovels(List<String> historyIds) {
        if (historyIds == null || historyIds.isEmpty()) {
            updateBookshelfDisplay();
            return;
        }

        // Xóa danh sách cũ
        historyNovels.clear();

        // TODO: Tải thông tin truyện từ Firestore hoặc API dựa trên danh sách ID

        // Giả lập dữ liệu cho giai đoạn phát triển
        mockHistoryNovels();

        updateBookshelfDisplay();
    }

    /**
     * Giả lập dữ liệu truyện yêu thích
     */
    private void mockFavoriteNovels() {
        favoriteNovels.add(new Novel("1", "Đấu La Đại Lục", "Đường Gia Tam Thiếu",
                "https://example.com/image1.jpg", "Huyền huyễn", 4.5f, 1000));
        favoriteNovels.add(new Novel("2", "Vũ Động Càn Khôn", "Thiên Tàm Thổ Đậu",
                "https://example.com/image2.jpg", "Tu tiên", 4.7f, 1200));
    }

    /**
     * Giả lập dữ liệu lịch sử đọc
     */
    private void mockHistoryNovels() {
        historyNovels.add(new Novel("3", "Tuyệt Thế Võ Thần", "Đông Phương Vũ",
                "https://example.com/image3.jpg", "Võ hiệp", 4.3f, 950));
        historyNovels.add(new Novel("4", "Thần Đạo Đan Tôn", "Cô Đơn Địa Phi",
                "https://example.com/image4.jpg", "Tu tiên", 4.4f, 850));
        historyNovels.add(new Novel("5", "Bàn Long", "Ngã Cật Tây Hồng Thị",
                "https://example.com/image5.jpg", "Huyền huyễn", 4.6f, 1100));
    }

    /**
     * Cập nhật hiển thị tủ truyện dựa trên tab đang chọn
     */
    private void updateBookshelfDisplay() {
        binding.progressBar.setVisibility(View.GONE);

        List<Novel> novelsToDisplay = currentTab == 0 ? favoriteNovels : historyNovels;

        if (novelsToDisplay.isEmpty()) {
            binding.tvEmptyBookshelf.setVisibility(View.VISIBLE);
            binding.tvEmptyBookshelf.setText(currentTab == 0 ?
                    R.string.empty_favorites : R.string.empty_history);
            binding.rvNovels.setVisibility(View.GONE);
        } else {
            binding.tvEmptyBookshelf.setVisibility(View.GONE);
            binding.rvNovels.setVisibility(View.VISIBLE);
            novelAdapter.updateData(novelsToDisplay);
        }
    }

    /**
     * Hiển thị trạng thái rỗng
     */
    private void showEmptyState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.tvEmptyBookshelf.setVisibility(View.VISIBLE);
        binding.tvEmptyBookshelf.setText(currentTab == 0 ?
                R.string.empty_favorites : R.string.empty_history);
        binding.rvNovels.setVisibility(View.GONE);
    }

    /**
     * Làm mới dữ liệu
     */
    private void refreshData() {
        loadUserData();

        // Kết thúc hiệu ứng refresh sau một thời gian
        new android.os.Handler().postDelayed(() -> {
            if (binding != null) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
