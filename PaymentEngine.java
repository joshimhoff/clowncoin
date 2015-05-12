import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress

// Payment Class
public class PaymentEngine implements PaymentEngineInterface {
    private Account account;
    private MarketplaceInterface marketplace;

    public PaymentEngine(String marketplacIP) {
        // In the future, this would load a persistent account from memory or web
        account = new Account();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(marketplacIP);
            marketplace = (MarketplaceInterface) registry.lookup("Marketplace");

            if (account.getID == null) {
                // New User. Get and set an account ID. Also register the new 
                account.setID(marketplace.regsiter(InetAddress.getAddress(), account.getPublicKey()));
            }
            bindToRegistry();
        } catch (RemoteException e) {
            System.err.println("Remote exception.");
        }
    }

    public void makePayment(String payeeIP, double amount) {
        Signature dsa = Signature.getInstance("SHA1withDSA");
        dsa.initSign(account.getPrivateKey());
        Transaction transaction = new Transaction(amount, account.getID(), payeeIP);
        dsa.update(transaction.toBytes());
        byte[] signature = dsa.sign();
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(payeeIP);
            engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
            engine.receivePayment(transaction, signature);
            System.out.printf("Payment of %f CC sent at %s to %s.\n",
                  t.getAmount(), t.getDateString(), t.getPayee());
        } catch (RemoteException e) {
            System.err.println("Remote exception.");
        }
    }

    public int receivePayment(Transaction t, byte[] signature) throws RemoteException {
        boolean verifies = false;

        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initVerify(marketplace.getKey(t.getPayer()));
            dsa.update(t.toBytes());
            verifies = dsa.verify(signature);
        } catch (RemoteException e) {
            System.err.println("Remote exception.");
        }

        if (verifies) {
            System.out.printf("Payment of %f CC reciever at %s from %s.\n",
                              t.getAmount(), t.getDateString(), t.getPayer());
            return 1;
        }
        return 0;
    }

    public double checkBalance() {
       //TODO: get balance from clown block
        return 0.0;
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
}
