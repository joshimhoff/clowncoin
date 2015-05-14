import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.io.Console;
import java.util.Scanner;
import java.util.InputMismatchException;

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

        System.out.println("Please enter IP address of Marketpalce: ");
        marketIP = "139.140.192.154";//in.nextLine();
       

        System.out.println("Registering new user...\n Please enter your IP:");
        thisIP = "139.140.192.154";//in.nextLine();


        PaymentEngine pe = new PaymentEngine(marketIP, thisIP);

        while (true) {
            System.out.println("\nPress 1 for your account balance.");
            System.out.println("Press 2 to make a payment.");
            System.out.println("Press 3 to view yout account info.");
            if (debug) System.out.println("Press 4 to view Marketplace (IPs).");
            if (debug) System.out.println("Press 5 to view Control Hood");

            // Parse request
            try {
                int selection = in.nextInt();
                System.out.println("");
                if (selection == 1) {
                    System.out.printf("Account balance is %f ClownCoins\n", pe.checkBalance());
                } else if (selection == 2) {
                    System.out.println("What acountID would you like to send to?");
                    
                    // TODO: display registry?
                    String payee = console.readLine();
                    
                    // TODO: do something with payee ID
                    System.out.println("How much ClownCoin would you like to send?");
                    double amount = Double.parseDouble(console.readLine());

                    System.out.printf("Sending %f ClownCoins to %s...\n", amount, payee);

                    pe.makePayment(payee, amount);
                } else if (selection == 3) {
                    pe.printAccountInfo();
                } else if (selection == 4 && debug) {
                    pe.printMarketplace();
                } else if (selection == 5 && debug) {
                    pe.printControlHood();
                }

            } catch (InputMismatchException e) {
                System.out.println("Command not parsed correctly.");
            }
        }
    }
}
