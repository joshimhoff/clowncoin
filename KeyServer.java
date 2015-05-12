import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

// Payment Class
public class KeyServer implements KeyServerInterface {
    Map<String, PublicKey> keys;

    public PaymentEngine() {
        keys = new HashMap<String, PublicKey>(); 
    }

    void setKey(String userId, PublicKey key) throws RemoteException {
        PublicKey alreadySet = keys.get(userId);
        if (alreadySet == null) {
            keys.put(userId, key); 
        }
    }

    PublicKey getKey(String userId) throws RemoteException {
        return keys.get(userId);
    }

    public static void bindToRegistry() {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            KeyServer server = new KeyServer();
            KeyServerInterface stub = (KeyServerInterface) UnicastRemoteObject.exportObject(server, 0);

            // Find and bind to registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("KeyServer", stub);

            System.out.println("KeyServer bound.");
        } catch (Exception e) {
            System.err.println("Exception during KeyServer binding:");
            e.printStackTrace();
        }
    }
}
