package com.example.eduquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityMainBinding;
import com.example.eduquiz.models.QuizAttempt;
import com.example.eduquiz.models.User;
import com.example.eduquiz.quiz.PerformanceActivity;
import com.example.eduquiz.quiz.QuizListActivity;
import com.example.eduquiz.quiz.RandomQuizActivity;
import com.example.eduquiz.userDetails.FeedbackActivity;
import com.example.eduquiz.userDetails.LoginActivity;
import com.example.eduquiz.userDetails.ProfileActivity;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseHelper.getAuth().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUid = currentUser.getUid();

        setupClickListeners();
        loadUserData();
        loadStudentStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUid != null) {
            loadStudentStatistics();
        }
    }

    private void setupClickListeners() {
        // Quick access cards
        binding.cardSubjectQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, QuizListActivity.class));
        });

        binding.cardRandomQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, RandomQuizActivity.class));
        });

        binding.cardPerformance.setOnClickListener(v -> {
            startActivity(new Intent(this, PerformanceActivity.class));
        });

        binding.cardFeedback.setOnClickListener(v -> {
            startActivity(new Intent(this, FeedbackActivity.class));
        });

        binding.cardStreak.setOnClickListener(v -> {
            Toast.makeText(this, "Daily Streak: Complete at least 1 quiz each day to maintain it!", Toast.LENGTH_SHORT).show();
        });

        // Top actions
        binding.btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        binding.btnNotification.setOnClickListener(v -> {
            Toast.makeText(this, "No new notifications right now.", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        binding.navQuizzes.setOnClickListener(v -> {
            startActivity(new Intent(this, QuizListActivity.class));
        });

        binding.navPerformance.setOnClickListener(v -> {
            startActivity(new Intent(this, PerformanceActivity.class));
        });

        binding.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void loadUserData() {
        FirebaseHelper.usersRef().child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getName() != null) {
                        binding.tvUserName.setText(user.getName() + " 👋");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadStudentStatistics() {
        FirebaseHelper.attemptsRef().child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuizAttempt> attempts = new ArrayList<>();
                int totalQuestionsAttempted = 0;
                int totalCorrectAnswers = 0;

                for (DataSnapshot attemptSnap : snapshot.getChildren()) {
                    QuizAttempt attempt = attemptSnap.getValue(QuizAttempt.class);
                    if (attempt != null) {
                        attempts.add(attempt);
                        totalQuestionsAttempted += attempt.getTotalQuestions();
                        totalCorrectAnswers += attempt.getCorrectAnswers();
                    }
                }

                // 1. Total attempts
                int totalAttemptsCount = attempts.size();
                binding.tvTotalAttempts.setText(String.valueOf(totalAttemptsCount));

                // 2. Accuracy % = (Correct / Total Questions) * 100
                int accuracyPercent = 0;
                if (totalQuestionsAttempted > 0) {
                    accuracyPercent = (int) Math.round(((double) totalCorrectAnswers / totalQuestionsAttempted) * 100);
                }
                binding.tvAccuracyPercent.setText(accuracyPercent + "%");

                // 3. Streak calculation
                int streakDays = FirebaseHelper.calculateStreak(attempts);
                binding.tvStreakDays.setText(streakDays + (streakDays == 1 ? " Day Streak" : " Days Streak"));

                if (streakDays > 0) {
                    binding.tvStreakSubtext.setText("Awesome consistency! Keep your momentum going 🔥");
                } else {
                    binding.tvStreakSubtext.setText("Attempt a quiz today to start your streak!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
