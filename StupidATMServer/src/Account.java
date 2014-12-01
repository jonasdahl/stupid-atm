import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

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
	    		return id;
	    	}
	    }

		ATMServer.log("User not found.");
		return 0;
	}
	
	//TODO Getters. Retrieve data from db-file.
	
	/**
	 * Reads current balance from the account database.
	 * @return double value of account balance, in kronor
	 * NB: database stores value in Ören so balance must
	 * be converted to Kr before being presented to the client.
	 */
	public int getBalance() {
		return balance;
	}
	
	// TODO Setters. If we want to update something, we also have to update the database file!
	
	/**
	 * Updates the balance in the account and the database
	 * @param addValue the deposited
	 * @throws IOException 
	 */
	public void deposit(int addValue) throws IOException {
		this.balance = this.balance + addValue;
		
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
	    	throw new IOException();
	    } 
	    
		//TODO: change balance in db
	}
	/**
	 * Considers a withdraw a negative deposit
	 * @param withdrawn amount
	 * @throws IOException 
	 */
	public int withdraw(int withdrawn) throws IOException{
		if (balance < withdrawn)
			return -1;
		deposit(0-withdrawn);
		return balance;
	}
	
	public boolean isSet() {
		if (id > 0)
			return true;
		return false;
	}
}
