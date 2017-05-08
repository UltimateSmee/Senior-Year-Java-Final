package GROUP_V0_5;

/*
 * Server class V 0.5c
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
 * This is the Server class. The server class is most likely the most important after
 * the start class because it is the second one to start. first it tries to open a 
 * Serversocket on whatever port it gets from start and then if it gets an exception, 
 * that means that there is already a server on that port. when it gets an exception 
 * it stops what it's doing and asks if you want to join that group (server).
 * If you say yes, it will start up the client class and from there. The client and server 
 * classes are somewhat similar with their purposes (almost). The server must allow the client 
 * to connect and then alll they do from there is communicate back and forth by having a 
 * retrieval thread which works in the background to take incoming messages. it also has a loop 
 * below that that is used for text input. if your message loop (the one you type in) sees that 
 * you added a "\" before your message, it will treat it as a command and it will check to see if 
 * it can do what you want. the client class is similar in this way too except that only the admin 
 * or moderator can use most of the commands. ALSO notice in the receive that the server sends the message back to everyone else. 
 * this is because the clients can not connect to each other so they use the server to do that for them. 
 * It's pretty much just like a client - server interaction except that the server is also participating.
 * In the future, I will likely get rid of the server class entirely because I would (probably not) have a server
 * to have the clients connect to.
 * 
 * TODO ADD MORE ERROR COMMENTS
 */

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;
import java.io.*;

public class Server extends Thread {

	private static ServerSocket socket;
	private ArrayList<DataOutputStream> out;
	private ArrayList<DataInputStream> in;
	private static ArrayList<DataOutputStream> outTemp;
	private static ArrayList<DataInputStream> inTemp;
	private static ArrayList<String> currentUsers;

	private static String lastUser;
	private static String userName;
	private boolean on;
	private boolean go = false;
	private static boolean go2 = false;
	private static boolean ready = false;
	private static int port;
	private static int groupSize;
	private static String groupName;
	private static Scanner scan;
	private static BufferedWriter writer;
	private static BufferedWriter writer2;
	private static int n = 0;
	private static int n2 = 0;

	private Thread t;
	private Thread conH;
	private String date;
	private String date2;

	// here is where it tries to make a serversocket
	public Server(int port, int groupSize) {
		scan = new Scanner(System.in);
		boolean cont = true;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			cont = false;
			System.out.println(
					"Uh oh. It looks like that group was already made." + "\nDo you want to join that group? (y/n)");
			String c = scan.next();

			if (c.toLowerCase().equals("y")) {
				try {
					Client.main(new String[] { ("" + port), userName, groupName });
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			} else {
				Start.main(null);
				cont = false;
			}
		}
		if (cont) {
			conH = new ConnectionHandler(socket, groupSize);
			conH.start();
			on = true;

			// TODO create a "last run" file that saves data that could be
			// important like n below
			/*
			 * try { n = Integer.parseInt(new
			 * File(System.getProperty("user.home") +
			 * "\\Desktop\\Logs\\Chat Logs\\" + groupName + "_Chat_" + date + "
			 * .txt").getName().split("\\(")[1]); n2 = Integer.parseInt(new
			 * File(System.getProperty("user.home") +
			 * "\\Desktop\\Logs\\Error Logs\\" + groupName + "_ELog_" + date + "
			 * .txt").getName().split("\\(")[1]);
			 * 
			 * } catch (IndexOutOfBoundsException e) {
			 * addToERRORLog("Error-Tried to get n for loggin but the file DNE..."
			 * + Arrays.toString(e.getStackTrace())); } System.out.println(n);
			 */
			begin();
		}
	}

	public static void main(String[] args) {
		System.out.print("");
		port = Integer.parseInt(args[0]);
		groupSize = Integer.parseInt(args[1]);
		userName = args[2];
		groupName = args[3];
		new Server(port, groupSize);
	}

