
package com.example.eduquiz.quiz;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eduquiz.adapters.PerformanceAdapter;
import com.example.eduquiz.databinding.ActivityPerformanceBinding;
import com.example.eduquiz.databinding.ItemSubjectStatBinding;
import com.example.eduquiz.models.QuizAttempt;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceActivity extends AppCompatActivity {

    private static final String TAG = "PerformanceActivity";

    private ActivityPerformanceBinding binding;
    private PerformanceAdapter adapter;

    private final List<QuizAttempt> attemptList = new ArrayList<>();

    private ValueEventListener performanceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPerformanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadPerformanceData();
    }

    private void setupRecyclerView() {
        adapter = new PerformanceAdapter(this::showAttemptDetailsDialog);

        binding.attemptsRecycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.attemptsRecycler.setAdapter(adapter);
    }

    private void loadPerformanceData() {

        String uid = FirebaseHelper.currentUserId();

        if (uid == null || uid.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Please log in first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        binding.progressbar.setVisibility(View.VISIBLE);

        performanceListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                binding.progressbar.setVisibility(View.GONE);

                attemptList.clear();

                long totalQuestions = 0;
                long totalCorrect = 0;
                long totalWrong = 0;

                // Subject-wise statistics
                Map<String, int[]> subjectStats = new HashMap<>();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    QuizAttempt attempt;

                    try {
                        attempt = snap.getValue(QuizAttempt.class);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading attempt: " + snap.getKey(), e);
                        continue;
                    }

                    if (attempt == null) {
                        continue;
                    }

                    attempt.setId(snap.getKey());

                    // Prevent invalid values from affecting calculations
                    int questions = Math.max(0, attempt.getTotalQuestions());
                    int correct = Math.max(0, attempt.getCorrectAnswers());
                    int wrong = Math.max(0, attempt.getWrongAnswers());

                    // Validate the attempt
                    if (questions == 0 || correct > questions) {
                        Log.w(TAG, "Invalid attempt data: " + snap.getKey());
                        continue;
                    }

                    attemptList.add(attempt);

                    // Overall statistics
                    totalQuestions += questions;
                    totalCorrect += correct;
                    totalWrong += wrong;

                    // Subject-wise statistics
                    String subject = attempt.getSubjectName();

                    if (subject == null || subject.trim().isEmpty()) {
                        subject = "General";
                    }

                    int[] counts = subjectStats.get(subject);

                    if (counts == null) {
                        counts = new int[]{0, 0};
                        subjectStats.put(subject, counts);
                    }

                    counts[0] += correct;
                    counts[1] += questions;
                }

                // Sort newest attempts first
                Collections.sort(
                        attemptList,
                        (a, b) -> Long.compare(
                                b.getTimestamp(),
                                a.getTimestamp()
                        )
                );

                adapter.setAttempts(attemptList);

                // Empty state
                if (attemptList.isEmpty()) {

                    binding.tvNoAttempts.setVisibility(View.VISIBLE);
                    binding.attemptsRecycler.setVisibility(View.GONE);
                    binding.tvNoSubjectStats.setVisibility(View.VISIBLE);

                } else {

                    binding.tvNoAttempts.setVisibility(View.GONE);
                    binding.attemptsRecycler.setVisibility(View.VISIBLE);
                    binding.tvNoSubjectStats.setVisibility(View.GONE);
                }

                // Calculate overall accuracy using all questions
                int accuracy = 0;

                if (totalQuestions > 0) {

                    accuracy = (int) Math.round(
                            ((double) totalCorrect / totalQuestions) * 100
                    );
                }

                // Keep progress within 0-100
                accuracy = Math.max(0, Math.min(100, accuracy));

                // Update overall performance
                binding.tvOverallAccuracy.setText(accuracy + "%");

                binding.progressAccuracyCircle.setMax(100);
                binding.progressAccuracyCircle.setProgress(accuracy);

                binding.tvTotalQuestionsStat.setText(
                        String.valueOf(totalQuestions)
                );

                binding.tvCorrectStat.setText(
                        String.valueOf(totalCorrect)
                );

                binding.tvWrongStat.setText(
                        String.valueOf(totalWrong)
                );

                Log.d(TAG,
                        "Total Questions: " + totalQuestions +
                                ", Correct: " + totalCorrect +
                                ", Wrong: " + totalWrong +
                                ", Overall Accuracy: " + accuracy + "%"
                );

                // Update subject-wise progress
                populateSubjectWiseBreakdown(subjectStats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                binding.progressbar.setVisibility(View.GONE);

                Log.e(TAG,
                        "Firebase performance error: " + error.getMessage()
                );

                Toast.makeText(
                        PerformanceActivity.this,
                        "Failed to load: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        FirebaseHelper.attemptsRef()
                .child(uid)
                .addValueEventListener(performanceListener);
    }

    private void populateSubjectWiseBreakdown(
            Map<String, int[]> subjectStats
    ) {

        binding.layoutSubjectStatsContainer.removeAllViews();

        if (subjectStats.isEmpty()) {

            binding.tvNoSubjectStats.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvNoSubjectStats.setVisibility(View.GONE);

        for (Map.Entry<String, int[]> entry : subjectStats.entrySet()) {

            String subject = entry.getKey();

            int correct = entry.getValue()[0];
            int total = entry.getValue()[1];

            int percent = 0;

            if (total > 0) {
                percent = (int) Math.round(
                        ((double) correct / total) * 100
                );
            }

            ItemSubjectStatBinding statBinding =
                    ItemSubjectStatBinding.inflate(
                            LayoutInflater.from(this),
                            binding.layoutSubjectStatsContainer,
                            false
                    );

            statBinding.tvStatSubjectName.setText(subject);

            statBinding.tvStatSubjectPercent.setText(
                    percent + "% (" + correct + "/" + total + ")"
            );

            statBinding.progressStatSubject.setMax(100);
            statBinding.progressStatSubject.setProgress(percent);

            binding.layoutSubjectStatsContainer.addView(
                    statBinding.getRoot()
            );
        }
    }

    private void showAttemptDetailsDialog(QuizAttempt attempt) {

        String quizTitle = attempt.getQuizTitle() != null
                ? attempt.getQuizTitle()
                : "Quiz Result";

        String subjectName = attempt.getSubjectName() != null
                ? attempt.getSubjectName()
                : "General";

        new AlertDialog.Builder(this)
                .setTitle(quizTitle)
                .setMessage(
                        "Subject: " + subjectName +
                                "\nDate: " + FirebaseHelper.formatDate(
                                attempt.getTimestamp()
                        ) +
                                "\nScore: " + attempt.getCorrectAnswers() +
                                " / " + attempt.getTotalQuestions() +
                                "\nAccuracy: " + attempt.getScorePercentage() + "%" +
                                "\nWrong Answers: " + attempt.getWrongAnswers()
                )
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (performanceListener != null) {
            String uid = FirebaseHelper.currentUserId();

            if (uid != null) {
                FirebaseHelper.attemptsRef()
                        .child(uid)
                        .removeEventListener(performanceListener);
            }
        }
    }
}
