package GROUP_V0_5;

/*	
 * Group Message V 0.5c
 * 
 * Kaleo Ngo
 * Principals of Java G/T
 * 
 * --- Do not user Start. Use MessageFrame for everything after V 0.5 ---
 * 
 * 
 * Big thanks to Mr. Marcus Yarbrough for helping me get a few things straight.
 * I used the very that he made as a reference:
 * 		 https://github.com/chispade/SimpleMessenger/tree/master/src/main/java
 * 
 * 
 * Checklist is as follows:
 * 	1. Groups with names --
 * 	2. User names --
 * 	3. ALLOW MULTIPLE CONNECIONS --
 * 	4. Leave / join without exiting the app (Client only) --
 * 	5a. Host Migration --
 *  5b. Add a lot more commands --
 *  5c. Add a logging option --
 *  5d. fix ban 
 *  5e. Finalize this version
 * 	6. Move to a GUI
 * 	7. Make it pretty
 * 	8. Add settings button
 * 	9. Allow manual group size -- kinda skipped ahead haha
 * 	10. Sent and read confirmation
 * 	11. Different way to send data about the client
 * 	18. Notifications??
 * 	19. Multiple groups at a time????
 * 	20. Encryption????????????
 * 
 * 
 * The START class is the start of the program. It is the one that gets everything 
 * squared around by getting the group name, port nmber, etc. set up.
 */

import java.util.Scanner;

public class Start {

	private static String[] Alpha = { "", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
			"p", "q", "u", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "4", "5", "6", "7", "8", "9",
			"_" };

	private static int port;
	private static String GROUP_NAME;
	private static String GROUP_SIZE = "10";

	private static Scanner scan;
	private static String userName;

	public static void main(String[] args) {

		scan = new Scanner(System.in);
		port = 5;

		System.out.print("Only A-Z, a-z, 0-9, and '_' will be accepted. Other characters will be deleted."
				+ "\n Enter nothing for group name OR username to exit." + "\n  Group Name: ");

		GROUP_NAME = scan.nextLine();
		if (GROUP_NAME.equals(""))
			System.exit(100);

		System.out.print("\n  Username: ");
		userName = scan.nextLine();

		System.out.println("\n");

		if (userName.equals(""))
			System.exit(100);
		Server.main(new String[] { getPort(GROUP_NAME), GROUP_SIZE, userName, GROUP_NAME });
	}

	// get port method using random math (literally) to come up with a port
	// number that will always be within the limit which is about from 0 -
	// 65500. it compares the characters from the string that it gets
	// from the scanner to an array up top that has the characters. Then it uses
	// the spaces of the characters to come up with the port.
	public static String getPort(String GROUP_NAME) {
		for (int i = 0; i < GROUP_NAME.length(); i++) {
			for (int p = 1; p < 38; p++) {
				if (Alpha[p].equals(GROUP_NAME.substring(i, i + 1).toLowerCase()))
					port += Math.abs((p - (4 * p)) * (p + p));
				// else
				// Start.GROUP_NAME = GROUP_NAME.replace(GROUP_NAME.substring(i,
				// i+1), "");
				if (port >= 65540)
					port -= p * p * p;
			}
		}
		return ("" + port);
	}

	// Yes yes I know this isn't acutally clearing the screen, but so far what
	// I've read says
	// that I have to create a command line process to clear it and idk what
	// that is.
	// private static void clearScreen() {for (int i = 0; i < 500; i++)
	// System.out.println();}
	// Never mind that, It doesn't work
}