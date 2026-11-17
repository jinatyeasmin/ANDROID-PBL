package com.example.eduquiz.userDetails;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.R;
import com.example.eduquiz.admin.AdminDashboardActivity;
import com.example.eduquiz.databinding.ActivityAdminBinding;
import com.example.eduquiz.models.Admin;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

public class adminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        setupPasswordToggle();
        setupActions();
    }

    private void setupPasswordToggle() {
        binding.ivAdminTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.etAdminPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.etAdminPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.etAdminPassword.setSelection(binding.etAdminPassword.getText().length());
        });
    }

    private void setupActions() {
        binding.btnAdminLogin.setOnClickListener(v -> performAdminLogin());

        binding.tvSetupInitialAdmin.setOnClickListener(v -> showSetupAdminDialog());
    }

    private void performAdminLogin() {
        String email = binding.etAdminEmail.getText().toString().trim();
        String password = binding.etAdminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etAdminEmail.setError("Admin email is required");
            binding.etAdminEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etAdminEmail.setError("Enter a valid email address");
            binding.etAdminEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etAdminPassword.setError("Password is required");
            binding.etAdminPassword.requestFocus();
            return;
        }

        setLoading(true);

        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && FirebaseHelper.getAuth().getCurrentUser() != null) {
                        String uid = FirebaseHelper.getAuth().getCurrentUser().getUid();

                        // Strict role check against admins/ database node
                        FirebaseHelper.adminsRef().child(uid).get().addOnCompleteListener(adminTask -> {
                            setLoading(false);
                            if (adminTask.isSuccessful() && adminTask.getResult() != null && adminTask.getResult().exists()) {
                                Toast.makeText(this, "Admin authorized successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, AdminDashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // Prevent student accounts from entering admin dashboard
                                FirebaseHelper.getAuth().signOut();
                                new AlertDialog.Builder(this)
                                        .setTitle("Access Denied")
                                        .setMessage("This account does not have administrator privileges. Please log in through the Student portal.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
                    } else {
                        setLoading(false);
                        String msg = task.getException() != null ? task.getException().getMessage() : "Admin authentication failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showSetupAdminDialog() {
        String email = binding.etAdminEmail.getText().toString().trim();
        String password = binding.etAdminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Please enter desired admin email and password (min 6 chars) in the fields above first.", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Register New Administrator")
                .setMessage("Register " + email + " as an authorized administrator in QuizMaster?")
                .setPositiveButton("Create Admin", (dialog, which) -> {
                    setLoading(true);
                    FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
                                    if (user != null) {
                                        Admin admin = new Admin(user.getUid(), email, "admin");
                                        FirebaseHelper.adminsRef().child(user.getUid()).setValue(admin)
                                                .addOnCompleteListener(dbTask -> {
                                                    setLoading(false);
                                                    Toast.makeText(this, "Admin registered successfully!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                    } else {
                                        setLoading(false);
                                    }
                                } else {
                                    setLoading(false);
                                    String msg = task.getException() != null ? task.getException().getMessage() : "Setup failed";
                                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setLoading(boolean loading) {
        binding.progressbar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnAdminLogin.setEnabled(!loading);
    }
}
