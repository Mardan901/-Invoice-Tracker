import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.swing.JFormattedTextField.AbstractFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class InvoiceDialogs {

    private InvoiceApp mainApp;

    public InvoiceDialogs(InvoiceApp mainApp) {
        this.mainApp = mainApp;
    }

    //Add Invoice Dialog
    public void showAddInvoiceDialog() {
        JFrame dialog = new JFrame("New Invoice Setup");
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setSize(550, 650); 
        dialog.setResizable(true); 
        dialog.setLayout(new BorderLayout(10, 10));

        JDatePickerImpl datePicker = createVisualDatePicker(null); 
        JTextField idPart1 = new JTextField(6); 
        JTextField idPart2 = new JTextField(4); 
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        idPanel.add(new JLabel("JNG"));
        idPanel.add(idPart1);
        idPanel.add(new JLabel("/INV/"));
        idPanel.add(idPart2);

        JTextField jobNoField = new JTextField();
        JComboBox<String> nameField = createCustomerDropdown(null); 
        JTextField descField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField remarkField = new JTextField();

        JPanel mainFormPanel = new JPanel();
        mainFormPanel.setLayout(new BoxLayout(mainFormPanel, BoxLayout.Y_AXIS));
        mainFormPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        Component[][] fields = {
                {new JLabel("Date:"), datePicker},
                {new JLabel("Invoice ID:"), idPanel},
                {new JLabel("Job No:"), jobNoField},
                {new JLabel("Customer Name:"), nameField},
                {new JLabel("Description:"), descField},
                {new JLabel("Total Amount (RM):"), amountField},
                {new JLabel("Remark:"), remarkField}
            };

        for (Component[] row : fields) {
            JPanel rowPanel = new JPanel(new BorderLayout(5, 5)) {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                    }
                };

            rowPanel.add(row[0], BorderLayout.NORTH);
            rowPanel.add(row[1], BorderLayout.CENTER);
            mainFormPanel.add(rowPanel);
            mainFormPanel.add(Box.createVerticalStrut(10)); 
        }

        JScrollPane formScroll = new JScrollPane(mainFormPanel);
        dialog.add(formScroll, BorderLayout.CENTER);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = new JButton("Save Invoice");
        JButton btnCancel = new JButton("Cancel");
        actionButtonPanel.add(btnCancel);
        actionButtonPanel.add(btnSave);
        dialog.add(actionButtonPanel, BorderLayout.SOUTH);

        updateComponentFont(dialog, mainApp.getCurrentFontSize());

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
                    try {
                        double amt = roundToTwoDecimals(Double.parseDouble(amountField.getText()));
                        String fullInvoiceId = "JNG" + idPart1.getText().trim() + "/INV/" + idPart2.getText().trim();
                        String selectedCustomer = nameField.getSelectedItem().toString();
                        String finalDateString = datePicker.getJFormattedTextField().getText();

                        mainApp.getManager().addNewInvoice(
                            finalDateString, fullInvoiceId, jobNoField.getText(), 
                            selectedCustomer, descField.getText(), amt, remarkField.getText()
                        );

                        mainApp.refreshTableData();
                        dialog.dispose(); 
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: Please check that amounts are valid numbers!", "Entry Warning", JOptionPane.WARNING_MESSAGE);
                    }
            });

        dialog.setLocationRelativeTo(mainApp.getFrame()); 
        dialog.setVisible(true);
    }

    ////dropdown for customer selection so no need to rewrite the same customer
    private JComboBox<String> createCustomerDropdown(String preSelectedCustomer) {
        JComboBox<String> box = new JComboBox<>();
        for (String c : mainApp.getManager().getCustomers()) box.addItem(c);
        box.addItem("+ Add New Customer");

        if (preSelectedCustomer != null && !preSelectedCustomer.trim().isEmpty()) {
            boolean found = false;
            for (int i = 0; i < box.getItemCount(); i++) {
                if (preSelectedCustomer.equals(box.getItemAt(i))) {
                    box.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                box.insertItemAt(preSelectedCustomer, 0); 
                box.setSelectedItem(preSelectedCustomer);
            }
        }

        box.addActionListener(e -> {
                    if ("+ Add New Customer".equals(box.getSelectedItem())) {
                        String newCust = JOptionPane.showInputDialog(mainApp.getFrame(), "Enter New Customer Name:");
                        if (newCust != null && !newCust.trim().isEmpty()) {
                            newCust = newCust.trim();
                            mainApp.getManager().addCustomer(newCust); 
                            box.removeItem("+ Add New Customer");
                            box.addItem(newCust);
                            box.addItem("+ Add New Customer");
                            box.setSelectedItem(newCust); 
                        } else {
                            box.setSelectedIndex(0); 
                        }
                    }
            });
        return box;
    }
    //add calendar function
    private JDatePickerImpl createVisualDatePicker(String existingDate) {
        UtilDateModel model = new UtilDateModel();
        if (existingDate != null && !existingDate.trim().isEmpty() && !existingDate.equals("-")) {
            try {
                String[] parts = existingDate.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; 
                int year = Integer.parseInt(parts[2]);
                model.setDate(year, month, day);
                model.setSelected(true);
            } catch (Exception e) {}
        }
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(datePanel, new DateLabelFormatter()); 
    }

    private void updateComponentFont(Component comp, int newSize) {
        Font currentFont = comp.getFont();
        if (currentFont != null) {
            comp.setFont(new Font(currentFont.getName(), currentFont.getStyle(), newSize));
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                updateComponentFont(child, newSize); 
            }
        }
    }

    private double roundToTwoDecimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
    //for date formatting
    class DateLabelFormatter extends AbstractFormatter {
        private String datePattern = "dd/MM/yyyy";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }

    //Edit Invoice Dialog
    public void showEditInvoiceDialog() {
        JTable table = mainApp.getTable();
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(mainApp.getFrame(), "Please select an invoice first.");
            return;
        }

        JFrame dialog = new JFrame("Modify Invoice Details");
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setSize(550, 650);
        dialog.setResizable(true);
        dialog.setLayout(new BorderLayout(10, 10));

        String oldId = table.getValueAt(selectedRow, 1).toString();
        JDatePickerImpl datePicker = createVisualDatePicker(table.getValueAt(selectedRow, 0).toString());

        JTextField idField = new JTextField(oldId);
        JTextField jobNoField = new JTextField(table.getValueAt(selectedRow, 2).toString());
        JComboBox<String> nameField = createCustomerDropdown(table.getValueAt(selectedRow, 3).toString());
        JTextField descField = new JTextField(table.getValueAt(selectedRow, 4).toString());
        JTextField amountField = new JTextField(table.getValueAt(selectedRow, 5).toString());
        JTextField paidField = new JTextField(table.getValueAt(selectedRow, 6).toString()); 
        JTextField remarkField = new JTextField(table.getValueAt(selectedRow, 10).toString());

        JPanel mainFormPanel = new JPanel();
        mainFormPanel.setLayout(new BoxLayout(mainFormPanel, BoxLayout.Y_AXIS));
        mainFormPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        Component[][] fields = {
                {new JLabel("Date:"), datePicker},
                {new JLabel("Invoice ID:"), idField},
                {new JLabel("Job No:"), jobNoField},
                {new JLabel("Customer Name:"), nameField},
                {new JLabel("Description:"), descField},
                {new JLabel("Total Amount (RM):"), amountField},
                {new JLabel("Amount Paid (RM):"), paidField},
                {new JLabel("Remark:"), remarkField}
            };

        for (Component[] row : fields) {
            JPanel rowPanel = new JPanel(new BorderLayout(5, 5)) {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                    }
                };

            rowPanel.add(row[0], BorderLayout.NORTH);
            rowPanel.add(row[1], BorderLayout.CENTER);
            mainFormPanel.add(rowPanel);
            mainFormPanel.add(Box.createVerticalStrut(10)); 
        }

        JScrollPane formScroll = new JScrollPane(mainFormPanel);
        dialog.add(formScroll, BorderLayout.CENTER);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = new JButton("Save Changes");
        JButton btnDelete = new JButton("Delete Invoice");
        JButton btnCancel = new JButton("Cancel");

        btnDelete.setBackground(new Color(220, 50, 50)); 
        btnDelete.setForeground(Color.WHITE);

        actionButtonPanel.add(btnDelete);
        actionButtonPanel.add(btnCancel);
        actionButtonPanel.add(btnSave);
        dialog.add(actionButtonPanel, BorderLayout.SOUTH);

        updateComponentFont(dialog, mainApp.getCurrentFontSize());

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
                    try {
                        double amt = roundToTwoDecimals(Double.parseDouble(amountField.getText()));
                        double paid = roundToTwoDecimals(Double.parseDouble(paidField.getText()));
                        String selectedCustomer = nameField.getSelectedItem().toString();
                        String finalDateString = datePicker.getJFormattedTextField().getText();

                        mainApp.getManager().updateInvoice(
                            oldId, finalDateString, idField.getText(), jobNoField.getText(), 
                            selectedCustomer, descField.getText(), amt, paid, remarkField.getText()
                        );
                        mainApp.refreshTableData();
                        dialog.dispose();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: Amounts must be valid numbers!", "Entry Warning", JOptionPane.WARNING_MESSAGE);
                    }
            });

        btnDelete.addActionListener(e -> {
                    int confirmDelete = JOptionPane.showConfirmDialog(
                            dialog, 
                            "Are you sure you want to PERMANENTLY delete Invoice " + oldId + "?", 
                            "Confirm System Wiping", 
                            JOptionPane.YES_NO_OPTION, 
                            JOptionPane.ERROR_MESSAGE
                        );
                    if (confirmDelete == JOptionPane.YES_OPTION) {
                        mainApp.getManager().deleteInvoice(oldId);
                        mainApp.refreshTableData();
                        dialog.dispose();
                    }
            });

        dialog.setLocationRelativeTo(mainApp.getFrame());
        dialog.setVisible(true);
    }

    //Update Payment Dialog
    public void showPaymentDialog() {
        JTable table = mainApp.getTable();
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(mainApp.getFrame(), "Please select an invoice first.");
            return;
        }

        String targetId = table.getValueAt(selectedRow, 1).toString(); 
        String totalAmt = table.getValueAt(selectedRow, 5).toString();
        String currentPaid = table.getValueAt(selectedRow, 6).toString();
        String oldDate = table.getValueAt(selectedRow, 9).toString();

        JFrame dialog = new JFrame("Record Payment - " + targetId);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setSize(450, 350); 
        dialog.setResizable(true); 
        dialog.setLayout(new BorderLayout(10, 10));

        JTextField payField = new JTextField(currentPaid);
        JDatePickerImpl datePicker = createVisualDatePicker(oldDate);

        JPanel mainFormPanel = new JPanel();
        mainFormPanel.setLayout(new BoxLayout(mainFormPanel, BoxLayout.Y_AXIS));
        mainFormPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel infoLabel = new JLabel("<html><b>Total Invoice Amount:</b> RM " + totalAmt + "</html>");
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoPanel.add(infoLabel);
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        mainFormPanel.add(infoPanel);
        mainFormPanel.add(Box.createVerticalStrut(15)); 

        Component[][] fields = {
                {new JLabel("Enter Total Amount Paid So Far (RM):"), payField},
                {new JLabel("Date of Payment:"), datePicker}
            };

        for (Component[] row : fields) {
            JPanel rowPanel = new JPanel(new BorderLayout(5, 5)) {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                    }
                };

            rowPanel.add(row[0], BorderLayout.NORTH);
            rowPanel.add(row[1], BorderLayout.CENTER);
            mainFormPanel.add(rowPanel);
            mainFormPanel.add(Box.createVerticalStrut(10)); 
        }

        JScrollPane formScroll = new JScrollPane(mainFormPanel);
        dialog.add(formScroll, BorderLayout.CENTER);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = new JButton("Save Payment");
        JButton btnCancel = new JButton("Cancel");
        actionButtonPanel.add(btnCancel);
        actionButtonPanel.add(btnSave);
        dialog.add(actionButtonPanel, BorderLayout.SOUTH);

        updateComponentFont(dialog, mainApp.getCurrentFontSize());

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
                    try {
                        double newPaid = roundToTwoDecimals(Double.parseDouble(payField.getText()));
                        String dateString = datePicker.getJFormattedTextField().getText();
                        mainApp.getManager().updatePayment(targetId, newPaid, dateString.trim().isEmpty() ? "-" : dateString);
                        mainApp.refreshTableData();
                        dialog.dispose(); 
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: Amount must be a valid number!", "Entry Warning", JOptionPane.WARNING_MESSAGE);
                    }
            });

        dialog.setLocationRelativeTo(mainApp.getFrame());
        dialog.setVisible(true);
    }
}
