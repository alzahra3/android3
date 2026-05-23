package com.smartlibrary.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * BookManagementActivity - Admin screen for CRUD operations on books.
 *
 * Buttons:
 *   Insert  - adds new book to Firebase
 *   Update  - unlocks fields for editing, saves changes
 *   Delete  - removes selected book with confirmation dialog
 *   View All - loads all books into ListView
 *   Reset  - clears all fields
 *   Close  - shows exit dialog
 *
 * On screen open: all fields DISABLED (as per assignment requirement).
 * Update/Delete buttons DISABLED until a record is selected.
 */
public class BookManagementActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor, etIsbn;
    private Spinner spinCategory;
    private MaterialButton btnInsert, btnUpdate, btnDelete, btnViewAll, btnReset, btnClose;
    private ListView lvBooks;

    private DatabaseReference dbBooks;
    private String selectedBookId = null;
    private List<Book> bookList = new ArrayList<>();
    private boolean isUpdateMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_management);

        // Firebase reference
        dbBooks = FirebaseDatabase.getInstance().getReference("books");

        // Bind views
        etTitle      = findViewById(R.id.etTitle);
        etAuthor     = findViewById(R.id.etAuthor);
        etIsbn       = findViewById(R.id.etIsbn);
        spinCategory = findViewById(R.id.spinCategory);
        btnInsert    = findViewById(R.id.btnInsert);
        btnUpdate    = findViewById(R.id.btnUpdate);
        btnDelete    = findViewById(R.id.btnDelete);
        btnViewAll   = findViewById(R.id.btnViewAll);
        btnReset     = findViewById(R.id.btnReset);
        btnClose     = findViewById(R.id.btnClose);
        lvBooks      = findViewById(R.id.lvBooks);

        // Category spinner setup
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
            this, R.array.book_categories, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategory.setAdapter(catAdapter);

        // Fields start DISABLED - requirement: "upon opening, fields should not be enabled"
        enableFields(false);

        // ── INSERT ──────────────────────────────────────────────────────
        btnInsert.setOnClickListener(v -> {
            // Enable fields for new entry
            enableFields(true);
            isUpdateMode = false;
            selectedBookId = null;
            clearFields();

            // Re-click to save when fields are filled
            btnInsert.setOnClickListener(v2 -> insertBook());
        });

        // ── UPDATE ──────────────────────────────────────────────────────
        btnUpdate.setOnClickListener(v -> {
            if (selectedBookId == null) {
                Toast.makeText(this, getString(R.string.err_select_record), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isUpdateMode) {
                // Unlock fields for editing
                enableFields(true);
                isUpdateMode = true;
                Toast.makeText(this, getString(R.string.toast_fields_unlocked), Toast.LENGTH_SHORT).show();
                btnUpdate.setText("Save");
            } else {
                // Save the update
                saveUpdate();
            }
        });

        // ── DELETE ──────────────────────────────────────────────────────
        btnDelete.setOnClickListener(v -> {
            if (selectedBookId == null) {
                Toast.makeText(this, getString(R.string.err_select_record), Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_title))
                .setMessage(getString(R.string.dialog_delete_msg))
                .setPositiveButton(getString(R.string.dialog_yes), (d, w) -> deleteBook())
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show();
        });

        // ── VIEW ALL ────────────────────────────────────────────────────
        btnViewAll.setOnClickListener(v -> loadAllBooks());

        // ── RESET ───────────────────────────────────────────────────────
        btnReset.setOnClickListener(v -> resetAll());

        // ── CLOSE ───────────────────────────────────────────────────────
        btnClose.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_exit_title))
                .setMessage(getString(R.string.dialog_exit_msg))
                .setPositiveButton(getString(R.string.dialog_yes), (d, w) -> finishAffinity())
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show());

        // ── MENU (navigate to Transaction / Report) ─────────────────────
        findViewById(R.id.btnMenu).setOnClickListener(v -> showMenuDialog());

        // ── LIST ITEM CLICK - populate fields from selected record ──────
        lvBooks.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < bookList.size()) {
                Book b = bookList.get(position);
                selectedBookId = b.getBookId();
                etTitle.setText(b.getTitle());
                etAuthor.setText(b.getAuthor());
                etIsbn.setText(b.getIsbn());

                // Set spinner to matching category
                ArrayAdapter adapter = (ArrayAdapter) spinCategory.getAdapter();
                int pos = adapter.getPosition(b.getCategory());
                if (pos >= 0) spinCategory.setSelection(pos);

                // Enable Update and Delete
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                isUpdateMode = false;
                btnUpdate.setText("Update");

                Toast.makeText(this, "Selected: " + b.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load records on start
        loadAllBooks();
    }

    // ── INSERT BOOK ──────────────────────────────────────────────────────
    private void insertBook() {
        String title  = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String isbn   = etIsbn.getText().toString().trim();
        String cat    = spinCategory.getSelectedItem().toString();

        // Validations
        if (title.isEmpty())  { etTitle.setError(getString(R.string.err_field_required));  etTitle.requestFocus();  return; }
        if (author.isEmpty()) { etAuthor.setError(getString(R.string.err_field_required)); etAuthor.requestFocus(); return; }
        if (isbn.isEmpty())   { etIsbn.setError(getString(R.string.err_field_required));   etIsbn.requestFocus();   return; }
        if (!isbn.matches("\\d{10}|\\d{13}")) { etIsbn.setError(getString(R.string.err_invalid_isbn)); etIsbn.requestFocus(); return; }
        if (cat.equals("Select Category")) { Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show(); return; }

        String bookId = dbBooks.push().getKey();
        Book book = new Book(bookId, title, author, isbn, cat);

        dbBooks.child(bookId).setValue(book)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, getString(R.string.toast_insert_success), Toast.LENGTH_SHORT).show();
                resetAll();
                // Restore Insert to its original click
                btnInsert.setOnClickListener(v2 -> {
                    enableFields(true); clearFields();
                    btnInsert.setOnClickListener(v3 -> insertBook());
                });
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ── SAVE UPDATE ──────────────────────────────────────────────────────
    private void saveUpdate() {
        String title  = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String isbn   = etIsbn.getText().toString().trim();
        String cat    = spinCategory.getSelectedItem().toString();

        if (title.isEmpty())  { etTitle.setError(getString(R.string.err_field_required));  return; }
        if (author.isEmpty()) { etAuthor.setError(getString(R.string.err_field_required)); return; }
        if (isbn.isEmpty())   { etIsbn.setError(getString(R.string.err_field_required));   return; }

        Book updated = new Book(selectedBookId, title, author, isbn, cat);
        dbBooks.child(selectedBookId).setValue(updated)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, getString(R.string.toast_update_success), Toast.LENGTH_SHORT).show();
                resetAll();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ── DELETE BOOK ──────────────────────────────────────────────────────
    private void deleteBook() {
        dbBooks.child(selectedBookId).removeValue()
            .addOnSuccessListener(a -> {
                Toast.makeText(this, getString(R.string.toast_delete_success), Toast.LENGTH_SHORT).show();
                resetAll();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ── LOAD ALL BOOKS ───────────────────────────────────────────────────
    private void loadAllBooks() {
        dbBooks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                bookList.clear();
                List<String> displayList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Book b = child.getValue(Book.class);
                    if (b != null) {
                        bookList.add(b);
                        displayList.add(b.getTitle() + " – " + b.getAuthor() + " [" + b.getCategory() + "]");
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    BookManagementActivity.this,
                    android.R.layout.simple_list_item_1,
                    displayList);
                lvBooks.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BookManagementActivity.this,
                    "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── HELPERS ──────────────────────────────────────────────────────────

    private void enableFields(boolean enable) {
        etTitle.setEnabled(enable);
        etAuthor.setEnabled(enable);
        etIsbn.setEnabled(enable);
        spinCategory.setEnabled(enable);
        etTitle.setBackground(enable
            ? getDrawable(R.drawable.bg_input)
            : getDrawable(R.drawable.bg_input_disabled));
        etAuthor.setBackground(enable
            ? getDrawable(R.drawable.bg_input)
            : getDrawable(R.drawable.bg_input_disabled));
        etIsbn.setBackground(enable
            ? getDrawable(R.drawable.bg_input)
            : getDrawable(R.drawable.bg_input_disabled));
    }

    private void clearFields() {
        etTitle.setText("");
        etAuthor.setText("");
        etIsbn.setText("");
        spinCategory.setSelection(0);
        etTitle.setError(null);
        etAuthor.setError(null);
        etIsbn.setError(null);
    }

    private void resetAll() {
        clearFields();
        enableFields(false);
        selectedBookId = null;
        isUpdateMode = false;
        btnUpdate.setEnabled(false);
        btnUpdate.setText("Update");
        btnDelete.setEnabled(false);
    }

    private void showMenuDialog() {
        String[] options = {"Fine Calculator", "View Report", "Logout"};
        new AlertDialog.Builder(this)
            .setTitle("Navigate")
            .setItems(options, (d, which) -> {
                if (which == 0) startActivity(new Intent(this, TransactionActivity.class));
                else if (which == 1) startActivity(new Intent(this, ReportActivity.class));
                else {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            })
            .show();
    }
}
