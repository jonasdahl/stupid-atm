import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

/**
 * The server thread. One thread for one client connection. Started by the server when new
 * connection is created. TODO Complete
 * @author Jonas Dahl & Nick Nyman
 * @version 1.0
 * @date 2014-11-28
 */
public class ATMServerThread extends Thread {
	/** The name of the thread. */
	private final static String threadName = "ATMServerThread";
	
	/** Some constants for receiving from client. */
	private final static String LOGIN = "L";
	private final static String BALANCE = "B";
	private final static String WITHDRAW = "W";
	private final static String DEPOSIT = "D";
	private final static String EXIT = "Q";

	/** Other constants, errors. */
	private final static String ERROR = "E ";
	private final static String AUTH_ERROR = ERROR + 1;
	private final static String INACTIVE_ERROR = ERROR + 2;
	
	/** Other constants, status codes. */
	private final static int TIMEOUT_TIME = 1;	// In minutes
	private final static int STATUS_OK = 1;
	
	/** Holds the socket so we always have the connection to client. */
    private Socket socket = null;
    /** The reader, where we get things the client sends us. */
    private BufferedReader in;
    /** The writer, where we can send something and it'll fly away to the client. */
    PrintWriter out;
    /** Saves the date for the last activity. */
	private Date lastActivity;
	
    /**
     * Constructor. Calls super constructor with a name of the thread and saves the
     * parameter.
     * @param socket the socket to the client.
     */
    public ATMServerThread(Socket socket) {
        super(threadName);
        this.socket = socket;
    }

    /**
     * Reads one line from the in-reader and returns it as a string. Blocks until string is received.
     * @return the string on input.
     * @prints logs the string to with log function on success.
     * @throws IOException if error occurs on read (se BufferedReader docs).
     */
    private String readLine() throws IOException {
    	String str;
    	while ((str = in.readLine()) != null) {
    		ATMServer.log("Read line '" + str + "'");
            return str;
    	}
    	return null;
    }

    /**
     * The run method is invoked when thread starts, ie when the client connects.
     * Starts a loop that checks incoming input.
     * @prints logs about client status, like when connected, disconnected and so on TODO :)
     */
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader
                (new InputStreamReader(socket.getInputStream()));
            String inputLine;
            boolean listening = true;
            while (listening) {
                inputLine = readLine();	// Reads input from client
                switch (inputLine) {
                case LOGIN:
                	handleLogin();
                    break;
                case BALANCE:
                	handleBalance();
                    break;
                case WITHDRAW: 
                	handleWithdraw();
                    break;
                case DEPOSIT:
                	handleDeposit();
                    break;
                case EXIT:
                	listening = false;
                	break;
                }
            }
            out.close();
            in.close();
            socket.close();
        } catch (IOException e){
            ATMServer.log("An error occurred when listening to socket.");
        }
        ATMServer.log("Client disconnected.");
    }
    
    /**
     * TODO HELA FUNKTIONEN
     * @param cardNumber card number to verify
     * @param pinCode pin code to verify
     * @return Member id > 0 on success, 0 on fail
     */
    private int verify(String cardNumber, String pinCode) {
    	if (cardNumber.equals("123456") && pinCode.equals("1234"))
    		return 1;
    	return 0;
    }
    
    /**
     * Checks if session is active, ie if date was set less than TIMEOUT_TIME mins ago.
     * @prints logs when new activation time is set
     * @return true if session is active, false otherwise
     */
    private boolean active() {
    	if (lastActivity == null) // There isn't even a login back in time!
    		return false;
    	
    	Date now = new Date();
    	Calendar cal = Calendar.getInstance();
        cal.setTime(now); 
        cal.add(Calendar.MINUTE, -TIMEOUT_TIME); // Cal is now TIMEOUT mins back in time!
        Date nowMinusTimeout = cal.getTime();
        Date lastActivity = this.lastActivity;
    	
    	if (nowMinusTimeout.compareTo(lastActivity) < 0) {
            this.lastActivity = now;	// Update last activity, because this is an activity
    		return true;
    	}
    	return false;
    }
    
    /**
     * TODO HELA FUNKTIONEN. VI SPARAR MONEYZ I ÖREN (INTS/LONGS/SHORTS/T-SHIRTS) OK, SÅ SLIPPER VI FLOATS OCH ATT PENGAR KAN BLI 1345,343453453424334657687564343 kr.
     * class Account ansvarar för conversion -> kr, allt denna behöver göra är anropa rätt Account's getBalance()
     * @param sessionId
     * @return the amount of money on the customer's account * 100 (159,59 kr is 15959)
     */
    private int getBalance() {
    	return 4711;
    }
    
    // TODO Parse int, do the withdraw, return status code
    private int withdraw(String amount) {
    	return STATUS_OK;
    }
    
    // TODO Parse int, do the deposit, return status code
    private int deposit(String amount) {
    	return STATUS_OK;
    }
    
    private void handleLogin() throws IOException {
    	ATMServer.log("Login requested.");
    	String cardNumber = readLine();
    	String pinCode = readLine();
    	int response = verify(cardNumber, pinCode);
    	if (response > 0) {
    		out.println(response);
        	this.lastActivity = new Date();
        	ATMServer.log("Login successful.");
    	} else {
    		out.println(AUTH_ERROR);
        	ATMServer.log("Login error.");
    	}
    }
    
    private void handleBalance() {
    	ATMServer.log("Balance requested.");
    	if (active()) {
    		out.println(getBalance());
        	ATMServer.log("Balance sent.");
    	} else {
    		out.println(INACTIVE_ERROR);
        	ATMServer.log("User inactive, no balance sent.");
    	}
    }
    
    private void handleWithdraw() throws IOException {
    	ATMServer.log("Withdraw requested.");
    	String code = readLine(); // TODO Check in list of two-digit passphrases
    	String amount = readLine();
    	if (active()) {
    		int result = withdraw(amount);
    		out.println(result);
        	ATMServer.log("Withdraw maybe successful (" + result + ").");
    	} else {
    		out.println(INACTIVE_ERROR);
        	ATMServer.log("User inactive, withdraw not accepted.");
    	}
    }
    
    // TODO: Gör om gör rätt, detta är bara en kopia av withdraw.
    private void handleDeposit() throws IOException {
    	ATMServer.log("Deposit requested.");
    	String code = readLine(); // TODO Check in list of two-digit passphrases
    	String amount = readLine();
    	if (active()) {
    		int result = deposit(amount);
    		out.println(result);
        	ATMServer.log("Deposit maybe successful (" + result + ").");
    	} else {
    		out.println(INACTIVE_ERROR);
        	ATMServer.log("User inactive, deposit not accepted.");
    	}
    }
}
