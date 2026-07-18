package controller;

import model.AdminAction;
import model.LibraryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * LibraryManager is the "LibraryDatabase" the brief describes: it owns
 * the collection of items and is the single place that adds, removes,
 * undoes and tracks access to them.
 *
 * Three of the four required data structures live here:
 *  - ArrayList<LibraryItem>  catalogue     (the main store)
 *  - Stack<AdminAction>      undoStack     (last-action undo)
 *  - LibraryItem[]           frequentCache (fixed-size "most accessed" cache)
 * (the fourth, Queue, lives in BorrowController for the reservation waitlist)
 */
public class LibraryManager {

    private List<LibraryItem> catalogue = new ArrayList<>();
    private Stack<AdminAction> undoStack = new Stack<>();

    // Fixed-size array cache of the 5 most-accessed items, kept in
    // descending order of access count. This is hand-rolled on purpose
    // instead of using a library data structure, per the brief.
    private LibraryItem[] frequentCache = new LibraryItem[5];

    public void addItem(LibraryItem item) {
        catalogue.add(item);
        undoStack.push(new AdminAction(AdminAction.ActionType.ADD, item, catalogue.size() - 1));
    }

    public boolean removeItem(String itemId) {
        for (int i = 0; i < catalogue.size(); i++) {
            if (catalogue.get(i).getItemId().equals(itemId)) {
                LibraryItem removed = catalogue.remove(i);
                undoStack.push(new AdminAction(AdminAction.ActionType.DELETE, removed, i));
                return true;
            }
        }
        return false;
    }

    public String undoLastAction() {
        if (undoStack.isEmpty()) {
            return "Nothing to undo.";
        }
        AdminAction last = undoStack.pop();
        if (last.getType() == AdminAction.ActionType.ADD) {
            catalogue.remove(last.getItem()); // undo an ADD by removing the item again
            return "Undo: removed the item that was just added (" + last.getItem().getTitle() + ")";
        } else {
            int idx = Math.min(last.getIndexInList(), catalogue.size());
            catalogue.add(idx, last.getItem()); // undo a DELETE by re-inserting it
            return "Undo: restored deleted item (" + last.getItem().getTitle() + ")";
        }
    }

    public void recordAccess(LibraryItem item) {
        item.registerAccess();
        updateFrequentCache(item);
    }

    /**
     * Keeps the fixed-size array sorted by access count, highest first.
     * When a new item out-accesses the weakest item currently cached,
     * it takes that slot. Then a small bubble-pass re-orders the array.
     * This is intentionally simple array manipulation, not a Collection.
     */
    private void updateFrequentCache(LibraryItem item) {
        boolean alreadyCached = false;
        for (LibraryItem cached : frequentCache) {
            if (cached == item) { alreadyCached = true; break; }
        }

        if (!alreadyCached) {
            int weakestSlot = -1;
            for (int i = 0; i < frequentCache.length; i++) {
                if (frequentCache[i] == null) { weakestSlot = i; break; }
                if (weakestSlot == -1 || frequentCache[i].getAccessCount() < frequentCache[weakestSlot].getAccessCount()) {
                    weakestSlot = i;
                }
            }
            if (weakestSlot != -1 &&
                    (frequentCache[weakestSlot] == null || frequentCache[weakestSlot].getAccessCount() < item.getAccessCount())) {
                frequentCache[weakestSlot] = item;
            }
        }

        // simple bubble pass so index 0 is always the most-accessed item
        for (int i = 0; i < frequentCache.length; i++) {
            for (int j = 0; j < frequentCache.length - 1 - i; j++) {
                if (frequentCache[j] == null) continue;
                if (frequentCache[j + 1] != null && frequentCache[j + 1].getAccessCount() > frequentCache[j].getAccessCount()) {
                    LibraryItem tmp = frequentCache[j];
                    frequentCache[j] = frequentCache[j + 1];
                    frequentCache[j + 1] = tmp;
                }
            }
        }
    }

    public LibraryItem[] getFrequentCache() { return frequentCache; }
    public List<LibraryItem> getCatalogue() { return catalogue; }
    public void setCatalogue(List<LibraryItem> items) { this.catalogue = items; }
}
