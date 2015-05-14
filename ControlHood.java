import java.util.Vector;

// ControlHood Class
public class ControlHood {
    Vector<Transaction> transactions;

    ControlHood() {
        transactions = new Vector<Transaction>();
    }

    void setControlHood(Vector<Transaction> t) { this.transactions = t; }
    Vector<Transaction> getControlHood() { return this.transactions; }

    void addTransaction(Transaction t) { this.transactions.add(t); }

    int size() { return transactions.size(); }

    double getBalance(String userID) {
        double ballance = 0;
        for (Transaction t : transactions) {
            if (t.getPayee().equals(userID)) {
                ballance += t.getAmount();
            }
            if (t.getPayer().equals(userID)) {
                ballance -= t.getAmount();
            }
        }
        return ballance;
    }

    void printCH() {
        System.out.printf("|  Control Hood:");
        for (Transaction t : transactions) {
            System.out.println("-----------------------");
            System.out.printf("Payer: %s\n", t.getPayer());
            System.out.printf("Payee: %s\n", t.getPayee());
            System.out.printf("Amount: %s\n", t.getAmount());
            System.out.printf("Time: %s\n", t.getDate());
        }

        if (transactions.size() > 0) {
            System.out.println("-----------------------");
        } else System.out.println(" empty.");
    }
}