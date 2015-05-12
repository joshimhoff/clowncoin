import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KeyServerInterface extends Remote {
    void setKey(String userId, PublicKey key) throws RemoteException;
    PublicKey getKey(String userId) throws RemoteException;
}
