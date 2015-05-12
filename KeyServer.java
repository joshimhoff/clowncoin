import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import javax.crypto.*;
import java.util.Map;
import java.util.HashMap;

// Payment Class
public class KeyServer implements KeyServerInterface {
    Map<String, PublicKey> keys;

    public KeyServer() {
        keys = new HashMap<String, PublicKey>(); 

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

    public void setKey(String userId, PublicKey key) throws RemoteException {
        PublicKey alreadySet = keys.get(userId);
        if (alreadySet == null) {
            keys.put(userId, key); 
        }
    }

    public PublicKey getKey(String userId) throws RemoteException {
        return keys.get(userId);
    }

    public static void main(String args[]) {
        KeyServer keyServer = new KeyServer();
    }
}
