// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import ocsf.client.*;

import java.io.*;

import edu.seg2105.client.common.*;

/**
 * This class overrides some of the methods defined in the abstract superclass
 * in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient {
	// Instance variables **********************************************

	/**
	 * The interface type variable. It allows the implementation of the display
	 * method in the client.
	 */
	ChatIF clientUI;
	String login;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the chat client.
	 *
	 * @param host     The server to connect to.
	 * @param port     The port number to connect on.
	 * @param clientUI The interface type variable.
	 */

	public ChatClient(String login, String host, int port, ChatIF clientUI) throws IOException {
		super(host, port); // Call the superclass constructor
		this.clientUI = clientUI;
		this.login = login;
		openConnection();
	}

	// Instance methods ************************************************

	/**
	 * This method handles all data that comes in from the server.
	 *
	 * @param msg The message from the server.
	 */
	public void handleMessageFromServer(Object msg) {
		clientUI.display(msg.toString());

	}

	/**
	 * This method handles all data coming from the UI
	 *
	 * @param message The message from the UI.
	 */
	public void handleMessageFromClientUI(String message) {
		try {
			if (message.startsWith("#")) {
				handleCommand(message);
				return;
			}
			
			if (!isConnected()) return;
			
			sendToServer(message);
		} catch (IOException e) {
			clientUI.display("Could not send message to server.  Terminating client.");
			quit();
		}
	}

	private void handleCommand(String command) {
		String[] arguments = command.split(" ");
		switch (arguments[0]) {
		case "#quit":
			quit();
			break;
		case "#logoff":
			try {
				closeConnection();
			} catch (IOException e) {
			}
			break;
		case "#sethost":
			if (!isConnected()) {
				clientUI.display("Cannot change host while connected");
				return;
			}
			
			try {
				setHost(arguments[1]);
			} catch (ArrayIndexOutOfBoundsException e) {
				clientUI.display("Must specify a host");
			}
			break;
		case "#setport":
			if (!isConnected()) {
				clientUI.display("Cannot change port while connected");
				return;
			}
			
			try {
				setPort(Integer.parseInt(arguments[1]));
			} catch (ArrayIndexOutOfBoundsException e) {
				clientUI.display("Must specify a port");
			} catch (NumberFormatException e) {
				clientUI.display("Invalid port");
			}
			break;
		case "#login":
			try {
				login = arguments[1];
				openConnection();
			} catch (IndexOutOfBoundsException e) {
				clientUI.display("Must specifgy login id");
			} catch (IOException e) {
				clientUI.display("Error connecting to the server");
			}
			break;
		case "#gethost":
			System.out.println(getHost());
			break;
		case "#getport":
			System.out.println(getPort());
			break;
		}
	}

	/**
	 * This method terminates the client.
	 */
	public void quit() {
		try {
			closeConnection();
		} catch (IOException e) {
		}
		System.exit(0);
	}

	/**
	 * Implements the hook method called after the connection has been closed. The
	 * default implementation does nothing. The method may be overriden by
	 * subclasses to perform special processing such as cleaning up and terminating,
	 * or attempting to reconnect.
	 */
	@Override
	protected void connectionClosed() {
		clientUI.display("Connection Closed");
	}

	/**
	 * hook method called each time an exception is thrown by the client's thread
	 * that is waiting for messages from the server. the method may be overridden by
	 * subclasses.
	 * 
	 * @param exception the exception raised.
	 */
	protected void connectionException(Exception exception) {
		clientUI.display("The server is shut down");
		System.exit(0);
	}

	/**
	 * Hook method called after a connection has been established.
	 */
	protected void connectionEstablished() {
		try {
			sendToServer("#login " + login);
		} catch (IOException e) {
		}
	}
}
//End of ChatClient class
