import java.security.*;
import javax.crypto.*;
import java.util.List;
import java.util.LinkedList;

public class Verifier implements Runnable {
    volatile PaymentEngine engine;
    volatile LinkedList<Transaction> transactions;
    volatile LinkedList<byte[]> signedTransactions;
    volatile LinkedList<PublicKey> keys;

    public boolean debug = true;

    public Verifier(PaymentEngine engine_) {
        engine = engine_;

        transactions = new LinkedList<Transaction>();
        signedTransactions = new LinkedList<byte[]>();
        keys = new LinkedList<PublicKey>();
    }

    public void update(Transaction t, byte[] st, PublicKey k) {
        transactions.add(t);
        signedTransactions.add(st);
        keys.add(k);
    }

    public void run() {
        while (true) {
            if (transactions.size() > 0) {
                verify(transactions.removeFirst(), 
                       signedTransactions.removeFirst(), 
                       keys.removeFirst());
            }
        }
    }

    public void verify(Transaction t, byte[] st, PublicKey k) {
        if (debug) System.out.println("~ Processing transaction verification ~");
        if (debug) System.out.printf ("~              Payer: %s              \n", t.getPayer());
        if (debug) System.out.printf ("~              Payee: %s              \n", t.getPayee());
        if (debug) System.out.printf ("~             Amount: %f              \n", t.getAmount());
        if (debug) System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");

        boolean verifies = false;

        // Simulate the proof of work puzzle via random numbers and Thread.sleep
        double waitTime = Math.random() * 25 + 5;
        try {
            Thread.sleep((int)waitTime*1000);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException.");
        }

        try {
            if (!t.getPayer().equals("0")) {
                Signature dsa = Signature.getInstance("SHA1withDSA");
                dsa.initVerify(k);
                dsa.update(t.toBytes());
                verifies = dsa.verify(st);
                if (engine.checkBalance(t.getPayer()) < t.getAmount()) {
                    verifies = false;
                }
            } else {
                if (debug) System.out.println("Verifying transaction from root");
                verifies = true;
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            System.out.println("TEST");
            System.err.println("InvalidKeyException.");
        } catch (SignatureException e) {
            System.err.println("SignatureException.");
        }

        for (Transaction existingT : engine.controlHood.getControlHood()) {
            if (existingT.equals(t)) {
                verifies = false;
            }
        }

        // If some user attempts to pay himself
        if (t.getPayer().equals(t.getPayee())) {
            verifies = false;
        }

        if (verifies) {
            System.out.printf("Verified payment of %f CC at %s from %s to %s. Broadcasting updated hood.\n",
                              t.getAmount(), t.getDateString(), t.getPayer(), t.getPayee());
            /* notifyPayerAndPayeeOfVerification(t); */
            engine.broadcastNewControlHood(t);
        }
    }
}
