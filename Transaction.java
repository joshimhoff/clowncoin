import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// Class represents a transaction.
// NOTE storing a date makes transactions unique enough for our purposes
//      (educational) since the date stores time in millisecond precision
public class Transaction implements java.io.Serializable {
    private double amount;
    private String payer;
    private String payee;
    private Date date;

    // Constructor
    // @param amount, the amount of coin to be transferred
    // @param payer, userId of the node paying as a string
    // @param payee, userId of the node receiving funds as a string
    public Transaction(double amount, String payer, String payee) {
        this.amount = amount;
        this.payer = payer;
        this.payee = payee;
        this.date = new Date();
    }

    // Getters and setters
    public void setAmount(double a) { this.amount = a; }
    public double getAmount() { return this.amount; }

    public void setPayer(String p) { this.payer = p; }
    public String getPayer() { return this.payer; }

    public void setPayee(String p) { this.payee = p; }
    public String getPayee() { return this.payee; }

    public void setDate(Date d) { this.date = d; }
    public Date getDate() { return this.date; }
    public String getDateString() {
        DateFormat dF = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        return dF.format(this.date);
    }

    // Checks if two transactions are equal, used to reject duplicate transactions
    // @param t, transaction to compare against
    public boolean equals(Transaction t) {
        if (this.amount == t.amount &&
            this.payer.equals(t.payer) &&
            this.payee.equals(t.payee) &&
            (this.date.compareTo(t.date) == 0)) {
            return true;
        } else {
            return false;
        }
    }

    // Write to byte[] for digital signing purposes
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
