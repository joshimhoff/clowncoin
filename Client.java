import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.io.Console;
import java.util.Scanner;
import java.util.InputMismatchException;

public class Client {

    public static void main(String args[]) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("| Welcome to your ClownCoin Wallet |");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("");
        
        PaymentEngine pe = new PaymentEngine();
        // TODO not correct
        pe.bindToRegistry();

        while (true) {
            Console console = System.console();
            Scanner in = new Scanner(System.in);

            System.out.println("Press 1 for your account balance.");
            System.out.println("Press 2 to make a payment.");

            // Parse request
            try {
                int selection = in.nextInt();
                System.out.println("");
                if (selection == 1) {
                    System.out.printf("Account balance is %f ClownCoins\n", pe.checkBalance());
                }
                if (selection == 2) {
                    System.out.println("What IP would you like to send to?");
                    
                    // TODO: display registry?
                    String payee = console.readLine();
                    
                    // TODO: do something with payee ID
                    System.out.println("How much ClownCoin would you like to send?");
                    double amount = Double.parseDouble(console.readLine());

                    System.out.printf("Sending %f ClownCoins to %s...\n", amount, payee);

                    pe.makePayment(payee, amount);
                }
            } catch (InputMismatchException e) {
                System.out.println("Command not parsed correctly.");
            }
        }
    }
}
