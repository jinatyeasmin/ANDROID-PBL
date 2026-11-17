package com.example.eduquiz.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eduquiz.adapters.SubjectAdapter;
import com.example.eduquiz.databinding.ActivityQuizListBinding;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.models.Subject;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuizListActivity extends AppCompatActivity {

    private ActivityQuizListBinding binding;
    private SubjectAdapter adapter;
    private final List<Subject> allSubjects = new ArrayList<>();
    private final List<Subject> filteredSubjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearch();
        loadSubjects();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSearchToggle.setOnClickListener(v -> {
            binding.etSearch.requestFocus();
        });
    }

    private void setupRecyclerView() {
        adapter = new SubjectAdapter(this::onSubjectClicked);
        binding.subjectsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.subjectsRecycler.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSubjects(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterSubjects(String query) {
        filteredSubjects.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredSubjects.addAll(allSubjects);
        } else {
            String lower = query.toLowerCase().trim();
            for (Subject s : allSubjects) {
                if ((s.getName() != null && s.getName().toLowerCase().contains(lower)) ||
                        (s.getDescription() != null && s.getDescription().toLowerCase().contains(lower))) {
                    filteredSubjects.add(s);
                }
            }
        }
        adapter.setSubjects(filteredSubjects);
        checkEmptyState();
    }

    private void loadSubjects() {
        binding.progressbar.setVisibility(View.VISIBLE);
        FirebaseHelper.subjectsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressbar.setVisibility(View.GONE);
                allSubjects.clear();

                for (DataSnapshot subSnap : snapshot.getChildren()) {
                    Subject subject = subSnap.getValue(Subject.class);
                    if (subject != null) {
                        subject.setId(subSnap.getKey());
                        allSubjects.add(subject);
                    }
                }

                filterSubjects(binding.etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressbar.setVisibility(View.GONE);
                Toast.makeText(QuizListActivity.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEmptyState() {
        if (filteredSubjects.isEmpty()) {
            binding.emptyLayout.setVisibility(View.VISIBLE);
            binding.subjectsRecycler.setVisibility(View.GONE);
        } else {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.subjectsRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void onSubjectClicked(Subject subject) {
        binding.progressbar.setVisibility(View.VISIBLE);

        // Fetch published quizzes for this subject
        FirebaseHelper.quizzesRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressbar.setVisibility(View.GONE);
                List<Quiz> quizzes = new ArrayList<>();

                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    Quiz quiz = qSnap.getValue(Quiz.class);
                    if (quiz != null && quiz.isPublished()) {
                        quiz.setId(qSnap.getKey());
                        if (subject.getId().equals(quiz.getSubjectId()) || subject.getId().equals(quiz.getSectionId())) {
                            quizzes.add(quiz);
                        }
                    }
                }

                if (quizzes.isEmpty()) {
                    Toast.makeText(QuizListActivity.this, "No published quizzes for " + subject.getName() + " yet.", Toast.LENGTH_SHORT).show();
                } else if (quizzes.size() == 1) {
                    openQuizInstructions(quizzes.get(0));
                } else {
                    showQuizSelectionDialog(subject.getName(), quizzes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressbar.setVisibility(View.GONE);
                Toast.makeText(QuizListActivity.this, "Error fetching quizzes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuizSelectionDialog(String subjectName, List<Quiz> quizzes) {
        String[] titles = new String[quizzes.size()];
        for (int i = 0; i < quizzes.size(); i++) {
            titles[i] = quizzes.get(i).getTitle() + " (" + quizzes.get(i).getTotalQuestions() + " Qs)";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Quiz - " + subjectName)
                .setItems(titles, (dialog, which) -> {
                    openQuizInstructions(quizzes.get(which));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openQuizInstructions(Quiz quiz) {
        Intent intent = new Intent(this, QuizInstructionsActivity.class);
        intent.putExtra("quiz", quiz);
        startActivity(intent);
    }
}
