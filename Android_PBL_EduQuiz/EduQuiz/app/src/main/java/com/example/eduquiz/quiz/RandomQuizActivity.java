package com.example.eduquiz.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityRandomQuizBinding;
import com.example.eduquiz.models.Question;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RandomQuizActivity extends AppCompatActivity {

    private ActivityRandomQuizBinding binding;
    private final List<Question> pooledQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRandomQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        loadEligibleQuestions();
    }

    private void loadEligibleQuestions() {
        binding.progressbar.setVisibility(View.VISIBLE);
        binding.btnStartRandom.setEnabled(false);

        // Fetch all questions from Firebase
        FirebaseHelper.questionsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressbar.setVisibility(View.GONE);
                pooledQuestions.clear();
                Set<String> uniqueTexts = new HashSet<>();

                for (DataSnapshot quizSnap : snapshot.getChildren()) {
                    for (DataSnapshot qSnap : quizSnap.getChildren()) {
                        Question q = qSnap.getValue(Question.class);
                        if (q != null && q.getQuestionText() != null && !uniqueTexts.contains(q.getQuestionText())) {
                            q.setId(qSnap.getKey());
                            pooledQuestions.add(q);
                            uniqueTexts.add(q.getQuestionText());
                        }
                    }
                }

                if (pooledQuestions.isEmpty()) {
                    binding.tvAvailableCount.setText("No questions available yet");
                    binding.btnStartRandom.setEnabled(false);
                    Toast.makeText(RandomQuizActivity.this, "No quiz questions found in the database.", Toast.LENGTH_SHORT).show();
                } else {
                    int count = Math.min(10, pooledQuestions.size());
                    binding.tvAvailableCount.setText(count + " Random Questions");
                    binding.btnStartRandom.setEnabled(true);
                    setupStartButton(count);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressbar.setVisibility(View.GONE);
                Toast.makeText(RandomQuizActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStartButton(int count) {
        binding.btnStartRandom.setOnClickListener(v -> {
            // Shuffle pool to ensure random selection each time
            Collections.shuffle(pooledQuestions);

            List<Question> selectedQuestions = new ArrayList<>();
            for (int i = 0; i < count && i < pooledQuestions.size(); i++) {
                selectedQuestions.add(pooledQuestions.get(i));
            }

            Quiz randomQuiz = new Quiz(
                    "random_quiz",
                    "all",
                    "All Subjects",
                    "Random Quiz Challenge",
                    "Mixed questions picked randomly across all subjects",
                    selectedQuestions.size(),
                    5
            );

            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("quiz", randomQuiz);
            intent.putExtra("questions", (Serializable) selectedQuestions);
            startActivity(intent);
            finish();
        });
    }
}
