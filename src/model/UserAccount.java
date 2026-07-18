package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one library patron. Composition, not inheritance: a
 * UserAccount HAS-A list of currently borrowed item ids and HAS-A
 * full borrowing history, rather than being some kind of LibraryItem.
 */
public class UserAccount {

    private String userId;
    private String name;
    private List<String> currentlyBorrowedItemIds = new ArrayList<>();
    private List<String> borrowHistory = new ArrayList<>();

    public UserAccount(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public List<String> getCurrentlyBorrowedItemIds() { return currentlyBorrowedItemIds; }
    public List<String> getBorrowHistory() { return borrowHistory; }

    public void recordBorrow(String itemId) {
        currentlyBorrowedItemIds.add(itemId);
        borrowHistory.add(itemId);
    }

    public void recordReturn(String itemId) {
        currentlyBorrowedItemIds.remove(itemId);
    }

    @Override
    public String toString() {
        return userId + " - " + name;
    }
}
