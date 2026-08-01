import javax.swing.*;
import java.awt.*;

public class BottomPanel extends JPanel {
    
    private JButton btnZoomOut;
    private JButton btnZoomIn;
    private JLabel totalPaidLabel;
    private JLabel totalUnpaidLabel;

    public BottomPanel() {
        setLayout(new BorderLayout());

        // increase/decrease font size to read things easier
        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnZoomOut = new JButton("➖"); 
        btnZoomIn = new JButton("➕");
        btnZoomOut.setMargin(new Insets(2, 6, 2, 6));
        btnZoomIn.setMargin(new Insets(2, 6, 2, 6));
        
        zoomPanel.add(new JLabel("Zoom: "));
        zoomPanel.add(btnZoomOut);
        zoomPanel.add(btnZoomIn);
        
        // Add zoom to the left side of the BottomPanel
        add(zoomPanel, BorderLayout.WEST); 

        // Display total Amount
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPaidLabel = new JLabel("Total Collected: RM 0.00  |  ");
        totalPaidLabel.setForeground(new Color(0, 150, 0));
        
        totalUnpaidLabel = new JLabel("Total Outstanding: RM 0.00");
        totalUnpaidLabel.setForeground(Color.RED);
        
        totalsPanel.add(totalPaidLabel);
        totalsPanel.add(totalUnpaidLabel);
        
        add(totalsPanel, BorderLayout.EAST); 
    }

    
    public JButton getBtnZoomIn() { 
        return btnZoomIn; 
    }
    public JButton getBtnZoomOut() { 
        return btnZoomOut; 
    }
    public JLabel getTotalPaidLabel() {
        return totalPaidLabel; }
        
    public JLabel getTotalUnpaidLabel() { 
        return totalUnpaidLabel; 
    }
}
