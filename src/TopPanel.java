import javax.swing.*;
import java.awt.*;
import java.io.File;

public class TopPanel extends JPanel {
    
    private JTextField searchField;
    private JComboBox<String> filterBox;
    private ButtonPanel buttonPanel;
    
    // For applyZoom method later
    private JLabel titleLabel;
    private JLabel logoLabel;
    private ImageIcon originalLogo; 

    public TopPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10)); 

        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.add(new JLabel("Search: "));
        
        searchField = new JTextField(15);
        controlsPanel.add(searchField);

        controlsPanel.add(new JLabel("  Filter: "));
        String[] filters = {"All", "Paid", "Partial", "Unpaid"}; 
        filterBox = new JComboBox<>(filters);
        controlsPanel.add(filterBox);

        
        buttonPanel = new ButtonPanel();
        controlsPanel.add(buttonPanel);

        add(controlsPanel, BorderLayout.WEST);

        //Logo and Title
        JPanel brandingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        titleLabel = new JLabel("SUMMARY INVOICE OUT");
        titleLabel.setForeground(new Color(40, 40, 100)); 

        logoLabel = new JLabel();
        try {
            File logoFile = new File("logo.jpg"); 
            if (logoFile.exists()) {
                originalLogo = new ImageIcon("logo.jpg"); 
                Image scaledImage = originalLogo.getImage().getScaledInstance(150, 50, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                logoLabel.setText("[LOGO]"); 
                logoLabel.setForeground(Color.GRAY);
            }
        } catch (Exception e) {
            logoLabel.setText("[LOGO]");
        }

        brandingPanel.add(titleLabel);
        brandingPanel.add(logoLabel);

        add(brandingPanel, BorderLayout.EAST);
    }

    // 3. Getters so the main app can attach search logic and zoom logic
    public JTextField getSearchField() { 
        return searchField; 
    }
    public JComboBox<String> getFilterBox() { 
        return filterBox; 
    }
    public ButtonPanel getButtonPanel() { 
        return buttonPanel; 
    }
    public JLabel getTitleLabel() {
        return titleLabel; 
    }
    public JLabel getLogoLabel() {
        return logoLabel; 
    }
    public ImageIcon getOriginalLogo() {
        return originalLogo; 
    }
}
