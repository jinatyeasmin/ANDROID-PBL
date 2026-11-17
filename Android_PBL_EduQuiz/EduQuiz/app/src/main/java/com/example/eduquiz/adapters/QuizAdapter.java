package com.example.eduquiz.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduquiz.R;
import com.example.eduquiz.models.Quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder> {

    public interface OnQuizClickListener {
        void onQuizClick(Quiz quiz);
    }

    private final List<Quiz> quizzes = new ArrayList<>();
    private final OnQuizClickListener listener;

    public QuizAdapter(OnQuizClickListener listener) {
        this.listener = listener;
    }

    public void setQuizzes(List<Quiz> newQuizzes) {
        quizzes.clear();
        if (newQuizzes != null) {
            quizzes.addAll(newQuizzes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quiz quiz = quizzes.get(position);
        holder.titleText.setText(quiz.getTitle());
        holder.descText.setText(quiz.getDescription());
        holder.countText.setText(quiz.getQuestionCount() + " Questions");
        holder.itemView.setOnClickListener(v -> listener.onQuizClick(quiz));
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descText;
        TextView countText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.quiz_title);
            descText = itemView.findViewById(R.id.quiz_desc);
            countText = itemView.findViewById(R.id.quiz_count);
        }
    }
}
