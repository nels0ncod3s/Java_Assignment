package model;

/** A physical book. Adds an ISBN on top of the shared LibraryItem fields. */
public class Book extends LibraryItem {

    private String isbn;

    public Book(String itemId, String title, String author, int year, String isbn) {
        super(itemId, title, author, year);
        this.isbn = isbn;
    }

    public String getIsbn() { return isbn; }

    @Override
    public String getCategory() { return "Book"; }

    @Override
    public double getDailyLateFee() { return 20.0; } // NGN 20 per day late
}
