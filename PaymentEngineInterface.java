import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;


public interface PaymentEngineInterface extends Remote {
    int verifyTransaction(Transaction t, byte[] signature) throws RemoteException;
    void receiveControlHood(Vector<Transaction> newControlHood) throws RemoteException;
    void receivePaymentNotification(Transaction t) throws RemoteException;
    void printControlHood() throws RemoteException;
    void printMarketplace() throws RemoteException;
}
