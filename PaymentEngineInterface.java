import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PaymentEngineInterface extends Remote {
    int verifyPayment(Transaction t, byte[] signature) throws RemoteException;
    void receiveControlHood(Vector<Transaction> newControlHood) throws RemoteException;
}
