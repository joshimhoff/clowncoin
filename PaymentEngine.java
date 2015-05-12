import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import javax.crypto.*;

// Payment Class
public class PaymentEngine implements PaymentEngineInterface {
    private Account account;
    private KeyServerInterface keyServer;

    public PaymentEngine() {
        account = new Account();

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1");
            keyServer = (KeyServerInterface) registry.lookup("KeyServer");
            keyServer.setKey(account.getID(), account.getPublicKey());
        } catch (RemoteException e) {
            System.err.println("RemoteException.");
        } catch (NotBoundException e) {
            System.err.println("NotBoundException.");
        }
    }

    public void makePayment(String payeeIP, double amount) {
        Transaction transaction = null;
        byte[] signature = null;
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initSign(account.getPrivateKey());
            transaction = new Transaction(amount, account.getLastName(), payeeIP);
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
            account.decrementBalance(amount);
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
            dsa.initVerify(keyServer.getKey(t.getPayer()));
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
            account.incrementBalance(t.getAmount());
            System.out.printf("Payment of %f CC reciever at %s from %s.\n",
                              t.getAmount(), t.getDateString(), t.getPayer());
            return 1;
        }
        return 0;
    }

    public double checkBalance() {
        return account.getBalance();
    }

    // TODO not correct
    public static void bindToRegistry() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            PaymentEngine engine = new PaymentEngine();
            PaymentEngineInterface stub = (PaymentEngineInterface) UnicastRemoteObject.exportObject(engine, 0);

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
