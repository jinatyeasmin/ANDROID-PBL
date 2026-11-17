package com.example.eduquiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eduquiz.admin.AdminDashboardActivity;
import com.example.eduquiz.userDetails.LoginActivity;
import com.example.eduquiz.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

public class splashscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        // Seed initial subjects & quizzes if database is fresh
        FirebaseHelper.seedInitialDataIfNeeded();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndNavigate, 1800);
    }

    private void checkAuthAndNavigate() {
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Check if current user is an admin
        FirebaseHelper.adminsRef().child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        });
    }
}
