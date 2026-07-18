package controller;

import model.LibraryItem;
import model.UserAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Handles the borrow/return workflow. Every item gets its own
 * reservation Queue: if it's already out, the requesting user is
 * queued instead of turned away, and whoever returns it automatically
 * hands it to the next person in line.
 */
public class BorrowController {

    private Map<String, Queue<String>> waitlists = new HashMap<>();
    private static final int DEFAULT_LOAN_DAYS = 14;

    public String borrow(LibraryItem item, UserAccount user) {
        if (item.isAvailable()) {
            item.borrowItem(user.getUserId(), DEFAULT_LOAN_DAYS);
            user.recordBorrow(item.getItemId());
            return user.getName() + " successfully borrowed \"" + item.getTitle() + "\". Due back in " + DEFAULT_LOAN_DAYS + " days.";
        } else {
            waitlists.computeIfAbsent(item.getItemId(), k -> new LinkedList<>()).add(user.getUserId());
            return "\"" + item.getTitle() + "\" is currently borrowed. " + user.getName() + " has been added to the reservation queue.";
        }
    }

    public String returnItem(LibraryItem item, UserAccount user) {
        boolean ok = item.returnItem();
        if (!ok) {
            return "This item wasn't marked as borrowed, so there's nothing to return.";
        }
        user.recordReturn(item.getItemId());

        Queue<String> queue = waitlists.get(item.getItemId());
        if (queue != null && !queue.isEmpty()) {
            String nextUserId = queue.poll(); // dequeue the next person in line
            item.borrowItem(nextUserId, DEFAULT_LOAN_DAYS);
            return "\"" + item.getTitle() + "\" returned by " + user.getName()
                    + " and automatically handed to the next person on the waitlist (user " + nextUserId + ").";
        }
        return "\"" + item.getTitle() + "\" returned by " + user.getName() + ". No one is waiting for it.";
    }

    public List<String> getWaitlist(String itemId) {
        Queue<String> queue = waitlists.get(itemId);
        return queue == null ? new ArrayList<>() : new ArrayList<>(queue);
    }
}
