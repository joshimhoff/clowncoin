import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import javax.crypto.*;
import java.net.InetAddress;
import java.util.Vector;

// This class is responsible for all network calls involving nodes in the 
// ClownCoin network. Java RMI is used to implement the calls for ease of
// development.
public class PaymentEngine implements PaymentEngineInterface {
    private Account account;
    public ControlHood controlHood; // TODO break in encapsulation
    private MarketplaceInterface marketplace;
    private Verifier verifier;

    private boolean debug = true;

    /* 
     * SECTION I
     * CONSTRUCTION
     */
    
    // Constructor
    // @param marketplaceIP, the IP address of marketplace as string
    // @param thisIP, the IP address of the client as string
    public PaymentEngine(String marketplaceIP, String thisIP) {
        // Account keeps track of keys and userId
        // NOTE account is not persistent
        account = new Account();

        // ControlHood is the equivalent of Bitcoin's public ledger
        controlHood = new ControlHood();

        // Verifier runs in a seperate thread and is always attempting to verify transactions
        verifier = new Verifier(this);
        Thread verifierThread = new Thread(verifier);
        verifierThread.start();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            // Find registry of marketplace
            Registry registry = LocateRegistry.getRegistry(marketplaceIP);
            marketplace = (MarketplaceInterface) registry.lookup("Marketplace");

            if (account.getID() == null) { // NOTE will always be true since accounts are not persistent
                // Marketplace assigns userIds
                account.setID(marketplace.register(thisIP, account.getPublicKey()));
            }

            // Notify registry of this engine
            bindToRegistry();
        } catch (Exception e) {
            System.err.println("Exception in PaymentEngine constructor.");
            e.printStackTrace();
        }
    }

    // Helper method called by constructor to bind this to registry
    private void bindToRegistry() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            PaymentEngineInterface stub = (PaymentEngineInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("PaymentEngine", stub);
            System.out.println("PaymentEngine bound.");
        } catch (Exception e) {
            System.err.println("Exception in PaymentEngine bindToRegistry.");
            e.printStackTrace();
        }
    }

    /* 
     * SECTION II
     * RMI CALLS
     */
    
    // Updates the verifiers queue of transactions to verify
    // @param t, transaction to be verified
    // @param signedTransaction, t signed with a private key
    public void verifyTransaction(Transaction t, byte[] signedTransaction) throws RemoteException {
        verifier.update(t, signedTransaction, marketplace.getKey(t.getPayer()));
    }

    // Update controlHood if newControlHood is well-formed
    // @param newControlHood, new controlHood from some node in the network
    public void receiveControlHood(Vector<Transaction> newControlHood) throws RemoteException {
        // Makes two checks
        // 1. Last two transactions should be unique
        //     NOTE important because multiple nodes may approve transaction at same time
        //          leading to an invalid control hood
        // 2. Size of controlHood is bigger than last controlHood
        //     NOTE important for dealing with forks in ClownCoin network
        int lastElemIndex = newControlHood.size() - 1;
        boolean repeat = ((lastElemIndex > 0) && 
                          (newControlHood.get(lastElemIndex).equals(newControlHood.get(lastElemIndex-1))));
        if (!repeat && newControlHood.size() > controlHood.size()) {
            controlHood.setControlHood(newControlHood);
        }
    }

    /* 
     * SECTION III
     * CALLS MADE BY CLIENT
     */
    
    // Report balance to client
    public double checkBalance() {
        return controlHood.getBalance(account.getID());
    }

    // Make transaction, sign with private key, and broadcast to network
    // @param payeeID, the id of person to be payed
    // @param amount, how many coins?
    public void makePayment(String payeeID, double amount) {
        Transaction transaction = null;
        byte[] signature = null;
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initSign(account.getPrivateKey());
            transaction = new Transaction(amount, account.getID(), payeeID);
            dsa.update(transaction.toBytes());
            signature = dsa.sign();
        } catch (Exception e) {
            System.err.println("Exception in PaymentEngine makePayment.");
            e.printStackTrace();
        }

        broadcastTransactionToBeVerified(transaction, signature);
    }

    // Broadcast transaction and signedTransaction to all nodes on network
    // @param transaction, to be verified
    // @param signedTransaction, transaction signed with private key
    private void broadcastTransactionToBeVerified(Transaction transaction, byte[] signedTransaction) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Registry registry;
            PaymentEngineInterface engine;
            // Broadcast to all nodes on network
            for (String ip : marketplace.getIPs().values()) {
                if (debug) System.out.printf("Broadcasting Transaction to ip %s.\n", ip);
                registry = LocateRegistry.getRegistry(ip);
                engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
                engine.verifyTransaction(transaction, signedTransaction);
            }
        } catch (Exception e) {
            System.err.println("Exception in PaymentEngine broadcastTransaction.");
            e.printStackTrace();
        }
    }

    // Print out all acount info
    public void printAccountInfo() {
        try {
            System.out.printf("\n| Acount Info\n  ---------\n");
            System.out.printf("| UserId: %s\n", account.getID());
            System.out.printf("| IP: %s\n", marketplace.getIP(account.getID()));
            System.out.printf("| Account Balance:%f\n", checkBalance());
            System.out.printf("  ---------\n");
        } catch (Exception e) {
            System.err.println("Exception in PaymentEngine printAccountInfo.");
            e.printStackTrace();
        }
    }

    // Print all registered IPs in network
    public void printMarketplace() {
        try {
            System.out.printf("|  Marketplace:\n");
            for (String s : marketplace.getIPs().keySet()) {
                System.out.println("------------");
                System.out.printf("%s, %s\n", s, marketplace.getIPs().get(s));
            }
            if (marketplace.getIPs().size() > 0) {
                System.out.println("------------");
            } else {
                System.out.println(" empty.");
            }
        } catch (Exception e) {
            System.err.println("Exception in PaymentEngine printAccountInfo.");
            e.printStackTrace();
        }
    }

    // Print out entire transaction history (controlHood)
    public void printControlHood(){
        controlHood.printCH();
    }

    // Get 10 free coins for testing purposes
    // NOTE coins come from userId 0, the "root" user
    //      root has infinite money for testing purposes and corresponds
    //      to no actual node on the network
    public void freeMoney() {
        Transaction t = new Transaction(10, "0", account.getID());
        broadcastTransactionToBeVerified(t, null);
    }

    /* 
     * SECTION IV
     * CALLS MADE BY VERIFIER
     */
    
    // Broadcast new transaction to all nodes in network
    // @param newTransaction, to be broadcasted
    public void broadcastNewControlHood(Transaction newTransaction) {
        if (debug) System.out.println("Broadcasting new control hood to all.");

        // Verification has succeeded, so add transaction to controlHood
        Vector<Transaction> newControlHood = controlHood.getControlHood();
        newControlHood.add(newTransaction);

        // Broadcast to all via RMI calls
        try {
            Registry registry;
            PaymentEngineInterface engine;
            for (String ip : marketplace.getIPs().values()) {
                registry = LocateRegistry.getRegistry(ip);
                engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
                engine.receiveControlHood(newControlHood);
            }
        } catch (Exception e) {
            System.err.println("Exception in PaymentEngine broadcastNewControlHood.");
            e.printStackTrace();
        }
    }

    // Return balance of some user according to the controlHood
    // @param id, the user ID
    public double checkBalance(String id) {
        return controlHood.getBalance(id);
    }
}
