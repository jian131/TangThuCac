package com.jian.tangthucac.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.tangthucac.API.ChineseNovelManager;
import com.jian.tangthucac.R;
import com.jian.tangthucac.activity.ChineseNovelSearchActivity;
import com.jian.tangthucac.activity.StoryDetailActivity;
import com.jian.tangthucac.adapter.ChapterAdapter;
import com.jian.tangthucac.adapter.SearchResultAdapter;
import com.jian.tangthucac.model.OriginalStory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment hiển thị tính năng Truyện Trung Quốc
 */
public class ChineseNovelFragment extends Fragment {

    private RecyclerView popularNovelsRecyclerView;
    private RecyclerView recentNovelsRecyclerView;
    private LinearLayout emptyStateLayout;
    private Button searchButton;
    private Button historyButton;
    private TextView emptyStateText;

    private ChineseNovelManager novelManager;
    private SearchResultAdapter popularAdapter;
    private SearchResultAdapter recentAdapter;

    private final List<OriginalStory> popularNovels = new ArrayList<>();
    private final List<OriginalStory> recentNovels = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chinese_novel, container, false);

        // Khởi tạo views
        popularNovelsRecyclerView = view.findViewById(R.id.popularNovelsRecyclerView);
        recentNovelsRecyclerView = view.findViewById(R.id.recentNovelsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        searchButton = view.findViewById(R.id.searchButton);
        historyButton = view.findViewById(R.id.historyButton);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Khởi tạo manager
        novelManager = ChineseNovelManager.getInstance();
        novelManager.initialize(getContext());

        // Thiết lập RecyclerViews
        setupRecyclerViews();

        // Thiết lập button listeners
        setupButtons();

        // Tải dữ liệu
        loadData();

        return view;
    }

    private void setupRecyclerViews() {
        // Thiết lập RecyclerView cho truyện phổ biến
        popularAdapter = new SearchResultAdapter(getContext(), popularNovels);
        popularNovelsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        popularNovelsRecyclerView.setAdapter(popularAdapter);

        // Thiết lập click listener
        popularAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onViewDetailClick(OriginalStory story) {
                openStoryDetail(story);
            }

            @Override
            public void onDownloadClick(OriginalStory story) {
                downloadStory(story);
            }
        });

        // Thiết lập RecyclerView cho truyện gần đây
        recentAdapter = new SearchResultAdapter(getContext(), recentNovels);
        recentNovelsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentNovelsRecyclerView.setAdapter(recentAdapter);

        // Thiết lập click listener
        recentAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onViewDetailClick(OriginalStory story) {
                openStoryDetail(story);
            }

            @Override
            public void onDownloadClick(OriginalStory story) {
                downloadStory(story);
            }
        });
    }

    private void setupButtons() {
        // Button tìm kiếm truyện
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChineseNovelSearchActivity.class);
            startActivity(intent);
        });

        // Button xem lịch sử
        historyButton.setOnClickListener(v -> {
            // TODO: Mở activity lịch sử đọc truyện
        });
    }

    private void loadData() {
        // Tải truyện phổ biến
        novelManager.getOriginalStories(new ChineseNovelManager.OnStoriesLoadedListener() {
            @Override
            public void onStoriesLoaded(List<OriginalStory> stories) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        popularNovels.clear();
                        if (stories != null && !stories.isEmpty()) {
                            // Lấy tối đa 10 truyện cho danh sách phổ biến
                            popularNovels.addAll(stories.subList(0, Math.min(10, stories.size())));
                        }
                        popularAdapter.notifyDataSetChanged();

                        updateEmptyState();
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // Xử lý lỗi
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(ChineseNovelFragment.this::updateEmptyState);
                }
            }
        });

        // Tải truyện gần đây
        // Tạm thời dùng chung danh sách với truyện phổ biến nhưng sắp xếp khác
        novelManager.getOriginalStories(new ChineseNovelManager.OnStoriesLoadedListener() {
            @Override
            public void onStoriesLoaded(List<OriginalStory> stories) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        recentNovels.clear();
                        if (stories != null && !stories.isEmpty()) {
                            // Đảo ngược danh sách để có thứ tự khác với truyện phổ biến
                            List<OriginalStory> reversedList = new ArrayList<>(stories);
                            Collections.reverse(reversedList);
                            // Lấy tối đa 10 truyện cho danh sách gần đây
                            recentNovels.addAll(reversedList.subList(0, Math.min(10, reversedList.size())));
                        }
                        recentAdapter.notifyDataSetChanged();

                        updateEmptyState();
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // Xử lý lỗi
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(ChineseNovelFragment.this::updateEmptyState);
                }
            }
        });
    }

    private void updateEmptyState() {
        // Kiểm tra nếu không có dữ liệu
        boolean isEmpty = popularNovels.isEmpty() && recentNovels.isEmpty();

        // Hiển thị hoặc ẩn trạng thái trống
        if (isEmpty) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            popularNovelsRecyclerView.setVisibility(View.GONE);
            recentNovelsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            popularNovelsRecyclerView.setVisibility(View.VISIBLE);
            recentNovelsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openStoryDetail(OriginalStory story) {
        if (story != null && story.getId() != null) {
            StoryDetailActivity.start(getContext(), story.getId());
        } else {
            Toast.makeText(getContext(), "Không thể mở truyện, thiếu thông tin ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadStory(OriginalStory story) {
        // TODO: Triển khai tính năng tải xuống truyện
        Toast.makeText(getContext(), "Tính năng tải xuống đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại fragment
        loadData();
    }
}
