package gui;

import controller.BorrowController;
import controller.LibraryManager;
import controller.ReportGenerator;
import controller.SearchEngine;
import controller.SortEngine;
import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;
import utils.FileHandler;
import utils.IDGenerator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * The main application window. It owns every "backend" object
 * (LibraryManager, BorrowController, SearchEngine, SortEngine,
 * ReportGenerator, and the list of users) and hands them out to each
 * tab, so all four tabs are always looking at the same shared data.
 */
public class MainWindow extends JFrame {

    private final LibraryManager libraryManager = new LibraryManager();
    private final BorrowController borrowController = new BorrowController();
    private final SearchEngine searchEngine = new SearchEngine();
    private final SortEngine sortEngine = new SortEngine();
    private final ReportGenerator reportGenerator = new ReportGenerator();
    private final List<UserAccount> users = new ArrayList<>();

    private static final String ITEMS_FILE = "library_items.txt";
    private static final String USERS_FILE = "library_users.txt";

    private JLabel statusBar;
    private JTabbedPane tabs;
    private boolean sortedByTitle = false; // tells the Search tab whether binary search is safe to use

    public MainWindow() {
        super("Smart Library Circulation & Automation System (SLCAS)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        loadData();
        seedDemoDataIfEmpty();

        setLayout(new BorderLayout());

        tabs = new JTabbedPane();
        tabs.addTab("View Items", new ViewItemsPanel(this));
        tabs.addTab("Borrow / Return", new BorrowPanel(this));
        tabs.addTab("Admin", new AdminPanel(this));
        tabs.addTab("Search & Sort", new SearchSortPanel(this));
        // keyboard mnemonics: Alt+1 .. Alt+4 jump straight to a tab
        tabs.setMnemonicAt(0, java.awt.event.KeyEvent.VK_1);
        tabs.setMnemonicAt(1, java.awt.event.KeyEvent.VK_2);
        tabs.setMnemonicAt(2, java.awt.event.KeyEvent.VK_3);
        tabs.setMnemonicAt(3, java.awt.event.KeyEvent.VK_4);
        add(tabs, BorderLayout.CENTER);

        statusBar = new JLabel(" Ready.");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
                dispose();
                System.exit(0);
            }
        });

        // TIMER-TRIGGERED OVERDUE REMINDER (advanced GUI technique + event-driven requirement)
        javax.swing.Timer overdueTimer = new javax.swing.Timer(20000, e -> checkOverdueItems());
        overdueTimer.start();
    }

    private void checkOverdueItems() {
        long overdueCount = libraryManager.getCatalogue().stream().filter(LibraryItem::isOverdue).count();
        if (overdueCount > 0) {
            setStatus(overdueCount + " item(s) currently overdue.");
        }
    }

    private void seedDemoDataIfEmpty() {
        if (libraryManager.getCatalogue().isEmpty()) {
            libraryManager.addItem(new Book(IDGenerator.nextId("BK"), "Things Fall Apart", "Chinua Achebe", 1958, "978-0435905255"));
            libraryManager.addItem(new Book(IDGenerator.nextId("BK"), "Half of a Yellow Sun", "Chimamanda Ngozi Adichie", 2006, "978-1400095209"));
            libraryManager.addItem(new Book(IDGenerator.nextId("BK"), "The Fishermen", "Chigozie Obioma", 2015, "978-0316338370"));
            libraryManager.addItem(new Magazine(IDGenerator.nextId("MG"), "TechCabal Insights", "TechCabal Staff", 2024, 12));
            libraryManager.addItem(new Journal(IDGenerator.nextId("JN"), "Journal of African Computing", "UNILAG CS Dept", 2023, "Vol. 4"));
        }
        if (users.isEmpty()) {
            users.add(new UserAccount("U-0001", "Nelson A."));
            users.add(new UserAccount("U-0002", "Amaka O."));
        }
    }

    private void loadData() {
        libraryManager.setCatalogue(FileHandler.loadItems(ITEMS_FILE));
        users.addAll(FileHandler.loadUsers(USERS_FILE));
    }

    public void saveData() {
        FileHandler.saveItems(libraryManager.getCatalogue(), ITEMS_FILE);
        FileHandler.saveUsers(users, USERS_FILE);
        setStatus("Data saved to " + ITEMS_FILE + " and " + USERS_FILE);
    }

    public void setStatus(String msg) {
        statusBar.setText(" " + msg);
    }

    public LibraryManager getLibraryManager() { return libraryManager; }
    public BorrowController getBorrowController() { return borrowController; }
    public SearchEngine getSearchEngine() { return searchEngine; }
    public SortEngine getSortEngine() { return sortEngine; }
    public ReportGenerator getReportGenerator() { return reportGenerator; }
    public List<UserAccount> getUsers() { return users; }
    public boolean isSortedByTitle() { return sortedByTitle; }
    public void setSortedByTitle(boolean v) { sortedByTitle = v; }
    public JTabbedPane getTabs() { return tabs; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
