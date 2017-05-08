package GROUP_V0_5;

/*
 * Client class V 0.5c
 * 
 * Kaleo Ngo
 * Principals of Java G/T
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
 *  5d. Possibly make logging another class to save a bunch of copied lines
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
 * Most of this class has been explained by the others so I will just comment the methods
 * 
 * TODO Add more error messages to client
 */
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;

public class Client extends Thread {

	private static Scanner scan;
	private static int port;
	private static boolean on = true;
	private static Socket socket;
	private static Thread retrieve;
	private static DataOutputStream out;
	private static DataInputStream in;
	private static String userName;
	private static String groupName;
	private static String[] adminCommands = { "getport", "getgroup", "getusers", "setgroupsize", "getgroupsize", "kick",
			"ban", "mod", "getp", "getg", "getu", "setgs", "getgs" };
	private String date;
	private String date2;
	private static int n = 0;
	private static int n2 = 0;
	private boolean ChatLogging;
	// private boolean Logging;
	private boolean go = false;
	private static boolean go2 = false;
	private static BufferedWriter writer;
	private static BufferedWriter writer2;

	public Client(String userName) throws Exception {
		Client.userName = userName;
		on = true;
		joinServer();
	}

	public static void main(String[] args) throws Exception {
		System.out.print("");
		port = Integer.parseInt(args[0]);
		userName = args[1];
		groupName = args[2];
		socket = new Socket("", port);
		new Client(userName);
	}

	public void joinServer() throws IOException {
		scan = new Scanner(System.in);
		out = new DataOutputStream(socket.getOutputStream()); // establish a
																// connection
																// with the
																// group
		in = new DataInputStream(socket.getInputStream());
		System.out.println("\nWelcome to the group " + userName + "!");
		out.writeUTF(userName + "-newN3W22"); // using a secret message to tell
												// the group that they are new
		out.flush();

		// this thread runs in the background to pick up any message even while
		// you are typing.
		retrieve = new Thread() {
			public void run() {
				while (on) {
					try {
						if (in.available() > 3) {

							String message = in.readUTF();

							if (ChatLogging)
								addToChatLog(message);

							if (message.split("-").length == 10) {
								exitGroup();
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								Server.main(new String[] { message.split("-")[0], message.split("-")[1], userName,
										groupName });
								socket.close();

							} else if (message.split("-").length == 11) {
								exitGroup();
								try {
									Thread.sleep(250);
									Client.main(new String[] { message.split("-")[0], userName, groupName });
									socket.close();
								} catch (Exception e) {
									e.printStackTrace();
								}

							} else if (message.split("-")[0].contains("rEmOvEdFrOmThEgRoUp")) {
								System.out.println("You have been kicked from the group..." + message.split("-")[1]);
								checkCommand("quit");
							} else {
								System.out.println(message);
								// System.out.format("%-40s, %-40s",
								// (message.split("-")[0] + ": " +
								// message.split("-")[1]), "\n");

							}
						}
					} catch (IOException ioe2) {
						ioe2.printStackTrace();

					}
				}
			}
		};
		retrieve.start();

		// this while loop just lets you type forever essentially
		while (on) {
			System.out.print("\nEnter: ");
			String message = scan.nextLine();

			// check for commands
			if (message.startsWith("\\")) {

				// I get a syntax error with only two \'s
				String command = message.replaceFirst("\\\\", "").toLowerCase();
				for (int i = 0; i < adminCommands.length; i++) {
					if (command.startsWith(adminCommands[i]))
						System.out.println("You do not have permission to use that command.");
					continue;
				}
				checkCommand(command);
			} else {
				try {
					out.writeUTF(userName + " - " + message);
					out.flush();
				} catch (IOException e) {
					on = false;
				}
			}
			if (ChatLogging)
				addToChatLog(userName + " - " + message);
		}
	}

