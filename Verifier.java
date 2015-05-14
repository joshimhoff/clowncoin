import java.security.*;
import javax.crypto.*;
import java.util.List;
import java.util.LinkedList;

// Waits on new transactions to verify and when found, attempts verification
// NOTE runs in a seperate thread from the paymentEngine, they communicate via
//      the update method.
public class Verifier implements Runnable {
    // Volatile since will be updated by multiple threads
    volatile PaymentEngine engine;
    volatile LinkedList<Transaction> transactions;
    volatile LinkedList<byte[]> signedTransactions;
    volatile LinkedList<PublicKey> keys;

    public boolean debug = true;

    // Constructor
    // @param engine_, the paymentEngine, used to broadcast the new CH after verification
    public Verifier(PaymentEngine engine_) {
        engine = engine_;

        transactions = new LinkedList<Transaction>();
        signedTransactions = new LinkedList<byte[]>();
        keys = new LinkedList<PublicKey>();
    }

    // Add transaction to verifier's queue
    // @param t, transaction to verify
    // @param st, signed transaction
    // @param k, public key of sender
    public void update(Transaction t, byte[] st, PublicKey k) {
        transactions.add(t);
        signedTransactions.add(st);
        keys.add(k);
    }

    // Poll transaction queue, if new transaction, attempt to verify
    public void run() {
        // NOTE polling is not the best way to do this, but we were
        //      strapped for time...
        while (true) {
            if (transactions.size() > 0) {
                verify(transactions.removeFirst(), 
                       signedTransactions.removeFirst(), 
                       keys.removeFirst());
            }
        }
    }

    // Verify (or reject) a transaction
    // @param t, transaction to verify
    // @param st, signed transaction
    // @param k, public key of sender
    public void verify(Transaction t, byte[] st, PublicKey k) {
        if (debug) System.out.println("~ Processing transaction verification ~");
        if (debug) System.out.printf ("~              Payer: %s              \n", t.getPayer());
        if (debug) System.out.printf ("~              Payee: %s              \n", t.getPayee());
        if (debug) System.out.printf ("~             Amount: %f              \n", t.getAmount());
        if (debug) System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");

        boolean verifies = false;

        try {
            if (!t.getPayer().equals("0")) {
                Signature dsa = Signature.getInstance("SHA1withDSA");
                dsa.initVerify(k);
                dsa.update(t.toBytes());
                verifies = dsa.verify(st);
            } else {
                if (debug) System.out.println("Verifying transaction from root");
                verifies = true;
            }
        } catch (Exception e) {
            System.err.println("Exception in Verifier verify.");
            e.printStackTrace();
        }

        // Reject if duplicate transaction
        for (Transaction existingT : engine.controlHood.getControlHood()) {
            if (existingT.equals(t)) {
                verifies = false;
            }
        }

        // TODO check that payer and payee exist

        // Reject if transaction amount is more than balance
        if (engine.checkBalance(t.getPayer()) < t.getAmount()) {
            verifies = false;
        }

        // Reject some user attempting to pay himself
        if (t.getPayer().equals(t.getPayee())) {
            verifies = false;
        }

        // Reject transaction if amount is zero or negative
        if (t.getAmount() <= 0)
            verifies = false;
        }

        if (verifies) {
            // Simulate the proof of work puzzle via random numbers and Thread.sleep
            double waitTime = Math.random() * 25 + 5;
            try {
                Thread.sleep((int)waitTime*1000);
            } catch (InterruptedException e) {
                System.err.println("InterruptedException.");
            }

            System.out.printf("Verified payment of %f CC at %s from %s to %s. Broadcasting updated hood.\n",
                              t.getAmount(), t.getDateString(), t.getPayer(), t.getPayee());
            engine.broadcastNewControlHood(t);
        }
    }
}
