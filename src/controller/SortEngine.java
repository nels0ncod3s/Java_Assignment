package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Four student-implemented sorting algorithms. The GUI lets the user
 * pick which one runs and which field (title/author/year) it sorts by.
 */
public class SortEngine {

    public static Comparator<LibraryItem> byTitle() {
        return Comparator.comparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<LibraryItem> byAuthor() {
        return Comparator.comparing(LibraryItem::getAuthor, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<LibraryItem> byYear() {
        return Comparator.comparingInt(LibraryItem::getYear);
    }

    // 1) SELECTION SORT - repeatedly find the smallest remaining item and swap it into place.
    public void selectionSort(List<LibraryItem> list, Comparator<LibraryItem> cmp) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int smallest = i;
            for (int j = i + 1; j < n; j++) {
                if (cmp.compare(list.get(j), list.get(smallest)) < 0) {
                    smallest = j;
                }
            }
            if (smallest != i) {
                LibraryItem tmp = list.get(i);
                list.set(i, list.get(smallest));
                list.set(smallest, tmp);
            }
        }
    }

    // 2) INSERTION SORT - build up a sorted section one item at a time, like sorting cards in your hand.
    public void insertionSort(List<LibraryItem> list, Comparator<LibraryItem> cmp) {
        for (int i = 1; i < list.size(); i++) {
            LibraryItem key = list.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    // 3) MERGE SORT (recommended) - split in half, sort each half recursively, then merge them back together.
    public void mergeSort(List<LibraryItem> list, Comparator<LibraryItem> cmp) {
        if (list.size() < 2) return;
        List<LibraryItem> sorted = mergeSortHelper(list, cmp);
        list.clear();
        list.addAll(sorted);
    }

    private List<LibraryItem> mergeSortHelper(List<LibraryItem> list, Comparator<LibraryItem> cmp) {
        if (list.size() <= 1) return list;
        int mid = list.size() / 2;
        List<LibraryItem> left = mergeSortHelper(new ArrayList<>(list.subList(0, mid)), cmp);
        List<LibraryItem> right = mergeSortHelper(new ArrayList<>(list.subList(mid, list.size())), cmp);
        return merge(left, right, cmp);
    }

    private List<LibraryItem> merge(List<LibraryItem> left, List<LibraryItem> right, Comparator<LibraryItem> cmp) {
        List<LibraryItem> merged = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) merged.add(left.get(i++));
            else merged.add(right.get(j++));
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }

    // 4) QUICK SORT - pick a pivot, push smaller items left and bigger items right, then repeat on each side.
    public void quickSort(List<LibraryItem> list, Comparator<LibraryItem> cmp) {
        quickSortHelper(list, 0, list.size() - 1, cmp);
    }

    private void quickSortHelper(List<LibraryItem> list, int low, int high, Comparator<LibraryItem> cmp) {
        if (low < high) {
            int p = partition(list, low, high, cmp);
            quickSortHelper(list, low, p - 1, cmp);
            quickSortHelper(list, p + 1, high, cmp);
        }
    }

    private int partition(List<LibraryItem> list, int low, int high, Comparator<LibraryItem> cmp) {
        LibraryItem pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(list.get(j), pivot) < 0) {
                i++;
                LibraryItem tmp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, tmp);
            }
        }
        LibraryItem tmp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, tmp);
        return i + 1;
    }
}
