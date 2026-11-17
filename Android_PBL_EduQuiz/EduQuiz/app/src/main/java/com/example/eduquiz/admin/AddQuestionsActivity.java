package com.example.eduquiz.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.R;
import com.example.eduquiz.databinding.ActivityAddQuestionsBinding;
import com.example.eduquiz.models.Question;
import com.example.eduquiz.models.Quiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddQuestionsActivity extends AppCompatActivity {

    private ActivityAddQuestionsBinding binding;
    private Quiz quiz;
    private int totalQuestions = 5;
    private int currentPosition = 0;
    private final List<Question> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddQuestionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        quiz = (Quiz) getIntent().getSerializableExtra("quiz");
        if (quiz == null) {
            Toast.makeText(this, "Quiz configuration missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        totalQuestions = quiz.getTotalQuestions();
        for (int i = 0; i < totalQuestions; i++) {
            questionList.add(new Question("q" + (i + 1), quiz.getId(), "", "", "", "", "", 1));
        }

        binding.btnBack.setOnClickListener(v -> showExitWarning());
        setupNavigation();
        loadQuestionAt(0);
    }

    private void loadQuestionAt(int index) {
        currentPosition = index;
        Question q = questionList.get(index);

        binding.tvQuestionCounter.setText("Question " + (index + 1) + " of " + totalQuestions);
        binding.tvHeaderTitle.setText(quiz.getTitle());

        binding.etQuestionText.setText(q.getQuestionText());
        binding.etOptionA.setText(q.getOptionA());
        binding.etOptionB.setText(q.getOptionB());
        binding.etOptionC.setText(q.getOptionC());
        binding.etOptionD.setText(q.getOptionD());

        switch (q.getCorrectAnswer()) {
            case 1:
                binding.rbOptionA.setChecked(true);
                break;
            case 2:
                binding.rbOptionB.setChecked(true);
                break;
            case 3:
                binding.rbOptionC.setChecked(true);
                break;
            case 4:
                binding.rbOptionD.setChecked(true);
                break;
            default:
                binding.rbOptionA.setChecked(true);
                break;
        }

        binding.btnPreviousQuestion.setEnabled(currentPosition > 0);
        binding.btnPreviousQuestion.setAlpha(currentPosition > 0 ? 1.0f : 0.4f);

        if (currentPosition == totalQuestions - 1) {
            binding.btnNextQuestion.setText("Save Last Question");
        } else {
            binding.btnNextQuestion.setText("Next Question");
        }

        updateCompletedBadge();
    }

    private boolean saveCurrentQuestion(boolean showErrors) {
        String qText = binding.etQuestionText.getText().toString().trim();
        String optA = binding.etOptionA.getText().toString().trim();
        String optB = binding.etOptionB.getText().toString().trim();
        String optC = binding.etOptionC.getText().toString().trim();
        String optD = binding.etOptionD.getText().toString().trim();

        int correct = 1;
        if (binding.rbOptionB.isChecked()) correct = 2;
        else if (binding.rbOptionC.isChecked()) correct = 3;
        else if (binding.rbOptionD.isChecked()) correct = 4;

        if (showErrors) {
            if (TextUtils.isEmpty(qText)) {
                binding.etQuestionText.setError("Question text is required");
                binding.etQuestionText.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(optA)) {
                binding.etOptionA.setError("Option A is required");
                binding.etOptionA.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(optB)) {
                binding.etOptionB.setError("Option B is required");
                binding.etOptionB.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(optC)) {
                binding.etOptionC.setError("Option C is required");
                binding.etOptionC.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(optD)) {
                binding.etOptionD.setError("Option D is required");
                binding.etOptionD.requestFocus();
                return false;
            }
        }

        Question q = questionList.get(currentPosition);
        q.setQuestionText(qText);
        q.setOptionA(optA);
        q.setOptionB(optB);
        q.setOptionC(optC);
        q.setOptionD(optD);
        q.setCorrectAnswer(correct);

        updateCompletedBadge();
        return true;
    }

    private void updateCompletedBadge() {
        int validCount = 0;
        for (Question q : questionList) {
            if (!TextUtils.isEmpty(q.getQuestionText()) &&
                    !TextUtils.isEmpty(q.getOptionA()) &&
                    !TextUtils.isEmpty(q.getOptionB()) &&
                    !TextUtils.isEmpty(q.getOptionC()) &&
                    !TextUtils.isEmpty(q.getOptionD())) {
                validCount++;
            }
        }
        binding.tvCompletedCount.setText(validCount + "/" + totalQuestions + " Completed");
    }

    private void setupNavigation() {
        binding.btnPreviousQuestion.setOnClickListener(v -> {
            saveCurrentQuestion(false);
            if (currentPosition > 0) {
                loadQuestionAt(currentPosition - 1);
            }
        });

        binding.btnNextQuestion.setOnClickListener(v -> {
            if (saveCurrentQuestion(true)) {
                if (currentPosition < totalQuestions - 1) {
                    loadQuestionAt(currentPosition + 1);
                } else {
                    Toast.makeText(this, "All questions reached. Click Preview & Publish when ready.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnPreviewQuiz.setOnClickListener(v -> {
            if (!saveCurrentQuestion(true)) {
                return;
            }

            // Verify that ALL questions have been fully entered
            for (int i = 0; i < totalQuestions; i++) {
                Question q = questionList.get(i);
                if (TextUtils.isEmpty(q.getQuestionText()) ||
                        TextUtils.isEmpty(q.getOptionA()) ||
                        TextUtils.isEmpty(q.getOptionB()) ||
                        TextUtils.isEmpty(q.getOptionC()) ||
                        TextUtils.isEmpty(q.getOptionD())) {
                    loadQuestionAt(i);
                    Toast.makeText(this, "Please complete Question " + (i + 1) + " before publishing.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            Intent intent = new Intent(this, PublishQuizActivity.class);
            intent.putExtra("quiz", quiz);
            intent.putExtra("questions", (Serializable) questionList);
            startActivity(intent);
        });
    }

    private void showExitWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Questions?")
                .setMessage("Are you sure you want to go back? Unsaved quiz questions will be lost.")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Keep Editing", null)
                .show();
    }
}
