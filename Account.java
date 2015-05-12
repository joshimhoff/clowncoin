import java.io.*;
import java.security.*;
import javax.crypto.*;

// Account Class
public class Account {
    private String accountId;
    private String userFirstName;
    private String userLastName;
    private double balance;
    private KeyPair keys;

    // Toy account for test purposes
    Account() {
        setFirstName("Johnny");
        setLastName("Appleseed");
        setBalance(10.0);

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            keys = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmException.");
        } catch (NoSuchProviderException e) {
            System.err.println("NoSuchProviderException.");
        }
    }

    public void setID(String id) { this.accountId = id; }
    public String getID() { return this.accountId; }
    
    public void setFirstName(String fn) { this.userFirstName = fn; }
    public String getFirstName() { return this.userFirstName; }
    
    public void setLastName(String ln) { this.userLastName = ln; }
    public String getLastName() { return this.userLastName; }

    public void setBalance(double b) { this.balance = b; }
    public void incrementBalance(double b) { this.balance += b; }
    public void decrementBalance(double b) { this.balance -= b; } 
    public double getBalance() { return balance; }

    public PublicKey getPublicKey() { return keys.getPublic(); }
    public PrivateKey getPrivateKey() { return keys.getPrivate(); }
}