	public void begin() {
		System.out.println("You are now the host." + "\nYou can use commands by pressing '\\' and typing the command.");

		// This is the server's retirval thread. I'm not sure why I didn't name
		// it retrieve.
		addToERRORLog("Starting message retrieve thread...");
		t = new Thread("Server Send") {
			public void run() {
				while (on) {
					try {
						if (ready) {
							in = inTemp;
							out = outTemp;
							ready = false;
							addToERRORLog("Updated in streams and out streams...");
						}
						if (in != null) { // this is a safety to make sure that
											// you don't get a null pointer
											// exception
							// A Unicode character can be between 1 and 4 bytes
							for (int i = 0; i < in.size(); i++) {
								if (in.get(i) != null && in.get(i).available() > 4) {

									String message = in.get(i).readUTF();

									addToChatLog(message);

									// if a member is new this will be at the
									// beginning of the message to tell if it is
									// new or not.
									// the string is weird to try to make sure
									// that they don't type this accidentally
									// because the messages are sent like this:
									// (user name - message). so, they could
									// make their name that accidentally
									// all of the "secret codes" will be unique
									// like that
									if (message.contains("newN3W22")) {
										lastUser = message.split("-")[0];
										ConnectionHandler.addUser(lastUser); // add
																				// the
																				// new
																				// user
																				// to
																				// the
																				// array

										for (int p = 0; p < out.size(); p++) {
											if (p != i) {
												out.get(p).writeUTF(lastUser + " has joined the group.");
												out.get(p).flush();
											}
										}
										System.out.println(lastUser + " has joined the group.");

										// to tell the connection handler to
										// remove that user
									} else if (message.contains("leaving13av1n6")) {
										System.out.println(lastUser + " has left the group.");
										ConnectionHandler.removeUser(lastUser);
										addToERRORLog("Removing user..." + lastUser);

									} else {
										System.out.println(message);
										// don't mind this part
										// This is kind of what i want the gui
										// message box to look like with
										// messages going back and forth
										// System.out.format("%40s, %40s", "",
										// (message.split("-")[1] + " :" +
										// message.split("-")[0]) + "\n");
										for (int p = 0; p < out.size(); p++) {
											if (p != i) {
												out.get(p).writeUTF(message);
												out.get(p).flush();

											}
										}
									}
								}
							}
						} else {
							System.out.print(""); // THIS BLANK SPACE IS VERY
													// IMPORTANT FOR SOME REASON
													// THAT I CAN"T EXPLAIN
													// BECAUSE IT ONLY WORKS
													// WITH IN AND I COULDN"T
													// FINS ANYTHING ONLINE
						}
					} catch (IOException e) {
						// For some reason the loop doesn't stop even though I
						// turned on to false
						// maybe adding a break here will help?
						addToERRORLog("Error-" + Arrays.toString(e.getStackTrace()));
						break;

					}
				}
			}
		};
		t.start();
		while (on) {
			System.out.print("\nEnter: ");
			String message = scan.nextLine();

			// this is the command distinguishing part
			if (message.startsWith("\\")) {
				// I get a syntax error with only two \'s
				checkCommand(message.replaceFirst("\\\\", "").toLowerCase());

			} else if (in != null) { // safety check before trying to send to
										// anyone and failing

				for (int p = 0; p < out.size(); p++) {
					try {
						out.get(p).writeUTF(userName + " - " + message);
						out.get(p).flush();

					} catch (IOException e) {
						// e.printStackTrace();
						// this is another section that i can't explain because
						// there is almost no way for me to find out
						System.out.println("Tried and failed(SOCKET EXECPTION ON THAT STREAM) to out to " + in.get(p)
								+ "\nAttempting to remove that stream...");
						ConnectionHandler.removeUser(out.get(p), "");
						addToERRORLog("Error-Socket Exception. Stream is no longer responding..."
								+ Arrays.toString(e.getStackTrace()));
						// on = false;

					}
				}
			}
			addToChatLog(userName + " - " + message);
		}
	}

