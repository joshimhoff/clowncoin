import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import javax.crypto.*;
import java.util.Map;
import java.util.HashMap;

// MarketPlace Class
public class Marketplace implements MarketplaceInterface {
    Map<String, PublicKey> keys;        // UserIDs to public keys
    Map<String, String> ips;            // UserIDs tp IP addresses

    public PaymentEngine() {
        keys = new HashMap<String, PublicKey>(); 
        ips = new HashMap<String, String>();

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            KeyServerInterface stub = (KeyServerInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("KeyServer", stub);
            System.out.println("KeyServer bound.");
        } catch (Exception e) {
            System.err.println("Exception during KeyServer binding:");
            e.printStackTrace();
        }
    }

    String register(String ip, PublicKey key) throws RemoteException {
        // Generate userID
        String newID = keys.size().toString();

        // Add public key and IP to maps        
        keys.put(newID, key);
        ips.put(newID, ip);

        // Return UserID
        return newID;
    }

    Vector<String> getNodes() throws RemoteException {
        Vector<String> nodes = new Vector<String>(ips.keySet().toArray());
        return nodes;
    }    

    PublicKey getKey(String userId) throws RemoteException {
        return keys.get(userId);
    }

    public static void main(String args[]) {
        Marketplace marketplace = new Marketplace();
    }
}
