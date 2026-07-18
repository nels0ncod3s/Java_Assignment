package controller;

import model.LibraryItem;
import model.UserAccount;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Produces the three simple reports the brief asks for. */
public class ReportGenerator {

    // Report 1: top N most-borrowed items.
    public List<LibraryItem> mostBorrowedItems(List<LibraryItem> catalogue, int topN) {
        List<LibraryItem> copy = new ArrayList<>(catalogue);
        copy.sort((a, b) -> b.getBorrowCount() - a.getBorrowCount());
        return copy.subList(0, Math.min(topN, copy.size()));
    }

    // Report 2: which users currently have an overdue item out.
    public List<String> usersWithOverdueItems(List<UserAccount> users, List<LibraryItem> catalogue) {
        List<String> result = new ArrayList<>();
        for (LibraryItem item : catalogue) {
            if (item.isOverdue()) {
                for (UserAccount u : users) {
                    if (u.getUserId().equals(item.getBorrowedByUserId())) {
                        result.add(u.getName() + " (" + u.getUserId() + ") - overdue on \"" + item.getTitle() + "\"");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Report 3: how many items exist per category - computed RECURSIVELY.
     * Instead of one loop over the whole list, we look at item[index],
     * record its category, then recurse on the rest of the list. This
     * is the "recursively compute total resource count by category"
     * option from section 6 of the brief.
     */
    public Map<String, Integer> categoryDistribution(List<LibraryItem> catalogue) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        countByCategoryRecursive(catalogue, 0, counts);
        return counts;
    }

    private void countByCategoryRecursive(List<LibraryItem> catalogue, int index, Map<String, Integer> counts) {
        if (index >= catalogue.size()) {
            return; // base case: reached the end of the list
        }
        String category = catalogue.get(index).getCategory();
        counts.put(category, counts.getOrDefault(category, 0) + 1);
        countByCategoryRecursive(catalogue, index + 1, counts); // recurse on the remaining items
    }
}
