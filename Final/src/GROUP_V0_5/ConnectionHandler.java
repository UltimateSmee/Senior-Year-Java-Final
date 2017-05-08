package GROUP_V0_5;

/*
 * ConnectionHandler V 0.5c
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
 * The Connection Handler Class is the class that manages incoming connections.
 * in the run method, it waits for a client to connect and then it proceeds down the loop.
 * it creates an output stream and an input stream for that individual socket by putting each additional
 * in / out into an arraylist. I chose to use an arraylist because the amount of users could vary based on the administrators taste.
 * Anyway, once the in/out streams are created, it moves on to update the input and output on the server class so that they can be used to
 * get the message out. This class also has methods to add member user names to an array or remove them. Later on I will try to add more information on the clients.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ConnectionHandler extends Thread {
	private static ServerSocket socket;
	private static Socket connection;

	public static ArrayList<DataOutputStream> out = new ArrayList<DataOutputStream>();
	public static ArrayList<DataInputStream> in = new ArrayList<DataInputStream>();
	private static ArrayList<String> userNames = new ArrayList<String>();
	private static ArrayList<String> userIps = new ArrayList<String>();

	private static int groupSize;
	public static boolean on;

	public ConnectionHandler(ServerSocket sockett, int groupSizee) {
		socket = sockett;
		groupSize = groupSizee;
		on = false;
		// System.out.println(this.getId());
	}

	@Override
	public void run() {
		// System.out.println("Started the connection handler");
		for (int i = 0; true; i++) {
			try {
				Server.addToERRORLog("Waiting for new users");
				connection = socket.accept();

				// This section is set up to let the user join and then be
				// removed for the max group size reason
				// this is better than ending the loop because the server can
				// change the group size while the loop is still going
				if (i >= groupSize) {
					out.add(new DataOutputStream(connection.getOutputStream()));
					in.add(new DataInputStream(connection.getInputStream()));
					sleep(50);
					removeUser(new DataOutputStream(connection.getOutputStream()), "Max group size reached");
					Server.addToERRORLog("Warning-Last User Removed...no space");
					Server.addToERRORLog("Warning-Max group size reached");

				} else {
					Server.addToERRORLog("New user..." + connection.getInetAddress().getHostAddress());

					out.add(new DataOutputStream(connection.getOutputStream()));
					in.add(new DataInputStream(connection.getInputStream()));
					userIps.add(connection.getInetAddress().getHostAddress());

					Server.setOut(out);
					Server.setIn(in);
				}

				// on just tells it if it should turn off or not.
				// this is for the exetGroup method in server.
				if (on) {

					break;
				}
			} catch (Exception e) {
				Server.addToERRORLog("Error-" + Arrays.toString(e.getStackTrace()));
				break;
			}
		}
		Server.addToERRORLog("Warning-Max amount of users reached...");
	}

	// This remove method is for normally removing a user by their user name.
	public static void removeUser(String user, String reason) {
		int numb = userNames.indexOf(user);
		try {
			out.get(numb).writeUTF("rEmOvEdFrOmThEgRoUp-" + reason);
			out.get(numb).flush();
			in.remove(numb);
			out.remove(numb);
			userNames.remove(numb);
		} catch (IOException | IndexOutOfBoundsException e) {
			Server.addToERRORLog("Error-Could not remove user..." + Arrays.toString(e.getStackTrace()));
		}
		Server.setOut(out);
		Server.setIn(in);
	}

	public static void removeUser(String user) {
		int numb = userNames.indexOf(user);
		try {
			in.remove(numb);
			out.remove(numb);
			userNames.remove(numb);
		} catch (IndexOutOfBoundsException e) {
			Server.addToERRORLog("Error-Could not remove user..." + Arrays.toString(e.getStackTrace()));
		}
		Server.setOut(out);
		Server.setIn(in);
	}

	// This remove method is for removing a user by their Output stream
	public static void removeUser(DataOutputStream user, String reason) {
		// int numb = out.indexOf(user);
		int numb = out.size() - 1;
		try {
			out.get(numb).writeUTF("rEmOvEdFrOmThEgRoUp-" + reason);
			out.get(numb).flush();
			in.remove(numb);
			out.remove(numb);
			userNames.remove(numb);
		} catch (IndexOutOfBoundsException | IOException e) {
			Server.addToERRORLog("Error-Could not remove user..." + Arrays.toString(e.getStackTrace()));
		}
		Server.setOut(out);
		Server.setIn(in);
	}

	public static ArrayList<DataInputStream> getIn() {
		return in;
	}

	public static ArrayList<DataOutputStream> getOut() {
		return out;
	}

	public static void addUser(String user) {
		if (!userNames.contains(user))
			userNames.add(user);
	}

	public static ArrayList<String> getUserList() {
		return userNames;
	}

	public static void setGroupSize(int size) {
		groupSize = size;
	}

	public static void removeUser(String tempuser, String temptime, String tempreason) {
		// TODO Auto-generated method stub
		
	}
}
