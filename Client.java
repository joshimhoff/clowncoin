import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.io.Console;
import java.util.Scanner;
import java.util.InputMismatchException;

// Client handles UI.
public class Client {

    public static void main(String[] args) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("| Welcome to your ClownCoin Wallet |");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("");
        
        String marketIP, thisIP;

        boolean debug = true;

        Console console = System.console();
        Scanner in = new Scanner(System.in);

        if (args.length != 2)
            System.out.println("USAGE ipOfMarketplace ipOfThisMachine");
        marketIP = args[0];
        thisIP = args[1];

        PaymentEngine pe = new PaymentEngine(marketIP, thisIP);

        // Loop on user input
        while (true) {
            System.out.println("\nEnter 1 for your account balance.");
            System.out.println("Enter 2 to make a payment.");
            System.out.println("Enter 3 to view yout account info.");
            if (debug) System.out.println("Enter 4 to view Marketplace (IPs).");
            if (debug) System.out.println("Enter 5 to view Control Hood.");
            if (debug) System.out.println("Enter 6 for FREE MONEY (10 ClownCoins).");

            // Parse request
            try {
                int selection = in.nextInt();
                System.out.println("");
                if (selection == 1) {
                    System.out.printf("Account balance is %f ClownCoins.\n", pe.checkBalance());
                } else if (selection == 2) {
                    System.out.println("What acountID would you like to send to?");
                    String payee = console.readLine();
                    System.out.println("How much ClownCoin would you like to send?");
                    double amount = Double.parseDouble(console.readLine());
                    System.out.printf("Sending %f ClownCoins to %s.\n", amount, payee);
                    pe.makePayment(payee, amount);
                } else if (selection == 3) {
                    pe.printAccountInfo();
                } else if (selection == 4 && debug) {
                    pe.printMarketplace();
                } else if (selection == 5 && debug) {
                    pe.printControlHood();
                } else if (selection == 6 && debug) {
                    pe.freeMoney();
                }
            } catch (InputMismatchException e) {
                System.out.println("Command not parsed correctly.");
            }
        }
    }
}
