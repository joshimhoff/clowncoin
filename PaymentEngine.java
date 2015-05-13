import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import javax.crypto.*;
import java.net.InetAddress;
import java.util.Vector;

// Payment Class
public class PaymentEngine implements PaymentEngineInterface {
    private Account account;
    private ControlHood controlHood;
    private MarketplaceInterface marketplace;

    public boolean debug = true;


    public PaymentEngine(String marketplaceIP, String thisIP) {
        // In the future, this would load a persistent account from memory or web
        account = new Account();
        controlHood = new ControlHood();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(marketplaceIP);
            for (String s : registry.list()) {
                System.out.printf("* %s in registry\n", s);
            }
            marketplace = (MarketplaceInterface) registry.lookup("Marketplace");

            if (account.getID() == null) {
                // New User. Get and set an account ID. Also register the new ID and IP
                account.setID(marketplace.register(thisIP, account.getPublicKey()));
                
                // Recieve ten ClownCoin from the system as a Welcome Present
            //    welcomePresent();
            }
            bindToRegistry();
        } catch (Exception e) {
            System.err.println("Exception during PaymentEngine binding:");
            e.printStackTrace();
        }

    }

    private void welcomePresent() {
        Transaction t = new Transaction(10, "0", account.getID());
        broadcastTransactionToBeVerified(t, null);

    }

    public void printMarketplace() {
        try {
            System.out.printf("|  Marketplace:\n");
            for (String s : marketplace.getIPs().keySet()) {
                System.out.println("------------");
                System.out.printf("%s, %s\n", s, marketplace.getIPs().get(s));
            }
            if (marketplace.getIPs().size() > 0) {
                System.out.println("------------");
            } else System.out.println(" empty.");
        
        } catch (RemoteException e) {
            System.err.println("RemoteException broadcasting transaction.");
        }

        welcomePresent();
    }

    public void printControlHood(){
            controlHood.printCH();
   
    }
 

    public void bindToRegistry() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            PaymentEngineInterface stub = (PaymentEngineInterface) UnicastRemoteObject.exportObject(this, 0);

            // Find and bind to registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("PaymentEngine", stub);

            System.out.println("PaymentEngine bound.");
        } catch (Exception e) {
            System.err.println("Exception during PaymentEngine binding:");
            e.printStackTrace();
        }
    }

    public void makePayment(String payeeID, double amount) {
        Transaction transaction = null;
        byte[] signature = null;
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initSign(account.getPrivateKey());
            transaction = new Transaction(amount, account.getID(), payeeID);
            dsa.update(transaction.toBytes());
            signature = dsa.sign();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            System.err.println("InvalidKeyException.");
        } catch (SignatureException e) {
            System.err.println("SignatureException.");
        }
        broadcastTransactionToBeVerified(transaction, signature);
        
    }

    private void broadcastTransactionToBeVerified(Transaction transaction, byte[] signedTransaction) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            // Ask network to verify payment
            Registry registry;
            PaymentEngineInterface engine;
            for (String ip : marketplace.getIPs().values()) {
                if (debug) System.out.printf("Broadcasting Transaction to ip %s\n", ip);
                registry = LocateRegistry.getRegistry(ip);
                engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
                engine.verifyTransaction(transaction, signedTransaction);
            }
        } catch (RemoteException e) {
            System.err.println("RemoteException broadcasting transaction.");
        } catch (NotBoundException e) {
            System.err.println("NotBoundException.");
        }
    }

    public int verifyTransaction(Transaction t, byte[] signedTransaction) throws RemoteException {
        if (debug) System.out.println("~ Processing transaction verification ~");
        if (debug) System.out.printf ("~              Payer: %s              \n", t.getPayer());
        if (debug) System.out.printf ("~              Payee: %s              \n", t.getPayee());
        if (debug) System.out.printf ("~             Amount: %f              \n", t.getAmount());
        if (debug) System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");

        boolean verifies = false;

        try {
            if (!t.getPayer().equals("0")) {
                Signature dsa = Signature.getInstance("SHA1withDSA");
                dsa.initVerify(marketplace.getKey(t.getPayer()));
                dsa.update(t.toBytes());
                verifies = dsa.verify(signedTransaction);
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

        for (Transaction existingT : controlHood.getControlHood()) {
            if (existingT.equals(t)) {
                verifies = false;
            }
        }

        if (verifies) {
            System.out.printf("Verified payment of %f CC at %s from %s to %s. Broadcasting updated hood.\n",
                              t.getAmount(), t.getDateString(), t.getPayer(), t.getPayee());
            notifyPayerAndPayeeOfVerification(t);
            broadcastNewControlHood(t);
            return 1;
        }                       // TODO: Handle failed verification
        return 0;
    }

    public void broadcastNewControlHood(Transaction newTransaction) {
        if (debug) System.out.println("Broadcasting new control hood to all");
        Vector<Transaction> newControlHood = controlHood.getControlHood();
        newControlHood.add(newTransaction);
        try {
            Registry registry;
            PaymentEngineInterface engine;
            for (String ip : marketplace.getIPs().values()) {
                registry = LocateRegistry.getRegistry(ip);
                engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
                engine.receiveControlHood(newControlHood);
            }
        } catch (RemoteException e) {               // TODO: Handle failed broadcast
            System.err.println("Remote Exception.");        
        } catch (NotBoundException e) {
            System.err.println("NotBoundException.");
        }
    }

    public void receiveControlHood(Vector<Transaction> newControlHood) throws RemoteException {
        if (newControlHood.size() > controlHood.size()) {
            controlHood.setControlHood(newControlHood);
        }
    }

    public double checkBalance() {
        return controlHood.getBallance(account.getID());
    }

    private void notifyPayerAndPayeeOfVerification(Transaction t) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {

            // Notify the payer of the transaction, unless the payer is root
            if (t.getPayer() != "0") {
                Registry registry = LocateRegistry.getRegistry(marketplace.getIP(t.getPayer()));
                PaymentEngineInterface engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
                engine.receivePaymentNotification(t);
            }

            // Notify the payee of the transaction
            Registry registry = LocateRegistry.getRegistry(marketplace.getIP(t.getPayee()));
            PaymentEngineInterface engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
            engine.receivePaymentNotification(t);
        } catch (RemoteException e) {
            System.err.println("RemoteException.");
        } catch (NotBoundException e) {
            System.err.println("NotBoundException.");
        }
    }

    public void receivePaymentNotification(Transaction t) throws RemoteException {
        if (t.getPayer().equals(account.getID())) {
            System.out.printf("Your payment of %f CC to id %s has been verified.\n", t.getAmount(), t.getPayee());
        } else if (t.getPayee().equals(account.getID())){
            System.out.printf("You have received a verified payment of %f CC from %s.\n", t.getAmount(), t.getPayer());
        } else {
            //TODO Handle?
        }
    }

    public void printAccountInfo() {
        try {
            System.out.printf("\n| Acount Info\n  ---------\n");
            System.out.printf("| UserId: %s\n", account.getID());
            System.out.printf("| IP: %s\n", marketplace.getIP(account.getID()));
            System.out.printf("| Account Balance:%f\n", checkBalance());
            System.out.printf("  ---------\n");
        } catch (RemoteException e) {
            System.err.println("RemoteException.");
        }
    }

}
