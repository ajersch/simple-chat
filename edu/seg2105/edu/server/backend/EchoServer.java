package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:

import java.io.IOException;

import edu.seg2105.client.common.ChatIF;

// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer {
	// Class variables *************************************************

	ChatIF serverUI;
	private boolean isClosed;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 */
	public EchoServer(int port, ChatIF serverUI) {
		super(port);
		isClosed = false;
		this.serverUI = serverUI;
	}

	// Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println("Message received: " + msg + " from " + client.getInfo("login"));
		if (msg.toString().startsWith("#login")) {
			String[] arguments = msg.toString().split(" ");
			try {
				String login = arguments[1];
				handleLogin(login, client);
			} catch (IndexOutOfBoundsException e) {
				try {
					client.sendToClient("Error logging in");
					client.close();
				} catch (IOException e1) {
				}
			}
			return;
		}
		this.sendToAllClients(client.getInfo("login") + ": " + msg);
	}

	private void handleLogin(String login, ConnectionToClient client) {
		if (client.getInfo("loggedin") != null) {
			try {
				client.sendToClient("Already connected and logged in");
				client.close();
			} catch (IOException e) {
			}
		}

		client.setInfo("login", login);
		client.setInfo("loggedin", true);
		
		sendToAllClients(client.getInfo("login") + " has logged on");
	}

	public void handleMessageFromServer(Object msg) {
		msg = "SERVER MSG> " + msg;
		System.out.println(msg);
		this.sendToAllClients(msg);
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
		isClosed = false;
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	/**
	 * Hook method called each time a new client connection is accepted. Prints a
	 * message showing that a client has connected
	 * 
	 * @param client the connection connected to the client.
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		serverUI.display("Client " + client.toString() + " has connected.");
	}

	/**
	 * Hook method called each time a client disconnects. Prints a message showing
	 * that a client has disconnected
	 * 
	 * @param client the connection with the client.
	 */
	@Override
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		serverUI.display(client.getInfo("login") + " has disconnected");
	}

	/**
	 * Hook method called when the server is closed.
	 */
	protected void serverClosed() {
		isClosed = true;
	}

	public void handleMessageFromServerUI(String message) {
		if (message.startsWith("#")) {
			handleCommand(message);
		} else {
			handleMessageFromServer(message);
		}
	}

	private void handleCommand(String message) {
		String arguments[] = message.split(" ");
		switch (arguments[0]) {
		case "#quit":
			try {
				close();
			} catch (IOException e) {
				serverUI.display("Exception");
			}
			System.exit(0);
		case "#stop":
			stopListening();
			break;
		case "#close":
			try {
				close();
			} catch (IOException e) {
			}
			break;
		case "#setport":
			if (!isClosed) {
				serverUI.display("Server must be closed to change the port");
				return;
			}

			try {
				int port = Integer.parseInt(arguments[1]);
				setPort(port);
			} catch (IndexOutOfBoundsException e) {
				serverUI.display("Port missing");
			} catch (NumberFormatException e) {
				serverUI.display("Invalid port");
			}
			break;
		case "#start":
			try {
				listen();
			} catch (IOException e) {
				serverUI.display("ERROR - Could not listen for clients!");
				System.exit(0);
			}
			break;
		case "#getport":
			serverUI.display(Integer.toString(getPort()));
			break;
		}
	}

	// Class methods ***************************************************
	public boolean isClosed() {
		return isClosed;
	}

}
//End of EchoServer class
