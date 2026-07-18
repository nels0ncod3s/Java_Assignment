package gui;

import controller.SearchEngine;
import controller.SortEngine;
import model.LibraryItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Comparator;
import java.util.List;

/** Tab 4: search the catalogue and re-sort it with a chosen algorithm. */
public class SearchSortPanel extends JPanel {

    private final MainWindow app;
    private JTextField queryField;
    private JComboBox<String> searchFieldCombo;
    private JComboBox<String> sortFieldCombo;
    private JComboBox<String> algorithmCombo;
    private DefaultTableModel resultsModel;
    private JLabel infoLabel;

    public SearchSortPanel(MainWindow app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildControls(), BorderLayout.NORTH);

        resultsModel = new DefaultTableModel(new String[]{"ID", "Type", "Title", "Author", "Year"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultsTable = new JTable(resultsModel);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        infoLabel = new JLabel("Tip: sort by Title first, then Search will automatically use fast recursive binary search.");
        add(infoLabel, BorderLayout.SOUTH);

        showAll();
    }

    private JPanel buildControls() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        queryField = new JTextField(15);
        searchFieldCombo = new JComboBox<>(new String[]{"Title", "Author"});
        JButton searchBtn = new JButton("Search");
        searchBtn.setToolTipText("Find an item (automatically chooses linear or recursive binary search)");
        searchBtn.addActionListener(e -> doSearch());

        sortFieldCombo = new JComboBox<>(new String[]{"Title", "Author", "Year"});
        algorithmCombo = new JComboBox<>(new String[]{"Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"});
        JButton sortBtn = new JButton("Sort");
        sortBtn.setToolTipText("Reorder the catalogue using the chosen algorithm");
        sortBtn.addActionListener(e -> doSort());

        JButton showAllBtn = new JButton("Show All");
        showAllBtn.addActionListener(e -> showAll());

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Search for:"), gbc);
        gbc.gridx = 1; panel.add(queryField, gbc);
        gbc.gridx = 2; panel.add(searchFieldCombo, gbc);
        gbc.gridx = 3; panel.add(searchBtn, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Sort by:"), gbc);
        gbc.gridx = 1; panel.add(sortFieldCombo, gbc);
        gbc.gridx = 2; panel.add(algorithmCombo, gbc);
        gbc.gridx = 3; panel.add(sortBtn, gbc);
        row++;
        gbc.gridx = 3; gbc.gridy = row; panel.add(showAllBtn, gbc);

        return panel;
    }

    private void doSort() {
        String field = (String) sortFieldCombo.getSelectedItem();
        String algo = (String) algorithmCombo.getSelectedItem();
        Comparator<LibraryItem> cmp = switch (field) {
            case "Title" -> SortEngine.byTitle();
            case "Author" -> SortEngine.byAuthor();
            default -> SortEngine.byYear();
        };

        List<LibraryItem> catalogue = app.getLibraryManager().getCatalogue();
        SortEngine sortEngine = app.getSortEngine();
        long start = System.nanoTime();
        switch (algo) {
            case "Selection Sort" -> sortEngine.selectionSort(catalogue, cmp);
            case "Insertion Sort" -> sortEngine.insertionSort(catalogue, cmp);
            case "Merge Sort" -> sortEngine.mergeSort(catalogue, cmp);
            case "Quick Sort" -> sortEngine.quickSort(catalogue, cmp);
        }
        long durationMicros = (System.nanoTime() - start) / 1000;

        app.setSortedByTitle(field.equals("Title")); // only a Title sort enables the binary-search shortcut
        infoLabel.setText(algo + " by " + field + " completed in " + durationMicros + " microseconds.");
        showAll();
    }

    private void doSearch() {
        String query = queryField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type something to search for.", "Empty search", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String field = (String) searchFieldCombo.getSelectedItem();
        SearchEngine engine = app.getSearchEngine();
        List<LibraryItem> catalogue = app.getLibraryManager().getCatalogue();
        LibraryItem found;
        String algoUsed;

        if (field.equals("Title")) {
            boolean sorted = app.isSortedByTitle();
            found = engine.smartSearchByTitle(catalogue, query, sorted);
            algoUsed = sorted ? "Recursive Binary Search (list is sorted by Title)" : "Linear Search (list is not sorted by Title)";
        } else {
            found = engine.linearSearchByAuthor(catalogue, query);
            algoUsed = "Linear Search (author lookups always scan, since we only track a Title sort state)";
        }

        infoLabel.setText("Used: " + algoUsed);
        resultsModel.setRowCount(0);
        if (found != null) {
            app.getLibraryManager().recordAccess(found);
            addRow(found);
        } else {
            JOptionPane.showMessageDialog(this, "No item found matching \"" + query + "\".", "Not found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAll() {
        resultsModel.setRowCount(0);
        for (LibraryItem item : app.getLibraryManager().getCatalogue()) addRow(item);
    }

    private void addRow(LibraryItem item) {
        resultsModel.addRow(new Object[]{item.getItemId(), item.getCategory(), item.getTitle(), item.getAuthor(), item.getYear()});
    }
}
