package gui;

import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import utils.FileHandler;
import utils.IDGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

/** Tab 3: add new items, delete/undo, generate reports, and import/export the catalogue. */
public class AdminPanel extends JPanel {

    private final MainWindow app;

    private JTextField titleField, authorField, yearField;
    private JComboBox<String> typeCombo;
    private CardLayout extraCardLayout;
    private JPanel extraFieldPanel; // swaps its visible child at runtime depending on the chosen type
    private JTextField isbnField, issueField, volumeField;
    private JTextField deleteIdField;
    private JTextArea reportArea;

    public AdminPanel(MainWindow app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topStack = new JPanel();
        topStack.setLayout(new javax.swing.BoxLayout(topStack, javax.swing.BoxLayout.Y_AXIS));
        topStack.add(buildAddForm());
        topStack.add(buildDeleteUndoRow());
        add(topStack, BorderLayout.NORTH);

        add(buildReportsAndFilePanel(), BorderLayout.CENTER);
    }

    private JPanel buildAddForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        titleField = new JTextField(15);
        authorField = new JTextField(15);
        yearField = new JTextField(6);
        typeCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});

        isbnField = new JTextField(12);
        issueField = new JTextField(12);
        volumeField = new JTextField(12);

        extraCardLayout = new CardLayout();
        extraFieldPanel = new JPanel(extraCardLayout);
        extraFieldPanel.add(labeledField("ISBN:", isbnField), "Book");
        extraFieldPanel.add(labeledField("Issue #:", issueField), "Magazine");
        extraFieldPanel.add(labeledField("Volume:", volumeField), "Journal");

        // DYNAMIC COMPONENT SWAP (advanced GUI technique): the extra field changes with the chosen type
        typeCombo.addActionListener(e -> extraCardLayout.show(extraFieldPanel, (String) typeCombo.getSelectedItem()));

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; panel.add(typeCombo, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; panel.add(titleField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; panel.add(authorField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; panel.add(yearField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Details:"), gbc);
        gbc.gridx = 1; panel.add(extraFieldPanel, gbc);
        row++;

        JButton addBtn = new JButton("Add Item");
        addBtn.setMnemonic(KeyEvent.VK_A);
        addBtn.setToolTipText("Validate the form and add a new item to the catalogue");
        addBtn.addActionListener(e -> doAddItem());
        gbc.gridx = 1; gbc.gridy = row; panel.add(addBtn, gbc);

        return panel;
    }

    private JPanel labeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void doAddItem() {
        // INPUT VALIDATION WITH DIALOG POPUPS (advanced GUI technique)
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String yearText = yearField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || yearText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Author and Year are all required.", "Missing information", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a whole number, e.g. 2023.", "Invalid year", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String type = (String) typeCombo.getSelectedItem();
        LibraryItem newItem;
        switch (type) {
            case "Book" -> newItem = new Book(IDGenerator.nextId("BK"), title, author, year, isbnField.getText().trim());
            case "Magazine" -> {
                int issue;
                try {
                    issue = Integer.parseInt(issueField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Issue number must be a whole number.", "Invalid issue #", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                newItem = new Magazine(IDGenerator.nextId("MG"), title, author, year, issue);
            }
            default -> newItem = new Journal(IDGenerator.nextId("JN"), title, author, year, volumeField.getText().trim());
        }

        app.getLibraryManager().addItem(newItem);
        app.setStatus("Added: " + newItem);
        clearForm();
    }

    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        yearField.setText("");
        isbnField.setText("");
        issueField.setText("");
        volumeField.setText("");
    }

    private JPanel buildDeleteUndoRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Delete / Undo"));

        deleteIdField = new JTextField(10);
        JButton deleteBtn = new JButton("Delete by ID");
        deleteBtn.setToolTipText("Remove the item with this ID (can be undone)");
        deleteBtn.addActionListener(e -> {
            String id = deleteIdField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter the ID of the item to delete.", "Missing ID", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean removed = app.getLibraryManager().removeItem(id);
            app.setStatus(removed ? "Deleted item " + id : "No item found with ID " + id);
            deleteIdField.setText("");
        });

        JButton undoBtn = new JButton("Undo Last Action");
        undoBtn.setMnemonic(KeyEvent.VK_U);
        undoBtn.setToolTipText("Reverse the last add or delete (Stack-based undo)");
        undoBtn.addActionListener(e -> app.setStatus(app.getLibraryManager().undoLastAction()));

        panel.add(new JLabel("Item ID:"));
        panel.add(deleteIdField);
        panel.add(deleteBtn);
        panel.add(undoBtn);
        return panel;
    }

    private JPanel buildReportsAndFilePanel() {
        JPanel outer = new JPanel(new BorderLayout(5, 5));

        reportArea = new JTextArea(8, 50);
        reportArea.setEditable(false);
        reportArea.setBorder(BorderFactory.createTitledBorder("Reports"));

        JButton reportBtn = new JButton("Generate Reports");
        reportBtn.addActionListener(e -> generateReports());

        JButton exportBtn = new JButton("Export Catalogue...");
        exportBtn.setToolTipText("Save the catalogue to a text file you choose");
        exportBtn.addActionListener(e -> exportCatalogue());

        JButton importBtn = new JButton("Import Catalogue...");
        importBtn.setToolTipText("Load catalogue items from a text file you choose");
        importBtn.addActionListener(e -> importCatalogue());

        JPanel buttons = new JPanel();
        buttons.add(reportBtn);
        buttons.add(exportBtn);
        buttons.add(importBtn);

        outer.add(buttons, BorderLayout.NORTH);
        outer.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return outer;
    }

    public void generateReports() {
        var rg = app.getReportGenerator();
        var lm = app.getLibraryManager();
        StringBuilder sb = new StringBuilder();

        sb.append("--- Most Borrowed Items ---\n");
        for (LibraryItem it : rg.mostBorrowedItems(lm.getCatalogue(), 5)) {
            sb.append(it.getTitle()).append(" - borrowed ").append(it.getBorrowCount()).append(" time(s)\n");
        }

        sb.append("\n--- Users With Overdue Items ---\n");
        var overdue = rg.usersWithOverdueItems(app.getUsers(), lm.getCatalogue());
        sb.append(overdue.isEmpty() ? "None right now.\n" : String.join("\n", overdue) + "\n");

        sb.append("\n--- Category Distribution ---\n");
        rg.categoryDistribution(lm.getCatalogue()).forEach((cat, count) -> sb.append(cat).append(": ").append(count).append("\n"));

        reportArea.setText(sb.toString());
    }

    private void exportCatalogue() {
        // FILE CHOOSER DIALOG (advanced GUI technique)
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export catalogue to...");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            FileHandler.saveItems(app.getLibraryManager().getCatalogue(), chooser.getSelectedFile().getAbsolutePath());
            app.setStatus("Catalogue exported to " + chooser.getSelectedFile().getName());
        }
    }

    private void importCatalogue() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import catalogue from...");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            var items = FileHandler.loadItems(chooser.getSelectedFile().getAbsolutePath());
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No valid items were found in that file.", "Import result", JOptionPane.WARNING_MESSAGE);
            } else {
                app.getLibraryManager().setCatalogue(items);
                app.setStatus("Imported " + items.size() + " item(s) from " + chooser.getSelectedFile().getName());
            }
        }
    }
}
