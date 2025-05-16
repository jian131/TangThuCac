package com.jian.tangthucac.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jian.tangthucac.R;
import com.jian.tangthucac.activities.LoginActivity;
import com.jian.tangthucac.databinding.FragmentProfileBinding;
import com.jian.tangthucac.models.User;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private User currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        setupUI();
        loadUserData();
    }

    private void setupUI() {
        // Nút đăng xuất
        binding.btnLogout.setOnClickListener(v -> logoutUser());

        // Nút chỉnh sửa thông tin
        binding.btnEditProfile.setOnClickListener(v -> openEditProfile());

        // Nút cài đặt ứng dụng
        binding.btnSettings.setOnClickListener(v -> openSettings());

        // Trạng thái ban đầu
        updateUIForLoginState();
    }

    private void updateUIForLoginState() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Đã đăng nhập
            binding.cardUserInfo.setVisibility(View.VISIBLE);
            binding.tvLoginPrompt.setVisibility(View.GONE);
            binding.btnLogin.setVisibility(View.GONE);
            binding.btnEditProfile.setVisibility(View.VISIBLE);
            binding.btnLogout.setVisibility(View.VISIBLE);
        } else {
            // Chưa đăng nhập
            binding.cardUserInfo.setVisibility(View.GONE);
            binding.tvLoginPrompt.setVisibility(View.VISIBLE);
            binding.btnLogin.setVisibility(View.VISIBLE);
            binding.btnEditProfile.setVisibility(View.GONE);
            binding.btnLogout.setVisibility(View.GONE);

            // Xử lý khi nhấn nút đăng nhập
            binding.btnLogin.setOnClickListener(v -> {
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivity(loginIntent);
            });
        }
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // Tải thông tin người dùng từ Firestore
        mFirestore.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);

                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        updateUIWithUserData();
                    } else {
                        // Trường hợp không tìm thấy thông tin người dùng trong Firestore
                        // nhưng đã xác thực với Firebase Auth
                        currentUser = new User(
                                firebaseUser.getUid(),
                                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Người dùng",
                                firebaseUser.getEmail(),
                                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : ""
                        );

                        // Lưu thông tin người dùng mới vào Firestore
                        saveUserToFirestore(currentUser);

                        updateUIWithUserData();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUIWithUserData() {
        if (currentUser == null || getContext() == null) {
            return;
        }

        // Hiển thị tên người dùng
        binding.tvUsername.setText(currentUser.getUsername());

        // Hiển thị email
        binding.tvEmail.setText(currentUser.getEmail());

        // Hiển thị số truyện yêu thích
        int favoriteCount = currentUser.getFavoriteNovels() != null ? currentUser.getFavoriteNovels().size() : 0;
        binding.tvFavoriteCount.setText(String.valueOf(favoriteCount));

        // Hiển thị số truyện đã đọc
        int historyCount = currentUser.getReadingHistory() != null ? currentUser.getReadingHistory().size() : 0;
        binding.tvHistoryCount.setText(String.valueOf(historyCount));

        // Tải ảnh đại diện
        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(getContext())
                    .load(currentUser.getAvatarUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.default_avatar);
        }
    }

    private void saveUserToFirestore(User user) {
        mFirestore.collection("users")
                .document(user.getUserId())
                .set(user)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        mAuth.signOut();

        // Cập nhật giao diện
        updateUIForLoginState();

        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    private void openEditProfile() {
        // TODO: Mở màn hình chỉnh sửa thông tin người dùng
        Toast.makeText(getContext(), "Chức năng chỉnh sửa thông tin đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        // TODO: Mở màn hình cài đặt ứng dụng
        Toast.makeText(getContext(), "Chức năng cài đặt đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
