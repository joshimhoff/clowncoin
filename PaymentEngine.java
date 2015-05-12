import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import javax.crypto.*;
import java.net.InetAddress;

// Payment Class
public class PaymentEngine implements PaymentEngineInterface {
    private Account account;
    private MarketplaceInterface marketplace;

    public PaymentEngine(String marketplaceIP) {
        // In the future, this would load a persistent account from memory or web
        account = new Account();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(marketplaceIP);
            marketplace = (MarketplaceInterface) registry.lookup("Marketplace");

            if (account.getID() == null) {
                // New User. Get and set an account ID. Also register the new 
                account.setID(marketplace.register(InetAddress.getLocalHost().getHostAddress(), account.getPublicKey()));
            }
        } catch (Exception e) {
            System.err.println("Exception during PaymentEngine binding:");
            e.printStackTrace();
        }

        bindToRegistry();
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

    public void makePayment(String payeeIP, double amount) {
        Transaction transaction = null;
        byte[] signature = null;
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initSign(account.getPrivateKey());
            transaction = new Transaction(amount, account.getID(), payeeIP);
            dsa.update(transaction.toBytes());
            signature = dsa.sign();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            System.err.println("InvalidKeyException.");
        } catch (SignatureException e) {
            System.err.println("SignatureException.");
        }
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(payeeIP);
            PaymentEngineInterface engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
            engine.receivePayment(transaction, signature);
            System.out.printf("Payment of %f CC sent at %s to %s.\n", 
                              transaction.getAmount(), transaction.getDateString(), transaction.getPayee());
        } catch (RemoteException e) {
            System.err.println("RemoteException.");
        } catch (NotBoundException e) {
            System.err.println("NotBoundException.");
        }
    }

    public int receivePayment(Transaction t, byte[] signature) throws RemoteException {
        boolean verifies = false;

        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initVerify(marketplace.getKey(t.getPayer()));
            dsa.update(t.toBytes());
            verifies = dsa.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            System.out.println("TEST");
            System.err.println("InvalidKeyException.");
        } catch (SignatureException e) {
            System.err.println("SignatureException.");
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

}
