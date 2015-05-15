import java.util.Vector;

// The controlHood stores the entire history of legitimate (verified) transactions
// ever made on the network. It stores the history as a Vector of transactions.
// The controlHoods of all nodes in the network should converge even if multiple
// versions of it exist on the network at certain points.
// NOTE corresponds to the public ledger in Bitcoin (control hood is a funnier name...)
public class ControlHood {
    Vector<Transaction> transactions;

    // Constructor
    ControlHood() {
        transactions = new Vector<Transaction>();
    }

    // Getter and setter for data
    // TODO break in encapsulation, update accordingly
    void setControlHood(Vector<Transaction> t) { this.transactions = t; }
    Vector<Transaction> getControlHood() { return this.transactions; }

    // Add transaction to the hood
    // @param t, verified transaction to be added
    void addTransaction(Transaction t) { this.transactions.add(t); }

    // Get size of transaction
    int size() { return transactions.size(); }

    // Get balance of some user
    // @param userID, id of user account being queried
    // @note balance is derived data of control hood
    double getBalance(String userID) {
        double balance = 0;
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            if (t.getPayee().equals(userID)) {
                balance += t.getAmount();
            }
            if (t.getPayer().equals(userID)) {
                balance -= t.getAmount();
            }
        }
        return balance;
    }

    // Print da hood, yo
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
