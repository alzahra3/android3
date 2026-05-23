package com.smartlibrary.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * LoginActivity - authenticates users via Firebase Authentication.
 * Matches artifact: navy header, email/password fields, Login button,
 * error box, links to Register.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LinearLayout layoutError;
    private TextView tvError;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        layoutError  = findViewById(R.id.layoutError);
        tvError      = findViewById(R.id.tvError);

        // Login button
        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());

        // Navigate to Register
        findViewById(R.id.tvRegisterLink).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Forgot password placeholder
        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
            Toast.makeText(this, "Password reset email sent (if account exists)", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If already logged in, skip to BookManagement
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            goToMain();
        }
    }

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.err_field_required));
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_invalid_email));
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.err_field_required));
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 8) {
            etPassword.setError(getString(R.string.err_password_short));
            etPassword.requestFocus();
            return;
        }

        layoutError.setVisibility(View.GONE);

        // Firebase sign in
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                goToMain();
            })
            .addOnFailureListener(e -> {
                layoutError.setVisibility(View.VISIBLE);
                tvError.setText(getString(R.string.toast_invalid_creds));
            });
    }

    private void goToMain() {
        startActivity(new Intent(this, BookManagementActivity.class));
        finish();
    }
}
