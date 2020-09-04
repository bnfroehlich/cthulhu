package bfroehlich.cthulhu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JFrame implements Networker {

	private JTextArea console;
	private JScrollPane consoleScroll;
	private JButton connect;
	private JPanel playerListPanel;
	
	private PrintWriter channelToServer;
	private GameWindow gameWindow;
		
	private String username;
	
	public static void main(String[] args) {
		new Client();
	}
	
	public Client() {
		super("Client");
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		init();
	}
	
	private void init() {
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.X_AXIS));
		add(wrapperPanel);
		
		JPanel panel = new JPanel();
		wrapperPanel.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		FlowLayout flow = new FlowLayout(FlowLayout.RIGHT, 10, 10);
		
		JPanel namePanel = new JPanel();
		namePanel.setLayout(flow);
		panel.add(namePanel);
		namePanel.add(new JLabel("username"));
		JTextField nameField = new JTextField("", 20);
		namePanel.add(nameField);
		
		JPanel hostPanel = new JPanel();
		hostPanel.setLayout(flow);
		panel.add(hostPanel);
		hostPanel.add(new JLabel("host IP"));
		JTextField hostIPField = new JTextField("", 20);
		hostPanel.add(hostIPField);
		
		JPanel portPanel = new JPanel();
		portPanel.setLayout(flow);
		panel.add(portPanel);
		portPanel.add(new JLabel("port"));
		JTextField portField = new JTextField("4321", 20);
		portPanel.add(portField);
		
		panel.add(Box.createVerticalStrut(10));
		connect = new JButton("Connect");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(nameField.getText().isEmpty() || hostIPField.getText().isEmpty()) {
					println("Empty input");
				}
				else {
					connect.setEnabled(false);
					try {
						connect(nameField.getText(), hostIPField.getText(), portField.getText());
					}
					catch(IOException ioe) {
						println("Unable to connect");
						println(ioe.getClass().getSimpleName() + ": " + ioe.getMessage());
						println("");
						connect.setEnabled(true);
					}
				}
			}
		});
		panel.add(connect);
		panel.add(Box.createVerticalStrut(10));

		console = new JTextArea();
		console.setEditable(false);
		consoleScroll = new JScrollPane(console);
		consoleScroll.setPreferredSize(new Dimension(350, 150));
		panel.add(consoleScroll);

		playerListPanel = new JPanel();
		JPanel playerListWrapperPanel = new JPanel();
		playerListWrapperPanel.add(playerListPanel);
		JScrollPane playerListWrapperScroll = new JScrollPane(playerListWrapperPanel);
		playerListWrapperScroll.setPreferredSize(new Dimension(300, 300));
		wrapperPanel.add(playerListWrapperScroll);
		
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
	
	private void connect(String username, String hostIP, String port) throws IOException {
		int portNumber = 4321;
		try {
			portNumber = Integer.parseInt(port);
		}
		catch(Exception e) {}
		this.username = username;
		
		println("Connecting to " + hostIP + " on port " + portNumber + "...");

	    Socket socket = new Socket(hostIP, portNumber);
	    if(socket.isConnected()) {
	    	println("Connected");
	    	println("");
	    }
	    else {
	    	println("Connection failed");
	    	println("");
	    	return;
	    }
	    
	    channelToServer = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
	        new InputStreamReader(socket.getInputStream()));
	    

	    Thread listener = new Thread(new Runnable() {
			public void run() {
				try {
					String fromServer = in.readLine();
				    while (fromServer != null) {
				        handleMessageFromServer(fromServer);
				        fromServer = in.readLine();
				    }
				}
				catch(IOException ioe) {
					println(ioe.getClass().getSimpleName() + ": " + ioe.getMessage());
					if(gameWindow != null) {
						println("Game terminated");
						gameWindow.dispose();
						gameWindow = null;
					}
					println("");
					connect.setEnabled(true);
				}
				finally {
					try {
						println("Connection lost");
						println("");
						playerListPanel.removeAll();
						connect.setEnabled(true);
						pack();
						repaint();
						socket.close();
					}
					catch(IOException ioe) {
						println(ioe.getClass().getSimpleName() + ": " + ioe.getMessage());
					}
				}
			}
		});
	    listener.setPriority(Thread.MIN_PRIORITY);
	    listener.start();
	    
	    sendMessageToServer("newclient " + username);
	}
	
	public void gameWindowClosed() {
		println("Window closed");
		channelToServer.println("gamewindowclosed");
	}
	
	private void updatePlayerList(ArrayList<String> playerUsernames) {
		
		playerListPanel.removeAll();
		playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
		playerListPanel.add(Box.createVerticalStrut(10));
		JLabel title = new JLabel("Players in the Room");
		title.setAlignmentX(CENTER_ALIGNMENT);
		playerListPanel.add(title);
		playerListPanel.add(Box.createVerticalStrut(10));
		for(String username : playerUsernames) {
			JPanel rowPanel = new JPanel();
			rowPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			playerListPanel.add(rowPanel);
			rowPanel.add(new JLabel(username));
			
			JButton suggestKick = new JButton("Suggest Kick");
			suggestKick.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendMessageToServer("suggestkick " + username);
				}
			});
			rowPanel.add(suggestKick);
			rowPanel.setPreferredSize(rowPanel.getPreferredSize());
			rowPanel.validate();
		}
		playerListPanel.add(Box.createVerticalGlue());
		
		pack();
	}
	
	private void handleMessageFromServer(String message) {
		String[] pieces = message.split(" ");
		String command = pieces[0];

		if(command.equals("setusername")) {
			this.username = message.substring(("setusername ".length()));
		}
		if(command.equals("kick")) {
			println("Kicked by server");
			if(gameWindow != null) {
				gameWindow.dispose();
				gameWindow = null;
			}
		}
		else if(command.equals("playerlist")) {
			String namesData = message.substring("playerlist [".length(), message.length()-1);
			ArrayList<String> playerUsernames = new ArrayList<String>(Arrays.asList(namesData.split(", ")));
			updatePlayerList(playerUsernames);
		}
		else if(command.equals("servermessage")) {
			println(message.substring("servermessage ".length()));
			println("");
		}
		else if(command.equals("launchctgamewindow")) {
			gameWindow = new CTGameWindow("Client (" + pieces[1] + " players)", username, this);
			println("Game started");
			println("");
		}
		else if(command.equals("launchskgamewindow")) {
			gameWindow = new SKGameWindow("Client (" + pieces[1] + " players)", username, this);
			println("Game started");
			println("");
		}
		else if(command.equals("terminategame")) {
			println("Game terminated");
			println(pieces[1]);
			println("");
			if(gameWindow != null) {
				gameWindow.dispose();
				gameWindow = null;
			}
		}
		else {
			if(gameWindow != null) {
				gameWindow.handleMessage(message);
			}
			else {
				//println("Server says: " + message);
			}
		}
	}
	
	private void sendMessageToServer(String message) {
		channelToServer.println(message);
	}
	
	public void handleUserInput(String input) {
		sendMessageToServer("userinput " + input);
	}
}