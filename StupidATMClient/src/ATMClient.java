import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
   @author Jonas Dahl & Nick Nyman.
   @version 1.0
   @date 2014-11-28
*/
public class ATMClient {
	/** Specifies port number. */
    private static int portNumber = 8989;
    /** Specifies host name. */
    private static String hostName = "127.0.0.1";
    /** Stores info about language. */
    private static Language t;
    /** Socket to server. */
    private static Socket socket;
    /** Out-stream to server. */
    private static PrintWriter out;
    /** In-stream from server. */
    private static BufferedReader in;
    /** In-stream from standard in. */
    private static BufferedReader stdIn;
    
    /** Some constants for user input. */
    private final static int LOGIN = 0;
    private final static int DEPOSIT= 1;
    private final static int WITHDRAW = 2;
    private final static int BALANCE = 3;
    private final static int QUIT = 4;
    private final static int LANGUAGE = 5;
    
    /**
     * Initializes ATMClient, sets socket, out, in and so on. Exits program if error occurs when
     * initializing those variables and prints some part of 
     */
    public ATMClient() {
    	try {
			t = new Language("enUS"); // Default language is English.
		} catch (IOException e) {
			System.out.println("An error occurred when reading language file.");
			System.exit(1);
		}	
        socket = null;
        out = null;
        in = null;
        stdIn = null;
        
        try {
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (UnknownHostException e) {
            System.err.println(t.t("unknown_host") + " " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println(t.t("no_io") + " " + hostName);
            System.exit(1);
        }
    }
    
    /**
     * Takes care of program initialization. Creates a new ATMClient object and runs it.
     * @param args not used
     * @throws IOException 
     */
    public static void main(String[] args) {
    	ATMClient client = new ATMClient();
    	client.start();   
    }
    
    /**
     * Reads one line (until \n) from standard input. Blocks until there is a line to read.
     * @return the string that has been read
     * @exits if the read gives an IOException
     */
    private String readLine(BufferedReader reader) {
    	String line = "";
    	try {
			line = reader.readLine();
		} catch (IOException e) {
			System.out.println();
			System.exit(1);
		}
    	return line;
    }

    /**
     * Turns text string to double. For example " 443,2" ==> 443.2 or "  44.22  " ==> 44.22
     * @param text the string to be parsed
     * @return the double parsed from the string or -1 if the string couldn't be parsed
     */
    private double grabDoubleFromText(String text) {
    	double value = 0;
    	try {
    		value = Double.parseDouble(text.trim().replace(',', '.'));
    	} catch (NumberFormatException e) {
    		value = -1;
    	}
    	return value;
    }

    /**
     * Turns text string to int. For example " 443" ==> 443 or "  44  " ==> 44
     * @param text the string to be parsed
     * @return the double parsed from the string or -1 if the string couldn't be parsed
     */
    private int grabIntFromText(String text) {
    	int value = 0;
    	try {
    		value = Integer.parseInt(text.trim());
    	} catch (NumberFormatException e) {
    		value = -1;
    	}
    	return value;
    }
    
    /**
     * Runs the client process.
     * @prints menu when menu is supposed to be printed.
     */
    public void start() {
    	String line;
        boolean cont = true;
        while (true) {
            clearScreen();
        	System.out.println(t.t("loginpage"));
        	line = readLine(stdIn);
        	if (line == null) 
        		break;
        	else login();
        	while (cont) {
        		System.out.println(t.t("menu"));
		    	int choice = grabIntFromText(readLine(stdIn));
		    	String response;
		    	switch (choice) {	// TODO Everything, constants for cases, nicer error handling osv.. =D
		    	case DEPOSIT:
		    		deposit();
		    		break;
		    	case WITHDRAW: 
		    		withdraw();
		    		break;
		    	case BALANCE: // Balance
		    		balance();
		    		break;
		    	case QUIT: // Quit
		    		clearScreen();
		    		out.println("Q");	// Send a "Q" for byebye
		    		cont = false;
		    		break;
		    	case LANGUAGE: // Change language
		    		language();
		    		break;
		    	}
        	}
        }
    }
    
    /**
     * Clears everything from terminal window on most platforms.
     * @prints some ascii characters to stdout and flushes.
     */
    public static void clearScreen() {
    	final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }
    
    /**
     * Handles login. 
     * @prints to stdout: clears screen and prints "login" to user, then asks for card number and pincode
     * 		   to socket: sending "L", card number and pin code to socket
     * @reads from stdin once after asked for card number and once after asking for pin code
     * 		  then reads from socket to get response
     */
    public void login() {
    	clearScreen();
		System.out.println(t.t("login"));
		System.out.print(t.t("card_number") + ": ");	// Ask for card number
		String cardNumber = readLine(stdIn);
		System.out.print(t.t("pincode") + ": ");	// Ask for pin code
		String pinCode = readLine(stdIn);
		out.println("L");	// Send an "L" for login
		//TODO: check that input is valid before sending it on
		out.println(cardNumber);  // Send card number
		out.println(pinCode);	  // Send pin code
		String response = readLine(in);
		if (response.startsWith("E")) {
			System.out.println(t.t("error"));
			//TODO: useful output to user in correct language    		
		} else {
			System.out.println(t.t("logged_in"));
		}
		enterToContinue();
    }
    /**
     * Handles deposit.
     * @prints to stdout: some info and questions about the deposit. 
     *         to socket: first "D", then the code and amount
     * @reads from stdin: some answers on questions about the withdrawal
     * 		  from socket: the response, if the deposit was successful or not
     */
    public void deposit() {
    	clearScreen();
		System.out.println(t.t("deposit"));
		System.out.print(t.t("two_digit_code") + ": ");	// Ask for code
		String code = readLine(stdIn);
		System.out.print(t.t("amount") + ": ");	// Ask for amount
		String amount = readLine(stdIn);
		out.println("D");	// Send a "D" for Deposit
		//TODO: check that input is valid before sending it on
		//TODO: send amounts as ints to guarantee bytesize <10. (1char in string = 1byte)
		out.println(code);  // Send code
		out.println(amount);	  // Send amount
		String response = readLine(in);
		if (response.startsWith("E")) {
			System.out.println(t.t("error"));
			//TODO: useful output to user in correct language
		} else {
			System.out.println(t.t("you_have") + " " + amount + " " + t.t("in_cash"));
			System.out.println(t.t("you_have") + " " + ((double)Integer.parseInt(response)/100) + " " + t.t("on_account"));
		}
		enterToContinue();
    }
    
    /**
     * Handles withdraw.
     * @prints to stdout: some info and questions about the withdrawal. 
     *         to socket: first "W", then the code and amount
     * @reads from stdin: some answers on questions about the withdrawal
     * 		  from socket: the response, if the withdrawal was successful or not
     */
    public void withdraw() {
    	clearScreen();
		System.out.println(t.t("withdraw"));
		System.out.print(t.t("two_digit_code") + ": ");	// Ask for code
		String code = readLine(stdIn);
		System.out.print(t.t("amount") + ": ");	// Ask for amount
		String amount = readLine(stdIn); 
		out.println("W");	// Send a "W" for withdrawal
		//TODO: check that input is valid before sending it on
		//TODO: send amounts as ints to guarantee bytesize <10. (1char in string = 1byte)
		out.println(code);  // Send code
		out.println(amount);	  // Send amount
		String response = readLine(in);
		if (response.startsWith("E")) {
			System.out.println(t.t("error"));
			//TODO: useful output to user in correct language
		} else {
			System.out.println(t.t("you_have") + " " + amount + " " + t.t("in_cash"));
			System.out.println(t.t("you_have") + " " + ((double)Integer.parseInt(response)/100) + " " + t.t("on_account"));
		}
		enterToContinue();
    }
    
    /**
     * Handles when the user requests balance.
     * @prints to stdout: info about the state
     * @reads from socket: the response from the server
     */
    private void balance() {
    	clearScreen();
		System.out.println(t.t("balance"));
		out.println("B");	// Send a "B" for balance
		String response = readLine(in);
		if (response.startsWith("E")) {
			System.out.println(t.t("error"));
			//TODO: useful output to user in correct language
		} else {
			System.out.println(t.t("you_have") + " " + ((float)Integer.parseInt(response)/100) + " " + t.t("on_account"));
		}
		enterToContinue();
    }
    
    /**
     * Handles when the user requests to change language.
     * @prints to stdout: the language question
     * @reads from stdin: the language user wants
     */
    private void language() {
    	clearScreen();
		System.out.println(t.t("language_question"));	// Ask for language
		String answer = readLine(stdIn);
		try {
			t = new Language(answer);
		} catch (IOException e) {} // TODO Handle error
    }
    
    /**
     * @prints a text that tells the user to push enter to continue
     * @reads the enter, or anything to next newline
     */
    private void enterToContinue() {
    	System.out.print(t.t("enter"));
		readLine(stdIn); // Throw away enter
	}
}   
