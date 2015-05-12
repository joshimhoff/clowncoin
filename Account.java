import java.io.*;
import java.security.*;
import javax.crypto.*;

// Account Class
public class Account {
    private String accountId;
    private KeyPair keys;

    // Toy account for test purposes
    Account() {
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

        accountId = null;
    }

    public void setID(String id) { this.accountId = id; }
    public String getID() { return this.accountId; }

    public PublicKey getPublicKey() { return keys.getPublic(); }
    public PrivateKey getPrivateKey() { return keys.getPrivate(); }
}
