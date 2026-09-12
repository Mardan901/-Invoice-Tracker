import javax.swing.*;
import java.awt.*;

public class MainMenuFrame extends JFrame {

    public MainMenuFrame() {
        setTitle("Contractor Project & Invoice Suite");
        setSize(450, 320);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel lblTitle = new JLabel("Invoice & Claim Manager", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton btnInvoice = new JButton("1. Invoice Tracker");
        JButton btnClaim = new JButton("2. Auto Invoice Claim (Photo Report)");
        JButton btnExit = new JButton("3. Exit System");

        btnInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnClaim.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Action Listeners for Navigation
        btnInvoice.addActionListener(e -> {
            new InvoiceApp(); 
        });

        btnClaim.addActionListener(e -> {
            new ClaimGeneratorFrame().setVisible(true);
        });

        btnExit.addActionListener(e -> confirmExit());

        btnPanel.add(btnInvoice);
        btnPanel.add(btnClaim);
        btnPanel.add(btnExit);

        mainPanel.add(btnPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to close the application?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenuFrame().setVisible(true);
        });
    }
}
