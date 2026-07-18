package model;

/** An academic journal. Adds a volume label on top of the shared fields. */
public class Journal extends LibraryItem {

    private String volumeNumber;

    public Journal(String itemId, String title, String author, int year, String volumeNumber) {
        super(itemId, title, author, year);
        this.volumeNumber = volumeNumber;
    }

    public String getVolumeNumber() { return volumeNumber; }

    @Override
    public String getCategory() { return "Journal"; }

    @Override
    public double getDailyLateFee() { return 30.0; } // journals are the most expensive to replace
}
