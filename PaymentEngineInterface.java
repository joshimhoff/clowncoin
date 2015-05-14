import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

// Documented in PaymentEngine.java
public interface PaymentEngineInterface extends Remote {
    void verifyTransaction(Transaction t, byte[] signature) throws RemoteException;
    void receiveControlHood(Vector<Transaction> newControlHood) throws RemoteException;
}
