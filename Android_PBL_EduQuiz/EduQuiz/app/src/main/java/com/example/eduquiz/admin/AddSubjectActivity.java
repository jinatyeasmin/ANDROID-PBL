package com.example.eduquiz.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.databinding.ActivityAddSubjectBinding;
import com.example.eduquiz.models.Subject;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;

public class AddSubjectActivity extends AppCompatActivity {

    private ActivityAddSubjectBinding binding;
    private final String[] iconKeys = {"math", "science", "cs", "history", "general"};
    private final String[] iconLabels = {
            "Mathematics (π)",
            "Science (🧪)",
            "Computer Science & Tech (💻)",
            "History & Civilizations (🏛️)",
            "General Knowledge (📚)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddSubjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupSpinner();
        binding.btnSaveSubject.setOnClickListener(v -> saveSubject());
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, iconLabels);
        binding.spinnerSubjectIcon.setAdapter(spinnerAdapter);
    }

    private void saveSubject() {
        String name = binding.etSubjectName.getText().toString().trim();
        String description = binding.etSubjectDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.etSubjectName.setError("Subject name is required");
            binding.etSubjectName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            binding.etSubjectDescription.setError("Description is required");
            binding.etSubjectDescription.requestFocus();
            return;
        }

        int selectedPos = binding.spinnerSubjectIcon.getSelectedItemPosition();
        String selectedIcon = iconKeys[selectedPos >= 0 && selectedPos < iconKeys.length ? selectedPos : 0];

        // Generate clean id
        String subjectId = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
        if (subjectId.isEmpty()) {
            subjectId = FirebaseHelper.subjectsRef().push().getKey();
        }

        setLoading(true);

        Subject subject = new Subject(subjectId, name, description, selectedIcon);

        DatabaseReference ref = FirebaseHelper.subjectsRef().child(subjectId);
        ref.setValue(subject).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Subject '" + name + "' added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save subject. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSaveSubject.setEnabled(!loading);
    }
}
