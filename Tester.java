import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// Automated tester for measuring performance and checking consistency.
// NOTE assumes that the tester node does not have userId 1
public class Tester {

    public static void main(String[] args) {
        String marketIP, thisIP;

        boolean debug = true;

        if (args.length != 2)
            System.out.println("USAGE ipOfMarketplace ipOfThisMachine");
        marketIP = args[0];
        thisIP = args[1];

        PaymentEngine pe = new PaymentEngine(marketIP, thisIP);

        long startTime = System.currentTimeMillis();
        long endTime = 0;

        // Starting test 1
        // 10 sequential free money requests
        for (int i = 0; i < 10;  i++) {
            pe.freeMoney();
        }

        // Loop forever
        while (true) {
            // Test complete
            if (pe.checkBalance() == 100) {
                endTime = System.currentTimeMillis();
                System.out.println("Time of test: " + (endTime - startTime));
                return;
            }
        }
    }
}
