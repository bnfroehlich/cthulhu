package bfroehlich.cthulhu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server extends JFrame implements Networker {

	private JTextArea console;
	private JScrollPane consoleScroll;
	private JButton playCthulhu;
	private JButton playSkull;
	private JPanel playerListPanel;
	
	private HashMap<String, Socket> clientSockets;
	private HashMap<String, PrintWriter> channelsToClients;
	private ArrayList<String> playerUsernames;
	private ArrayList<String> playersInGame;
	private String myUsername;
	
	private GameWindow gameWindow;
	private GameEngine engine;
	
	public static void main(String[] args) {
		
		new Server();
	}
	
	public Server() {
		super("Server");
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		init();
	}
	
	private void init() {
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.X_AXIS));
		add(wrapperPanel);
				
		JPanel mainPanel = new JPanel();
		wrapperPanel.add(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		FlowLayout flow = new FlowLayout(FlowLayout.RIGHT, 10, 10);
		
		JPanel namePanel = new JPanel();
		namePanel.setLayout(flow);
		mainPanel.add(namePanel);
		namePanel.add(new JLabel("username"));
		JTextField nameField = new JTextField("", 20);
		namePanel.add(nameField);
		
		JPanel portPanel = new JPanel();
		portPanel.setLayout(flow);
		mainPanel.add(portPanel);
		portPanel.add(new JLabel("port"));
		JTextField portField = new JTextField("4321", 20);
		portPanel.add(portField);
		
		JButton connect = new JButton("Launch server");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(nameField.getText().isEmpty()) {
					println("Empty input");
				}
				else {
					connect.setEnabled(false);
					launchServer(nameField.getText(), portField.getText());
				}
			}
		});
		mainPanel.add(connect);
		
		JPanel playCthulhuPanel = new JPanel();
		playCthulhuPanel.setLayout(flow);
		mainPanel.add(playCthulhuPanel);
		playCthulhu = new JButton("Play Cthulhu");
		playCthulhu.setEnabled(false);
		playCthulhuPanel.add(playCthulhu);
		JCheckBox deluxe = new JCheckBox("Deluxe");
		deluxe.setSelected(true);
		playCthulhuPanel.add(deluxe);
		playCthulhu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playersInGame = new ArrayList<String>(playerUsernames);
				launchCthulhu(deluxe.isSelected());
				updatePlayButtons();
			}
		});
		
		playSkull = new JButton("Play Skull");
		playSkull.setEnabled(false);
		playSkull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playersInGame = new ArrayList<String>(playerUsernames);
				launchSkull();
				updatePlayButtons();
			}
		});
		mainPanel.add(playSkull);
		
		console = new JTextArea();
		console.setEditable(false);
		consoleScroll = new JScrollPane(console);
		consoleScroll.setPreferredSize(new Dimension(350, 150));
		mainPanel.add(consoleScroll);
		
		playerListPanel = new JPanel();
		JPanel playerListWrapperPanel = new JPanel();
		playerListWrapperPanel.add(playerListPanel);
		wrapperPanel.add(playerListWrapperPanel);
		
		pack();
	}
	
	private void updatePlayerList() {
		sendMessageToAllUsers("playerlist " + playerUsernames);
		
		playerListPanel.removeAll();
		playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
		playerListPanel.add(Box.createVerticalStrut(10));
		playerListPanel.add(new JLabel("Players in the Room"));
		playerListPanel.add(Box.createVerticalStrut(10));
		for(String username : playerUsernames) {
			JPanel rowPanel = new JPanel();
			rowPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			playerListPanel.add(rowPanel);
			playerListPanel.add(Box.createVerticalStrut(10));
			rowPanel.add(new JLabel(username));
			JTextField rowField = new JTextField(20);
			rowPanel.add(rowField);

			JButton send = new JButton("Message");
			send.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(!rowField.getText().trim().isEmpty()) {
						sendMessageToUser(username, "servermessage " + rowField.getText());
						rowField.setText("");
					}
				}
			});
			rowPanel.add(send);
			
			JButton kick = new JButton("Kick");
			if(username.equals(myUsername)) {
				kick.setEnabled(false);
			}
			Component thiss = this;
			kick.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int confirm = JOptionPane.showConfirmDialog(thiss, "Kick " + username + "?", "Really?", JOptionPane.YES_NO_OPTION);
					if(confirm == JOptionPane.YES_OPTION) {
						try {
							println("Kicking " + username + "...");
							sendMessageToAllUsers("servermessage " + username + " has been kicked");
							sendMessageToUser(username, "kick");
							if(clientSockets.get(username) != null && !clientSockets.get(username).isClosed()) {
								clientSockets.get(username).close();
							}
						}
						catch(IOException ioe) {
							println(ioe.getClass().getSimpleName() + ": " + ioe.getMessage());
						}
					}
				}
			});
			rowPanel.add(kick);
			rowPanel.setPreferredSize(rowPanel.getPreferredSize());
			rowPanel.validate();
		}
		playerListPanel.add(Box.createVerticalGlue());
		
		pack();
	}
	
	private void print(String text) {
		console.setText(console.getText() + text);
		JScrollBar vertical = consoleScroll.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
	}
	
	private void println(String text) {
		print(text + "\n");
	}
	
	private void clientLeft(String clientUsername) {
		channelsToClients.remove(clientUsername);
		playerUsernames.remove(clientUsername);
		if(gameWindow != null && playersInGame != null && playersInGame.contains(clientUsername)) {
			terminateGame("disconnected," + clientUsername);
		}
		else {
			println("Players in the room: " + playerUsernames);
			println("");
		}
		updatePlayButtons();
		updatePlayerList();
	}
	
	private void launchServer(String myUsername, String port) {
		myUsername = myUsername.replaceAll("\\s+","");
		this.myUsername = myUsername;
		playerUsernames = new ArrayList<String>();
		playerUsernames.add(myUsername);
		updatePlayerList();
		
		Thread listensForClients = new Thread(new Runnable() {
			public void run() {
				ServerSocket serverSocket = null;
				try {
					int portNumber = 4321;
					try {
						portNumber = Integer.parseInt(port);
					}
					catch(Exception e) {}
					
//					System.out.println(UPnP.getExternalIP());
//					System.out.println(UPnP.getLocalIP());
//					System.out.println("isupnpavail " + portNumber + ": " + UPnP.isUPnPAvailable());
//					System.out.println("ismappedtcp " + portNumber + ": " + UPnP.isMappedTCP(portNumber));
//					System.out.println("ismappedudp " + portNumber + ": " + UPnP.isMappedUDP(portNumber));
//					System.out.println("openporttcp " + portNumber + ": " + UPnP.openPortTCP(portNumber));
//					System.out.println("openportudp " + portNumber + ": " + UPnP.openPortUDP(portNumber));
					
					serverSocket = new ServerSocket(portNumber);
					channelsToClients = new HashMap<String, PrintWriter>();
					clientSockets = new HashMap<String, Socket>();
					println("Listening for clients on port " + portNumber + "...");
					println("");
					
					while(true) {
					    Socket clientSocket = serverSocket.accept();
					    println("Client accepted");
					    
					    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					    BufferedReader in = new BufferedReader(
					        new InputStreamReader(clientSocket.getInputStream()));
					    					    
					    String firstMessage = in.readLine();
					    String[] pieces = firstMessage.split(" ");
						String command = pieces[0];
						
						if(command.equals("newclient")) {
							String aName = pieces[1];
							aName = aName.replaceAll("\\s+","").replaceAll(",","");
							while(aName.isEmpty() || aName.equals("null") || playerUsernames.contains(aName)) {
								aName += "1";
							}
							
							playerUsernames.add(aName);
							
							channelsToClients.put(aName, out);
						    clientSockets.put(aName, clientSocket);
						    sendMessageToUser(aName, "setusername " + aName);
							sendMessageToAllUsers("servermessage " + aName + " joined the room");
							println("Players in the room: " + playerUsernames);
							println("");
						    updatePlayerList();
							updatePlayButtons();
							
							String clientUsername = aName;
						    
						    Thread listensToAClient = new Thread(new Runnable() {
								public void run() {
									try {
										String fromClient = in.readLine();
									    while (fromClient != null) {
									        handleMessageFromClient(clientUsername, fromClient);
									        fromClient = in.readLine();
									    }
									}
									catch(IOException ioe) {
										println(ioe.getClass().getSimpleName() + ": " + ioe.getMessage());
									}
									finally {
										println(clientUsername + " left the room");
										println("");
										clientLeft(clientUsername);
									}
								}
							});
						    listensToAClient.setPriority(Thread.MIN_PRIORITY);
						    listensToAClient.start();
						}
					}
				}
				catch(IOException ioe) {
					println(ioe.getClass().getSimpleName() + ": " + ioe.getMessage());
				}
				finally {
					try {
						println("Closing server socket");
						if(serverSocket != null) {
							serverSocket.close();
						}
					}
					catch(IOException ioe) {
						println(ioe.getClass().getSimpleName() + ": " + ioe.getMessage());
					}
				}
			}
		});
		listensForClients.setPriority(Thread.MIN_PRIORITY);
		listensForClients.start();
		
	}
	
	private void launchCthulhu(boolean deluxe) {
		
		int numPlayers = channelsToClients.size() + 1;
		gameWindow = new CTGameWindow("Server (" + numPlayers + " players)", myUsername, this);
		CTGameEngine ctEngine = new CTGameEngine(this);
		ctEngine.newGame(playerUsernames, null, deluxe);
		engine = ctEngine;

		println("Game started");
		println("");
		sendMessageToAllUsers("launchctgamewindow " + playerUsernames.size());

		ctEngine.newGame(playerUsernames, null, deluxe);
	}
	
	private void launchSkull() {
		
		int numPlayers = channelsToClients.size() + 1;
		gameWindow = new SKGameWindow("Server (" + numPlayers + " players)", myUsername, this);
		SKGameEngine skEngine = new SKGameEngine(this);
		engine = skEngine;

		println("Game started");
		println("");
		sendMessageToAllUsers("launchskgamewindow " + playerUsernames.size());
		skEngine.newGame(playerUsernames, null);
		
	}
	
	private void handleMessageFromClient(String clientSourceName, String message) {
		String[] pieces = message.split(" ");
		String command = pieces[0];
		
		if(command.equals("suggestkick")) {
			sendMessageToAllUsers("servermessage " + clientSourceName + " suggests kicking " + message.substring("suggestkick ".length()));
		}
		else if(command.equals("userinput")) {
			engine.handleUserInput(clientSourceName, pieces[1]);
		}
		else if(command.equals("gamewindowclosed")) {
			terminateGame("gamewindowclosed," + clientSourceName);
		}
	}
	
	public void sendMessageToAllUsers(String message) {
		for(String aUsername : this.playerUsernames) {
			sendMessageToUser(aUsername, message);
		}
	}
	
	public void sendMessageToUser(String clientName, String message) {
		if(clientName.equals(myUsername)) {
			String[] pieces = message.split(" ");
			String command = pieces[0];
			if(command.equals("terminategame")) {
				if(gameWindow != null) {
					println("Game terminated");
					updatePlayButtons();
					gameWindow.dispose();
					gameWindow = null;
				}
				println(pieces[1]);
				println("");
				println("Players in the room: " + playerUsernames);
				updatePlayButtons();
			}
			else if(command.equals("servermessage")) {
				println(message.substring("servermessage ".length()));
				println("");
			}
			else {
				if(gameWindow != null) {
					gameWindow.handleMessage(message);
				}
			}
		}
		else {
			PrintWriter channel = channelsToClients.get(clientName);
			channel.println(message);
		}
	}
	
	private void updatePlayButtons() {
		if(gameWindow != null) {
			playCthulhu.setEnabled(false);
			playSkull.setEnabled(false);
		}
		else if(playerUsernames.size() > 8) {
			playCthulhu.setEnabled(false);
			playSkull.setEnabled(false);
		}
		else if(playerUsernames.size() > 6) {
			playCthulhu.setEnabled(true);
			playSkull.setEnabled(false);
		}
		else if(playerUsernames.size() > 1) {
			playCthulhu.setEnabled(true);
			playSkull.setEnabled(true);
		}
		else {
			playCthulhu.setEnabled(false);
			playSkull.setEnabled(false);
		}
	}
	
	public void handleUserInput(String input) {
		engine.handleUserInput(myUsername, input);
	}
	
	private void terminateGame(String reason) {
		playersInGame = null;
		sendMessageToAllUsers("terminategame " + reason);
	}
	
	public void gameWindowClosed() {
		terminateGame("gamewindowclosed," + myUsername);
	}
}