	// TODO add more commands for the client
	// the client can only use \quit and \log and \help at the moment
	public void checkCommand(String command) {

		// I check to see if they are trying to get help [command] first
		// because otherwise it would only stop at help because that's how if
		// statements work
		if (command.split(" ").length == 2 && command.split(" ")[0].equals("help")) {
			helpCommand(command.split(" ")[1]);

		} else if (command.equals("help")) {
			System.out.println("Always remember to add a \"\\\" before your command."
					+ "\nAlso always remember that most commands will obly be accesessible to"
					+ "\nthe administrator or a moderator.\n");
			System.out.format("%-20s, %-14s, %-10s", "Command", "Other Forms", "Description\n\n");
			System.out.format("%-20s, %-14s, %-10s", "   QUIT", "",
					"   The QUIT command makes you leave your current group.\n");
			System.out.format("%-20s, %-14s, %-10s", "   GETPORT", "   GETP",
					"   The GETPORT command returns the port that the group is on.\n");
			System.out.format("%-20s, %-14s, %-10s", "   GETGROUP", "   GETG",
					"   The GETGROUP command returns the current group.\n");
			System.out.format("%-20s, %-14s, %-10s", "   GETUSERS", "   GETU",
					"   The GETUSERS command returns information about the group members.\n");
			System.out.format("%-20s, %-14s, %-10s", "   SETGROUPSIZE", "   SETGS",
					"   The SETGROUPSIZE command allows you to set the maximum size of your group.\n");
			System.out.format("%-20s, %-14s, %-10s", "   GETGROUPSIZE", "   GETGS",
					"   The GETGROUPSIZE command returns the maximum size of your group.\n");
			System.out.format("%-20s, %-14s, %-10s", "   KICK", "",
					"   The KICK command forces a group member to leave the group.\n");
			System.out.format("%-20s, %-14s, %-10s", "   BAN*", "",
					"   The BAN command forces a group member to leave the group for an indefinite amount of time.\n");
			System.out.format("%-20s, %-14s, %-10s", "   LOG", "   ",
					"   The LOG command can log your chat to a file destination of your choice.\n");
			System.out.format("%-20s, %-14s, %-10s", "   ERRORLOG*", "   TELOG",
					"   The ERRORLOG command can log \"geeky\" details about the group.\n");
			System.out.format("%-20s, %-14s, %-10s", "   MOD*", "",
					"   The MOD command can set a group member's status as moderator to on or off\n");
			System.out.println("\nFor more information on certain commands, type HELP [command-name]");
			System.out.println("	* Commands with an asterisk are a work in progress...\n");

			// this command exits the group by calling the method exitGroup and
			// then going the start again
		} else if (command.equals("quit")) {
			exitGroup();
			try {
				this.interrupt();
				this.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Start.main(null);
		} else if (command.startsWith("log")) {
			if (command.split("\"").length > 2)
				toggleChatLogging(command.split("\"")[1]);
			else
				toggleChatLogging();

		} else if (command.startsWith("errorlog") || command.startsWith("telog")) {
			if (command.split("\"").length > 2)
				toggleERRORLogging(command.split("\"")[1]);
			else
				toggleERRORLogging();
		} else {
			System.out.println("That command could not be found.");
		}
	}

	// the help commands method just displays more information on whatever
	// command you enter.
	// get into a group and type \help then you'll see at the bottom that you
	// can do ex. \help kick
	public void helpCommand(String command) {

		// Help section for help
		if (command.equals("help")) {
			System.out.println("The Synatax for this command is:" + "\n\n   HELP [ command ]"
					+ "\n\n   The HELP command is the command that you use when you don't know"
					+ "\n   what else to do with you life. period.");

			// Help section for quit
		} else if (command.equals("quit")) {
			System.out.println("The Synatax for this command is:" + "\n\n   QUIT"
					+ "\n\n   The QUIT command is used when you desire to leave your current group.");

			// Help section for get port
		} else if (command.equals("getport") || command.equals("getp")) {
			System.out.println("The Synatax for this command is:" + "\n\n   GETPORT"
					+ "\n\n   The GETPORT command can tell you which port your current" + "\n   group is using.");

			// Help section for get group
		} else if (command.equals("getgroup") || command.equals("getg")) {
			System.out.println("The Synatax for this command is:" + "\n\n   GETGROUP"
					+ "\n\n   The GETGROUP command can tell you which group" + "\n   you are currently using.");

			// Help section for get users
		} else if (command.equals("getusers") || command.equals("getu")) {
			System.out.println("The Synatax for this command is:" + "\n\n   GETUSERS [ user ]"
					+ "\n\n   The GETUSERS command can tell you (the admin) information"
					+ "\n   about one of you group members.");

			// Help section for set group size
		} else if (command.startsWith("setgroupsize") || command.equals("setgs")) {
			System.out.println("The syntax of this command is:" + "\n\n   SETGROUPSIZE [ size ]"
					+ "\n\n   The SETGROUPSIZE command is the command to use when you need to only allow"
					+ "\n   your buddies to enter. Remember, this field can not be left blank and it can not be \"0\".");

			// Help section for getGroupSize
		} else if (command.equals("getgroupsize") || command.equals("getgs")) {
			System.out.println("The syntax of this command is:" + "\n\n   GETGROUPSIZE"
					+ "\n\n   The GETGROUPSIZE command is the command to use when you need to know if"
					+ "\n   you need to add more space for your buddies to enter.");

			// Help section for kick
		} else if (command.startsWith("kick")) {
			System.out.println("The syntax of this command is:" + "\n\n   KICK [ user ] [ \" reason \" ]"
					+ "\n\n   The KICK command is used to forcefully remove someone from the group."
					+ "\n   If the argument is left blank, no user will be removed. If the user name is"
					+ "\n   not found, no user will be removed and this message will pop up... SURROUND THE REASON WITH \" \".");

			// Help section for log
		} else if (command.startsWith("log")) {
			System.out.println("The syntax of this command is:" + "\n\n   LOG [ \" directory \" ]"
					+ "\n\n   The LOG command is used to record your chat history into a file."
					+ "\n   If the argument is left blank, the default directory is Desktop\\Logs\\Chat Logs."
					+ "\n   P.S. Adding a directory is still a work in progress at the moment.");

			// Help section for errorlog
		} else if (command.startsWith("errorlog") || command.startsWith("telog")) {
			System.out.println("The syntax of this command is:" + "\n\n   ERRORLOG [ \" directory \" ]"
					+ "\n\n   The LOG command is used for error logging. It will log any errors as well as executions in the code."
					+ "\n   If the argument is left blank, the default directory is Desktop\\Logs\\Error Logs."
					+ "\n   P.S. Adding a directory is still a work in progress at the moment.");

		} else {
			System.out.println("That command could not be found.");
		}
	}

	// very straight-forward...it adds the messages to the log
	private void addToChatLog(String message) {
		if (go) {
			try {
				writer.append("\n"
						+ new SimpleDateFormat("[ MM/dd/yyyy - HH:mm:ss ]").format(Calendar.getInstance().getTime())
						+ "   " + message.split("-")[0] + ": " + message.split("-")[1]);
				writer.newLine();
				writer.flush();
			} catch (IOException e) {
				addToERRORLog("Error-Problem adding to the chat log..." + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	// This toggle chat logging to on or off and it will out put by default to
	// the users desktop in the folder called chat logs in a folder called logs
	private void toggleChatLogging() {
		date = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
		String directory = System.getProperty("user.home") + "\\Desktop\\Logs\\Chat Logs";

		if (!go) {
			go = true;
			System.out.println("Logging is on...Saving to " + directory);
		} else {
			go = false;
			System.out.println("Logging is off...");
		}

		if (go) {
			File logFolder = new File(directory);
			File log = new File(logFolder.getPath() + "\\" + groupName + "_" + date + ".txt");

			if (!logFolder.exists()) {
				logFolder.mkdirs();
			}

			if (log.exists()) {
				n++;
			}

			// I concat just incase you leave a group then come back to it and
			// don'r want to overwrite your previous chat log
			if (n != 0) {
				date = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime())
						.concat(("(" + n + ")"));
			} else {
				date = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
			}

			log = new File(logFolder.getPath() + "\\" + groupName + "_" + date + ".txt");

			try {
				writer = new BufferedWriter(
						new FileWriter(logFolder.getPath() + "\\" + groupName + "_" + date + ".txt"));
			} catch (IOException e) {
				addToERRORLog("Error-Problem with file directory..." + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	// Work in progress...
	// This is chat logging for specified directory
	private void toggleChatLogging(String directory) {
		date = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());

		if (!go) {
			go = true;
			System.out.println("Logging is on...Saving to " + directory);
		} else {
			go = false;
			System.out.println("Logging is off...");
		}

		if (go) {
			File logFolder = new File(directory);
			File log = new File(logFolder.getPath() + "\\" + groupName + "_" + date + ".txt");

			if (!logFolder.exists()) {
				logFolder.mkdirs();
			}

			if (log.exists()) {
				n++;
			}

			if (n != 0) {
				date = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime())
						.concat(("(" + n + ")"));
			} else {
				date = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
			}

			log = new File(logFolder.getPath() + "\\" + groupName + "_" + date + ".txt");

			try {
				writer = new BufferedWriter(
						new FileWriter(logFolder.getPath() + "\\" + groupName + "_" + date + ".txt"));
			} catch (IOException e) {
				addToERRORLog("Error-Problem with file directory..." + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	// This toggles error logging which is basically kind of like a more in
	// depth version
	// where it will log stuff like who connects, when, any errors that occur
	// etc. it will out put by default to
	// the users desktop in the folder called connection logs in a folder called
	// logs
	private void toggleERRORLogging() {
		date2 = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
		String directory = System.getProperty("user.home") + "\\Desktop\\Logs\\Error Logs";

		if (!go2) {
			go2 = true;
			System.out.println("Error logging is on...Saving to " + directory);
		} else {
			go2 = false;
			System.out.println("Error logging is off...");
		}

		if (go2) {
			File logFolder = new File(directory);
			File log = new File(logFolder.getPath() + "\\" + groupName + "_LOG_" + date2 + ".txt");

			if (!logFolder.exists()) {
				logFolder.mkdirs();
			}

			if (log.exists()) {
				n2++;
			}

			// I concat just incase you leave a group then come back to it and
			// don'r want to overwrite your previous chat log
			if (n2 != 0) {
				date2 = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime())
						.concat(("(" + n2 + ")"));
			} else {
				date2 = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
			}

			log = new File(logFolder.getPath() + "\\" + groupName + "_LOG_" + date2 + ".txt");

			try {
				writer2 = new BufferedWriter(
						new FileWriter(logFolder.getPath() + "\\" + groupName + "_LOG_" + date2 + ".txt"));
			} catch (IOException e) {
				addToERRORLog("Error-Problem with file directory..." + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	// Toggle error logging toggles logging for errors in the program and what
	// not
	private void toggleERRORLogging(String directory) {
		date2 = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());

		if (!go2) {
			go2 = true;
			System.out.println("Error logging is on...Saving to " + directory);
		} else {
			go2 = false;
			System.out.println("Error logging is off...");
		}

		if (go2) {
			File logFolder = new File(directory);
			File log = new File(logFolder.getPath() + "\\" + groupName + "_LOG_" + date2 + ".txt");

			if (!logFolder.exists()) {
				logFolder.mkdirs();
			}

			if (log.exists()) {
				n2++;
			}

			// I concat just incase you leave a group then come back to it and
			// don'r want to overwrite your previous chat log
			if (n2 != 0) {
				date2 = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime())
						.concat(("(" + n2 + ")"));
			} else {
				date2 = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
			}

			log = new File(logFolder.getPath() + "\\" + groupName + "_LOG_" + date2 + ".txt");

			try {
				writer2 = new BufferedWriter(
						new FileWriter(logFolder.getPath() + "\\" + groupName + "_LOG_" + date2 + ".txt"));
			} catch (IOException e) {
				addToERRORLog("Error-Problem with file directory..." + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	// Also straight forward. it add the errors to the error log
	// it will add either Error: "error" or a notificatiion like "starting
	// connection handler
	public static void addToERRORLog(String message) {
		if (go2) {
			try {
				if (message.split("-").length == 2) {
					writer2.append("\n"
							+ new SimpleDateFormat("[ MM/dd/yyyy - HH:mm:ss ]").format(Calendar.getInstance().getTime())
							+ "   " + message.split("-")[0] + ": " + message.split("-")[1]);
				} else {
					writer2.append("\n"
							+ new SimpleDateFormat("[ MM/dd/yyyy - HH:mm:ss ]").format(Calendar.getInstance().getTime())
							+ "   " + message);
				}
				writer2.newLine();
				writer2.flush();
			} catch (IOException e) {
				addToERRORLog("Error-Could not add message to the error log..." + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	private void exitGroup() {
		try {
			on = false;
			out.writeUTF(userName + "-leaving13av1n6");
			out.flush();

			socket.close();
			this.join();
			out.close();
			in.close();
		} catch (IOException | InterruptedException e) {
			// e.printStackTrace();
			try {
				socket.close();
				this.join();
			} catch (IOException | InterruptedException e1) {
				// e1.printStackTrace();
			}

		}
	}
}
