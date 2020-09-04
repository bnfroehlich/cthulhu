package bfroehlich.cthulhu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class CTGameEngine implements GameEngine {
	
	private ArrayList<String> playerList;
	private CTGameState gameState;
	private Server server;
	private boolean deluxe;

	private CTActionPendingInput pendingAction;
		
	private String flashlightControllerRestOfThisRound;
	private String privateEyeRevealedPlayer;
	private String privateEyeKnowledgeablePlayer;
	
	public CTGameEngine(Server server) {
		this.server = server;
	}
	
	private CTGameState sample() {
		String[] names = {"Al", "Bob", "Carl", "Dan", "Ed", "Frank", "Gil", "Hank"};
		ArrayList<String> names2 = new ArrayList<String>(Arrays.asList(names));
		return getStartingGameState(names2, false);
	}
	
	private ArrayList<CTCard> startingDeck(int players, boolean deluxe) {
		ArrayList<CTCard> deck = new ArrayList<CTCard>();
		deck.add(new CTCard(CTCard.Value.Cthulhu));
		for(int i = 0; i < players; i++) {
			deck.add(new CTCard(CTCard.Value.Clue));
		}
		if(deluxe) {
			CTCard[] specialsArr = {new CTCard(CTCard.Value.EvilPresence), new CTCard(CTCard.Value.Mirage), new CTCard(CTCard.Value.InsanitysGrasp), new CTCard(CTCard.Value.Paranoia), new CTCard(CTCard.Value.PrescientVision), new CTCard(CTCard.Value.PrivateEye)};
			ArrayList<CTCard> specials = new ArrayList<CTCard>(Arrays.asList(specialsArr));
			Collections.shuffle(specials);
			if(players <= 6) {
				specials.remove(0);
			}
			deck.addAll(specials);
		}
		while(deck.size() < players*5) {
			deck.add(new CTCard(CTCard.Value.Rock));
		}
		return deck;
	}
	
	private CTGameState getStartingGameState(ArrayList<String> playerNames, boolean deluxe) {
		ArrayList<CTCard> deck = startingDeck(playerNames.size(), deluxe);
		
		Collections.shuffle(deck);
		int handSize = deck.size()/playerNames.size();
		ArrayList<CTPlayer> players = new ArrayList<CTPlayer>();
		
		String[][] rolesData = {{"Investigator", "Cultist"}, 																				//2 player game
				{"Investigator", "Investigator", "Cultist"},																				//3
				{"Investigator", "Investigator", "Investigator", "Cultist"},																//4
				{"Investigator", "Investigator", "Investigator", "Cultist", "Cultist", "Investigator"}, 									//5
				{"Investigator", "Investigator", "Investigator", "Cultist", "Cultist", "Investigator"}, 									//6
				{"Investigator", "Investigator", "Investigator", "Cultist", "Cultist", "Investigator", "Cultist", "Investigator"}, 			//7
				{"Investigator", "Investigator", "Investigator", "Cultist", "Cultist", "Investigator", "Cultist", "Investigator"}}; 		//8
		
		ArrayList<String> rolesDeck = new ArrayList<String>(Arrays.asList(rolesData[playerNames.size() - 2]));
		Collections.shuffle(rolesDeck);
		
		for(int i = 0; i < playerNames.size(); i++) {
			ArrayList<CTCard> cards = new ArrayList<CTCard>();
			for(int j = 0; j < handSize; j++) {
				CTCard card = deck.get(i*handSize + j);
				card.setHidden(true);
				cards.add(card);
			}
			CTCardSet playerCardSet = new CTCardSet(cards);
			CTPlayer player = new CTPlayer(playerNames.get(i), CTRole.valueOf(rolesDeck.remove(0)), playerCardSet);
			players.add(player);
		}
		
		return new CTGameState(players, null, null);
	}
	
	public void newGame(ArrayList<String> playerNames, String firstPlayer, boolean deluxe) {
		gameState = getStartingGameState(playerNames, deluxe);
		this.deluxe = deluxe;
		
		privateEyeKnowledgeablePlayer = null;
		privateEyeRevealedPlayer = null;
		flashlightControllerRestOfThisRound = null;
				
		Random rand = new Random();
		ArrayList<CTPlayer> players = gameState.getPlayers();
		if(firstPlayer == null) {
			firstPlayer = players.get(rand.nextInt(players.size())).getName();
		}

		gameState.getInPlay().add(new CTCardSet(""));
		gameState.getInPlay().add(new CTCardSet(""));
		gameState.getInPlay().add(new CTCardSet(""));
		gameState.getInPlay().add(new CTCardSet(""));
		
		sendGameState();
		
		server.sendMessageToAllUsers("newgame " + deluxe);
		
		gameLog("New game\nRound " + (gameState.getRoundNum()+1));
		nextTurn(firstPlayer);
	}
	
	public void handleUserInput(String playerSource, String input) {
		Thread handlerThread = new Thread(new Runnable() {
			
			public void run() {
				String[] pieces = input.split(",");
				String command = pieces[0];
				if(command.equals("announcehand")) {
					gameState.getPlayerByName(playerSource).setAnnouncedHand(pieces[1]);
					sendGameState();
				}
				else if(pendingAction != null) {
					pendingAction.handleUserInput(playerSource, input);
					sendGameState();
				}
			}
		});
		//so the (server) GUI won't freeze while the input is handled
		handlerThread.setPriority(Thread.MIN_PRIORITY);
		handlerThread.start();
	}
	
	private void nextTurn(String playerToMove) {
		if(gameState.cardsInPlay() >= (gameState.getRoundNum()+1)*gameState.getPlayers().size()) {
			//round ends
			if(gameState.getRoundNum() >= 3) {
				gameOver(false, playerToMove);
			}
			else {
				//new round
				gameLog("Round ends");
				sendGameState();
				try {
					Thread.sleep(3000);
				}
				catch(InterruptedException ie) {}
				reshuffleHands();
				flashlightControllerRestOfThisRound = null;
				server.sendMessageToAllUsers("newround");
				giveFlashlightAwaitMove(playerToMove);
			}
		}
		else {
			//continue this round
			if(flashlightControllerRestOfThisRound != null) {
				giveFlashlightAwaitMove(flashlightControllerRestOfThisRound);
			}
			else {
				giveFlashlightAwaitMove(playerToMove);
			}
		}
	}
	
	private void giveFlashlightAwaitMove(String playerToMove) {
		for(CTPlayer aPlayer : gameState.getPlayers()) {
			aPlayer.setHasFlashlight(false);
		}
		gameState.getPlayerByName(playerToMove).setHasFlashlight(true);
		
		sendGameState();
		
		server.sendMessageToAllUsers("clearalerts");
		
		setPlayerAwaitingInput(playerToMove);
		pendingAction = new CTActionPendingInput() {
			
			public void handleUserInput(String playerSource, String input) {
				String[] pieces = input.split(",");
				String command = pieces[0];
				if(command.equals("cardclicked") && playerSource.equals(playerToMove)) {
					String cardClickedLocation = pieces[1];
					if(!cardClickedLocation.equals("null") && !cardClickedLocation.equals(playerToMove)) {
						//a legal card has been investigated
						pendingAction = null;
						setPlayerAwaitingInput(null);
						
						int cardClickedNum = Integer.parseInt(pieces[2]);
						CTCard cardClicked = gameState.getPlayerByName(cardClickedLocation).getCardSet().getCards().get(cardClickedNum);
						gameLog(cardClickedLocation + " revealed " + cardClicked.getValue());
						
						sleepPlayCard(cardClickedLocation, cardClickedNum, playerSource);
					}
				}
			}
		};
		
		server.sendMessageToUser(playerToMove, "taketurn");
	}
	
	private void reshuffleHands() {
		gameState.setRoundNum(gameState.getRoundNum() + 1);
		gameLog("Round " + (gameState.getRoundNum() + 1));
		
		ArrayList<CTCard> deck = new ArrayList<CTCard>();
		for(CTPlayer player : gameState.getPlayers()) {
			ArrayList<CTCard> cards = player.getCardSet().getCards();
			deck.addAll(cards);
			cards.clear();
		}
		ArrayList<CTCard> discard = gameState.getDiscard().getCards();
		deck.addAll(discard);
		discard.clear();
		
		for(CTCard card : deck) {
			card.setHidden(true);
		}
		
		Collections.shuffle(deck);
		int handSize = deck.size()/gameState.getPlayers().size();
		for(CTPlayer player : gameState.getPlayers()) {
			for(int i = 0; i < handSize; i++) {
				player.getCardSet().getCards().add(deck.remove(0));
			}
			player.setAnnouncedHand("");
		}
		
		
		sendGameState();
	}
	
	private void sleepPlayCard(String cardOwnerName, int cardNum, String investigatorName) {
		//location and number within that location id the card
		
		CTPlayer cardOwner = gameState.getPlayerByName(cardOwnerName);
		ArrayList<CTCard> cardClickedArr = cardOwner.getCardSet().getCards();
		CTCard cardClicked = cardClickedArr.get(cardNum);
		
		cardClicked.setHidden(false);

		server.sendMessageToAllUsers("clearalerts");
		sendGameState();
		
		try {
			Thread.sleep(1500);
		}
		catch(InterruptedException ie) {}
				
		int roundNum = gameState.getRoundNum();
		
		cardClickedArr.remove(cardNum);
		ArrayList<CTCard> inPlayThisRound = gameState.getInPlay().get(roundNum).getCards();
		inPlayThisRound.add(cardClicked);
		sendGameState();
		
		if(cardClicked.getValue() == CTCard.Value.Cthulhu) {
			gameOver(false, cardOwnerName);
		}
		else if(gameState.cluesInPlay() >= gameState.getPlayers().size()) {
			//all clues found
			gameOver(true, cardOwnerName);
		}
		else {
			boolean proceedNextTurn = true;
			//special rocks
			if(cardClicked.getValue() == CTCard.Value.EvilPresence) {
				ArrayList<CTCard> cards = cardOwner.getCardSet().getCards();
				boolean discardedClue = false;
				for(CTCard card : cards) {
					if(card.getValue() == CTCard.Value.Clue) {
						discardedClue = true;
					}
				}
				gameState.getDiscard().getCards().addAll(cards);
				cards.clear();
				if(discardedClue && gameState.getRoundNum() >= 3) {
					gameOver(false, cardOwnerName);
					proceedNextTurn = false;
				}
			}
			else if(cardClicked.getValue() == CTCard.Value.Mirage) {
				//find last played clue, discard it
				ArrayList<CTCardSet> inPlay = gameState.getInPlay();
				for(int i = inPlay.size()-1; i >= 0; i--) {
					CTCardSet inPlayRound = inPlay.get(i);
					boolean foundClue = false;
					for(int j = inPlayRound.getCards().size()-1; j >= 0; j--) {
						if(inPlayRound.getCards().get(j).getValue() == CTCard.Value.Clue) {
							CTCard removed = inPlayRound.getCards().remove(j);
							gameState.getDiscard().getCards().add(removed);
							foundClue = true;
							if(gameState.getRoundNum() >= 3) {
								gameOver(false, cardOwnerName);
								proceedNextTurn = false;
							}
							break;
						}
					}
					if(foundClue) {
						break;
					}
				}
			}
			else if(cardClicked.getValue() == CTCard.Value.Paranoia) {
				flashlightControllerRestOfThisRound = cardOwnerName;
			}
			else if(cardClicked.getValue() == CTCard.Value.PrescientVision) {
				server.sendMessageToAllUsers("clearalerts");
				setPlayerAwaitingInput(cardOwnerName);
				pendingAction = new CTActionPendingInput() {
					
					public void handleUserInput(String playerSource, String input) {
						String[] pieces = input.split(",");
						String command = pieces[0];
						if(command.equals("cardclicked") && playerSource.equals(cardOwnerName)) {
							String cardClickedLocation = pieces[1];
							if(!cardClickedLocation.equals("null")) {
								//a legal card has been looked up
								pendingAction = null;
								setPlayerAwaitingInput(null);
								
								int cardClickedNum = Integer.parseInt(pieces[2]);
								CTCard cardClicked = gameState.getPlayerByName(cardClickedLocation).getCardSet().getCards().get(cardClickedNum);
								gameLog(cardClickedLocation + " showed " + cardClicked.getValue() + " in position " + (cardClickedNum+1) + " due to Prescient Vision");
								
								cardClicked.setHidden(false);

								server.sendMessageToAllUsers("clearalerts");
								sendGameState();
								
								nextTurn(playerSource);
							}
						}
					}
				};
				
				server.sendMessageToUser(cardOwnerName, "lookupcard");
				proceedNextTurn = false;
			}
			else if(cardClicked.getValue() == CTCard.Value.PrivateEye) {
				privateEyeRevealedPlayer = cardOwnerName;
				privateEyeKnowledgeablePlayer = investigatorName;
			}
						
			if(proceedNextTurn) {
				nextTurn(cardOwnerName);
			}
		}
	}
	
	private void awaitOKNextGame(String toGoFirstNextGame) {
		HashMap<String, Boolean> oksNextGameReceived = new HashMap<String, Boolean>();
		for(CTPlayer player : gameState.getPlayers()) {
			oksNextGameReceived.put(player.getName(), false);
		}
		
		setAllPlayersAwaitingInput();
		pendingAction = new CTActionPendingInput() {
			
			public void handleUserInput(String playerSource, String input) {
				String[] pieces = input.split(",");
				String command = pieces[0];
				
				if(command.equals("readynext")) {
					gameState.getPlayerByName(playerSource).setAwaitingInput(false);
					oksNextGameReceived.put(playerSource, true);
					boolean allReady = true;
					for(CTPlayer player : gameState.getPlayers()) {
						if(!oksNextGameReceived.get(player.getName())) {
							allReady = false;
							break;
						}
					}
					if(allReady) {
						pendingAction = null;
						newGame(gameState.getPlayerNames(), toGoFirstNextGame, deluxe);
					}
				}
			}
		};
		
		server.sendMessageToAllUsers("awaitreadynext");
	}
	
	private void gameOver(boolean investigatorsWin, String toGoFirstNextGame) {
		gameLog("Game over");
		if(investigatorsWin) {
			gameLog("Big Investigators win");
			gameState.setWinner(CTRole.Investigator);
		}
		else {
			gameLog("Big Cultists win");
			gameState.setWinner(CTRole.Cultist);
		}
		awaitOKNextGame(toGoFirstNextGame);
	}
	
	private void setPlayerAwaitingInput(String player) {
		for(CTPlayer aPlayer : gameState.getPlayers()) {
			aPlayer.setAwaitingInput(false);
		}
		if(player != null) {
			gameState.getPlayerByName(player).setAwaitingInput(true);
		}
		
		sendGameState();
	}
	
	private void setAllPlayersAwaitingInput() {
		for(CTPlayer aPlayer : gameState.getPlayers()) {
			aPlayer.setAwaitingInput(true);
		}		
		sendGameState();
	}
	
	private void gameLog(String message) {
		String[] lines = message.split("\n");
		for(String line : lines) {
			server.sendMessageToAllUsers("message " + line);
		}
	}
	
	private void sendGameState() {
		for(CTPlayer player : gameState.getPlayers()) {
			CTGameState hiddenState = hideGameStateInfo(gameState, player.getName());
			server.sendMessageToUser(player.getName(), "gamestate " + hiddenState);
		}
	}
	
	private CTGameState hideGameStateInfo(CTGameState gameState, String viewerName) {
		CTGameState hiddenState = new CTGameState(gameState);
		
		ArrayList<CTPlayer> players = hiddenState.getPlayers();
		for(CTPlayer player : players) {
			if(player.getName().equals(viewerName) || gameState.getWinner() != null) {
				ArrayList<CTCard> cards = player.getCardSet().getCards();
				for(int i = 0; i < cards.size(); i++) {
					cards.get(i).setHidden(false);
				}
			}
			else {
				if(player.getName().equals(privateEyeRevealedPlayer) && viewerName.equals(privateEyeKnowledgeablePlayer)) {
					//leave face up
				}
				else {
					player.setRole(CTRole.Hidden);
				}
			}
		}
		return hiddenState;
	}
	
}