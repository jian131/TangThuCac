package com.jian.tangthucac.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jian.tangthucac.adapters.NovelAdapter;
import com.jian.tangthucac.databinding.FragmentHomeBinding;
import com.jian.tangthucac.models.Novel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;

    // Danh sách truyện mẫu (sẽ được thay thế bằng dữ liệu từ API trong giai đoạn sau)
    private List<Novel> popularNovels = new ArrayList<>();
    private List<Novel> recentNovels = new ArrayList<>();
    private List<Novel> recommendedNovels = new ArrayList<>();

    // Adapters
    private NovelAdapter popularAdapter;
    private NovelAdapter recentAdapter;
    private NovelAdapter recommendedAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Hiển thị tên người dùng
        if (currentUser != null) {
            String welcomeMessage = "Xin chào, " + currentUser.getDisplayName();
            binding.tvWelcome.setText(welcomeMessage);
        }

        // Khởi tạo dữ liệu mẫu
        initSampleData();

        // Thiết lập RecyclerViews
        setupRecyclerViews();

        // Xử lý sự kiện refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void initSampleData() {
        // Khởi tạo dữ liệu mẫu cho danh sách truyện
        // Trong giai đoạn sau, dữ liệu này sẽ được lấy từ API

        // Truyện phổ biến
        popularNovels.add(new Novel("1", "Đấu La Đại Lục", "Đường Gia Tam Thiếu",
                "https://example.com/image1.jpg", "Huyền huyễn", 4.5f, 1000));
        popularNovels.add(new Novel("2", "Vũ Động Càn Khôn", "Thiên Tàm Thổ Đậu",
                "https://example.com/image2.jpg", "Tu tiên", 4.7f, 1200));
        popularNovels.add(new Novel("3", "Tuyệt Thế Võ Thần", "Đông Phương Vũ",
                "https://example.com/image3.jpg", "Võ hiệp", 4.3f, 950));

        // Truyện mới cập nhật
        recentNovels.add(new Novel("4", "Thần Đạo Đan Tôn", "Cô Đơn Địa Phi",
                "https://example.com/image4.jpg", "Tu tiên", 4.4f, 850));
        recentNovels.add(new Novel("5", "Bàn Long", "Ngã Cật Tây Hồng Thị",
                "https://example.com/image5.jpg", "Huyền huyễn", 4.6f, 1100));
        recentNovels.add(new Novel("6", "Phàm Nhân Tu Tiên", "Vong Ngữ",
                "https://example.com/image6.jpg", "Tu tiên", 4.8f, 1300));

        // Truyện đề xuất
        recommendedNovels.add(new Novel("7", "Tiên Nghịch", "Nhĩ Căn",
                "https://example.com/image7.jpg", "Tu tiên", 4.5f, 980));
        recommendedNovels.add(new Novel("8", "Đế Tôn", "Liệp Phi Sắc",
                "https://example.com/image8.jpg", "Huyền huyễn", 4.2f, 800));
        recommendedNovels.add(new Novel("9", "Ngã Dục Phong Thiên", "Nhĩ Căn",
                "https://example.com/image9.jpg", "Tu tiên", 4.4f, 920));
    }

    private void setupRecyclerViews() {
        // Thiết lập adapter và layout manager cho các RecyclerView

        // Truyện phổ biến
        binding.rvPopularNovels.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = new NovelAdapter(getContext(), popularNovels, false);
        binding.rvPopularNovels.setAdapter(popularAdapter);

        // Truyện mới cập nhật
        binding.rvRecentNovels.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        recentAdapter = new NovelAdapter(getContext(), recentNovels, false);
        binding.rvRecentNovels.setAdapter(recentAdapter);

        // Truyện đề xuất
        binding.rvRecommendedNovels.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new NovelAdapter(getContext(), recommendedNovels, false);
        binding.rvRecommendedNovels.setAdapter(recommendedAdapter);
    }

    private void refreshData() {
        // Giả lập tải lại dữ liệu
        // Trong giai đoạn sau, phương thức này sẽ gọi API để lấy dữ liệu mới

        // Giả lập thời gian tải
        new android.os.Handler().postDelayed(() -> {
            // Cập nhật adapters
            popularAdapter.notifyDataSetChanged();
            recentAdapter.notifyDataSetChanged();
            recommendedAdapter.notifyDataSetChanged();

            // Kết thúc hiệu ứng refresh
            binding.swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Đã cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
        }, 1500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
