package com.mybank.tui;

import com.mybank.domain.Account;
import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.OverDraftAmountException;
import com.mybank.domain.SavingsAccount;

import com.mybank.reporting.ExtendedCustomerReport;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;
import org.fusesource.jansi.*;

/**
 * Sample application to show how jLine can be used.
 *
 * @author sandarenu
 *
 */
/**
 * Console client for 'Banking' example
 *
 * @author Alexander 'Taurus' Babich
 */
public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    private Account loadedAccount;

    private String[] commandsList;

    public void init() {
        commandsList = new String[]{"help", "customers", "customer", "report", "account", "view-balance", "withdraw", "deposit", "save", "exit"};
    }

    public void run() {
        AnsiConsole.systemInstall(); // needed to support ansi on Windows cmd
        printWelcomeMessage();
        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completors = new LinkedList<Completer>();

        completors.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completors));

        LineReader reader = readerBuilder.build();

        String line;
        PrintWriter out = new PrintWriter(System.out);

        while ((line = readLine(reader, "")) != null) {
            if ("help".equals(line)) {
                printHelp();
            } else if ("customers".equals(line)) {
                AttributedStringBuilder a = new AttributedStringBuilder()
                        .append("\nThis is all of your ")
                        .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                        .append(":");

                System.out.println(a.toAnsi());
                if (Bank.getNumberOfCustomers() > 0) {
                    System.out.println("\nLast name\tFirst Name\tBalance");
                    System.out.println("---------------------------------------");
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        System.out.println(Bank.getCustomer(i).getLastName() + "\t\t" + Bank.getCustomer(i).getFirstName() + "\t\t$" + Bank.getCustomer(i).getAccount(0).getBalance());
                    }
                } else {
                    System.out.println(ANSI_RED+"Your bank has no customers!"+ANSI_RESET);
                }

            } else if (line.indexOf("customer") != -1) {
                try {
                    int custNo = 0;
                    if (line.length() > 8) {
                        String strNum = line.split(" ")[1];
                        if (strNum != null) {
                            custNo = Integer.parseInt(strNum);
                        }
                    }                    
                    Customer cust = Bank.getCustomer(custNo);
                    String accType = cust.getAccount(0) instanceof CheckingAccount ? "Checkinh" : "Savings";
                    
                    AttributedStringBuilder a = new AttributedStringBuilder()
                            .append("\nThis is detailed information about customer #")
                            .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                            .append("!");

                    System.out.println(a.toAnsi());
                    
                    System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
                    System.out.println("-------------------------------------------------------");
                    System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + accType + "\t$" + cust.getAccount(0).getBalance());
                } catch (Exception e) {
                    System.out
                        .println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
                }
            } else if ("report".equals(line)) {
                ExtendedCustomerReport customerReport = new ExtendedCustomerReport();
                System.out.println(customerReport.generateStringReport());
            } else if (line.startsWith("account")) {
                String[] args = line.split(" ");
                if (args.length > 2) {
                    try {
                        int customerNumber = Integer.parseInt(args[1]);
                        Customer customer = Bank.getCustomer(customerNumber);
                        int accountIndex = Integer.parseInt(args[2]);
                        loadedAccount = customer.getAccount(accountIndex);
                        System.out.println("Successfully loaded requested account!");
                    } catch (NumberFormatException e) {
                        System.out
                            .println(ANSI_RED + "Can't parse one of two numbers, please try again" + ANSI_RESET);
                    } catch (Exception e) {
                        System.out
                            .println(ANSI_RED + "ERROR! Wrong customer and/or account number!" + ANSI_RESET);
                    }
                } else {
                    System.out
                        .println(ANSI_RED + "Expected 2 arguments, got " + (args.length - 1) + ANSI_RESET);
                }
            } else if ("view-balance".equals(line)) {
                if (loadedAccount != null) {
                    String accountType = "Unknown";
                    if (loadedAccount instanceof SavingsAccount) accountType = "Savings";
                    else if (loadedAccount instanceof CheckingAccount) accountType = "Checking";
                    System.out.println(accountType + " Account: current balance is $" + loadedAccount.getBalance());
                } else {
                    System.out
                        .println(ANSI_RED + "Load customers account with 'account' command before using this one." + ANSI_RESET);
                }
            } else if (line.startsWith("withdraw")) {
                if (loadedAccount != null) {
                    String[] args = line.split(" ");
                    if (args.length > 1) {
                        try {
                            int amountToWithdraw = Integer.parseInt(args[1]);
                            loadedAccount.withdraw(amountToWithdraw);
                            System.out.println("Successfuly withdrawn $" + amountToWithdraw);
                        } catch (NumberFormatException e) {
                            System.out
                                .println(ANSI_RED + "Can't parse amount to withdraw, please try again" + ANSI_RESET);
                        } catch(OverDraftAmountException e) {
                            System.out
                                .println(ANSI_RED + "Can't withdraw that amount, please try again" + ANSI_RESET);
                        } catch (Exception e) {
                            System.out
                                .println(ANSI_RED + "Some unexpected error happened, please try again" + ANSI_RESET);
                        }
                    } else {
                        System.out
                            .println(ANSI_RED + "Expected 1 argument, got " + (args.length - 1) + ANSI_RESET);
                    }
                } else {
                    System.out
                        .println(ANSI_RED + "Load customers account with 'account' command before using this one." + ANSI_RESET);
                }
            } else if (line.startsWith("deposit")) {
                if (loadedAccount != null) {
                    String[] args = line.split(" ");
                    if (args.length > 1) {
                        try {
                            int amountToDeposit = Integer.parseInt(args[1]);
                            loadedAccount.deposit(amountToDeposit);
                            System.out.println("Successfuly deposited $" + amountToDeposit);
                        } catch (NumberFormatException e) {
                            System.out
                                .println(ANSI_RED + "Can't parse amount to deposit, please try again" + ANSI_RESET);
                        } catch (Exception e) {
                            System.out
                                .println(ANSI_RED + "Some unexpected error happened, please try again" + ANSI_RESET);
                        }
                    } else {
                        System.out
                            .println(ANSI_RED + "Expected 1 argument, got " + (args.length - 1) + ANSI_RESET);
                    }
                } else {
                    System.out
                        .println(ANSI_RED + "Load customers account with 'account' command before using this one." + ANSI_RESET);
                }
            } else if ("save".equals(line)) {
                if (saveBankInfo() == true) {
                    System.err.println("Successfully saved bank info");
                } else {
                    System.out
                        .println(ANSI_RED + "Failed to save bank info" + ANSI_RESET);
                }
            } else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;
            } else {
                System.out
                        .println(ANSI_RED + "Invalid command, For assistance press TAB or type \"help\" then hit ENTER." + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private String getCustomerInfo(Customer customer) {
        StringBuilder sb = new StringBuilder()
        .append(customer.getFirstName()).append("\t").append(customer.getLastName()).append("\t").append(customer.getNumberOfAccounts()).append("\n");
        for (int i = 0; i < customer.getNumberOfAccounts(); i++) {
            Account account = customer.getAccount(i);
            if (account instanceof SavingsAccount) {
                sb.append("S\t").append(account.getBalance()).append("\t").append("0,05\n");
            }
            else if (account instanceof CheckingAccount) {
                sb.append("C\t").append(account.getBalance()).append("\t").append("100,00\n");
            }
        }
        return sb.toString();
    }
    
    private boolean saveBankInfo() {
        try {
            StringBuilder sb = new StringBuilder()
            .append(Bank.getNumberOfCustomers()).append("\n\n");
            for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                sb.append(getCustomerInfo(Bank.getCustomer(i))).append("\n");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter("test.dat"));
            writer.write(sb.toString());
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void printWelcomeMessage() {
        System.out
                .println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET + "! \nFor assistance press TAB or type \"help\" then hit ENTER.");

    }

    private void printHelp() {
        System.out.println("help\t\t\t\t\t\t- Show help");
        System.out.println("customers\t\t\t\t\t- Show list of customers");
        System.out.println("customer \'index\'\t\t\t\t- Show customer details");
        System.out.println("report\t\t\t\t\t\t- Show report about all customers");
        System.out.println("account 'customer number' 'account index'\t- load customers account to work with 'view-balance', 'withdraw', 'deposit' commands");
        System.out.println("view-balance\t\t\t\t\t- view balance of loaded account");
        System.out.println("withdraw 'amount to withdraw'\t\t\t- withdraw chosen amount of $ from loaded account");
        System.out.println("deposit 'amount to deposit'\t\t\t- deposit chosen amount of $ to loaded account");
        System.out.println("save\t\t\t\t\t\t- save information about all account in file");
        System.out.println("exit\t\t\t\t\t\t- Exit the app");

    }

    private String readLine(LineReader reader, String promtMessage) {
        try {
            String line = reader.readLine(promtMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET);
            return line.trim();
        } catch (UserInterruptException e) {
            // e.g. ^C
            return null;
        } catch (EndOfFileException e) {
            // e.g. ^D
            return null;
        }
    }

    public static void main(String[] args) {

        Bank.addCustomer("John", "Doe");
        Bank.addCustomer("Fox", "Mulder");
        Bank.getCustomer(0).addAccount(new CheckingAccount(2000));
        Bank.getCustomer(1).addAccount(new SavingsAccount(1000, 3));

        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.run();
    }
}
