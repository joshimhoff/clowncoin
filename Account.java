import java.io.*;
import java.security.*;
import javax.crypto.*;

// Class represents an account on the ClownCoin network. Stores two things.
// 1. An ID that is assigned by the centralized marketplace.
// 2. A public and private key used to protect against fraud.
//     NOTE if the private key is lost, coins cannot ever be recovered.
public class Account {
    private String accountId;
    private KeyPair keys;

    // Constructor
    Account() {
        try {
            // Generate cryptographic keys
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            keys = keyGen.generateKeyPair();
        } catch (Exception e) {
            System.err.println("Exception in Account constructor.");
            e.printStackTrace();
        }

        // NOTE accountId is null until marketplace assigns one
        accountId = null;
    }

    // Getters and setters
    public void setID(String id) { this.accountId = id; }
    public String getID() { return this.accountId; }

    public PublicKey getPublicKey() { return keys.getPublic(); }
    public PrivateKey getPrivateKey() { return keys.getPrivate(); }
}
