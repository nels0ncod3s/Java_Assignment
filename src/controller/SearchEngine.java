package controller;

import model.LibraryItem;

import java.util.List;

/**
 * Three student-implemented search algorithms, plus a "smart" dispatcher
 * that picks the right one depending on whether the catalogue is
 * currently sorted by title - exactly what section 4 of the brief asks for.
 */
public class SearchEngine {

    // 1) LINEAR SEARCH - correct no matter the current order. O(n).
    public LibraryItem linearSearchByTitle(List<LibraryItem> items, String title) {
        for (LibraryItem item : items) {
            if (item.getTitle().equalsIgnoreCase(title)) {
                return item;
            }
        }
        return null;
    }

    public LibraryItem linearSearchByAuthor(List<LibraryItem> items, String author) {
        for (LibraryItem item : items) {
            if (item.getAuthor().equalsIgnoreCase(author)) {
                return item;
            }
        }
        return null;
    }

    // 2) ITERATIVE BINARY SEARCH - only correct if the list is sorted by title already.
    public LibraryItem binarySearchByTitle(List<LibraryItem> sortedItems, String title) {
        int low = 0, high = sortedItems.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = sortedItems.get(mid).getTitle().compareToIgnoreCase(title);
            if (cmp == 0) return sortedItems.get(mid);
            else if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    // 3) RECURSIVE BINARY SEARCH - same rule, implemented recursively.
    // This satisfies both the "recursive search" option in section 4 AND
    // gives us one of the recursion demonstrations required in section 6.
    public LibraryItem recursiveBinarySearchByTitle(List<LibraryItem> sortedItems, String title, int low, int high) {
        if (low > high) {
            return null; // base case: search space is empty, nothing found
        }
        int mid = (low + high) / 2;
        int cmp = sortedItems.get(mid).getTitle().compareToIgnoreCase(title);
        if (cmp == 0) {
            return sortedItems.get(mid);
        } else if (cmp < 0) {
            return recursiveBinarySearchByTitle(sortedItems, title, mid + 1, high);
        } else {
            return recursiveBinarySearchByTitle(sortedItems, title, low, mid - 1);
        }
    }

    /**
     * Chooses recursive binary search when the caller tells us the list
     * is sorted by title (fast: O(log n)), otherwise falls back to a
     * linear scan (always correct, but O(n)).
     */
    public LibraryItem smartSearchByTitle(List<LibraryItem> items, String title, boolean isSortedByTitle) {
        if (isSortedByTitle) {
            return recursiveBinarySearchByTitle(items, title, 0, items.size() - 1);
        } else {
            return linearSearchByTitle(items, title);
        }
    }
}
