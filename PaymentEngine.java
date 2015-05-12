import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

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
            Registry registry = LocateRegistry.getRegistry(TODO);
            keyServer = (KeyServerInterface) registry.lookup("KeyServer");
            keyServer.setKey(account.getID(), account.getPublicKey());
        } catch (RemoteException e) {
            System.err.println("Remote exception.");
        }
    }

    public void makePayment(String payeeIP, double amount) {
        Signature dsa = Signature.getInstance("SHA1withDSA");
        dsa.initSign(account.getPrivateKey());
        Transaction transaction = new Transaction(amount, account.getLastName(), payeeIP);
        dsa.update(transaction.toBytes());
        byte[] signature = dsa.sign();
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(payeeIP);
            engine = (PaymentEngineInterface) registry.lookup("PaymentEngine");
            engine.receivePayment(transaction, signature);
            account.decrementBalance(amount);
        } catch (RemoteException e) {
            System.err.println("Remote exception.");
        }
    }

    public int receivePayment(Transaction t, byte[] signature) throws RemoteException {
        boolean verifies = false;

        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initVerify(keyServer.getKey(t.getPayer()));
            dsa.update(t.toBytes());
            verifies = dsa.verify(signature);
        } catch (RemoteException e) {
            System.err.println("Remote exception.");
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
