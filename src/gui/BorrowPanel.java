package gui;

import model.LibraryItem;
import model.UserAccount;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

/** Tab 2: pick a user and an item, then borrow or return it. */
public class BorrowPanel extends JPanel {

    private final MainWindow app;
    private JComboBox<UserAccount> userCombo;
    private JComboBox<LibraryItem> itemCombo;
    private JTextArea waitlistArea;
    private JTextArea logArea;

    public BorrowPanel(MainWindow app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        userCombo = new JComboBox<>(app.getUsers().toArray(new UserAccount[0]));
        itemCombo = new JComboBox<>(app.getLibraryManager().getCatalogue().toArray(new LibraryItem[0]));
        itemCombo.addActionListener(e -> refreshWaitlist());

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("User:"), gbc);
        gbc.gridx = 1; form.add(userCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Item:"), gbc);
        gbc.gridx = 1; form.add(itemCombo, gbc);

        JButton borrowBtn = new JButton("Borrow");
        borrowBtn.setToolTipText("Borrow the selected item for the selected user");
        JButton returnBtn = new JButton("Return");
        returnBtn.setToolTipText("Return the selected item");
        borrowBtn.addActionListener(e -> doBorrow());
        returnBtn.addActionListener(e -> doReturn());

        JPanel buttonRow = new JPanel();
        buttonRow.add(borrowBtn);
        buttonRow.add(returnBtn);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(buttonRow, gbc);

        add(form, BorderLayout.NORTH);

        waitlistArea = new JTextArea(4, 40);
        waitlistArea.setEditable(false);
        waitlistArea.setBorder(BorderFactory.createTitledBorder("Reservation Queue for selected item"));

        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createTitledBorder("Activity Log"));

        JPanel center = new JPanel(new GridLayout(2, 1, 5, 5));
        center.add(new JScrollPane(waitlistArea));
        center.add(new JScrollPane(logArea));
        add(center, BorderLayout.CENTER);

        refreshWaitlist();
    }

    private void doBorrow() {
        UserAccount user = (UserAccount) userCombo.getSelectedItem();
        LibraryItem item = (LibraryItem) itemCombo.getSelectedItem();
        if (user == null || item == null) {
            JOptionPane.showMessageDialog(this, "Please select a user and an item.", "Missing selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        app.getLibraryManager().recordAccess(item); // counts as an "access" for the MFU cache
        String message = app.getBorrowController().borrow(item, user);
        log(message);
        app.setStatus(message);
        refreshWaitlist();
    }

    private void doReturn() {
        UserAccount user = (UserAccount) userCombo.getSelectedItem();
        LibraryItem item = (LibraryItem) itemCombo.getSelectedItem();
        if (user == null || item == null) {
            JOptionPane.showMessageDialog(this, "Please select a user and an item.", "Missing selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String message = app.getBorrowController().returnItem(item, user);
        log(message);
        app.setStatus(message);
        refreshWaitlist();
    }

    private void refreshWaitlist() {
        LibraryItem item = (LibraryItem) itemCombo.getSelectedItem();
        if (item == null) { waitlistArea.setText(""); return; }
        List<String> waiting = app.getBorrowController().getWaitlist(item.getItemId());
        waitlistArea.setText(waiting.isEmpty() ? "(no one waiting)" : String.join("\n", waiting));
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }
}
