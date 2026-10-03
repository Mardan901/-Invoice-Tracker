import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClaimGeneratorFrame extends JFrame {
    private JComboBox<String> cmbPreset;
    private JSpinner spinnerPages;
    private JPanel gridPreviewPanel;
    private JTextArea txtProjectTitle;

    private final List<ImageSlotPanel> slotPanels = new ArrayList<>();
    private final List<JTextField> pageLabelFields = new ArrayList<>(); // NEW: One label per page
    private static File lastDirectory = null;

    public ClaimGeneratorFrame() {
        setTitle("Auto Invoice Claim Generator");
        setSize(850, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel northContainer = new JPanel(new BorderLayout());

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolbar.add(new JLabel("Layout Preset:"));

        cmbPreset = new JComboBox<>(new String[]{ 
                "8 Photos Table (4x2)", 
                "6 Photos Table (3x2)",
                "4 Photos Table (2x2)", 
                "2 Photos Table (1x2)", 
                "1 Photo Full Page"
            }); 
        cmbPreset.addActionListener(e -> updateLayout());
        toolbar.add(cmbPreset);

        toolbar.add(new JLabel("    Pages:"));
        spinnerPages = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1)); 
        spinnerPages.addChangeListener(e -> updateLayout());
        toolbar.add(spinnerPages);

        northContainer.add(toolbar, BorderLayout.NORTH);

        // Dynamic Title Area
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));

        JLabel lblTitle = new JLabel("Project Title (Prints on PDF Header):");
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtProjectTitle = new JTextArea(2, 50);
        txtProjectTitle.setText("Supply, Deliver and Install the Stud\nBolts & I-Beam Tracks for BMU Systems");
        txtProjectTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtProjectTitle.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollTitle = new JScrollPane(txtProjectTitle);
        scrollTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(lblTitle);
        titlePanel.add(scrollTitle);
        northContainer.add(titlePanel, BorderLayout.CENTER);

        add(northContainer, BorderLayout.NORTH);

        // Center Grid Area
        JPanel centerContainer = new JPanel(new BorderLayout());
        JLabel lblInstructions = new JLabel("Click any box to add photos. You can assign a different Table Header for each page below.", JLabel.CENTER);
        lblInstructions.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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

    private int getSlotsPerPage() {

        switch (cmbPreset.getSelectedIndex()) {

            case 0: return 8;
            case 1: return 6;
            case 2: return 4;
            case 3: return 2;
            case 4: return 1;
            default: return 8;
        } 
    }

    private void handleSlotClick(ImageSlotPanel clickedSlot) {
        // 1. Find exactly which box the user clicked
        int startIndex = slotPanels.indexOf(clickedSlot);
        if (startIndex == -1) return;

        // 2. Calculate how many slots are left starting from this box
        int slotsLeft = slotPanels.size() - startIndex;

        java.awt.FileDialog chooser = new java.awt.FileDialog(this, 
                "Select up to " + slotsLeft + " image(s) (Starts filling from clicked box)", 
                java.awt.FileDialog.LOAD);
        chooser.setMultipleMode(true);
        chooser.setFile("*.jpg;*.jpeg;*.png");
        if (lastDirectory != null) chooser.setDirectory(lastDirectory.getAbsolutePath());

        chooser.setVisible(true); 
        File[] selectedFiles = chooser.getFiles();

        if (selectedFiles != null && selectedFiles.length > 0) {
            lastDirectory = selectedFiles[0].getParentFile();

            // Warn if they picked too many, but don't block them—just fill what fits
            if (selectedFiles.length > slotsLeft) {
                JOptionPane.showMessageDialog(this,
                    "You selected " + selectedFiles.length + " images, but only " + slotsLeft + " slots are available from this point.\nThe extra images will be ignored.",
                    "Too Many Images", JOptionPane.INFORMATION_MESSAGE);
            }

            // 3. Insert the selected images starting exactly where they clicked
            int filesToProcess = Math.min(selectedFiles.length, slotsLeft);
            for (int i = 0; i < filesToProcess; i++) {
                slotPanels.get(startIndex + i).setImage(selectedFiles[i]);
            }
        }
    }

    private void updateLayout() {
        gridPreviewPanel.removeAll();
        gridPreviewPanel.setLayout(new BoxLayout(gridPreviewPanel, BoxLayout.Y_AXIS));

        int pages = (int) spinnerPages.getValue();
        int slotsPerPage = getSlotsPerPage();
        int totalSlots = pages * slotsPerPage;

        // Preserve existing data in slots and text fields if user simply changes layout
        while (slotPanels.size() < totalSlots) {
            slotPanels.add(new ImageSlotPanel(this::handleSlotClick));
        }
        while (pageLabelFields.size() < pages) {
            pageLabelFields.add(new JTextField());
        }

        int cols = (slotsPerPage == 8 || slotsPerPage == 6 || slotsPerPage == 4) ? 2 : 1;
        int rows = (slotsPerPage == 8) ? 4 : ((slotsPerPage == 6) ? 3 : ((slotsPerPage == 4) ? 2 : slotsPerPage));
        int rowHeight = (slotsPerPage == 8) ? 170 : (slotsPerPage == 6) ? 220 : 320; 

        for (int p = 0; p < pages; p++) {
            // Create Page-Specific Header Input
            JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
            headerPanel.setMaximumSize(new Dimension(800, 75)); 
            headerPanel.setPreferredSize(new Dimension(750, 75));

            JLabel lblPage = new JLabel("Page " + (p + 1) + " Table Header (e.g., SEBELUM) - Leave blank for none:");
            lblPage.setFont(new Font("Segoe UI", Font.BOLD, 12));
            headerPanel.add(lblPage, BorderLayout.NORTH);

            JTextField txtLabel = pageLabelFields.get(p);
            txtLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            headerPanel.add(txtLabel, BorderLayout.CENTER);

            gridPreviewPanel.add(headerPanel);

            // Create Grid for this specific page
            JPanel pageGrid = new JPanel(new GridLayout(rows, cols, 10, 10));
            pageGrid.setMaximumSize(new Dimension(800, rows * rowHeight));
            pageGrid.setPreferredSize(new Dimension(750, rows * rowHeight));

            for (int i = 0; i < slotsPerPage; i++) {
                pageGrid.add(slotPanels.get((p * slotsPerPage) + i));
            }
            gridPreviewPanel.add(pageGrid);
        }

        gridPreviewPanel.revalidate();
        gridPreviewPanel.repaint();
    }

    private void validateAndSave() {
        int totalSlots = (int) spinnerPages.getValue() * getSlotsPerPage();
        int filledSlots = 0;
        for (int i = 0; i < totalSlots; i++) {
            if (slotPanels.get(i).hasImage()) filledSlots++;
        }

        if (filledSlots < totalSlots) {
            JOptionPane.showMessageDialog(this,
                "Not enough images chosen.\nRequired: " + totalSlots + "\nSelected: " + filledSlots,
                "Insufficient Images", JOptionPane.WARNING_MESSAGE);
            return; 
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
            int slotsPerPage = getSlotsPerPage();

            float[][] coordinates;
            if (slotsPerPage == 6) {
                // Corrected 3 Rows x 2 Columns layout
                coordinates = new float[][]{
                    {50f, 508.33f, 247.5f, 216.67f}, {297.5f, 508.33f, 247.5f, 216.67f}, 
                    {50f, 291.66f, 247.5f, 216.67f}, {297.5f, 291.66f, 247.5f, 216.67f}, 
                    {50f, 75f,     247.5f, 216.66f}, {297.5f, 75f,     247.5f, 216.66f}  
                };
            } else if (slotsPerPage == 8) {
                coordinates = new float[][]{
                    {50f, 562.5f, 247.5f, 162.5f}, {297.5f, 562.5f, 247.5f, 162.5f},
                    {50f, 400f,   247.5f, 162.5f}, {297.5f, 400f,   247.5f, 162.5f},
                    {50f, 237.5f, 247.5f, 162.5f}, {297.5f, 237.5f, 247.5f, 162.5f},
                    {50f, 75f,    247.5f, 162.5f}, {297.5f, 75f,    247.5f, 162.5f}
                };
            } else if (slotsPerPage == 4) {
                coordinates = new float[][]{
                    {50f, 400f, 247.5f, 325f}, {297.5f, 400f, 247.5f, 325f},
                    {50f, 75f,  247.5f, 325f}, {297.5f, 75f,  247.5f, 325f}
                };
            } else if (slotsPerPage == 2) {
                coordinates = new float[][]{ {50f, 400f, 495f, 325f}, {50f, 75f,  495f, 325f} };
            } else {
                coordinates = new float[][]{ {50f, 75f, 495f, 650f} };
            }

            for (int p = 0; p < pages; p++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.setNonStrokingColor(41, 105, 176);
                    contentStream.newLineAtOffset(50, 800);

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

                    // Check for the Master Label of THIS SPECIFIC PAGE
                    String groupLabel = pageLabelFields.get(p).getText().trim().toUpperCase();
                    if (!groupLabel.isEmpty()) {
                        contentStream.setStrokingColor(0, 0, 0); 
                        contentStream.setLineWidth(1f);
                        contentStream.addRect(50f, 725f, 495f, 20f); 
                        contentStream.stroke();

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        contentStream.setNonStrokingColor(0, 0, 0);
                        float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(groupLabel) / 1000 * 10;
                        contentStream.newLineAtOffset(50 + (495 - textWidth) / 2, 725 + 6);
                        contentStream.showText(groupLabel);
                        contentStream.endText();
                    }

                    for (int i = 0; i < slotsPerPage; i++) {
                        int globalSlotIndex = (p * slotsPerPage) + i;
                        ImageSlotPanel slot = slotPanels.get(globalSlotIndex);

                        if (!slot.hasImage()) continue; 

                        float boxX = coordinates[i][0];
                        float boxY = coordinates[i][1];
                        float boxW = coordinates[i][2];
                        float boxH = coordinates[i][3];

                        contentStream.setStrokingColor(0, 0, 0);
                        contentStream.setLineWidth(1f);
                        contentStream.addRect(boxX, boxY, boxW, boxH);
                        contentStream.stroke();

                        // Adjust this margin variable to increase/decrease white space in PDF
                        float margin = 3f; 
                        PDImageXObject pdImage = PDImageXObject.createFromFile(slot.getImageFile().getAbsolutePath(), document);
                        contentStream.drawImage(pdImage, boxX + margin, boxY + margin, boxW - (margin * 2), boxH - (margin * 2));
                    }
                }
            } 
            document.save(outputFile);
            mergeWithInvoice(outputFile);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mergeWithInvoice(File claimFile) {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setDialogTitle("Select the Invoice PDF to attach this claim to");
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf");
        chooser.setFileFilter(filter);
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
                for (ImageSlotPanel slot : slotPanels) slot.clearImage(); 

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error merging PDFs: " + ex.getMessage(), "Merge Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Merge cancelled. Standalone claim saved.", "Merge Skipped", JOptionPane.INFORMATION_MESSAGE);
            for (ImageSlotPanel slot : slotPanels) slot.clearImage(); 
        }
    }
}
