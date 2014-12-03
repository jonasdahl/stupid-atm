import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * A class for a bank account
 * @author Jonas Dahl & Nick Nyman
 * @version 1.0
 * @date 2014-11-29
 */
public class Account {
	private int id;
	private String personalID;
	private String name;
	private int cardNumber;
	private int pinCode;
	private int balance;
	private static ArrayList<String> authcodes;
	
	/**
	 * Loads user if exists from the database into this object.
	 * @return true if user existed, false if cardnumber or pincode where wrong
	 */
	public int verifyAndLoad(String cardNo, String pinC) throws IOException {
		// TODO Verify and check if user exists in file, then add info to instance variables.
		String fileText = "";
	    BufferedReader br = null;
	    try {
	    	ATMServer.log("verifyAndLoad() called.");
	    	FileReader fl = new FileReader("src/accounts.txt");
	    	ATMServer.log("File read.");
			br = new BufferedReader(fl);
			ATMServer.log("Database opened.");
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        fileText = sb.toString();
	    	br.close();
	    } catch(IOException e) {
	    	ATMServer.log("Database read failed:");
	    	ATMServer.log(e.getMessage());
	    	throw new IOException();
	    } 
	    
	    for (String line : fileText.split("\n")) {
	    	String[] parts = line.split("\\|");
	    	if (parts.length != 6) {
	    		ATMServer.log("Error in database file: " + Arrays.toString(parts));
	    		continue;
	    	}
	    	if (parts[3].trim().equals(cardNo) && parts[4].trim().equals(pinC)) {
	    		id = Integer.parseInt(parts[0].trim());
	    		personalID = parts[1].trim();
	    		name = parts[2].trim();
	    		cardNumber = Integer.parseInt(parts[3].trim());
	    		pinCode = Integer.parseInt(parts[4].trim());
	    		balance = Integer.parseInt(parts[5].trim());
	    		authcodes = new ArrayList<String>(Arrays.asList("01","03","05","07","09","11","13","15","17","19","21","23","25","27","29","31","33","35","37","39","41","43","45","47","49","51","53","55","57","59","61","63","65","67","69","71","73","75","77","79","81","83","85","87","89","91","93","95","97","99"));
	    		return id;
	    	}
	    }
		ATMServer.log("User not found.");
		return 0;
	}
	
	/**
	 * Checks whether the entered two-digit authorization code is among the pre-approved ones
	 * @param enteredCode, sent from ServerThread <- Client <- user
	 * @return boolean value, true if code OK false if not
	 */
	public boolean authorize(String enteredCode){
		if(authcodes.contains(enteredCode))
			return true;
		else return false;
	}
	
	/**
	 * Reads current balance from the account database.
	 * @return double value of account balance, in kronor
	 * NB: database stores value in Ören so balance must
	 * be converted to Kr before being presented to the client.
	 */
	public int getBalance() {
		return balance;
	}
	
	/**
	 * Updates the balance in the account and the database
	 * @param addValue the deposited
	 * @throws IOException 
	 */
	public String deposit(int addValue) {
		if (addValue < 0)
			return ATMServerThread.NEGATIVE_ERROR;
		this.balance = this.balance + addValue;
		editBalance();
	    return "" + balance;
	}
	
	private boolean editBalance() {
		String fileText = "";
	    BufferedReader br = null;
	    try {
			br = new BufferedReader(new FileReader("src/accounts.txt"));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            String[] parts = line.split("\\|");
	            if (parts.length == 6) {
	            	if (parts[0].trim().equals("" + id)) {
	            		line = id + " | " + personalID + " | " + name + " | " + cardNumber + " | " + pinCode + " | " + balance;
	            	}
	            }
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        fileText = sb.toString();
	    	br.close();
	    	PrintWriter writer = new PrintWriter("src/accounts.txt", "UTF-8");
	    	writer.print(fileText);
	    	writer.close();
	    } catch(IOException e) {
	    	ATMServer.log("Database read failed:");
	    	ATMServer.log(e.getMessage());
	    	return false;
	    } 
		return true;
	}
	
	/**
	 * Considers a withdraw a negative deposit
	 * @param withdrawn amount
	 * @throws IOException 
	 */
	public String withdraw(int withdrawn) throws IOException{
		if (withdrawn < 0)
			return ATMServerThread.NEGATIVE_ERROR;
		if (balance < withdrawn)
			return ATMServerThread.BROKE_ERROR;
		this.balance = this.balance - withdrawn;
		editBalance();
		return "" + balance;
	}
	
	public boolean isSet() {
		if (id > 0)
			return true;
		return false;
	}
}
