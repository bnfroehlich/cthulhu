package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerListModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CTGameWindow extends GameWindow {

	private String username;
	private Dimension cardSize;
	private boolean deluxe;
	
	private CTBoard board;
	private JTextArea alertTextArea;
	private JPanel buttonPanel;
	
	protected JTextArea console;
	protected JScrollPane consoleScroll;
	
	private HashMap<String, Image> fullsizeImages;

	public CTGameWindow(String title, String username, Networker networker) {
		super(title, networker);
		this.username = username;
		cardSize = Main.ctCardSize;
		
		init();
	}
	
	private void init() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		this.add(panel);
		
		board = new CTBoard(this);
		panel.add(board);
		
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
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
		buttonPanel.setPreferredSize(new Dimension(400, 100));
		outputPanel.add(buttonPanel);
		
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
		JLabel zoomLabel = new JLabel("Zoom");
		zoomMenu.add(zoomLabel);
		JSlider zoom = new JSlider(JSlider.HORIZONTAL);
		zoom.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setZoom(((JSlider) e.getSource()).getValue());
			}
		});
		zoomMenu.add(zoom);

		JMenu showCardsMenu = new JMenu("View All Cards");
		bar.add(showCardsMenu);
		JMenuItem showCards = new JMenuItem("View All Cards");
		showCardsMenu.add(showCards);
		showCards.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCards();
			}
		});
		
		pack();
	}
	
	private void setZoom(int zoom) {
		if(zoom <= 0) {
			zoom = 1;
		}
		double ratio = ((double) zoom)/50.0;
		cardSize = zoomDimension(Main.ctCardSize, ratio);
		CTCardSetPanel.loadImages(cardSize);
		CTPlayerPanel.loadImages(cardSize);
		board.zoomBoard(cardSize);
		
		pack();
		repaint();
	}

	private Dimension zoomDimension(Dimension dim, double ratio) {
		return new Dimension((int) (ratio*((double) dim.width)), (int) (ratio*((double) dim.height)));
	}
	
	private void showCards() {
		JDialog window = new JDialog(this, "Cards");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		window.add(panel);
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.add(listPanel);
		
		JLabel dispLabel = new JLabel();
		dispLabel.setIcon(new ImageIcon(Main.loadImage("blank.png", -1, -1, true)));
		panel.add(dispLabel);
		
		Thread imageLoaderThread = new Thread(new Runnable() {
			public void run() {
				if(fullsizeImages == null) {
					fullsizeImages = new HashMap<String, Image>();
				}
				CTCard.Value[] cardNames = CTCard.Value.values();
				for(CTCard.Value cardName : cardNames) {
					if(!fullsizeImages.containsKey(cardName.toString())) {
						Image image = Main.loadImage(cardName.toString().toLowerCase() + ".png", -1, -1, true);
						fullsizeImages.put(cardName.toString(), image);
					}
				}
			}
		});
		imageLoaderThread.setPriority(Thread.MIN_PRIORITY);
		imageLoaderThread.start();
		
		ButtonGroup group = new ButtonGroup();
		for(CTCard.Value card : CTCard.Value.values()) {
			JRadioButton cardRadio = new JRadioButton(card.toString());
			cardRadio.setFont(new Font("Dialog", Font.PLAIN, 20));
			group.add(cardRadio);
			listPanel.add(cardRadio);
			cardRadio.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(fullsizeImages != null && fullsizeImages.containsKey(cardRadio.getText())) {
						dispLabel.setText("");
						dispLabel.setIcon(new ImageIcon(fullsizeImages.get(cardRadio.getText())));
						window.pack();
					}
					else {
						dispLabel.setIcon(null);
						dispLabel.setText("Image not found");
					}
				}
			});
			
			if(card == CTCard.Value.Cthulhu) {
				listPanel.add(Box.createVerticalStrut(30));
			}
		}
		
		window.pack();
		window.setVisible(true);
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
			String data = message.substring("message".length() + 1);
			println(data);
		}
		else if(command.equals("gamestate")) {
			String data = message.substring("gamestate".length() + 1);
			setGameState(new CTGameState(data));
		}
		else if(command.equals("newgame")) {
			alert("");
			this.deluxe = Boolean.parseBoolean(pieces[1]);
			showAnnounceButtons();
		}
		else if(command.equals("newround")) {
			showAnnounceButtons();
		}
		else if(command.equals("taketurn")) {
			alert("Your turn\nProbe a card");
		}
		else if(command.equals("clearalerts")) {
			alert("");
		}
		else if(command.equals("lookupcard")) {
			alert("Prescient Vision\nLookup a card");
		}
