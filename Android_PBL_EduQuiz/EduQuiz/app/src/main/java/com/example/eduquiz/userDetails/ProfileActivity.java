package com.example.eduquiz.userDetails;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityProfileBinding;
import com.example.eduquiz.models.QuizAttempt;
import com.example.eduquiz.models.User;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser fbUser = FirebaseHelper.getAuth().getCurrentUser();
        if (fbUser == null) {
            finish();
            return;
        }

        String uid = fbUser.getUid();
        binding.tvProfileEmail.setText(fbUser.getEmail() != null ? fbUser.getEmail() : "");

        FirebaseHelper.usersRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        binding.tvProfileName.setText(user.getName());
                        if (user.getCreatedAt() > 0) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                            binding.tvMemberSince.setText(sdf.format(new Date(user.getCreatedAt())));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Load dynamic stats from attempts
        FirebaseHelper.attemptsRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuizAttempt> attempts = new ArrayList<>();
                int totalQ = 0;
                int totalCorrect = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    QuizAttempt a = snap.getValue(QuizAttempt.class);
                    if (a != null) {
                        attempts.add(a);
                        totalQ += a.getTotalQuestions();
                        totalCorrect += a.getCorrectAnswers();
                    }
                }

                binding.tvProfileQuizzes.setText(String.valueOf(attempts.size()));

                int accuracy = totalQ > 0 ? (int) Math.round(((double) totalCorrect / totalQ) * 100) : 0;
                binding.tvProfileAccuracy.setText(accuracy + "%");

                int streak = FirebaseHelper.calculateStreak(attempts);
                binding.tvProfileStreak.setText(streak + " 🔥");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out from QuizMaster?")
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
