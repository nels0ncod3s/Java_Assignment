package gui;

import model.LibraryItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

/** Tab 1: a table view of every item in the catalogue right now. */
public class ViewItemsPanel extends JPanel {

    private final MainWindow app;
    private final ItemTableModel tableModel;
    private final JLabel cacheLabel;

    public ViewItemsPanel(MainWindow app) {
        this.app = app;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new ItemTableModel(app.getLibraryManager().getCatalogue());
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        // CUSTOM RENDERER (advanced GUI technique): colour borrowed rows differently
        table.setDefaultRenderer(Object.class, new BorrowedRowRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setToolTipText("Reload the table from the current catalogue");
        refreshBtn.addActionListener(e -> refresh());

        cacheLabel = new JLabel("Most accessed items will appear here.");
        JPanel top = new JPanel(new BorderLayout());
        top.add(refreshBtn, BorderLayout.WEST);
        top.add(cacheLabel, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        refresh();
    }

    public void refresh() {
        tableModel.fireTableDataChanged();
        StringBuilder sb = new StringBuilder("Most accessed: ");
        for (LibraryItem item : app.getLibraryManager().getFrequentCache()) {
            if (item != null) {
                sb.append(item.getTitle()).append(" (").append(item.getAccessCount()).append(")  ");
            }
        }
        cacheLabel.setText(sb.toString());
    }

    private static class ItemTableModel extends AbstractTableModel {
        private final List<LibraryItem> data;
        private final String[] cols = {"ID", "Type", "Title", "Author", "Year", "Status"};

        ItemTableModel(List<LibraryItem> data) { this.data = data; }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            LibraryItem it = data.get(r);
            return switch (c) {
                case 0 -> it.getItemId();
                case 1 -> it.getCategory();   // polymorphic call - works for any LibraryItem subtype
                case 2 -> it.getTitle();
                case 3 -> it.getAuthor();
                case 4 -> it.getYear();
                case 5 -> it.isBorrowed() ? ("Borrowed (due " + it.getDueDate() + ")") : "Available";
                default -> "";
            };
        }
    }

    private static class BorrowedRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Object statusVal = table.getValueAt(row, 5);
            boolean borrowed = statusVal != null && statusVal.toString().startsWith("Borrowed");
            if (!isSelected) {
                c.setBackground(borrowed ? new Color(255, 235, 205) : Color.WHITE);
            }
            return c;
        }
    }
}
