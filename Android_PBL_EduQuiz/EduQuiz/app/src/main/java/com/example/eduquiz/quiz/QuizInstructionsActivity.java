package com.example.eduquiz.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityQuizInstructionsBinding;
import com.example.eduquiz.models.Question;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuizInstructionsActivity extends AppCompatActivity {

    private ActivityQuizInstructionsBinding binding;
    private Quiz quiz;
    private final List<Question> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizInstructionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        quiz = (Quiz) getIntent().getSerializableExtra("quiz");
        if (quiz == null) {
            Toast.makeText(this, "Quiz not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        loadQuestions();
        setupActions();
    }

    private void setupUI() {
        binding.tvSubjectBadge.setText(quiz.getSubjectName() != null ? quiz.getSubjectName() : "General");
        binding.tvQuizTitle.setText(quiz.getTitle());
        binding.tvQuizDescription.setText(quiz.getDescription());

        int timeLimit = quiz.getTimeLimitMinutes();
        if (timeLimit > 0) {
            binding.tvTimeLimit.setText(timeLimit + " Mins");
        } else {
            binding.tvTimeLimit.setText("Untimed");
        }
    }

    private void loadQuestions() {
        binding.progressbar.setVisibility(View.VISIBLE);
        binding.btnStartQuiz.setEnabled(false);

        FirebaseHelper.questionsRef().child(quiz.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressbar.setVisibility(View.GONE);
                questionList.clear();

                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    Question q = qSnap.getValue(Question.class);
                    if (q != null) {
                        q.setId(qSnap.getKey());
                        questionList.add(q);
                    }
                }

                binding.tvTotalQuestions.setText(String.valueOf(questionList.size()));

                if (questionList.isEmpty()) {
                    Toast.makeText(QuizInstructionsActivity.this, "This quiz has no questions yet.", Toast.LENGTH_SHORT).show();
                    binding.btnStartQuiz.setEnabled(false);
                } else {
                    binding.btnStartQuiz.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressbar.setVisibility(View.GONE);
                Toast.makeText(QuizInstructionsActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnStartQuiz.setOnClickListener(v -> {
            if (questionList.isEmpty()) {
                Toast.makeText(this, "No questions available to start.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("quiz", quiz);
            intent.putExtra("questions", (Serializable) questionList);
            startActivity(intent);
            finish();
        });
    }
}
