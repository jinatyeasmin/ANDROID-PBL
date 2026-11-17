package com.example.eduquiz.userDetails;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.R;
import com.example.eduquiz.databinding.ActivityFeedbackBinding;
import com.example.eduquiz.models.FeedbackModel;
import com.example.eduquiz.models.User;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class FeedbackActivity extends AppCompatActivity {

    private ActivityFeedbackBinding binding;
    private int selectedRating = 5;
    private String studentName = "Student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupStarRatings();
        loadStudentName();

        binding.btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void loadStudentName() {
        String uid = FirebaseHelper.currentUserId();
        if (uid != null) {
            FirebaseHelper.usersRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getName() != null) {
                            studentName = user.getName();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void setupStarRatings() {
        ImageView[] stars = {
                binding.star1,
                binding.star2,
                binding.star3,
                binding.star4,
                binding.star5
        };

        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i + 1;
            stars[i].setOnClickListener(v -> setRating(starIndex));
        }

        setRating(5);
    }

    private void setRating(int rating) {
        selectedRating = rating;

        ImageView[] stars = {
                binding.star1,
                binding.star2,
                binding.star3,
                binding.star4,
                binding.star5
        };

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_border);
            }
        }

        switch (rating) {
            case 1:
                binding.tvRatingDescription.setText("Poor (1/5)");
                break;
            case 2:
                binding.tvRatingDescription.setText("Fair (2/5)");
                break;
            case 3:
                binding.tvRatingDescription.setText("Good (3/5)");
                break;
            case 4:
                binding.tvRatingDescription.setText("Very Good (4/5)");
                break;
            case 5:
            default:
                binding.tvRatingDescription.setText("Excellent (5/5)");
                break;
        }
    }

    private void submitFeedback() {
        String message = binding.etFeedbackMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            binding.etFeedbackMessage.setError("Please write your feedback before submitting");
            binding.etFeedbackMessage.requestFocus();
            return;
        }

        String uid = FirebaseHelper.currentUserId();
        if (uid == null) {
            Toast.makeText(this, "Please log in to submit feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseHelper.feedbackRef();
        String feedbackId = ref.push().getKey();
        if (feedbackId == null) return;

        setLoading(true);

        FeedbackModel feedback = new FeedbackModel(
                feedbackId,
                uid,
                studentName,
                selectedRating,
                message,
                System.currentTimeMillis(),
                FirebaseHelper.getTodayDateString()
        );

        ref.child(feedbackId).setValue(feedback).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                new AlertDialog.Builder(this)
                        .setTitle("Thank You!")
                        .setMessage("Your feedback has been submitted successfully. We appreciate your thoughts!")
                        .setPositiveButton("Done", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(this, "Failed to submit feedback. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmitFeedback.setEnabled(!loading);
    }
}