	// place to check the command for matches
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
			System.out.format("%-20s, %-14s, %-10s", "   ERRORLOG", "   TELOG",
					"   The ERRORLOG command can log \"geeky\" details about the group.\n");
			System.out.format("%-20s, %-14s, %-10s", "   MOD*", "",
					"   The MOD command can set a group member's status as moderator to on or off\n");
			System.out.println("\nFor more information on certain commands, type HELP [command-name]");
			System.out.println("	* Commands with an asterisk are a work in progress...\n");

			// this command exits the group by calling the method exitGroup and
			// then going the start again
		} else if (command.equals("quit")) {
			// System.out.println("Exiting");
			exitGroup();
			Start.main(null);
			try {
				this.join();
			} catch (InterruptedException e) {
				addToERRORLog(
						"Error-Could not join this thread to main thread..." + Arrays.toString(e.getStackTrace()));
			}

			// getport just returns the current port number
		} else if (command.equals("getport") || command.equals("getp")) {
			System.out.println("	Port: " + socket.getLocalPort());

			// get group just returns the current group
		} else if (command.equals("getgroup") || command.equals("getg")) {
			System.out.println("	Group: " + groupName);

			// TODO get users returns the amount of users, names of users, and
			// later,
			// more info about a user
			// if the command is \getUsers [user]...I don;t have that at the
			// moment because I don;t really have any more info on the user
			// later i can get the connection handler to log some info for me
		} else if (command.startsWith("getusers") || command.startsWith("getu")) {
			currentUsers = ConnectionHandler.getUserList();
			System.out.println("Current Number Of Users: " + currentUsers.size());
			if (currentUsers.size() == 0) {
				System.out.println("	No Current Users");
			} else {
				System.out.println("	Users:");
				for (int i = 0; i < currentUsers.size(); i++) {
					System.out.println("	   " + currentUsers.get(i));
				}
			}

			// Command to set the maximum group size allowed for that group
		} else if (command.startsWith("setgroupsize") || command.startsWith("setgs")) {
			if (command.split(" ").length == 2) {
				int newSize = Integer.parseInt(command.split(" ")[1]);
				groupSize = newSize;
				ConnectionHandler.setGroupSize(newSize);
				System.out.println("	GroupSize --> " + groupSize);
			} else {
				helpCommand("setgs");
			}

			// command to return the maximum group size
		} else if (command.equals("getgroupsize") || command.equals("getgs")) {
			System.out.println("GroupSize --> " + groupSize);

			// Command to remove the requested user
		} else if (command.startsWith("kick")) {
			try {

				// Tempuser is the User-to-be-removed
				String tempuser = command.split(" ")[1];
				String tempreason = command.split("\"")[1];
				ConnectionHandler.removeUser(tempuser, tempreason);
				System.out.println("\nRemoved " + tempuser + " from the group.");

			} catch (IndexOutOfBoundsException e) {
				helpCommand("kick");
			}

		}  else if (command.startsWith("ban")) {
			try {

				// Tempuser is the User-to-be-removed 
				String tempuser = command.split(" ")[1];
				String tempreason = command.split("\"")[1];
				String temptime = command.split(" ")[2];
				ConnectionHandler.removeUser(tempuser, temptime, tempreason);
				System.out.println("\nRemoved " + tempuser + " from the group for " + temptime);

			} catch (IndexOutOfBoundsException e) {
				helpCommand("ban");
			}

		} else if (command.startsWith("log")) {
			if (command.split("\"").length > 1)
				toggleChatLogging(command.split("\"")[1]);
			else
				toggleChatLogging();

		} else if (command.startsWith("errorlog") || command.startsWith("telog")) {
			if (command.split("\"").length > 1)
				toggleERRORLogging(command.split("\"")[1]);
			else
				toggleERRORLogging();
		} else if (command.startsWith("testerror")) {
			try {
				new Throwable("dfsadsa");
			} catch (Throwable t) {
				addToERRORLog("Test Error" + Arrays.toString(t.getStackTrace()));
			}
		} else {
			System.out.println("That command could not be found.");
		}

		// TODO add more commands
		// add a moderator option for later
		// in ver 0.6 or something
		// add a ban command
		// add a log option
		// add connection log
		// add more information about users
		// add more info to the help screens
	}

	// The help command method is the method that shows additional information
	// about the certain command
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

			// Help section for ban
		}  else if (command.startsWith("ban")) {
			System.out.println("The syntax of this command is:" + "\n\n   BAN [ user ] [ time ] [ \" reason \" ]"
					+ "\n\n   The BAN command is used to forcefully remove someone from the group for however long you want."
					+ "\n   If the argument is left blank, no user will be removed. If the user name is"
					+ "\n   not found, no user will be removed and this message will pop up... SURROUND THE REASON WITH \" \"."
					+ "\n   Always make sure to put the user and the time before the reason. Also for the time you can put m,h, or d"
					+ "\n   for minuets, hours, and days."
					+ "\n   CURRENTLY A WORK IN PROGRESS.");

			// Help section for log
		} else if (command.startsWith("log")) {
			System.out.println("The syntax of this command is:" + "\n\n   LOG [ \" directory \" ]"
					+ "\n\n   The LOG command is used to record your chat history into a file."
					+ "\n   If the argument is left blank, the default directory is Desktop\\Logs\\Chat Logs.");

			// Help section for errorlog
		} else if (command.startsWith("errorlog") || command.startsWith("telog")) {
			System.out.println("The syntax of this command is:" + "\n\n   ERRORLOG [ \" directory \" ]"
					+ "\n\n   The LOG command is used for error logging. It will log any errors as well as executions in the code."
					+ "\n   If the argument is left blank, the default directory is Desktop\\Logs\\Error Logs.");

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

	// This is the exit group method that basically attempts to close everything
	// down before proceeding.
	private void exitGroup() {
		try {
			on = false;
			go = false;
			t.join();
			ConnectionHandler.on = true;

			// since the connection handler waits for a connections, it won't be
			// able to join the thread because it's waiting so, i had someone
			// join then leave. it doesnt go anymore because i triggered the
			// break that will stop the loop

			Socket socketq = new Socket("", port);
			ConnectionHandler.addUser("tEmPUsERDon0tUseTHiSName");
			Server.sleep(50);

			socketq.close();
			ConnectionHandler.removeUser("tEmPUsERDon0tUseTHiSName");

			conH.join();
			socket.close();
			this.join();

			// this part tells the other users who will be the next host of the
			// group
			if (out != null && out.size() > 0) {
				out.get(0).writeUTF(port + "-" + groupSize + "- - - - - - - - ");
				out.get(0).flush();
				for (int p = 1; p < out.size(); p++) {
					out.get(p).writeUTF(port + "- - - - - - - - - - ");
					out.get(p).flush();
				}

				for (int p = 0; p < out.size(); p++) {
					out.get(p).close();
					in.get(p).close();
				}
			}
		} catch (IOException | InterruptedException | IndexOutOfBoundsException e) {
			try {
				socket.close();
				this.join();
			} catch (IOException | InterruptedException e1) {
				addToERRORLog("Error-Could not close socket and join thread..." + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	// get a fresh new out and in with these next methods
	public static void setOut(ArrayList<DataOutputStream> newOut) {
		outTemp = newOut;
		ready = true;
	}

	public ArrayList<DataOutputStream> getOut() {
		return this.getOut();
	}

	public static void setIn(ArrayList<DataInputStream> newIn) {
		inTemp = newIn;
	}

	public ArrayList<DataInputStream> getIn() {
		return this.getIn();
	}

}