package com.example.eduquiz.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduquiz.databinding.ItemSubjectBinding;
import com.example.eduquiz.models.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject);
    }

    private List<Subject> subjects = new ArrayList<>();
    private final OnSubjectClickListener listener;

    public SubjectAdapter(OnSubjectClickListener listener) {
        this.listener = listener;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects != null ? subjects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubjectBinding binding = ItemSubjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        holder.bind(subjects.get(position));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubjectBinding binding;

        public SubjectViewHolder(@NonNull ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Subject subject) {
            binding.tvSubjectName.setText(subject.getName());

            int qCount = subject.getQuestionCount();
            if (qCount > 0) {
                binding.tvQuestionCount.setText(qCount + " Questions");
            } else if (subject.getQuizCount() > 0) {
                binding.tvQuestionCount.setText(subject.getQuizCount() + " Quizzes");
            } else {
                binding.tvQuestionCount.setText("Explore Questions");
            }

            // Set icon emoji based on subject name or icon
            String icon = subject.getIcon() != null ? subject.getIcon().toLowerCase() : "";
            String name = subject.getName() != null ? subject.getName().toLowerCase() : "";

            if (icon.contains("math") || name.contains("math")) {
                binding.tvSubjectBadge.setText("π");
            } else if (icon.contains("science") || name.contains("science") || name.contains("chem") || name.contains("bio") || name.contains("physic")) {
                binding.tvSubjectBadge.setText("🧪");
            } else if (icon.contains("hist") || name.contains("hist") || name.contains("civic") || name.contains("geog")) {
                binding.tvSubjectBadge.setText("🏛️");
            } else if (icon.contains("cs") || name.contains("comp") || name.contains("java") || name.contains("python") || name.contains("code")) {
                binding.tvSubjectBadge.setText("💻");
            } else {
                binding.tvSubjectBadge.setText("📚");
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubjectClick(subject);
                }
            });
        }
    }
}
