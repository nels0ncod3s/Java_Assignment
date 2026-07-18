package utils;

import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes plain pipe-delimited text files so the library's
 * data survives between runs (the "Persistent" requirement). Every
 * method is wrapped in try/catch so a missing or corrupted file never
 * crashes the app - it just starts with an empty catalogue instead.
 */
public class FileHandler {

    // ----- LIBRARY ITEMS -----

    public static void saveItems(List<LibraryItem> items, String filePath) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            for (LibraryItem item : items) {
                out.println(itemToLine(item));
            }
        } catch (IOException e) {
            System.err.println("Could not save catalogue: " + e.getMessage());
        }
    }

    private static String itemToLine(LibraryItem item) {
        String extra;
        if (item instanceof Book b) extra = b.getIsbn();
        else if (item instanceof Magazine m) extra = String.valueOf(m.getIssueNumber());
        else if (item instanceof Journal j) extra = j.getVolumeNumber();
        else extra = "";

        return String.join("|",
                item.getCategory(),
                item.getItemId(),
                item.getTitle(),
                item.getAuthor(),
                String.valueOf(item.getYear()),
                extra,
                String.valueOf(item.isBorrowed()),
                item.getBorrowedByUserId() == null ? "-" : item.getBorrowedByUserId(),
                item.getDueDate() == null ? "-" : item.getDueDate().toString(),
                String.valueOf(item.getAccessCount()),
                String.valueOf(item.getBorrowCount())
        );
    }

    public static List<LibraryItem> loadItems(String filePath) {
        List<LibraryItem> items = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) return items; // first run - nothing saved yet, that's fine

        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    items.add(lineToItem(line));
                } catch (Exception badLine) {
                    System.err.println("Skipped a corrupted line in the catalogue file: " + badLine.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load catalogue: " + e.getMessage());
        }
        return items;
    }

    private static LibraryItem lineToItem(String line) {
        String[] p = line.split("\\|", -1);
        String type = p[0], id = p[1], title = p[2], author = p[3];
        int year = Integer.parseInt(p[4]);
        String extra = p[5];
        boolean borrowed = Boolean.parseBoolean(p[6]);
        String borrowedBy = p[7].equals("-") ? null : p[7];
        String due = p[8].equals("-") ? null : p[8];
        int accessCount = Integer.parseInt(p[9]);
        int borrowCount = Integer.parseInt(p[10]);

        LibraryItem item;
        switch (type) {
            case "Book" -> item = new Book(id, title, author, year, extra);
            case "Magazine" -> item = new Magazine(id, title, author, year, extra.isEmpty() ? 0 : Integer.parseInt(extra));
            case "Journal" -> item = new Journal(id, title, author, year, extra);
            default -> throw new IllegalArgumentException("Unknown item type: " + type);
        }

        for (int i = 0; i < accessCount; i++) item.registerAccess();
        item.restoreState(borrowed, borrowedBy, due == null ? null : LocalDate.parse(due), borrowCount);
        return item;
    }

    // ----- USERS -----

    public static void saveUsers(List<UserAccount> users, String filePath) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            for (UserAccount u : users) {
                out.println(u.getUserId() + "|" + u.getName() + "|"
                        + String.join(",", u.getCurrentlyBorrowedItemIds()) + "|"
                        + String.join(",", u.getBorrowHistory()));
            }
        } catch (IOException e) {
            System.err.println("Could not save users: " + e.getMessage());
        }
    }

    public static List<UserAccount> loadUsers(String filePath) {
        List<UserAccount> users = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) return users;

        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                UserAccount u = new UserAccount(p[0], p[1]);
                if (p.length > 2 && !p[2].isEmpty()) {
                    for (String id : p[2].split(",")) u.recordBorrow(id);
                }
                users.add(u);
            }
        } catch (IOException e) {
            System.err.println("Could not load users: " + e.getMessage());
        }
        return users;
    }
}
