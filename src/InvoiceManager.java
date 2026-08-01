import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class InvoiceManager {
    private ArrayList<Invoice> invoiceList;
    private ArrayList<String> customerList; 
    
    private final String FILE_NAME = "invoices.txt";
    private final String CUSTOMER_FILE = "customers.txt"; 
    //create ArrayList for invoice list
    public InvoiceManager() {
        invoiceList = new ArrayList<>();
        customerList = new ArrayList<>();
        loadCustomers(); 
        loadInvoices();
    }

    
    //load customer name so no need to rewrite customer name for new invoices
    private void loadCustomers() {
        try {
            File file = new File(CUSTOMER_FILE);
            if (!file.exists()) {
                //add blank name to not trigger error
                customerList.add("");
                saveCustomers();
                return; 
            }
            //read customer name list
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine().trim();
                if (!name.isEmpty()) customerList.add(name);
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }
    //save customer name into file so it can be load even after closing apps
    private void saveCustomers() {
        try {
            FileWriter fw = new FileWriter(CUSTOMER_FILE, false);
            PrintWriter pw = new PrintWriter(fw);
            for (String customer : customerList) {
                pw.println(customer);
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("Error saving customers: " + e.getMessage());
        }
    }

    public void addCustomer(String name) {
        //prevent duplicate name
        if (!customerList.contains(name)) {
            customerList.add(name);
            saveCustomers();
        }
    }

    public ArrayList<String> getCustomers() {
        return customerList;
    }

    //load invoices and its data
    private void loadInvoices() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) return; 

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split("\\|"); 

                if (data.length == 10) {
                    Invoice inv = new Invoice(data[0], data[1], data[2], data[3], data[4], Double.parseDouble(data[5]), Double.parseDouble(data[6]), data[7], data[8], data[9]);
                    invoiceList.add(inv);
                }
            }
            scanner.close();
            saveAllToFile(); 
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }
    //to rewrite edited invoices
    private void saveAllToFile() {
        try {
            FileWriter fw = new FileWriter(FILE_NAME, false);
            PrintWriter pw = new PrintWriter(fw);
            for (Invoice inv : invoiceList) {
                pw.println(inv.toFileString());
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public void addNewInvoice(String date, String id, String jobNo, String customer, String desc, double amount, String remark) {
        Invoice newInv = new Invoice(date, id, jobNo, customer, desc, amount, 0.0, "Unpaid", "-", remark);
        invoiceList.add(newInv); 
        saveAllToFile();         
    }
    //Dynamic status update based on how much has been paid
    public void updatePayment(String targetId, double newAmountPaid, String datePaid) {
        for (Invoice inv : invoiceList) {
            if (inv.getInvoiceId().equals(targetId)) {
                inv.setAmountPaid(newAmountPaid);
                if (newAmountPaid >= inv.getAmount()) {
                    inv.setStatus("Paid");
                    inv.setPaidDate(datePaid);
                } else if (newAmountPaid > 0) {
                    inv.setStatus("Partial");
                    inv.setPaidDate(datePaid);
                } else {
                    inv.setStatus("Unpaid");
                    inv.setPaidDate("-");
                }
                saveAllToFile(); 
                break; 
            }
        }
    }
    //feature to edit/update invoice
    public void updateInvoice(String oldId, String newDate, String newId, String newJobNo, String newCustomer, String newDesc, double newAmount, double newAmtPaid, String newRemark) {
        for (int i = 0; i < invoiceList.size(); i++) {
            Invoice inv = invoiceList.get(i);
            if (inv.getInvoiceId().equals(oldId)) {
                Invoice updatedInv = new Invoice(newDate, newId, newJobNo, newCustomer, newDesc, newAmount, newAmtPaid, inv.getStatus(), inv.getPaidDate(), newRemark);
                invoiceList.set(i, updatedInv); 
                updatePayment(newId, newAmtPaid, inv.getPaidDate()); 
                break;
            }
        }
    }

    public void deleteInvoice(String targetId) {
        invoiceList.removeIf(inv -> inv.getInvoiceId().equals(targetId));
        saveAllToFile();
    }
    
    public ArrayList<Invoice> getAllInvoices() {
        return invoiceList;
    }
}
