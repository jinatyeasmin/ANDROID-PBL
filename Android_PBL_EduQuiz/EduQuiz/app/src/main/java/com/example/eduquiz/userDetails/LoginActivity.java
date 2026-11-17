package com.example.eduquiz.userDetails;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.MainActivity;
import com.example.eduquiz.R;
import com.example.eduquiz.admin.AdminDashboardActivity;
import com.example.eduquiz.databinding.ActivityLoginBinding;
import com.example.eduquiz.models.User;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private boolean isLoginMode = true;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if user already logged in
        if (FirebaseHelper.getAuth().getCurrentUser() != null) {
            checkUserRoleAndProceed(FirebaseHelper.getAuth().getCurrentUser());
            return;
        }

        setupTabToggles();
        setupPasswordToggle();
        setupActions();
    }

    private void setupTabToggles() {
        binding.tabLogin.setOnClickListener(v -> setLoginMode(true));
        binding.tabRegister.setOnClickListener(v -> setLoginMode(false));
        binding.tvSwitchPrompt.setOnClickListener(v -> setLoginMode(!isLoginMode));
    }

    private void setLoginMode(boolean login) {
        isLoginMode = login;
        if (isLoginMode) {
            binding.tabLogin.setBackgroundResource(R.drawable.bg_segmented_active);
            binding.tabLogin.setTextColor(getColor(R.color.white));
            binding.tabRegister.setBackgroundColor(getColor(android.R.color.transparent));
            binding.tabRegister.setTextColor(getColor(R.color.text_secondary));

            binding.tvTitle.setText(R.string.welcome_back);
            binding.tvSubtitle.setText(R.string.login_to_continue);
            binding.layoutName.setVisibility(View.GONE);
            binding.layoutConfirmPassword.setVisibility(View.GONE);
            binding.btnAction.setText(R.string.login);
            binding.tvSwitchPrompt.setText("Don't have an account? Register Here");
        } else {
            binding.tabRegister.setBackgroundResource(R.drawable.bg_segmented_active);
            binding.tabRegister.setTextColor(getColor(R.color.white));
            binding.tabLogin.setBackgroundColor(getColor(android.R.color.transparent));
            binding.tabLogin.setTextColor(getColor(R.color.text_secondary));

            binding.tvTitle.setText("Create Account");
            binding.tvSubtitle.setText("Join QuizMaster and start learning!");
            binding.layoutName.setVisibility(View.VISIBLE);
            binding.layoutConfirmPassword.setVisibility(View.VISIBLE);
            binding.btnAction.setText(R.string.register);
            binding.tvSwitchPrompt.setText("Already have an account? Login Here");
        }
    }

    private void setupPasswordToggle() {
        binding.ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });
    }

    private void setupActions() {
        binding.btnAction.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performRegister();
            }
        });

        binding.layoutAdminPortal.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, adminActivity.class));
        });
    }

    private void performLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email address");
            binding.etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        }

        setLoading(true);
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        checkUserRoleAndProceed(user);
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void performRegister() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("Full Name is required");
            binding.etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email address");
            binding.etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), name, email, System.currentTimeMillis());
                            FirebaseHelper.usersRef().child(firebaseUser.getUid()).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        setLoading(false);
                                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        } else {
                            setLoading(false);
                        }
                    } else {
                        setLoading(false);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRoleAndProceed(FirebaseUser user) {
        if (user == null) return;
        setLoading(true);
        FirebaseHelper.adminsRef().child(user.getUid()).get().addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnAction.setEnabled(!loading);
    }
}
