package com.example.eduquiz.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityAddQuizBinding;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.models.Subject;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddQuizActivity extends AppCompatActivity {

    private ActivityAddQuizBinding binding;
    private final List<Subject> subjectList = new ArrayList<>();
    private final Integer[] questionCounts = {3, 5, 10, 15, 20};
    private final String[] timeLimitLabels = {"Untimed", "3 Minutes", "5 Minutes", "10 Minutes", "15 Minutes", "20 Minutes"};
    private final int[] timeLimitValues = {0, 3, 5, 10, 15, 20};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupStaticSpinners();
        loadSubjects();
        setupActions();
    }

    private void setupStaticSpinners() {
        ArrayAdapter<Integer> countAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, questionCounts);
        binding.spinnerQuestionCount.setAdapter(countAdapter);
        binding.spinnerQuestionCount.setSelection(1); // default 5

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, timeLimitLabels);
        binding.spinnerTimeLimit.setAdapter(timeAdapter);
        binding.spinnerTimeLimit.setSelection(2); // default 5 mins
    }

    private void loadSubjects() {
        binding.progressbar.setVisibility(View.VISIBLE);
        FirebaseHelper.subjectsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressbar.setVisibility(View.GONE);
                subjectList.clear();
                List<String> subjectNames = new ArrayList<>();

                for (DataSnapshot subSnap : snapshot.getChildren()) {
                    Subject sub = subSnap.getValue(Subject.class);
                    if (sub != null) {
                        sub.setId(subSnap.getKey());
                        subjectList.add(sub);
                        subjectNames.add(sub.getName());
                    }
                }

                if (subjectList.isEmpty()) {
                    Toast.makeText(AddQuizActivity.this, "No subjects found. Please add a subject first.", Toast.LENGTH_LONG).show();
                    binding.btnContinueToQuestions.setEnabled(false);
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddQuizActivity.this, android.R.layout.simple_spinner_dropdown_item, subjectNames);
                    binding.spinnerSubjects.setAdapter(adapter);
                    binding.btnContinueToQuestions.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressbar.setVisibility(View.GONE);
                Toast.makeText(AddQuizActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupActions() {
        binding.btnContinueToQuestions.setOnClickListener(v -> {
            if (subjectList.isEmpty()) {
                Toast.makeText(this, "Please add a subject first", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedSubjectPos = binding.spinnerSubjects.getSelectedItemPosition();
            if (selectedSubjectPos < 0 || selectedSubjectPos >= subjectList.size()) {
                Toast.makeText(this, "Please select a valid subject", Toast.LENGTH_SHORT).show();
                return;
            }

            Subject selectedSubject = subjectList.get(selectedSubjectPos);

            String title = binding.etQuizTitle.getText().toString().trim();
            String description = binding.etQuizDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                binding.etQuizTitle.setError("Quiz title is required");
                binding.etQuizTitle.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(description)) {
                binding.etQuizDescription.setError("Quiz description is required");
                binding.etQuizDescription.requestFocus();
                return;
            }

            int questionCount = questionCounts[binding.spinnerQuestionCount.getSelectedItemPosition()];
            int timeLimit = timeLimitValues[binding.spinnerTimeLimit.getSelectedItemPosition()];

            String quizId = FirebaseHelper.quizzesRef().push().getKey();

            Quiz newQuiz = new Quiz(
                    quizId,
                    selectedSubject.getId(),
                    selectedSubject.getName(),
                    title,
                    description,
                    questionCount,
                    timeLimit
            );

            Intent intent = new Intent(this, AddQuestionsActivity.class);
            intent.putExtra("quiz", newQuiz);
            startActivity(intent);
            finish();
        });
    }
}
