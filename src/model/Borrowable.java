package model;

/**
 * Anything in the library that a patron can borrow must be able to do
 * these three things. Book, Magazine and Journal all end up satisfying
 * this contract because LibraryItem (their parent class) implements it.
 */
public interface Borrowable {
    boolean borrowItem(String userId, int loanDays);
    boolean returnItem();
    boolean isAvailable();
}
