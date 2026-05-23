package com.smartlibrary.app;

/**
 * Book model class for Firebase Realtime Database.
 * Each field maps directly to a JSON node in Firebase.
 */
public class Book {

    private String bookId;
    private String title;
    private String author;
    private String isbn;
    private String category;

    // Required empty constructor for Firebase deserialization
    public Book() {}

    public Book(String bookId, String title, String author, String isbn, String category) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
    }

    // Getters
    public String getBookId()   { return bookId; }
    public String getTitle()    { return title; }
    public String getAuthor()   { return author; }
    public String getIsbn()     { return isbn; }
    public String getCategory() { return category; }

    // Setters
    public void setBookId(String bookId)     { this.bookId = bookId; }
    public void setTitle(String title)       { this.title = title; }
    public void setAuthor(String author)     { this.author = author; }
    public void setIsbn(String isbn)         { this.isbn = isbn; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return title + " – " + author;
    }
}
