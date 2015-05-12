import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PaymentEngineInterface extends Remote {
    int receivePayment(Transaction t, byte[] signature) throws RemoteException;
}
