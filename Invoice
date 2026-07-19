public class Invoice {
    private String date;
    private String invoiceId;
    private String jobNo;
    private String customerName;
    private String description;
    private double amount;
    private double amountPaid; 
    private String status;
    private String paidDate;
    private String remark; 

    public Invoice(String date, String id, String jobNo, String name, String desc, double amt, double amtPaid, String stat, String paidDate, String remark) {
        this.date = date;
        this.invoiceId = id;
        this.jobNo = jobNo;
        this.customerName = name;
        this.description = desc;
        this.amount = amt;
        this.amountPaid = amtPaid;
        this.status = stat;
        this.paidDate = paidDate;
        this.remark = remark;
    }
    //getter
    public String getDate() { 
        return date; 
    }
    public String getInvoiceId() { 
        return invoiceId; 
    }
    public String getJobNo() { 
        return jobNo; 
    }
    public String getCustomerName() { 
        return customerName;
    }
    public String getDescription() { 
        return description; 
    }
    public double getAmount() { 
        return amount; 
    }
    public double getAmountPaid() { 
        return amountPaid; 
    } 
    public String getStatus() { 
        return status; 
    }
    public String getPaidDate() { 
        return paidDate; 
    }
    public String getRemark() { 
        return remark; 
    } 

    //setter
    public void setAmountPaid(double newAmountPaid) { 
        this.amountPaid = newAmountPaid; 
    }
    public void setStatus(String newStatus) { 
        this.status = newStatus; 
    }
    public void setPaidDate(String newPaidDate) {
        this.paidDate = newPaidDate; 
    }
    
    public String toFileString() {
        return date + "|" + invoiceId + "|" + jobNo + "|" + customerName + "|" + description + "|" + amount + "|" + amountPaid + "|" + status + "|" + paidDate + "|" + remark;
    }
}
