package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SKGameWindow extends GameWindow {
	
	private SKBoard board;
	private Dimension cardSize;
	
	private JTextArea alertTextArea;
	private JPanel buttonPanel;
	private JScrollPane buttonScroll;

	protected JTextArea console;
	protected JScrollPane consoleScroll;
	protected String username;
	
	private SKGameState gameState;

	public SKGameWindow(String title, String username, Networker networker) {
		super(title, networker);
		this.username = username;
		cardSize = Main.skCardSize;
		
		init();
	}
	
	private void init() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		this.add(panel);
		
		board = new SKBoard(this);
		panel.add(board);
		
		JPanel outputPanel = new JPanel();
		FlowLayout flow = new FlowLayout(FlowLayout.CENTER, 10, 10);
		outputPanel.setLayout(flow);
		panel.add(outputPanel);

		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK);
				
		alertTextArea = new JTextArea(2, 10);
		alertTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
		alertTextArea.setEditable(false);
		alertTextArea.setBorder(border);
		alertTextArea.setPreferredSize(new Dimension(200, 100));
		alertTextArea.setWrapStyleWord(true);
		outputPanel.add(alertTextArea);

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		buttonScroll = new JScrollPane(buttonPanel);
		buttonScroll.setPreferredSize(new Dimension(220, 100));
		outputPanel.add(buttonScroll);
		
		setVisible(true);
		console = new JTextArea();
		console.setEditable(false);
		consoleScroll = new JScrollPane(console);
		consoleScroll.setPreferredSize(new Dimension(300, 100));
		outputPanel.add(consoleScroll);
		
		JMenuBar bar = new JMenuBar();
		setJMenuBar(bar);
		
		JMenu zoomMenu = new JMenu("Zoom");
		bar.add(zoomMenu);
		JSlider zoom = new JSlider(JSlider.HORIZONTAL);
		zoom.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setZoom(((JSlider) e.getSource()).getValue());
			}
		});
		zoomMenu.add(zoom);
		
		JMenu toolsMenu = new JMenu("Tools");
		bar.add(toolsMenu);
		JMenuItem chronobreak = new JMenuItem("Chronobreak");
		JFrame thiss = this;
		chronobreak.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(gameState != null && (gameState.getWinner() == null || gameState.getWinner().equals("null") || gameState.getWinner().trim().equals(""))) {
					ArrayList<String> playerList = gameState.getPlayerNames();
					JDialog window = new JDialog(thiss, "Chronobreak");
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
					window.add(panel);
					
					panel.add(new JLabel("Whose turn to start?"));
					ButtonGroup group = new ButtonGroup();
					for(String player : playerList) {
						JRadioButton radio = new JRadioButton(player);
						radio.setActionCommand(player);
						panel.add(radio);
						group.add(radio);
						if(group.getSelection() == null) {
							radio.setSelected(true);
						}
					}
					panel.add(Box.createVerticalStrut(10));
					panel.add(new JLabel("Password: which animal is the most tilt-proof?"));
					JTextField text = new JTextField(10);
					panel.add(text);
					panel.add(Box.createVerticalStrut(10));
					JButton chronobreak = new JButton("Chronobreak");
					chronobreak.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(text.getText().trim().toLowerCase().equals("tortoise")) {
								window.dispose();
								networker.handleUserInput("chronobreak," + group.getSelection().getActionCommand());
							}
							else {
								panel.add(new JLabel("Wrong password"));
								window.pack();
							}
						}
					});
					panel.add(chronobreak);
					
					window.pack();
					window.setVisible(true);
				}
			}
		});
		toolsMenu.add(chronobreak);
		
		pack();
		
	}
	
	private void setZoom(int zoom) {
		if(zoom <= 0) {
			zoom = 1;
		}
		double ratio = ((double) zoom)/50.0;
		cardSize = zoomDimension(Main.skCardSize, ratio);
		SKCard.loadImages(cardSize);
		SKPlayerTile.loadImages(cardSize);
		board.zoomBoard(cardSize);
		
		pack();
		repaint();
	}

	private Dimension zoomDimension(Dimension dim, double ratio) {
		return new Dimension((int) (ratio*((double) dim.width)), (int) (ratio*((double) dim.height)));
	}
	
	public Dimension getCardSize() {
		return cardSize;
	}
	
	protected void print(String text) {
		console.setText(console.getText() + text);
		JScrollBar vertical = consoleScroll.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
	}
	
	protected void println(String text) {
		print(text + "\n");
	}
	
	private void alert(String text) {
		alertTextArea.setText(text);
	}
	
	public void handleMessage(String message) {
		String[] pieces = message.split(" ");
		String command = pieces[0];

		if(command.equals("message")) {
			String data = message.substring("message ".length());
			println(data);
		}
		if(command.equals("alert")) {
			String data = message.substring("alert ".length());
			alert(data);
		}
		else if(command.equals("gamestate")) {
			String data = message.substring("gamestate".length() + 1);
			setGameState(new SKGameState(data));
		}
		else if(command.equals("clearalerts")) {
			buttonPanel.removeAll();
			alert("");
		}
		else if(command.equals("awaitok")) {
			JButton ok = new JButton("OK");
			ok.setFont(new Font("Times New Roman", Font.BOLD, 40));
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					networker.handleUserInput("ok");
				}
			});
			showButton(ok);
		}
		else if(command.equals("playcard")) {
			alert("Your turn\nPlay a card");
		}
		else if(command.equals("playcardorbid")) {
			alert("Your turn\nPlay a card or bid");
			showBidButtons(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]), false);
		}
		else if(command.equals("bid")) {
			alert("Your turn to bid");
			showBidButtons(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]), true);
		}
		else if(command.equals("mustbid")) {
			alert("Your turn\nMust bid");
			showBidButtons(Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]), false);
		}
		else if(command.equals("findflowers")) {
			int num = Integer.parseInt(pieces[1]);
			String msg = "Find more flowers";
			alert(msg);
		}
		else if(command.equals("discard")) {
			alert("Discard a card");
		}
		else if(command.equals("takecard")) {
			alert("Take a card from " + pieces[1]);
		}
		else if(command.equals("awaitrevealinplay")) {
			int num = Integer.parseInt(pieces[1]);
			String msg = "Your " + num + " bid stands\nReveal your cards";
			alert(msg);
		}
		else if(command.equals("awaitreadynext")) {
			JButton readynext = new JButton("Readynext");
			readynext.setFont(new Font("Times New Roman", Font.BOLD, 30));
			readynext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					networker.handleUserInput("readynext," + username);
					readynext.setEnabled(false);
				}
			});
			showButton(readynext);
		}
		
		pack();
		repaint();
	}
	
	private void showBidButtons(int min, int max, boolean canPass) {
		buttonPanel.removeAll();
		JPanel bidPanel = new JPanel();
		bidPanel.setLayout(new BoxLayout(bidPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(bidPanel);
		
		int rows = (max+3)/4;
		if(canPass) {
			rows++;
		}
		int cols = Math.min(max, 4);
		int panelW = buttonScroll.getPreferredSize().width - 10;
		int panelH = buttonScroll.getPreferredSize().height - 10;
		
		Font font = new Font("Times New Roman", Font.PLAIN, Math.max(45-10*rows, 10));
		
		JPanel numPanel = new JPanel();
		numPanel.setLayout(new GridLayout((max+3)/4, 4));
		for(int i = 1; i <= max; i++) {
			JButton but = new JButton(""+i);
			but.setFont(font);
			but.setPreferredSize(new Dimension(panelW/cols, panelH/rows));
			if(i >= min) {
				but.setEnabled(true);
				but.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						networker.handleUserInput("bid," + but.getText());
					}
				});
			}
			else {
				but.setEnabled(false);
			}
			numPanel.add(but);
		}
		bidPanel.add(numPanel);
		
		if(canPass) {
			JButton pass = new JButton("Pass");
			pass.setFont(font);
			pass.setPreferredSize(new Dimension(panelW/2, panelH/rows));
			pass.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					networker.handleUserInput("pass");
				}
			});
			pass.setAlignmentX(CENTER_ALIGNMENT);
			bidPanel.add(pass);
		}
		
		pack();
		repaint();
	}
	
	private void showButton(JButton button) {
		buttonPanel.removeAll();
		buttonPanel.add(button);
		
		pack();
		repaint();
	}
	
	private void setGameState(SKGameState state) {
		this.gameState = state;
		board.updateBoard(state, username, cardSize);
		pack();
		repaint();
	}
	
	public void cardClicked(String location, int num) {
		networker.handleUserInput("cardclicked," + location + "," + num);
	}
	
	public void playerClicked(String location) {
		networker.handleUserInput("playerclicked," + location);
	}
}