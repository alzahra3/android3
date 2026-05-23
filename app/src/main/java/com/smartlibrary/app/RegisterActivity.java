package com.smartlibrary.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

/**
 * RegisterActivity - creates new user in Firebase Auth and stores
 * member record in Firebase Realtime Database under /members/{uid}.
 * Matches artifact: all fields, role spinner, Register and Reset buttons.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword, etPhone;
    private Spinner spinRole;
    private LinearLayout layoutStatus;
    private TextView tvStatus;
    private FirebaseAuth mAuth;
    private DatabaseReference dbMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase
        mAuth     = FirebaseAuth.getInstance();
        dbMembers = FirebaseDatabase.getInstance().getReference("members");

        // Bind views
        etFullName        = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone           = findViewById(R.id.etPhone);
        spinRole          = findViewById(R.id.spinRole);
        layoutStatus      = findViewById(R.id.layoutStatus);
        tvStatus          = findViewById(R.id.tvStatus);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Register button
        findViewById(R.id.btnRegister).setOnClickListener(v -> attemptRegister());

        // Reset button
        findViewById(R.id.btnReset).setOnClickListener(v -> resetFields());

        // Login link
        findViewById(R.id.tvLoginLink).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String fullName  = etFullName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();
        String confirm   = etConfirmPassword.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String role      = spinRole.getSelectedItem().toString();

        layoutStatus.setVisibility(View.GONE);

        // Validations
        if (fullName.isEmpty()) { etFullName.setError(getString(R.string.err_field_required)); etFullName.requestFocus(); return; }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_invalid_email)); etEmail.requestFocus(); return;
        }
        if (password.length() < 8) { etPassword.setError(getString(R.string.err_password_short)); etPassword.requestFocus(); return; }
        if (!password.equals(confirm)) { etConfirmPassword.setError(getString(R.string.err_password_mismatch)); etConfirmPassword.requestFocus(); return; }
        if (phone.isEmpty() || !phone.replaceAll("[^0-9]", "").matches("\\d{8,}")) {
            etPhone.setError(getString(R.string.err_invalid_phone)); etPhone.requestFocus(); return;
        }

        // Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();

                // Store member record in Realtime Database
                Map<String, Object> member = new HashMap<>();
                member.put("uid",      uid);
                member.put("name",     fullName);
                member.put("email",    email);
                member.put("phone",    phone);
                member.put("role",     role);

                dbMembers.child(uid).setValue(member)
                    .addOnSuccessListener(a -> {
                        layoutStatus.setVisibility(View.VISIBLE);
                        tvStatus.setText("✓ " + getString(R.string.toast_register_success));
                        Toast.makeText(this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> {
                layoutStatus.setVisibility(View.VISIBLE);
                tvStatus.setTextColor(getColor(R.color.error));
                tvStatus.setText("✗ " + e.getMessage());
            });
    }

    private void resetFields() {
        etFullName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        etPhone.setText("");
        spinRole.setSelection(0);
        layoutStatus.setVisibility(View.GONE);
    }
}
