package com.example.eduquiz.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityPublishQuizBinding;
import com.example.eduquiz.databinding.ItemPreviewQuestionBinding;
import com.example.eduquiz.models.Question;
import com.example.eduquiz.models.Quiz;
import com.example.eduquiz.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PublishQuizActivity extends AppCompatActivity {

    private ActivityPublishQuizBinding binding;
    private Quiz quiz;
    private List<Question> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublishQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        quiz = (Quiz) getIntent().getSerializableExtra("quiz");
        List<Question> qs = (List<Question>) getIntent().getSerializableExtra("questions");
        if (qs != null) {
            questionList = qs;
        }

        if (quiz == null || questionList.isEmpty()) {
            Toast.makeText(this, "Quiz preview data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());

        displayQuizSummary();
        renderQuestionsPreview();

        binding.btnPublishQuiz.setOnClickListener(v -> showPublishConfirmationDialog());
    }

    private void displayQuizSummary() {
        binding.tvPublishSubject.setText(quiz.getSubjectName() != null ? quiz.getSubjectName() : "General");
        binding.tvPublishTitle.setText(quiz.getTitle());
        binding.tvPublishDescription.setText(quiz.getDescription());

        String meta = questionList.size() + " Questions";
        if (quiz.getTimeLimitMinutes() > 0) {
            meta += " • " + quiz.getTimeLimitMinutes() + " Mins Limit";
        } else {
            meta += " • Untimed";
        }
        binding.tvPublishMeta.setText(meta);
    }

    private void renderQuestionsPreview() {
        binding.layoutPreviewQuestionsContainer.removeAllViews();

        for (int i = 0; i < questionList.size(); i++) {
            Question q = questionList.get(i);
            ItemPreviewQuestionBinding qBinding = ItemPreviewQuestionBinding.inflate(
                    LayoutInflater.from(this), binding.layoutPreviewQuestionsContainer, false);

            qBinding.tvPreviewQuestionNum.setText("Question " + (i + 1) + " of " + questionList.size());
            qBinding.tvPreviewQuestionText.setText(q.getQuestionText());
            qBinding.tvPreviewOptA.setText("A) " + q.getOptionA());
            qBinding.tvPreviewOptB.setText("B) " + q.getOptionB());
            qBinding.tvPreviewOptC.setText("C) " + q.getOptionC());
            qBinding.tvPreviewOptD.setText("D) " + q.getOptionD());

            String correctLetter = "A";
            switch (q.getCorrectAnswer()) {
                case 2: correctLetter = "B"; break;
                case 3: correctLetter = "C"; break;
                case 4: correctLetter = "D"; break;
            }
            qBinding.tvPreviewCorrect.setText("Correct Answer: Option " + correctLetter);

            binding.layoutPreviewQuestionsContainer.addView(qBinding.getRoot());
        }
    }

    private void showPublishConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Publish Quiz")
                .setMessage("Publish '" + quiz.getTitle() + "' to the student catalog now?")
                .setPositiveButton("Publish Now", (dialog, which) -> performPublish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performPublish() {
        setLoading(true);

        quiz.setPublished(true);
        quiz.setTotalQuestions(questionList.size());
        quiz.setCreatedAt(System.currentTimeMillis());

        // 1. Save Quiz record under quizzes/
        FirebaseHelper.quizzesRef().child(quiz.getId()).setValue(quiz)
                .addOnCompleteListener(quizTask -> {
                    if (quizTask.isSuccessful()) {
                        // 2. Save Questions under questions/{quizId}/{qId}
                        for (int i = 0; i < questionList.size(); i++) {
                            Question q = questionList.get(i);
                            String qId = "q" + (i + 1);
                            q.setId(qId);
                            q.setQuizId(quiz.getId());
                            FirebaseHelper.questionsRef().child(quiz.getId()).child(qId).setValue(q);
                        }

                        // 3. Update question count on subject
                        if (quiz.getSubjectId() != null) {
                            FirebaseHelper.subjectsRef().child(quiz.getSubjectId()).child("questionCount")
                                    .setValue(questionList.size());
                        }

                        setLoading(false);
                        Toast.makeText(this, "Quiz published successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, AdminDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Failed to publish quiz. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnPublishQuiz.setEnabled(!loading);
    }
}
