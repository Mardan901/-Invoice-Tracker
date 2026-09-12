import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageSlotPanel extends JPanel {
    private BufferedImage image;
    private File imageFile;
    private double zoomFactor = 1.0;
    
    // --- NEW: Panning Variables ---
    private double panX = 0; // Stored in actual image pixels
    private double panY = 0; // Stored in actual image pixels
    private double currentScale = 1.0; 
    private int lastMouseX;
    private int lastMouseY;
    
    private boolean isActive = false;
    private final Color BRAND_BLUE = new Color(41, 105, 176);
    private final Color ACTIVE_COLOR = new Color(255, 153, 0); 
    
    private static File lastDirectory = null; 

    public ImageSlotPanel(Consumer<ImageSlotPanel> onSelectCallback) {
        setBackground(Color.WHITE);
        updateBorder();
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // Changed to hand cursor to imply dragging

        // --- NEW: Integrated Click and Drag Listeners ---
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onSelectCallback != null) onSelectCallback.accept(ImageSlotPanel.this);
                if (imageFile == null || e.getClickCount() == 2) selectImage();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (onSelectCallback != null) onSelectCallback.accept(ImageSlotPanel.this);
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (image != null) {
                    double dx = e.getX() - lastMouseX;
                    double dy = e.getY() - lastMouseY;
                    
                    // Divide by scale so panning speed matches zoom level perfectly
                    panX += dx / currentScale;
                    panY += dy / currentScale;
                    
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        updateBorder();
    }

    private void updateBorder() {
        // We no longer use setBorder(). Instead, we repaint to trigger the custom border drawing.
        repaint();
    }

    private void selectImage() {
        JFileChooser chooser = new JFileChooser();
        
        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }
        
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    this.image = img;
                    this.imageFile = file;
                    this.zoomFactor = 1.0; 
                    
                    // Reset pan when a new image is loaded
                    this.panX = 0;
                    this.panY = 0;
                    
                    lastDirectory = chooser.getCurrentDirectory(); 
                    
                    repaint();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading image: " + ex.getMessage());
            }
        }
    }

    public void clearImage() {
        this.image = null;
        this.imageFile = null;
        this.zoomFactor = 1.0;
        this.panX = 0;
        this.panY = 0;
        this.isActive = false;
        updateBorder();
        repaint();
    }

    public File getImageFile() { return imageFile; }
    public boolean hasImage() { return imageFile != null; }
    public double getZoomFactor() { return zoomFactor; }
    
    // --- NEW: Getters for the PDF Engine ---
    public double getPanX() { return panX; }
    public double getPanY() { return panY; }

    public void setZoomFactor(double factor) {
        this.zoomFactor = factor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (image == null) {
            // Draw a dashed placeholder border when empty so users know where to click
            g2d.setColor(isActive ? ACTIVE_COLOR : Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(isActive ? 3 : 1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
            g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String prompt = "+ Click to add photo";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(prompt)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(prompt, x, y);
            return;
        }

        double panelW = getWidth(), panelH = getHeight();
        double imgW = image.getWidth(), imgH = image.getHeight();
        
        currentScale = Math.min(panelW / imgW, panelH / imgH) * zoomFactor;
        
        int drawW = (int) (imgW * currentScale);
        int drawHeight = (int) (imgH * currentScale);
        
        int x = (int) (((panelW - drawW) / 2) + (panX * currentScale));
        int y = (int) (((panelH - drawHeight) / 2) + (panY * currentScale));

        g2d.drawImage(image, x, y, drawW, drawHeight, null);

        // --- NEW: Dynamic Hugging Border ---
        // Calculates the intersection between the panel bounds and the image bounds
        int borderX = Math.max(0, x);
        int borderY = Math.max(0, y);
        int borderW = Math.min(getWidth(), x + drawW) - borderX - 1;
        int borderH = Math.min(getHeight(), y + drawHeight) - borderY - 1;

        if (borderW > 0 && borderH > 0) {
            g2d.setColor(isActive ? ACTIVE_COLOR : BRAND_BLUE);
            g2d.setStroke(new BasicStroke(isActive ? 3 : 2));
            g2d.drawRect(borderX, borderY, borderW, borderH);
        }
    }
}
