package com.example.eduquiz.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduquiz.R;
import com.example.eduquiz.databinding.ItemPerformanceBinding;
import com.example.eduquiz.models.QuizAttempt;
import com.example.eduquiz.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.PerformanceViewHolder> {

    public interface OnAttemptClickListener {
        void onAttemptClick(QuizAttempt attempt);
    }

    private final List<QuizAttempt> attempts = new ArrayList<>();
    private final OnAttemptClickListener listener;

    public PerformanceAdapter(OnAttemptClickListener listener) {
        this.listener = listener;
    }

    public void setAttempts(List<QuizAttempt> newAttempts) {
        attempts.clear();
        if (newAttempts != null) {
            attempts.addAll(newAttempts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PerformanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPerformanceBinding binding = ItemPerformanceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PerformanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PerformanceViewHolder holder, int position) {
        holder.bind(attempts.get(position));
    }

    @Override
    public int getItemCount() {
        return attempts.size();
    }

    class PerformanceViewHolder extends RecyclerView.ViewHolder {
        private final ItemPerformanceBinding binding;

        public PerformanceViewHolder(@NonNull ItemPerformanceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(QuizAttempt attempt) {
            String subject = attempt.getSubjectName() != null ? attempt.getSubjectName() : "General";
            binding.tvAttemptSubject.setText(subject);

            String title = attempt.getQuizTitle() != null ? attempt.getQuizTitle() : "Quiz Attempt";
            binding.tvAttemptTitle.setText(title);

            if (attempt.getTimestamp() > 0) {
                binding.tvAttemptDate.setText(FirebaseHelper.formatDate(attempt.getTimestamp()));
            } else if (attempt.getDateString() != null) {
                binding.tvAttemptDate.setText(attempt.getDateString());
            } else {
                binding.tvAttemptDate.setText("Recently");
            }

            int percent = attempt.getScorePercentage();
            binding.tvAttemptPercentage.setText(percent + "%");
            binding.tvAttemptScoreDetail.setText(attempt.getCorrectAnswers() + "/" + attempt.getTotalQuestions() + " Correct");

            if (percent >= 80) {
                binding.tvAttemptPercentage.setTextColor(binding.getRoot().getContext().getColor(R.color.success));
            } else if (percent >= 50) {
                binding.tvAttemptPercentage.setTextColor(binding.getRoot().getContext().getColor(R.color.primary));
            } else {
                binding.tvAttemptPercentage.setTextColor(binding.getRoot().getContext().getColor(R.color.error));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAttemptClick(attempt);
                }
            });
        }
    }
}
