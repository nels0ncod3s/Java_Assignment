package model;

/**
 * A tiny "memory" of one admin operation (adding or deleting an item),
 * pushed onto a Stack in LibraryManager so that operation can be undone.
 */
public class AdminAction {

    public enum ActionType { ADD, DELETE }

    private final ActionType type;
    private final LibraryItem item;
    private final int indexInList; // where the item sat in the catalogue, so a delete can be undone in place

    public AdminAction(ActionType type, LibraryItem item, int indexInList) {
        this.type = type;
        this.item = item;
        this.indexInList = indexInList;
    }

    public ActionType getType() { return type; }
    public LibraryItem getItem() { return item; }
    public int getIndexInList() { return indexInList; }
}
