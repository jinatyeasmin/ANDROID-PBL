package com.example.eduquiz.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduquiz.databinding.ActivityAdminFeedbackBinding;
import com.example.eduquiz.databinding.ItemAdminFeedbackBinding;
import com.example.eduquiz.models.FeedbackModel;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminFeedbackActivity extends AppCompatActivity {

    private ActivityAdminFeedbackBinding binding;
    private final List<FeedbackModel> feedbackList = new ArrayList<>();
    private FeedbackAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new FeedbackAdapter();
        binding.recyclerFeedback.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFeedback.setAdapter(adapter);

        loadFeedback();
    }

    private void loadFeedback() {
        binding.progressbar.setVisibility(View.VISIBLE);
        FirebaseHelper.feedbackRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressbar.setVisibility(View.GONE);
                feedbackList.clear();

                for (DataSnapshot fbSnap : snapshot.getChildren()) {
                    FeedbackModel fb = fbSnap.getValue(FeedbackModel.class);
                    if (fb != null) {
                        fb.setId(fbSnap.getKey());
                        feedbackList.add(fb);
                    }
                }

                // Sort newest first
                Collections.sort(feedbackList, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                adapter.notifyDataSetChanged();

                if (feedbackList.isEmpty()) {
                    binding.tvEmptyFeedback.setVisibility(View.VISIBLE);
                    binding.recyclerFeedback.setVisibility(View.GONE);
                } else {
                    binding.tvEmptyFeedback.setVisibility(View.GONE);
                    binding.recyclerFeedback.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressbar.setVisibility(View.GONE);
            }
        });
    }

    class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

        @NonNull
        @Override
        public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAdminFeedbackBinding itemBinding = ItemAdminFeedbackBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new FeedbackViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
            FeedbackModel feedback = feedbackList.get(position);

            holder.binding.tvFeedbackStudentName.setText(
                    feedback.getUserName() != null ? feedback.getUserName() : "Student");

            holder.binding.tvFeedbackRatingBadge.setText("★ " + ((int) feedback.getRating()) + ".0");
            holder.binding.tvFeedbackContent.setText(feedback.getMessage());

            if (feedback.getTimestamp() > 0) {
                holder.binding.tvFeedbackTimestamp.setText(FirebaseHelper.formatDate(feedback.getTimestamp()));
            } else if (feedback.getDateString() != null) {
                holder.binding.tvFeedbackTimestamp.setText(feedback.getDateString());
            } else {
                holder.binding.tvFeedbackTimestamp.setText("Recently");
            }
        }

        @Override
        public int getItemCount() {
            return feedbackList.size();
        }

        class FeedbackViewHolder extends RecyclerView.ViewHolder {
            final ItemAdminFeedbackBinding binding;

            FeedbackViewHolder(ItemAdminFeedbackBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
