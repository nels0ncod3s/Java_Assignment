package model;

import java.time.LocalDate;

/**
 * LibraryItem is the abstract parent for every resource the library owns.
 * It holds the fields and behaviour every item shares (an id, a title,
 * borrowing state, etc.) and implements Borrowable once so that Book,
 * Magazine and Journal don't each have to reinvent the borrowing logic.
 *
 * Two methods are left abstract on purpose: getCategory() and
 * getDailyLateFee(). Each subclass answers them differently, which is
 * the polymorphism the assignment asks for - a single method written
 * against LibraryItem (see LibraryManager / ReportGenerator) can call
 * these two methods on a Book, a Magazine or a Journal without ever
 * knowing which one it actually is.
 */
public abstract class LibraryItem implements Borrowable {

    protected String itemId;
    protected String title;
    protected String author;
    protected int year;

    protected boolean borrowed;
    protected String borrowedByUserId;
    protected LocalDate dueDate;

    protected int accessCount;   // how many times this item was looked up (feeds the MFU cache)
    protected int borrowCount;   // how many times this item has ever been borrowed (feeds reports)

    public LibraryItem(String itemId, String title, String author, int year) {
        this.itemId = itemId;
        this.title = title;
        this.author = author;
        this.year = year;
        this.borrowed = false;
        this.accessCount = 0;
        this.borrowCount = 0;
    }

    // ---- must be implemented differently by every subclass ----
    public abstract String getCategory();
    public abstract double getDailyLateFee();

    // ---- shared borrowing behaviour, inherited by every subclass ----

    @Override
    public boolean isAvailable() {
        return !borrowed;
    }

    @Override
    public boolean borrowItem(String userId, int loanDays) {
        if (borrowed) {
            return false;
        }
        borrowed = true;
        borrowedByUserId = userId;
        dueDate = LocalDate.now().plusDays(loanDays);
        borrowCount++;
        return true;
    }

    @Override
    public boolean returnItem() {
        if (!borrowed) {
            return false;
        }
        borrowed = false;
        borrowedByUserId = null;
        dueDate = null;
        return true;
    }

    public boolean isOverdue() {
        return borrowed && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    public void registerAccess() {
        accessCount++;
    }

    /**
     * RECURSIVE late-fee calculator. Instead of just multiplying
     * daysLate * dailyFee, this walks the days down to zero one at a
     * time, which is the recursive component required by the brief.
     */
    public double calculateLateFeeRecursive(long daysLate) {
        if (daysLate <= 0) {
            return 0.0; // base case: no days late, no fee
        }
        return getDailyLateFee() + calculateLateFeeRecursive(daysLate - 1);
    }

    /**
     * Used only by FileHandler when reloading saved data, so we can put
     * an item back into the exact state it was in when the program was
     * last closed (borrowed/not, due date, how many times borrowed).
     */
    public void restoreState(boolean borrowed, String borrowedByUserId, LocalDate dueDate, int borrowCount) {
        this.borrowed = borrowed;
        this.borrowedByUserId = borrowedByUserId;
        this.dueDate = dueDate;
        this.borrowCount = borrowCount;
    }

    public String getItemId() { return itemId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public boolean isBorrowed() { return borrowed; }
    public String getBorrowedByUserId() { return borrowedByUserId; }
    public LocalDate getDueDate() { return dueDate; }
    public int getAccessCount() { return accessCount; }
    public int getBorrowCount() { return borrowCount; }

    @Override
    public String toString() {
        String status = borrowed ? ("Borrowed (due " + dueDate + ")") : "Available";
        return String.format("[%s] %s - %s (%d) | %s", getCategory(), title, author, year, status);
    }
}
