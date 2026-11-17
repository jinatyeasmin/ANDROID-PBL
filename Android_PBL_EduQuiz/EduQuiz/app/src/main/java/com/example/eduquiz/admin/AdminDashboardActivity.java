package com.example.eduquiz.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityAdminDashboardBinding;
import com.example.eduquiz.userDetails.LoginActivity;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupClickListeners();
        loadStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void setupClickListeners() {
        binding.btnAdminLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        binding.cardAddSubject.setOnClickListener(v -> {
            startActivity(new Intent(this, AddSubjectActivity.class));
        });

        binding.cardAddQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, AddQuizActivity.class));
        });

        binding.cardManageContent.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageContentActivity.class));
        });

        binding.cardStudentFeedback.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminFeedbackActivity.class));
        });
    }

    private void loadStatistics() {
        // 1. Total Registered Students
        FirebaseHelper.usersRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long studentCount = snapshot.getChildrenCount();
                binding.tvTotalStudents.setText(String.valueOf(studentCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // 2. Total Subjects
        FirebaseHelper.subjectsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long subjectsCount = snapshot.getChildrenCount();
                binding.tvTotalSubjectsCount.setText("Subjects: " + subjectsCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // 3. Total Quizzes
        FirebaseHelper.quizzesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int publishedCount = 0;
                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    Boolean published = qSnap.child("published").getValue(Boolean.class);
                    if (published != null && published) {
                        publishedCount++;
                    }
                }
                binding.tvTotalQuizzesCount.setText("Published Quizzes: " + publishedCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // 4. Feedback count
        FirebaseHelper.feedbackRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (count > 0) {
                    binding.tvFeedbackCountSubtext.setText(count + " reviews received from students");
                } else {
                    binding.tvFeedbackCountSubtext.setText("Review ratings and suggestions from students");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Admin Panel")
                .setMessage("Are you sure you want to sign out from the Admin Dashboard?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseHelper.getAuth().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
