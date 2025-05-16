package com.jian.tangthucac.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.chip.Chip;
import com.jian.tangthucac.R;
import com.jian.tangthucac.adapters.NovelAdapter;
import com.jian.tangthucac.databinding.FragmentSearchBinding;
import com.jian.tangthucac.models.Novel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private List<Novel> searchResults = new ArrayList<>();
    private NovelAdapter searchAdapter;

    // Danh sách các thể loại truyện
    private final String[] genres = {
        "Tiên Hiệp", "Huyền Huyễn", "Võ Hiệp", "Đô Thị", "Khoa Huyễn",
        "Kỳ Ảo", "Lịch Sử", "Đồng Nhân", "Ngôn Tình", "Quân Sự"
    };

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập SearchView
        setupSearchView();

        // Thiết lập RecyclerView kết quả tìm kiếm
        setupResultsRecyclerView();

        // Thiết lập các thể loại
        setupGenreChips();

        // Thiết lập các bộ lọc
        setupFilterButtons();
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Khi người dùng nhấn tìm kiếm
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Có thể sử dụng cho tìm kiếm tự động khi người dùng gõ
                // Nếu truyện được lưu cục bộ
                return false;
            }
        });
    }

    private void setupResultsRecyclerView() {
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        searchAdapter = new NovelAdapter(getContext(), searchResults, true);
        binding.rvSearchResults.setAdapter(searchAdapter);
    }

    private void setupGenreChips() {
        // Thêm các thể loại vào chipGroup
        for (String genre : genres) {
            Chip chip = new Chip(getContext());
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Xử lý khi chọn thể loại
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // TODO: Lọc truyện theo thể loại đã chọn
                Toast.makeText(getContext(), "Đã chọn thể loại: " + genre, Toast.LENGTH_SHORT).show();
            });

            binding.chipGroupGenres.addView(chip);
        }
    }

    private void setupFilterButtons() {
        // Xử lý khi người dùng nhấn vào nút sắp xếp
        binding.btnSort.setOnClickListener(v -> {
            // TODO: Hiển thị dialog chọn các tùy chọn sắp xếp
            Toast.makeText(getContext(), "Chức năng sắp xếp đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        // Xử lý khi người dùng nhấn vào nút lọc
        binding.btnFilter.setOnClickListener(v -> {
            // TODO: Hiển thị dialog chọn các tùy chọn lọc (trạng thái, số chương, v.v.)
            Toast.makeText(getContext(), "Chức năng lọc đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Phương thức thực hiện tìm kiếm
     * @param query Từ khóa tìm kiếm
     */
    private void performSearch(String query) {
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);

        // TODO: Thực hiện tìm kiếm thực sự bằng API
        // Hiện tại chỉ là mẫu giả lập

        // Giả lập tìm kiếm
        mockSearch(query);
    }

    /**
     * Phương thức giả lập kết quả tìm kiếm
     * Sẽ được thay thế bằng API thực trong giai đoạn sau
     */
    private void mockSearch(String query) {
        // Giả lập thời gian tìm kiếm
        new android.os.Handler().postDelayed(() -> {
            // Xóa kết quả cũ
            searchResults.clear();

            // Giả lập kết quả tìm kiếm
            if (query.toLowerCase().contains("tu") || query.toLowerCase().contains("tiên")) {
                searchResults.add(new Novel("101", "Tu Chân Liệt Truyện", "Mạc Mặc",
                        "https://example.com/image101.jpg", "Tu tiên", 4.6f, 1500));
                searchResults.add(new Novel("102", "Tiên Nghịch", "Nhĩ Căn",
                        "https://example.com/image102.jpg", "Tu tiên", 4.5f, 980));
                searchResults.add(new Novel("103", "Tiên Đạo Chủ", "Thịnh Giả Hành",
                        "https://example.com/image103.jpg", "Tu tiên", 4.3f, 850));
            } else if (query.toLowerCase().contains("huyền") || query.toLowerCase().contains("đấu")) {
                searchResults.add(new Novel("201", "Đấu Phá Thương Khung", "Thiên Tàm Thổ Đậu",
                        "https://example.com/image201.jpg", "Huyền huyễn", 4.8f, 1800));
                searchResults.add(new Novel("202", "Đấu La Đại Lục", "Đường Gia Tam Thiếu",
                        "https://example.com/image202.jpg", "Huyền huyễn", 4.7f, 1600));
            }

            // Hiển thị kết quả
            binding.progressBar.setVisibility(View.GONE);

            if (searchResults.isEmpty()) {
                binding.tvNoResults.setVisibility(View.VISIBLE);
                binding.tvNoResults.setText("Không tìm thấy kết quả cho: " + query);
            } else {
                binding.tvNoResults.setVisibility(View.GONE);
                searchAdapter.updateData(searchResults);
            }
        }, 1500);
    }

    /**
     * Phương thức để dịch từ khóa tìm kiếm từ tiếng Việt sang tiếng Trung
     * để sử dụng API tìm kiếm từ nguồn truyện Trung Quốc
     */
    private void translateAndSearch(String vietnameseKeyword) {
        // TODO: Sử dụng API dịch thuật để dịch từ khóa
        // và sau đó thực hiện tìm kiếm với từ khóa đã dịch
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
