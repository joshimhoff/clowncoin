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

// The centralized marketplace. Has two major purposes.
// 1. Acts as a key server for the public key crytography.
// 2. Assigns user IDs and keeps track of users on ClownCoin network.
public class Marketplace implements MarketplaceInterface {
    private Map<String, PublicKey> keys;        // userIDs to public keys
    private Map<String, String> ips;            // userIDs to IP addresses

    // Constructor
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
            System.err.println("Exception in MarketPlace binding.");
            e.printStackTrace();
        }
    }

    // Register a new node on network
    // @param ip, the IP address of new node
    // @param key, the public key of new node
    // @returns the assigned user ID
    public String register(String ip, PublicKey key) throws RemoteException {
        // Generate userID
        String newID = Integer.toString(keys.size()+ 1); 

        // Add public key and IP to maps        
        keys.put(newID, key);
        ips.put(newID, ip);

        // Return UserID
        return newID;
    }

    // Get public key of some user
    // @param userId, the ID of the user
    public PublicKey getKey(String userId) throws RemoteException {
        return keys.get(userId);
    }

    // Get IP of some user
    // @param userId, the ID of the user
    // @note only used for testing purposes, not a necessary function for a
    //       working ClownCoin network
    public String getIP(String userId) throws RemoteException {
        return ips.get(userId);
    }

    // Get IPs of all nodes on the network
    public Map<String, String> getIPs() throws RemoteException {
        return ips;
    }

    public static void main(String args[]) {
        Marketplace marketplace = new Marketplace();
    }
}
