package model;

/** A magazine issue. Adds an issue number on top of the shared fields. */
public class Magazine extends LibraryItem {

    private int issueNumber;

    public Magazine(String itemId, String title, String author, int year, int issueNumber) {
        super(itemId, title, author, year);
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() { return issueNumber; }

    @Override
    public String getCategory() { return "Magazine"; }

    @Override
    public double getDailyLateFee() { return 10.0; } // cheaper to replace than a book
}
