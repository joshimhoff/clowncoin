import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.*;
import javax.crypto.*;

public interface KeyServerInterface extends Remote {
    void setKey(String userId, PublicKey key) throws RemoteException;
    PublicKey getKey(String userId) throws RemoteException;
}
