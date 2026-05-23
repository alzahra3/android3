package com.smartlibrary.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * TransactionActivity - Overdue fine calculator.
 *
 * Formula: Fine = Days Overdue × OMR 0.100
 *
 * Buttons: Calculate, Reset, Close
 * Bottom nav: Home, Books, Fines (active), Report
 *
 * Matches artifact: formula info box, green result panel,
 * navy header, bottom navigation bar.
 */
public class TransactionActivity extends AppCompatActivity {

    private static final double FINE_PER_DAY = 0.100;

    private EditText etMemberName, etBookTitle, etDueDate, etDays;
    private TextView tvFineResult, tvFineFormula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // Bind views
        etMemberName  = findViewById(R.id.etMemberName);
        etBookTitle   = findViewById(R.id.etBookTitle);
        etDueDate     = findViewById(R.id.etDueDate);
        etDays        = findViewById(R.id.etDays);
        tvFineResult  = findViewById(R.id.tvFineResult);
        tvFineFormula = findViewById(R.id.tvFineFormula);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // CALCULATE button
        findViewById(R.id.btnCalculate).setOnClickListener(v -> calculateFine());

        // RESET button
        findViewById(R.id.btnReset).setOnClickListener(v -> resetFields());

        // CLOSE button
        findViewById(R.id.btnClose).setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_exit_title))
                .setMessage(getString(R.string.dialog_exit_msg))
                .setPositiveButton(getString(R.string.dialog_yes), (d, w) -> finishAffinity())
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show());

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, BookManagementActivity.class));
            finish();
        });
        findViewById(R.id.navBooks).setOnClickListener(v -> {
            startActivity(new Intent(this, BookManagementActivity.class));
            finish();
        });
        findViewById(R.id.navReport).setOnClickListener(v -> {
            startActivity(new Intent(this, ReportActivity.class));
        });
    }

    private void calculateFine() {
        String memberName = etMemberName.getText().toString().trim();
        String daysStr    = etDays.getText().toString().trim();

        // Validate member name
        if (memberName.isEmpty()) {
            etMemberName.setError(getString(R.string.err_field_required));
            etMemberName.requestFocus();
            return;
        }

        // Validate days
        if (daysStr.isEmpty()) {
            etDays.setError(getString(R.string.err_invalid_days));
            etDays.requestFocus();
            return;
        }

        int days;
        try {
            days = Integer.parseInt(daysStr);
        } catch (NumberFormatException e) {
            etDays.setError(getString(R.string.err_invalid_days));
            etDays.requestFocus();
            return;
        }

        if (days < 0) {
            etDays.setError(getString(R.string.err_invalid_days));
            etDays.requestFocus();
            return;
        }

        // Apply fine formula
        double fine = days * FINE_PER_DAY;
        tvFineResult.setText(String.format("OMR %.3f", fine));
        tvFineFormula.setText(days + " days × OMR 0.100 = OMR " + String.format("%.3f", fine));

        if (days == 0) {
            Toast.makeText(this, "No overdue fine for this member.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Fine calculated: OMR " + String.format("%.3f", fine), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetFields() {
        etMemberName.setText("");
        etBookTitle.setText("");
        etDueDate.setText("");
        etDays.setText("");
        tvFineResult.setText(getString(R.string.fine_default));
        tvFineFormula.setText("");
        etMemberName.setError(null);
        etDays.setError(null);
    }
}
