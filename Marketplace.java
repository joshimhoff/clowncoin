import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

// Payment Class
public class Marketplace implements MarketplaceInterface {
    Map<String, PublicKey> keys;        // UserIDs to public keys
    Map<String, String> ips;            // UserIDs tp IP addresses

    public PaymentEngine() {
        keys = new HashMap<String, PublicKey>(); 
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
        Vector<String> = new Vector<String>(ips.keySet().toArray());
    }    

    PublicKey getKey(String userId) throws RemoteException {
        return keys.get(userId);
    }

    public static void bindToRegistry() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            Marketplace server = new Marketplace();
            MarketplaceInterface stub = (MarketplaceInterface) UnicastRemoteObject.exportObject(server, 0);

            // Find and bind to registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Marketplace", stub);

            System.out.println("Marketplace bound.");
        } catch (Exception e) {
            System.err.println("Exception during Marketplace binding:");
            e.printStackTrace();
        }
    }
}
