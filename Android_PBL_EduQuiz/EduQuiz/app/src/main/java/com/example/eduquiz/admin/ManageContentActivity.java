package com.example.eduquiz.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduquiz.R;
import com.example.eduquiz.databinding.ActivityManageContentBinding;
import com.example.eduquiz.databinding.ItemManageQuizBinding;
import com.example.eduquiz.databinding.ItemManageSubjectBinding;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.models.Subject;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageContentActivity extends AppCompatActivity {

    private ActivityManageContentBinding binding;
    private boolean isQuizzesTab = true;

    private final List<Quiz> quizList = new ArrayList<>();
    private final List<Subject> subjectList = new ArrayList<>();

    private QuizManageAdapter quizAdapter;
    private SubjectManageAdapter subjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupAdapters();
        setupTabs();
        loadQuizzes();
        loadSubjects();
    }

    private void setupAdapters() {
        quizAdapter = new QuizManageAdapter();
        subjectAdapter = new SubjectManageAdapter();

        binding.recyclerContent.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerContent.setAdapter(quizAdapter);
    }

    private void setupTabs() {
        binding.tabQuizzes.setOnClickListener(v -> setTab(true));
        binding.tabSubjects.setOnClickListener(v -> setTab(false));
    }

    private void setTab(boolean quizzes) {
        isQuizzesTab = quizzes;
        if (isQuizzesTab) {
            binding.tabQuizzes.setBackgroundResource(R.drawable.bg_segmented_active);
            binding.tabQuizzes.setTextColor(getColor(R.color.white));
            binding.tabSubjects.setBackgroundColor(getColor(android.R.color.transparent));
            binding.tabSubjects.setTextColor(getColor(R.color.text_secondary));

            binding.recyclerContent.setAdapter(quizAdapter);
            updateEmptyState(quizList.isEmpty());
        } else {
            binding.tabSubjects.setBackgroundResource(R.drawable.bg_segmented_active);
            binding.tabSubjects.setTextColor(getColor(R.color.white));
            binding.tabQuizzes.setBackgroundColor(getColor(android.R.color.transparent));
            binding.tabQuizzes.setTextColor(getColor(R.color.text_secondary));

            binding.recyclerContent.setAdapter(subjectAdapter);
            updateEmptyState(subjectList.isEmpty());
        }
    }

    private void loadQuizzes() {
        binding.progressbar.setVisibility(View.VISIBLE);
        FirebaseHelper.quizzesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressbar.setVisibility(View.GONE);
                quizList.clear();

                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    Quiz q = qSnap.getValue(Quiz.class);
                    if (q != null) {
                        q.setId(qSnap.getKey());
                        quizList.add(q);
                    }
                }

                quizAdapter.notifyDataSetChanged();
                if (isQuizzesTab) {
                    updateEmptyState(quizList.isEmpty());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressbar.setVisibility(View.GONE);
            }
        });
    }

    private void loadSubjects() {
        FirebaseHelper.subjectsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();

                for (DataSnapshot subSnap : snapshot.getChildren()) {
                    Subject sub = subSnap.getValue(Subject.class);
                    if (sub != null) {
                        sub.setId(subSnap.getKey());
                        subjectList.add(sub);
                    }
                }

                subjectAdapter.notifyDataSetChanged();
                if (!isQuizzesTab) {
                    updateEmptyState(subjectList.isEmpty());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.tvEmptyContent.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerContent.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // Quiz Adapter
    class QuizManageAdapter extends RecyclerView.Adapter<QuizManageAdapter.QuizViewHolder> {

        @NonNull
        @Override
        public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemManageQuizBinding itemBinding = ItemManageQuizBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new QuizViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
            Quiz quiz = quizList.get(position);

            holder.binding.tvQuizSubjectTag.setText(quiz.getSubjectName() != null ? quiz.getSubjectName() : "General");
            holder.binding.tvManageQuizTitle.setText(quiz.getTitle());
            holder.binding.tvManageQuizMeta.setText(quiz.getTotalQuestions() + " Questions • " +
                    (quiz.getTimeLimitMinutes() > 0 ? quiz.getTimeLimitMinutes() + " Mins" : "Untimed"));

            boolean isPub = quiz.isPublished();
            holder.binding.tvQuizStatusBadge.setText(isPub ? "Published" : "Draft");
            holder.binding.tvQuizStatusBadge.setTextColor(getColor(isPub ? R.color.success : R.color.text_secondary));

            // Toggle publish
            holder.binding.btnTogglePublish.setOnClickListener(v -> {
                boolean newStatus = !quiz.isPublished();
                FirebaseHelper.quizzesRef().child(quiz.getId()).child("published").setValue(newStatus)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ManageContentActivity.this,
                                    newStatus ? "Quiz published" : "Quiz set to draft", Toast.LENGTH_SHORT).show();
                        });
            });

            // Delete quiz
            holder.binding.btnDeleteQuiz.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageContentActivity.this)
                        .setTitle("Delete Quiz")
                        .setMessage("Are you sure you want to delete '" + quiz.getTitle() + "' and its questions?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseHelper.quizzesRef().child(quiz.getId()).removeValue();
                            FirebaseHelper.questionsRef().child(quiz.getId()).removeValue();
                            Toast.makeText(ManageContentActivity.this, "Quiz deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return quizList.size();
        }

        class QuizViewHolder extends RecyclerView.ViewHolder {
            final ItemManageQuizBinding binding;

            QuizViewHolder(ItemManageQuizBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    // Subject Adapter
    class SubjectManageAdapter extends RecyclerView.Adapter<SubjectManageAdapter.SubjectViewHolder> {

        @NonNull
        @Override
        public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemManageSubjectBinding itemBinding = ItemManageSubjectBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SubjectViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
            Subject subject = subjectList.get(position);

            holder.binding.tvManageSubjectName.setText(subject.getName());
            holder.binding.tvManageSubjectDesc.setText(subject.getDescription());

            holder.binding.btnDeleteSubject.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageContentActivity.this)
                        .setTitle("Delete Subject")
                        .setMessage("Are you sure you want to delete the subject '" + subject.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseHelper.subjectsRef().child(subject.getId()).removeValue();
                            Toast.makeText(ManageContentActivity.this, "Subject deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        class SubjectViewHolder extends RecyclerView.ViewHolder {
            final ItemManageSubjectBinding binding;

            SubjectViewHolder(ItemManageSubjectBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
