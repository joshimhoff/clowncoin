import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// Transaction class
public class Transaction implements java.io.Serializable {
    private double amount;
    private String payer;
    private String payee;
    private Date date;

    public Transaction(double amount, String payer, String payee) {
        this.amount = amount;
        this.payer = payer;
        this.payee = payee;
        setDateToCurrent();
    }

    public void setAmount(double a) { this.amount = a; }
    public double getAmount() { return this.amount; }

    public void setPayer(String p) { this.payer = p; }
    public String getPayer() { return this.payer; }

    public void setPayee(String p) { this.payee = p; }
    public String getPayee() { return this.payee; }

    public void setDate(Date d) { this.date = d; }
    public void setDateToCurrent() {this.date = new Date(); }
    public Date getDate() { return this.date; }
    public String getDateString() {
        DateFormat dF = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        return dF.format(this.date);
    }

    public boolean equals(Transaction t) {
        if (this.amount == t.amount &&
            this.payer.equals(t.payer) &&
            this.payee.equals(t.payee) &&
            this.date.compareTo(t.date) == 0 {
            return true;
        } else {
            return false;
        }
    }

    public byte[] toBytes() {
        ByteArrayOutputStream out = null;

        try {
            out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
        } catch (IOException e) {
            System.err.println("IOException.");
        }

    	return out.toByteArray();
    }
}
