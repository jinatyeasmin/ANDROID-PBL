package com.example.eduquiz.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.R;
import com.example.eduquiz.databinding.ActivityQuizBinding;
import com.example.eduquiz.models.Question;
import com.example.eduquiz.models.Quiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private ActivityQuizBinding binding;
    private Quiz quiz;
    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int[] userAnswers;
    private CountDownTimer countDownTimer;
    private boolean isSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        quiz = (Quiz) getIntent().getSerializableExtra("quiz");
        List<Question> receivedQuestions = (List<Question>) getIntent().getSerializableExtra("questions");

        if (receivedQuestions != null && !receivedQuestions.isEmpty()) {
            questions = receivedQuestions;
        }

        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions found for this quiz.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userAnswers = new int[questions.size()];

        setupToolbar();
        setupOptions();
        setupNavigation();
        setupBackPressHandler();
        startTimer();

        displayQuestion(0);
    }

    private void setupToolbar() {
        if (quiz != null && quiz.getTitle() != null) {
            binding.tvToolbarTitle.setText(quiz.getTitle());
        } else {
            binding.tvToolbarTitle.setText("Quiz Attempt");
        }

        binding.btnCloseQuiz.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void setupOptions() {
        binding.layoutOptionA.setOnClickListener(v -> selectOption(1));
        binding.layoutOptionB.setOnClickListener(v -> selectOption(2));
        binding.layoutOptionC.setOnClickListener(v -> selectOption(3));
        binding.layoutOptionD.setOnClickListener(v -> selectOption(4));
    }

    private void selectOption(int optionNumber) {
        userAnswers[currentIndex] = optionNumber;
        questions.get(currentIndex).setSelectedAnswer(optionNumber);
        updateOptionStyles(optionNumber);
        updateAnsweredStatus();
    }

    private void updateOptionStyles(int selected) {
        // Reset all options to default state
        binding.layoutOptionA.setBackgroundResource(selected == 1 ? R.drawable.bg_option_selected : R.drawable.bg_option_default);
        binding.indicatorOptionA.setBackgroundResource(selected == 1 ? R.drawable.bg_radio_selected : R.drawable.bg_radio_unselected);

        binding.layoutOptionB.setBackgroundResource(selected == 2 ? R.drawable.bg_option_selected : R.drawable.bg_option_default);
        binding.indicatorOptionB.setBackgroundResource(selected == 2 ? R.drawable.bg_radio_selected : R.drawable.bg_radio_unselected);

        binding.layoutOptionC.setBackgroundResource(selected == 3 ? R.drawable.bg_option_selected : R.drawable.bg_option_default);
        binding.indicatorOptionC.setBackgroundResource(selected == 3 ? R.drawable.bg_radio_selected : R.drawable.bg_radio_unselected);

        binding.layoutOptionD.setBackgroundResource(selected == 4 ? R.drawable.bg_option_selected : R.drawable.bg_option_default);
        binding.indicatorOptionD.setBackgroundResource(selected == 4 ? R.drawable.bg_radio_selected : R.drawable.bg_radio_unselected);
    }

    private void displayQuestion(int index) {
        currentIndex = index;
        Question q = questions.get(index);

        binding.tvQuestionCounter.setText("Q " + (index + 1) + "/" + questions.size());
        int progress = ((index + 1) * 100) / questions.size();
        binding.progressQuestion.setProgress(progress);

        binding.tvQuestionText.setText(q.getQuestionText());
        binding.tvOptionA.setText(q.getOptionA());
        binding.tvOptionB.setText(q.getOptionB());
        binding.tvOptionC.setText(q.getOptionC());
        binding.tvOptionD.setText(q.getOptionD());

        // Restore previously selected answer
        updateOptionStyles(userAnswers[index]);
        updateAnsweredStatus();

        // Previous button state
        binding.btnPrevious.setEnabled(currentIndex > 0);
        binding.btnPrevious.setAlpha(currentIndex > 0 ? 1.0f : 0.4f);

        // Next / Submit button text
        if (currentIndex == questions.size() - 1) {
            binding.btnNext.setText("Submit");
        } else {
            binding.btnNext.setText("Next");
        }
    }

    private void updateAnsweredStatus() {
        int answeredCount = 0;
        for (int ans : userAnswers) {
            if (ans != 0) answeredCount++;
        }
        binding.tvAnsweredStatus.setText("Answered: " + answeredCount + "/" + questions.size());
    }

    private void setupNavigation() {
        binding.btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                displayQuestion(currentIndex - 1);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentIndex == questions.size() - 1) {
                showSubmitConfirmationDialog();
            } else {
                displayQuestion(currentIndex + 1);
            }
        });
    }

    private void startTimer() {
        int minutes = 5;
        if (quiz != null && quiz.getTimeLimitMinutes() > 0) {
            minutes = quiz.getTimeLimitMinutes();
        }
        long millis = minutes * 60 * 1000L;

        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                long mins = totalSeconds / 60;
                long secs = totalSeconds % 60;
                binding.tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                binding.tvTimer.setText("00:00");
                Toast.makeText(QuizActivity.this, "Time is up! Submitting your answers.", Toast.LENGTH_SHORT).show();
                performSubmit();
            }
        }.start();
    }

    private void showSubmitConfirmationDialog() {
        int answered = 0;
        for (int ans : userAnswers) {
            if (ans != 0) answered++;
        }

        new AlertDialog.Builder(this)
                .setTitle("Submit Quiz")
                .setMessage("You have answered " + answered + " of " + questions.size() + " questions.\n\nAre you sure you want to finish and submit?")
                .setPositiveButton("Submit Now", (dialog, which) -> performSubmit())
                .setNegativeButton("Keep Reviewing", null)
                .show();
    }

    private void performSubmit() {
        if (isSubmitted) return;
        isSubmitted = true;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Calculate score
        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (userAnswers[i] == questions.get(i).getCorrectAnswer()) {
                correctCount++;
            }
        }

        int total = questions.size();
        int wrongCount = total - correctCount;
        int percentage = total > 0 ? (int) Math.round(((double) correctCount / total) * 100) : 0;

        Intent resultIntent = new Intent(this, QuizResultActivity.class);
        resultIntent.putExtra("quiz", quiz);
        resultIntent.putExtra("score", correctCount);
        resultIntent.putExtra("total_questions", total);
        resultIntent.putExtra("wrong_count", wrongCount);
        resultIntent.putExtra("percentage", percentage);
        resultIntent.putExtra("questions", (Serializable) questions);
        startActivity(resultIntent);
        finish();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Quiz?")
                .setMessage("Are you sure you want to exit? Your answers and progress will not be recorded.")
                .setPositiveButton("Quit", (dialog, which) -> {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    finish();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
