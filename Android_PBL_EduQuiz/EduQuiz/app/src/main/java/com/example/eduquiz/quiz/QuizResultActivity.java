package com.example.eduquiz.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.MainActivity;
import com.example.eduquiz.R;
import com.example.eduquiz.databinding.ActivityQuizResultBinding;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.models.QuizAttempt;
import com.example.eduquiz.models.User;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuizResultActivity extends AppCompatActivity {

    private ActivityQuizResultBinding binding;
    private Quiz quiz;
    private int score = 0;
    private int totalQuestions = 0;
    private int wrongCount = 0;
    private int percentage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        quiz = (Quiz) getIntent().getSerializableExtra("quiz");
        score = getIntent().getIntExtra("score", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 0);
        wrongCount = getIntent().getIntExtra("wrong_count", 0);
        percentage = getIntent().getIntExtra("percentage", 0);

        displayResults();
        saveAttemptToFirebase();
        setupActions();
    }

    private void displayResults() {
        if (quiz != null) {
            String subject = quiz.getSubjectName() != null ? quiz.getSubjectName() : "General";
            String title = quiz.getTitle() != null ? quiz.getTitle() : "Quiz";
            binding.tvQuizSubject.setText(subject + " • " + title);
        } else {
            binding.tvQuizSubject.setText("Random Quiz Challenge");
        }

        binding.tvScorePercentage.setText(percentage + "%");
        binding.tvCorrectCount.setText(String.valueOf(score));
        binding.tvWrongCount.setText(String.valueOf(wrongCount));
        binding.tvTotalCount.setText(String.valueOf(totalQuestions));

        // Color coding for score
        if (percentage >= 80) {
            binding.tvScorePercentage.setTextColor(getColor(R.color.success));
        } else if (percentage >= 50) {
            binding.tvScorePercentage.setTextColor(getColor(R.color.primary));
        } else {
            binding.tvScorePercentage.setTextColor(getColor(R.color.error));
        }
    }

    private void saveAttemptToFirebase() {
        String uid = FirebaseHelper.currentUserId();
        if (uid == null) return;

        DatabaseReference userAttemptsRef = FirebaseHelper.attemptsRef().child(uid);
        String attemptId = userAttemptsRef.push().getKey();
        if (attemptId == null) return;

        String quizId = quiz != null ? quiz.getId() : "random_quiz";
        String quizTitle = quiz != null ? quiz.getTitle() : "Random Quiz";
        String subjectId = quiz != null ? quiz.getSubjectId() : "all";
        String subjectName = quiz != null ? quiz.getSubjectName() : "All Subjects";
        boolean isRandom = (quiz == null || "random_quiz".equals(quiz.getId()));

        QuizAttempt attempt = new QuizAttempt(
                attemptId,
                uid,
                "Student",
                quizId,
                quizTitle,
                subjectId,
                subjectName,
                totalQuestions,
                score,
                wrongCount,
                percentage,
                System.currentTimeMillis(),
                FirebaseHelper.getTodayDateString(),
                isRandom
        );

        userAttemptsRef.child(attemptId).setValue(attempt).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateUserStats(uid);
            }
        });
    }

    private void updateUserStats(String uid) {
        // Fetch all attempts for this user to recalculate totals and streaks accurately
        FirebaseHelper.attemptsRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuizAttempt> allAttempts = new ArrayList<>();
                int totalAttemptsCount = 0;
                int totalQ = 0;
                int totalCorrect = 0;
                int totalWrong = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    QuizAttempt a = snap.getValue(QuizAttempt.class);
                    if (a != null) {
                        allAttempts.add(a);
                        totalAttemptsCount++;
                        totalQ += a.getTotalQuestions();
                        totalCorrect += a.getCorrectAnswers();
                        totalWrong += a.getWrongAnswers();
                    }
                }

                int streak = FirebaseHelper.calculateStreak(allAttempts);

                DatabaseReference userRef = FirebaseHelper.usersRef().child(uid);
                userRef.child("totalAttempts").setValue(totalAttemptsCount);
                userRef.child("totalQuestions").setValue(totalQ);
                userRef.child("totalCorrect").setValue(totalCorrect);
                userRef.child("totalWrong").setValue(totalWrong);
                userRef.child("streak").setValue(streak);
                userRef.child("lastActiveDate").setValue(FirebaseHelper.getTodayDateString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupActions() {
        binding.btnViewPerformance.setOnClickListener(v -> {
            Intent intent = new Intent(this, PerformanceActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnTryAgain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
