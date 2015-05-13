import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import javax.crypto.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;
import java.util.Set;

// MarketPlace Class
public class Marketplace implements MarketplaceInterface {
    private Map<String, PublicKey> keys;        // UserIDs to public keys
    private Map<String, String> ips;            // UserIDs tp IP addresses

    public Marketplace() {
        keys = new HashMap<String, PublicKey>(); 
        ips = new HashMap<String, String>();

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            MarketplaceInterface stub = (MarketplaceInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Marketplace", stub);
            System.out.println("Marketplace bound.");
        } catch (Exception e) {
            System.err.println("Exception during MarketPlace binding:");
            e.printStackTrace();
        }
    }

    public String register(String ip, PublicKey key) throws RemoteException {
        // Generate userID
        String newID = Integer.toString(keys.size()+ 1); 

        // Add public key and IP to maps        
        keys.put(newID, key);
        ips.put(newID, ip);

        // Return UserID
        return newID;
    }

    

    // public Set<String> getNodes() throws RemoteException {
    //     return ips.keySet();

    // }    

    public PublicKey getKey(String userId) throws RemoteException {
        return keys.get(userId);
    }

    public Map<String, String> getIPs() throws RemoteException {
        return ips;
    }

    public String getIP(String userId) throws RemoteException {
        return ips.get(userId);
    }

    public static void main(String args[]) {
        Marketplace marketplace = new Marketplace();
    }
}
