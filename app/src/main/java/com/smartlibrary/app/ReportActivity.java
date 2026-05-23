package com.smartlibrary.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportActivity - displays all book records from Firebase.
 * Supports live search filtering by title, author, or ISBN.
 * Matches artifact: search bar, grouped list, bottom nav.
 */
public class ReportActivity extends AppCompatActivity {

    private EditText etSearch;
    private ListView lvAllBooks;
    private TextView tvRecordCount;
    private DatabaseReference dbBooks;

    private List<Book> allBooks = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<String> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbBooks = FirebaseDatabase.getInstance().getReference("books");

        etSearch      = findViewById(R.id.etSearch);
        lvAllBooks    = findViewById(R.id.lvAllBooks);
        tvRecordCount = findViewById(R.id.tvRecordCount);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        lvAllBooks.setAdapter(adapter);

        // Search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int af) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int before, int count) {
                filterBooks(s.toString());
            }
        });

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, BookManagementActivity.class));
            finish();
        });
        findViewById(R.id.navBooks).setOnClickListener(v -> {
            startActivity(new Intent(this, BookManagementActivity.class));
            finish();
        });
        findViewById(R.id.navFines).setOnClickListener(v -> {
            startActivity(new Intent(this, TransactionActivity.class));
        });

        // Load records
        loadBooks();
    }

    private void loadBooks() {
        dbBooks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allBooks.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Book b = child.getValue(Book.class);
                    if (b != null) allBooks.add(b);
                }
                tvRecordCount.setText("Total: " + allBooks.size() + " records");
                filterBooks(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ReportActivity.this,
                    "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterBooks(String query) {
        displayList.clear();
        String q = query.toLowerCase().trim();
        for (Book b : allBooks) {
            if (q.isEmpty()
                || b.getTitle().toLowerCase().contains(q)
                || b.getAuthor().toLowerCase().contains(q)
                || b.getIsbn().toLowerCase().contains(q)) {

                displayList.add(
                    b.getTitle() + "\n"
                    + b.getAuthor() + "  •  ISBN: " + b.getIsbn()
                    + "  [" + b.getCategory() + "]"
                );
            }
        }
        adapter.notifyDataSetChanged();
        tvRecordCount.setText("Showing: " + displayList.size() + " / " + allBooks.size());
    }
}
