import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.*;
import javax.crypto.*;
import java.util.Set;
import java.util.Map;

public interface MarketplaceInterface extends Remote {
    String register(String ip, PublicKey key) throws RemoteException;
    // Set<String> getNodes() throws RemoteException;
    PublicKey getKey(String userId) throws RemoteException;
    String getIP(String userId) throws RemoteException;
    Map<String, String> getIPs() throws RemoteException;

}
