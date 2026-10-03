import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class ImageSlotPanel extends JPanel {
    private BufferedImage image;
    private File imageFile;

    public ImageSlotPanel(Consumer<ImageSlotPanel> onClickCallback) {
        setBackground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClickCallback != null) {
                    onClickCallback.accept(ImageSlotPanel.this);
                }
            }
        });
    }

    public void setImage(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                this.image = img;
                this.imageFile = file;
                repaint();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage());
        }
    }

    public void clearImage() {
        this.image = null;
        this.imageFile = null;
        repaint();
    }

    public File getImageFile() { return imageFile; }
    public boolean hasImage() { return imageFile != null; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int padding = 3;
        int drawX = padding;
        int drawY = padding;
        int drawW = getWidth() - (padding * 2);
        int drawH = getHeight() - (padding * 2);

        if (image == null) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
            g2d.drawRect(drawX, drawY, drawW, drawH);

            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String prompt = "+ Click to add photo";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(prompt)) / 2;
            int y = drawY + (drawH - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(prompt, x, y);
            return;
        }

        // Stretch image exactly to panel
        g2d.drawImage(image, drawX, drawY, drawW, drawH, null);

        // Draw standard UI border
        g2d.setColor(new Color(41, 105, 176));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(drawX, drawY, drawW, drawH);
    }
}