//		else if(command.equals("awaitokplaycard")) {
//			JButton ok = new JButton("OK");
//			ok.setFont(new Font("Times New Roman", Font.BOLD, 40));
//			ok.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					alertShowTextArea();
//					networker.handleUserInput("okplaycard," + username);
//				}
//			});
//			alertShowButton(ok);
//		}
//		else if(command.equals("awaitokreshuffle")) {
//			JButton reshuffle = new JButton("Reshuffle");
//			reshuffle.setFont(new Font("Times New Roman", Font.BOLD, 40));
//			reshuffle.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					networker.handleUserInput("okreshuffle," + username);
//					alertShowTextArea();
//				}
//			});
//			alertShowButton(reshuffle);
//		}
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
	
	private void showAnnounceButtons() {
		buttonPanel.removeAll();
		buttonPanel.setLayout(new GridBagLayout());
		
		JPanel subButtonPanel = new JPanel();
		subButtonPanel.setLayout(new GridBagLayout());
		buttonPanel.add(subButtonPanel);
		
		String[] ints = {"0", "1", "2", "3", "4", "5"};
		JSpinner clueSpinner = new JSpinner(new SpinnerListModel(ints));
		((JSpinner.DefaultEditor) clueSpinner.getEditor()).getTextField().setColumns(1);
		
		JPanel row1 = new JPanel();
		row1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		GridBagConstraints row1Const = new GridBagConstraints();
		subButtonPanel.add(row1, row1Const);
		row1.add(new JLabel("Clues"));
		row1.add(clueSpinner);
		
		JCheckBox cthulhuCheck = new JCheckBox("Cthulhu");
		GridBagConstraints row2Const = new GridBagConstraints();
		row2Const.gridy = 1;
		subButtonPanel.add(cthulhuCheck, row2Const);
		
		ArrayList<JCheckBox> specialChecks = new ArrayList<JCheckBox>();
		if(deluxe) {
			JPanel specialPanel = new JPanel();
			specialPanel.setLayout(new GridLayout(3, 2));
			GridBagConstraints specialConst = new GridBagConstraints();
			specialConst.gridx = 2;
			specialConst.gridheight = 3;
			specialConst.insets = new Insets(0, 30, 0, 0);
			subButtonPanel.add(specialPanel, specialConst);
			
			String[] specialNames = {"Evil Presence", "Mirage", "Paranoia", "Prescient Vision", "Private Eye", "Insanity's Grasp"};
			for(String name : specialNames) {
				JCheckBox check = new JCheckBox(name);
				specialPanel.add(check);
				specialChecks.add(check);
			}
		}
		
		JButton announce = new JButton("Announce");
		announce.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String announcement = clueSpinner.getValue() + "";
				if(cthulhuCheck.isSelected()) {
					announcement += "-Cthulhu";
				}
				if(deluxe) {
					String specialNames = "";
					for(JCheckBox check : specialChecks) {
						if(check.isSelected()) {
							if(check.getText().equals("Insanity's Grasp")) {
								announcement = "Insane";
								networker.handleUserInput("announcehand," + announcement);
								return;
							}
							for(char c : check.getText().toCharArray()) {
								if(Character.isUpperCase(c)) {
									specialNames += c;
								}
							}
							specialNames += "•";
						}
					}
					if(!specialNames.isEmpty()) {
						announcement += "-" + specialNames.substring(0, specialNames.length()-1);
					}
				}
				networker.handleUserInput("announcehand," + announcement);
			}
		});
		GridBagConstraints row3Const = new GridBagConstraints();
		row3Const.gridx = 0;
		row3Const.gridy = 2;
		subButtonPanel.add(announce, row3Const);
	}
	
	private void showButton(JButton button) {
		buttonPanel.removeAll();
		buttonPanel.add(button);
	}
	
	private void setGameState(CTGameState state) {
		board.updateBoard(state, username, cardSize);
		pack();
		repaint();
	}
	
	public void cardClicked(String location, int num) {
		networker.handleUserInput("cardclicked," + location + "," + num);
	}
}