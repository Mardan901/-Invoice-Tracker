import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClaimGeneratorFrame extends JFrame {
    private JComboBox<String> cmbPreset;
    private JSpinner spinnerPages;
    private JSlider zoomSlider;
    private JPanel gridPreviewPanel;
    private JLabel lblInstructions;
    
    // --- NEW: Dynamic Title Field ---
    private JTextArea txtProjectTitle;
    
    private final List<ImageSlotPanel> slotPanels = new ArrayList<>();
    
    private ImageSlotPanel activeSlot = null; 
    private boolean isUpdatingSlider = false;

    public ClaimGeneratorFrame() {
        setTitle("Auto Invoice Claim Generator");
        setSize(850, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        try {
            setIconImage(ImageIO.read(new File("logo.jpg")));
        } catch (Exception e) {
            System.out.println("App icon logo.jpg not found.");
        }
        
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Top Panel Container (Holds Toolbar and Title Field)
        JPanel northContainer = new JPanel(new BorderLayout());

        // 1. Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        toolbar.add(new JLabel("Layout Preset:"));
        cmbPreset = new JComboBox<>(new String[]{
                "4 Photos per Page (2x2)", 
                "2 Photos per Page (1x2)", 
                "1 Photo Full Page"
            });
        cmbPreset.addActionListener(e -> updateLayout());
        toolbar.add(cmbPreset);

        toolbar.add(new JLabel("    Pages:"));
        spinnerPages = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1)); 
        spinnerPages.addChangeListener(e -> updateLayout());
        toolbar.add(spinnerPages);

        toolbar.add(new JLabel("         Selected Image Zoom:"));
        zoomSlider = new JSlider(70, 150, 100);
        zoomSlider.setEnabled(false); 
        
        zoomSlider.addChangeListener(e -> {
            if (isUpdatingSlider || activeSlot == null) return;
            double factor = zoomSlider.getValue() / 100.0;
            activeSlot.setZoomFactor(factor);
        });
        toolbar.add(zoomSlider);
        
        northContainer.add(toolbar, BorderLayout.NORTH);

        // 2. --- NEW: Dynamic Title Input Panel ---
        JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));
        titlePanel.add(new JLabel("Project Title (Prints on PDF Header):"), BorderLayout.NORTH);
        
        txtProjectTitle = new JTextArea(3, 50);
        // Set a default text so the user knows what goes here
        txtProjectTitle.setText("Supply, Deliver and Install the Stud\nBolts & I-Beam Tracks for BMU\nSystems for Client Project");
        txtProjectTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtProjectTitle.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        titlePanel.add(txtProjectTitle, BorderLayout.CENTER);
        northContainer.add(titlePanel, BorderLayout.CENTER);

        add(northContainer, BorderLayout.NORTH);

        // Center Grid Area
        JPanel centerContainer = new JPanel(new BorderLayout());
        
        lblInstructions = new JLabel("Click an empty box to add a photo. Double-click a photo to replace it.", JLabel.CENTER);
        lblInstructions.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInstructions.setForeground(Color.DARK_GRAY);
        centerContainer.add(lblInstructions, BorderLayout.NORTH);

        gridPreviewPanel = new JPanel();
        gridPreviewPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(gridPreviewPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        
        add(centerContainer, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnGenerate = new JButton("Generate & Merge Claim PDF");
        btnGenerate.addActionListener(e -> validateAndSave());
        bottomBar.add(btnGenerate);

        add(bottomBar, BorderLayout.SOUTH);

        updateLayout();
    }

    private void setActiveSlot(ImageSlotPanel panel) {
        if (activeSlot != null) {
            activeSlot.setActive(false);
        }
        
        activeSlot = panel;
        
        if (activeSlot != null) {
            activeSlot.setActive(true);
            
            zoomSlider.setEnabled(true);
            isUpdatingSlider = true;
            zoomSlider.setValue((int)(activeSlot.getZoomFactor() * 100));
            isUpdatingSlider = false;
        }
    }

    private void updateLayout() {
        gridPreviewPanel.removeAll();
        
        int pages = (int) spinnerPages.getValue();
        int selectedIndex = cmbPreset.getSelectedIndex();
        int slotsPerPage = (selectedIndex == 0) ? 4 : (selectedIndex == 1 ? 2 : 1);
        int totalSlots = pages * slotsPerPage;

        int cols = (selectedIndex == 0) ? 2 : 1;
        int rows = (selectedIndex == 0) ? (pages * 2) : (pages * slotsPerPage);

        gridPreviewPanel.setLayout(new GridLayout(rows, cols, 10, 10));
        gridPreviewPanel.setPreferredSize(new Dimension(750, rows * 350)); 

        while (slotPanels.size() < totalSlots) {
            slotPanels.add(new ImageSlotPanel(this::setActiveSlot));
        }

        for (int i = 0; i < totalSlots; i++) {
            gridPreviewPanel.add(slotPanels.get(i));
        }

        if (activeSlot != null && !activeSlot.isShowing()) {
            activeSlot.setActive(false);
            activeSlot = null;
            zoomSlider.setEnabled(false);
        }

        gridPreviewPanel.revalidate();
        gridPreviewPanel.repaint();
    }

    private void validateAndSave() {
        int pages = (int) spinnerPages.getValue();
        int slotsPerPage = cmbPreset.getSelectedIndex() == 0 ? 4 : (cmbPreset.getSelectedIndex() == 1 ? 2 : 1);
        int totalSlots = pages * slotsPerPage;
        
        int filledSlots = 0;
        
        for(int i = 0; i < totalSlots; i++) {
            if (slotPanels.get(i).hasImage()) filledSlots++;
        }

        if (filledSlots < totalSlots) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Incomplete data: You only uploaded " + filledSlots + 
                " out of " + totalSlots + " expected images.\n\nAre you sure you want to continue?",
                "Incomplete Data Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return; 
        }

        exportToPDF();
    }

    private void exportToPDF() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdir();
        
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        File outputFile = new File("data/Progress_Claim_" + timestamp + ".pdf");

        try (PDDocument document = new PDDocument()) {
            
            int pages = (int) spinnerPages.getValue();
            int slotsPerPage = cmbPreset.getSelectedIndex() == 0 ? 4 : (cmbPreset.getSelectedIndex() == 1 ? 2 : 1);
            int[][] coordinates = (slotsPerPage == 4) ? new int[][]{{50, 410, 235, 315}, {310, 410, 235, 315}, {50, 75, 235, 315}, {310, 75, 235, 315}} :
                                  (slotsPerPage == 2) ? new int[][]{{50, 410, 495, 315}, {50, 75, 495, 315}} :
                                  new int[][]{{50, 220, 495, 505}};

            for (int p = 0; p < pages; p++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    
                    // --- NEW: Dynamic Header Drawing ---
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.setNonStrokingColor(41, 105, 176);
                    contentStream.newLineAtOffset(50, 800);
                    
                    // Split the text by new lines so PDFBox can draw them sequentially
                    String[] titleLines = txtProjectTitle.getText().split("\\n");
                    for (String line : titleLines) {
                        contentStream.showText(line.trim());
                        contentStream.newLineAtOffset(0, -15);
                    }
                    contentStream.endText();

                    File logoFile = new File("logo.jpg"); 
                    if (logoFile.exists()) {
                        PDImageXObject pdLogo = PDImageXObject.createFromFile(logoFile.getAbsolutePath(), document);
                        float scale = 130f / pdLogo.getWidth();
                        contentStream.drawImage(pdLogo, 410, 760, 130f, pdLogo.getHeight() * scale);
                    }

                    contentStream.setStrokingColor(41, 105, 176);
                    contentStream.setLineWidth(2f);
                    contentStream.moveTo(50, 745);
                    contentStream.lineTo(545, 745);
                    contentStream.stroke();

                    for (int i = 0; i < slotsPerPage; i++) {
                        int globalSlotIndex = (p * slotsPerPage) + i;
                        ImageSlotPanel slot = slotPanels.get(globalSlotIndex);
                        
                        if (!slot.hasImage()) continue; 

                        File imgFile = slot.getImageFile();
                        int[] box = coordinates[i]; 

                        PDImageXObject pdImage = PDImageXObject.createFromFile(imgFile.getAbsolutePath(), document);
                        float scale = Math.min((float) box[2] / pdImage.getWidth(), (float) box[3] / pdImage.getHeight());
                        
                        scale *= slot.getZoomFactor(); 
                        
                        float drawW = pdImage.getWidth() * scale;
                        float drawH = pdImage.getHeight() * scale;
                        float drawX = box[0] + (box[2] - drawW) / 2f + (float)(slot.getPanX() * scale);
                        float drawY = box[1] + (box[3] - drawH) / 2f - (float)(slot.getPanY() * scale);

                        contentStream.saveGraphicsState(); 
                        contentStream.addRect(box[0], box[1], box[2], box[3]);
                        contentStream.clip(); 
                        contentStream.drawImage(pdImage, drawX, drawY, drawW, drawH);
                        contentStream.restoreGraphicsState(); 

                        // --- NEW: Dynamic PDF Hugging Border ---
                        float boxLeft = box[0];
                        float boxRight = box[0] + box[2];
                        float boxBottom = box[1];
                        float boxTop = box[1] + box[3];
                        
                        float imgLeft = drawX;
                        float imgRight = drawX + drawW;
                        float imgBottom = drawY;
                        float imgTop = drawY + drawH;
                        
                        float borderLeft = Math.max(boxLeft, imgLeft);
                        float borderRight = Math.min(boxRight, imgRight);
                        float borderBottom = Math.max(boxBottom, imgBottom);
                        float borderTop = Math.min(boxTop, imgTop);
                        
                        if (borderRight > borderLeft && borderTop > borderBottom) {
                            contentStream.setStrokingColor(41, 105, 176);
                            contentStream.setLineWidth(2f);
                            contentStream.addRect(borderLeft, borderBottom, (borderRight - borderLeft), (borderTop - borderBottom));
                            contentStream.stroke();
                        }
                    }
                }
            } 

            document.save(outputFile);
            mergeWithInvoice(outputFile);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetForm() {
        for (ImageSlotPanel slot : slotPanels) {
            slot.clearImage();
        }
        
        if (activeSlot != null) {
            activeSlot.setActive(false);
            activeSlot = null;
        }
        
        zoomSlider.setEnabled(false);
        zoomSlider.setValue(100);
        // Note: I deliberately left the txtProjectTitle intact so you don't have to retype it for back-to-back claims on the same project
    }

    private void mergeWithInvoice(File claimFile) {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setDialogTitle("Select the Invoice PDF to attach this claim to");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File invoiceFile = chooser.getSelectedFile();
            String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            File finalFile = new File("data/Final_Combined_Invoice_" + ts + ".pdf");

            try {
                PDFMergerUtility merger = new PDFMergerUtility();
                merger.setDestinationFileName(finalFile.getAbsolutePath());
                merger.addSource(invoiceFile);
                merger.addSource(claimFile);
                merger.mergeDocuments(null);
                
                JOptionPane.showMessageDialog(this, "Successfully merged!\nSaved at: " + finalFile.getAbsolutePath(), "Merge Complete", JOptionPane.INFORMATION_MESSAGE);
                resetForm(); 
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error merging PDFs: " + ex.getMessage(), "Merge Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Merge cancelled. The standalone claim sheet was still saved.", "Merge Skipped", JOptionPane.INFORMATION_MESSAGE);
            resetForm(); 
        }
    }
}
