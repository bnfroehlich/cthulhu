package bfroehlich.cthulhu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class SKGameEngine implements GameEngine {

	private SKGameState gameState;
	private Server server;

//	private String awaitingCardPlayFrom;
//	private String awaitingPlayerClickFrom;
//	private String awaitingSelfClick;
//	private String awaitingDiscard;
//	private String awaitingHaveCardTaken;
//	private String awaitingTakeCard;
//
//	private String awaitingOkDiscardACard;
//	private String awaitingOkLoseRandomCard;
//	private String awaitingOkHaveCardTaken;
//	private String awaitingOkTakeCard;
//	private String awaitingOkGainPoint;

//	private HashMap<String, Boolean> oksNextGameReceived;
//	private String toGoFirstNextGame;
	
	private SKActionPendingInput pendingAction;

	public SKGameEngine(Server server) {
		this.server = server;
	}

	private SKGameState sample() {
		String[] names = {"Al", "Bob", "Carl", "Dan", "Ed", "Frank"};
		ArrayList<String> names2 = new ArrayList<String>(Arrays.asList(names));
		return getStartingGameState(names2);
	}

	private SKGameState getStartingGameState(ArrayList<String> playerNames) {
		ArrayList<SKPlayer> players = new ArrayList<SKPlayer>();
		ArrayList<SKColor> colors = new ArrayList<SKColor>(Arrays.asList(SKColor.values()));
		Collections.shuffle(colors);

		for(int i = 0; i < playerNames.size(); i++) {
			ArrayList<SKCard> cards = new ArrayList<SKCard>();
			cards.add(new SKCard(colors.get(i), SKCard.Value.Flower, false));
			cards.add(new SKCard(colors.get(i), SKCard.Value.Flower, false));
			cards.add(new SKCard(colors.get(i), SKCard.Value.Flower, false));
			cards.add(new SKCard(colors.get(i), SKCard.Value.Skull, false));
			SKPlayer player = new SKPlayer(playerNames.get(i), colors.get(i), cards);
			players.add(player);
		}

		ArrayList<SKGamePiece> discard = new ArrayList<SKGamePiece>();
//		for(int i = 0; i < 3; i++) {
//			discard.add(new SKCard(colors.get(i), SKCard.Value.Flower, false));
//			discard.add(new SKCard(colors.get(i), SKCard.Value.Flower, false));
//			discard.add(new SKCard(colors.get(i), SKCard.Value.Flower, false));
//			discard.add(new SKCard(colors.get(i), SKCard.Value.Skull, false));
//			discard.add(new SKPlayerTile(colors.get(i) + "-false"));
//		}

		return new SKGameState(discard, players, null);
	}

	public void newGame(ArrayList<String> playerNames, String firstPlayer) {
		gameState = getStartingGameState(playerNames);

		Random rand = new Random();
		ArrayList<SKPlayer> players = gameState.getPlayers();
		if(firstPlayer == null) {
			firstPlayer = players.get(rand.nextInt(players.size())).getName();
		}

		sendGameState();

		server.sendMessageToAllUsers("newgame");

		gameLog("New game");
		askForMove(firstPlayer, true);
	}
	
	public void handleUserInput(String playerSource, String input) {
		Thread handlerThread = new Thread(new Runnable() {
			
			public void run() {
				String[] pieces = input.split(",");
				String command = pieces[0];

				if(command.equals("chronobreak")) {
					String playerToStart = pieces[1];
					gameLog("Chronobreak, " + playerToStart + " to start");
					pendingAction = null;
					setPlayerAwaitingInput(null);
					clearBids();
					recallCards();
					sendGameState();
					askForMove(playerToStart, true);
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

	private void checkForHigherBids(String firstBidder, String highBidder, int bid) {
		String nextBidder = firstBidder;
		while(true) {
			String nextBidderBid = gameState.getPlayerByName(nextBidder).getBid();
			if(nextBidder.equals(highBidder)) {
				//all the way around the table; everyone else has passed; high bid stands
				highBidStands(highBidder, bid);
				break;
			}
			else if(nextBidderBid == null || !nextBidderBid.equals("-")) {
				//found a player who has not passed. They bid
				askForBid(nextBidder, bid + 1);
				break;
			}
			nextBidder = gameState.getNextPlayer(nextBidder);
		}
	}

	private void askForMove(String playerToMoveName, boolean roundStart) {		
		server.sendMessageToAllUsers("clearalerts");
		if(roundStart) {
			gameLog(playerToMoveName + " starts");
		}

		SKPlayer playerToMove = gameState.getPlayerByName(playerToMoveName);

		if(playerToMove.getInPlay().isEmpty()) {
			//must play a card
			setPlayerAwaitingInput(playerToMoveName);
			pendingAction = new SKActionPendingInput() {
				public void handleUserInput(String playerSource, String input) {
					String[] pieces = input.split(",");
					String command = pieces[0];

					if(command.equals("cardclicked")) {
						//assume it is a hand card
						//location is the hand owner's name
						String cardClickedLocation = pieces[1];
						if(playerSource.equals(playerToMoveName) && playerSource.equals(cardClickedLocation)) {
							//a legal card has been chosen to be played
							pendingAction = null;
							setPlayerAwaitingInput(null);

							int cardClickedNum = Integer.parseInt(pieces[2]);
							playCard(cardClickedLocation, cardClickedNum);
						}
					}
				}
			};
			server.sendMessageToUser(playerToMoveName, "playcard");
		}
		else if(playerToMove.getHand().isEmpty()) {
			//must bid
			setPlayerAwaitingInput(playerToMoveName);
			pendingAction = new SKActionPendingInput() {
				public void handleUserInput(String playerSource, String input) {
					String[] pieces = input.split(",");
					String command = pieces[0];
					if(command.equals("bid")) {
						pendingAction = null;
						setPlayerAwaitingInput(null);
						server.sendMessageToAllUsers("clearalerts");
						gameState.getPlayerByName(playerSource).setBid(pieces[1]);
						gameLog(playerSource + " bid " + pieces[1]);
						sendGameState();
		
						int bid = Integer.parseInt(pieces[1]);
						if(bid >= gameState.getTotalInPlay()) {
							//max bid
							highBidStands(playerSource, bid);
						}
						else {
							checkForHigherBids(gameState.getNextPlayer(playerSource), playerSource, bid);
						}
					}
				}
			};
			server.sendMessageToUser(playerToMoveName, "mustbid 1 " + gameState.getTotalInPlay());
		}
		else {
			//can playcard or bid
			setPlayerAwaitingInput(playerToMoveName);
			pendingAction = new SKActionPendingInput() {
				public void handleUserInput(String playerSource, String input) {
					String[] pieces = input.split(",");
					String command = pieces[0];
					if(command.equals("cardclicked")) {
						//assume it is a hand card
						//location is the hand owner's name
						String cardClickedLocation = pieces[1];
						if(playerSource.equals(playerToMoveName) && playerSource.equals(cardClickedLocation)) {
							//a legal card has been chosen to be played
							pendingAction = null;
							setPlayerAwaitingInput(null);

							int cardClickedNum = Integer.parseInt(pieces[2]);
							playCard(cardClickedLocation, cardClickedNum);
						}
					}
					else if(command.equals("bid")) {
						pendingAction = null;
						setPlayerAwaitingInput(null);
						bidReceived(playerSource, pieces[1]);
					}
				}
			};
			server.sendMessageToUser(playerToMoveName, "playcardorbid 1 " + gameState.getTotalInPlay());
		}

	}
	
	private void playCard(String location, int num) {

		SKCard cardClicked = gameState.getPlayerByName(location).getHand().remove(num);
		cardClicked.setHidden(true);
		gameState.getPlayerByName(location).getInPlay().add(cardClicked);

		sendGameState();

		askForMove(gameState.getNextPlayer(location), false);
	}
	
	private void bidReceived(String bidderName, String bidString) {
		server.sendMessageToAllUsers("clearalerts");
		gameState.getPlayerByName(bidderName).setBid(bidString);
		gameLog(bidderName + " bid " + bidString);
		sendGameState();

		int bid = Integer.parseInt(bidString);
		if(bid >= gameState.getTotalInPlay()) {
			//max bid
			highBidStands(bidderName, bid);
		}
		else {
			checkForHigherBids(gameState.getNextPlayer(bidderName), bidderName, bid);
		}
	}

	private void askForBid(String playerName, int min) {
		server.sendMessageToAllUsers("clearalerts");
		//can bid or pass
		setPlayerAwaitingInput(playerName);
		pendingAction = new SKActionPendingInput() {
			public void handleUserInput(String playerSource, String input) {
				String[] pieces = input.split(",");
				String command = pieces[0];
				if(command.equals("bid")) {
					pendingAction = null;
					setPlayerAwaitingInput(null);
					bidReceived(playerSource, pieces[1]);
				}
				else if(command.equals("pass")) {
					gameState.getPlayerByName(playerSource).setBid("-");
					gameLog(playerSource + " passed");
					sendGameState();

					checkForHigherBids(gameState.getNextPlayer(playerSource), gameState.getHighBidder(), gameState.getHighBid());
				}
			}
		};
		server.sendMessageToUser(playerName, "bid " + min + " " + gameState.getTotalInPlay());
	}

	private void highBidStands(String highBidder, int num) {
		server.sendMessageToAllUsers("clearalerts");
		alert(highBidder + "'s " + num + " bid stands");
		gameLog(highBidder + "'s " + num + " bid stands");

		setPlayerAwaitingInput(highBidder);
		pendingAction = new SKActionPendingInput() {			
			public void handleUserInput(String playerSource, String input) {
				String[] pieces = input.split(",");
				String command = pieces[0];
				if(command.equals("playerclicked")) {
					String playerClicked = pieces[1];
					if(playerSource.equals(highBidder) && playerSource.equals(playerClicked)) {
						//must click on myself to reveal my own hand after my high bid stands
						pendingAction = null;
						setPlayerAwaitingInput(null);
						
						SKPlayer player = gameState.getPlayerByName(playerClicked);
						ArrayList<SKCard> playerInPlay = player.getInPlay();
						int bid = gameState.getHighBid();
						boolean revealedSkull = false;
						for(int i = 0; i < playerInPlay.size() && i < bid; i++) {
							//reveal cards from the top down until we satisfy the bid or find a skull
							SKCard card = playerInPlay.get(playerInPlay.size() - 1 - i);
							card.setHidden(false);
							if(card.getValue() == SKCard.Value.Skull) {
								revealedSkull = true;
								break;
							}
						}
						sendGameState();
						if(revealedSkull) {
							pendingAction = null; //done searching for flowers
							setPlayerAwaitingInput(null);
							foundSkull(player.getName(), player.getName());
						}
						else if(gameState.getFlowersRevealed() >= bid) {
							pendingAction = null; //done searching for flowers
							setPlayerAwaitingInput(null);
							foundEnoughFlowers(player.getName(), gameState.getFlowersRevealed());
						}
						else {
							askToFindFlowers(player.getName(), bid);
						}
					}
				}
			}
		};
		server.sendMessageToUser(highBidder, "awaitrevealinplay " + num);
	}

	private void askToFindFlowers(String flowerFinder, int num) {
		server.sendMessageToAllUsers("clearalerts");
		setPlayerAwaitingInput(flowerFinder);
		pendingAction = new SKActionPendingInput() {
			public void handleUserInput(String playerSource, String input) {
				String[] pieces = input.split(",");
				String command = pieces[0];
				if(command.equals("playerclicked")) {
					String playerClicked = pieces[1];
					if(playerSource.equals(flowerFinder) && !playerSource.equals(playerClicked)) {
						//a legal player has been selected
						ArrayList<SKCard> inPlay = gameState.getPlayerByName(playerClicked).getInPlay();
						
						for(int i = inPlay.size()-1; i >= 0; i--) {
							//reveal the next hidden card, moving from the top down
							SKCard next = inPlay.get(i);
							if(next.isHidden()) {
								next.setHidden(false);
								sendGameState();
								if(next.getValue() == SKCard.Value.Skull) {
									pendingAction = null; //done searching for flowers
									setPlayerAwaitingInput(null);
									foundSkull(flowerFinder, playerClicked);
								}
								else {
									if(gameState.getFlowersRevealed() >= gameState.getHighBid()) {
										pendingAction = null; //done searching for flowers
										setPlayerAwaitingInput(null);
										foundEnoughFlowers(flowerFinder, gameState.getFlowersRevealed());
									}
									else {
										//await more flower finding
									}
								}
								break;
							}
						}

						sendGameState();
					}
				}
			}
		};
		server.sendMessageToUser(flowerFinder, "findflowers " + num);
	}

	private void askToDiscardCard(String playerToDiscard) {
		gameLog(playerToDiscard + " discards a card");
		server.sendMessageToAllUsers("clearalerts");
		
		//assume all cards have been recalled to hand
		if(gameState.getPlayerByName(playerToDiscard).getHand().size() == 1) {
			//if there is only 1 card, auto-choose to discard it
			discardCard(playerToDiscard, 0);
		}
		else {
			setPlayerAwaitingInput(playerToDiscard);
			pendingAction = new SKActionPendingInput() {
				public void handleUserInput(String playerSource, String input) {
					String[] pieces = input.split(",");
					String command = pieces[0];
					if(command.equals("cardclicked")) {
						String cardClickedLocation = pieces[1];
						if(playerSource.equals(playerToDiscard) && cardClickedLocation.equals(playerSource)) {
							//a legal card has been chosen to be discarded
							pendingAction = null;
							setPlayerAwaitingInput(null);
		
							int cardClickedNum = Integer.parseInt(pieces[2]);
							discardCard(cardClickedLocation, cardClickedNum);
						}
					}
				}
			};
			server.sendMessageToUser(playerToDiscard, "discard");
		}
	}

	private void askToTakeCard(String playerToLose, String playerToTake) {
		gameLog(playerToTake + " takes a card from " + playerToLose);
		server.sendMessageToAllUsers("clearalerts");
		
		//assume all cards have been recalled to hand
		if(gameState.getPlayerByName(playerToLose).getHand().size() == 1) {
			//if there is only 1 card, auto-choose to discard it
			discardCard(playerToLose, 0);
		}
		else {
			setPlayerAwaitingInput(playerToTake);
			pendingAction = new SKActionPendingInput() {
				public void handleUserInput(String playerSource, String input) {
					String[] pieces = input.split(",");
					String command = pieces[0];
					if(command.equals("cardclicked")) {
						String cardClickedLocation = pieces[1];
						if(playerSource.equals(playerToTake) && cardClickedLocation.equals(playerToLose)) {
							//a legal card has been chosen to be taken
							pendingAction = null;
							setPlayerAwaitingInput(null);
	
							int cardClickedNum = Integer.parseInt(pieces[2]);
							discardCard(cardClickedLocation, cardClickedNum);
						}
					}
				}
			};
			server.sendMessageToUser(playerToTake, "takecard " + playerToLose);
		}
	}

	private void loseRandomCard(String player) {
		gameLog(player + " loses a random card");
		
		recallCards();
		clearBids();
		Random rand = new Random();
		ArrayList<SKCard> hand = gameState.getPlayerByName(player).getHand();
		SKCard card = hand.remove(rand.nextInt(hand.size()));

		card.setHidden(true);
		gameState.getDiscard().add(card);
		sendGameState();

		proceedAfterLostCard(player);
	}
	
	private void discardCard(String location, int num) {
		SKCard card = gameState.getPlayerByName(location).getHand().remove(num);
		card.setHidden(true);
		gameState.getDiscard().add(card);

		sendGameState();

		proceedAfterLostCard(location);
	}

	private void proceedAfterLostCard(String player) {
		if(gameState.playersAlive().size() == 1) {
			//if game over, stop
			gameLog(player + " died");
			gameOver(gameState.playersAlive().get(0).getName());
		}
		else if(gameState.getPlayerByName(player).getHand().size() > 0) {
			//if that player still alive, it's their turn
			askForMove(player, true);
		}
		else {
			//proceed to next player
			gameLog(player + " died");
			gameState.getDiscard().add(new SKPlayerTile(gameState.getPlayerByName(player)));
			sendGameState();
			askForMove(gameState.getNextPlayer(player), true);
		}
	}

	private void gameOver(String winner) {
		alert("Big " + winner + " wins");
		gameLog("Game over");
		gameLog("Big " + winner + " wins");

		gameState.setWinner(winner);
		awaitOKNextGame(winner);
		server.sendMessageToAllUsers("awaitreadynext");
	}
	
	private void awaitOKNextGame(String toGoFirstNextGame) {
		HashMap<String, Boolean> oksNextGameReceived = new HashMap<String, Boolean>();
		for(SKPlayer player : gameState.getPlayers()) {
			oksNextGameReceived.put(player.getName(), false);
		}

		setAllPlayersAwaitingInput();
		pendingAction = new SKActionPendingInput() {			
			public void handleUserInput(String playerSource, String input) {
				String[] pieces = input.split(",");
				String command = pieces[0];
				if(command.equals("readynext")) {
					oksNextGameReceived.put(playerSource, true);
					gameState.getPlayerByName(playerSource).setAwaitingInput(false);
					boolean allReady = true;
					for(SKPlayer player : gameState.getPlayers()) {
						if(!oksNextGameReceived.get(player.getName())) {
							allReady = false;
							break;
						}
					}
					if(allReady) {
						pendingAction = null;
						newGame(gameState.getPlayerNames(), toGoFirstNextGame);
					}
				}
			}
		};
		
		server.sendMessageToAllUsers("awaitreadynext");
	}

	private void clearBids() {
		for(SKPlayer player : gameState.getPlayers()) {
			player.setBid(null);
		}
		sendGameState();
	}

	private void foundSkull(String skullFinderName, String skullOwnerName) {
		if(gameState.getPlayerByName(skullFinderName).getHand().size() + gameState.getPlayerByName(skullFinderName).getInPlay().size() <= 1) {
			alert(skullFinderName + " died");
		}
		else {
			alert(skullFinderName + " found " + skullOwnerName + "'s skull");
		}
		gameLog(skullFinderName + " found " + skullOwnerName + "'s skull");
		SKPlayer skullFinderPlayer = gameState.getPlayerByName(skullFinderName);
		if(skullFinderPlayer.getHand().size() + skullFinderPlayer.getInPlay().size() == 1 && gameState.playersAlive().size() == 2) {
			//don't reset the board if game is over
			String winner = null;
			for(SKPlayer alivePlayer : gameState.playersAlive()) {
				if(!alivePlayer.getName().equals(skullFinderName)) {
					winner = alivePlayer.getName();
					break;
				}
			}
			gameLog(skullFinderName + " died");
			gameOver(winner);
		}
		else {
			try {
				Thread.sleep(2000);
			}
			catch(InterruptedException ie) {}
			if(skullFinderName.equals(skullOwnerName)) {
				recallCards();
				clearBids();
				askToDiscardCard(skullFinderName);
			}
			else {
				recallCards();
				clearBids();
				askToTakeCard(skullFinderName, skullOwnerName);
			}
		}
	}

	private void foundEnoughFlowers(String playerName, int flowers) {
		alert(playerName + " succeeded");
		String msg = playerName + " found " + flowers + " flower";
		if(gameState.getFlowersRevealed() > 1) { 
			msg += "s";
		}
		gameLog(msg);
		gameLog(playerName + " scores a point");
		
		SKPlayer player = gameState.getPlayerByName(playerName);
		player.setScore(player.getScore() + 1);
		if(player.getScore() >= 2) {
			gameOver(playerName);
		}
		else {
			try {
				Thread.sleep(2000);
			}
			catch(InterruptedException ie) {}
			clearBids();
			recallCards();
			sendGameState();
			askForMove(playerName, true);
		}
	}

//	private void awaitOkDiscardCard(String playerName) {
//
//		SKPlayer player = gameState.getPlayerByName(playerName);
//		if(player.getHand().size() + player.getInPlay().size() == 1 && gameState.playersAlive().size() == 2) {
//			//skip asking for OK if game is over
//			String winner = null;
//			for(SKPlayer alivePlayer : gameState.playersAlive()) {
//				if(!alivePlayer.getName().equals(playerName)) {
//					winner = alivePlayer.getName();
//					break;
//				}
//			}
//			gameOver(winner);
//		}
//		else {
//			awaitingOkDiscardACard = playerName;
//			server.sendMessageToUser(playerName, "awaitok");
//		}
//	}
//
//	private void awaitOkLoseRandomCard(String playerName) {
//		gameLog(playerName + " loses a random card");
//
//		SKPlayer player = gameState.getPlayerByName(playerName);
//		if(player.getHand().size() + player.getInPlay().size() == 1 && gameState.playersAlive().size() == 2) {
//			//skip asking for OK if game is over
//			String winner = null;
//			for(SKPlayer alivePlayer : gameState.playersAlive()) {
//				if(!alivePlayer.getName().equals(playerName)) {
//					winner = alivePlayer.getName();
//					break;
//				}
//			}
//			gameOver(winner);
//		}
//		else {
//			awaitingOkLoseRandomCard = playerName;
//			server.sendMessageToUser(playerName, "awaitok");
//		}
//	}
//
//	private void awaitOkTakeCard(String playerToLose, String playerToTake) {
//
//		SKPlayer player = gameState.getPlayerByName(playerToLose);
//		if(player.getHand().size() + player.getInPlay().size() == 1 && gameState.playersAlive().size() == 2) {
//			//skip asking for OK if game is over
//			String winner = null;
//			for(SKPlayer alivePlayer : gameState.playersAlive()) {
//				if(!alivePlayer.getName().equals(playerToLose)) {
//					winner = alivePlayer.getName();
//					break;
//				}
//			}
//			gameOver(winner);
//		}
//		else {
//			awaitingOkHaveCardTaken = playerToLose;
//			awaitingOkTakeCard = playerToTake;
//			server.sendMessageToUser(playerToLose, "awaitok");
//		}
//	}
//
//	private void awaitOkGainPoint(String playerName) {
//		gameLog(playerName + " scores a point");
//
//		SKPlayer player = gameState.getPlayerByName(playerName);
//		if(player.getScore() >= 1) {
//			//skip asking for OK if game is over
//			player.setScore(player.getScore() + 1);
//			gameOver(playerName);
//		}
//		else {
//			awaitingOkGainPoint = playerName;
//			server.sendMessageToUser(playerName, "awaitok");
//		}
//	}

	private void recallCards() {
		for(SKPlayer player : gameState.getPlayers()) {
			player.getHand().addAll(player.getInPlay());
			player.getInPlay().clear();
		}
	}
	
	private void setPlayerAwaitingInput(String player) {
		for(SKPlayer aPlayer : gameState.getPlayers()) {
			aPlayer.setAwaitingInput(false);
		}
		if(player != null) {
			gameState.getPlayerByName(player).setAwaitingInput(true);
		}
		
		sendGameState();
	}
	
	private void setAllPlayersAwaitingInput() {
		for(SKPlayer aPlayer : gameState.getPlayers()) {
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

	private void alert(String message) {
		String[] lines = message.split("\n");
		for(String line : lines) {
			server.sendMessageToAllUsers("alert " + line);
		}
	}

	private void sendGameState() {
		for(SKPlayer player : gameState.getPlayers()) {
			SKGameState hiddenState = hideGameStateInfo(gameState, player.getName());
			server.sendMessageToUser(player.getName(), "gamestate " + hiddenState);
		}
	}

	private SKGameState hideGameStateInfo(SKGameState gameState, String viewerName) {
		SKGameState hiddenState = new SKGameState(gameState);
		for(SKPlayer player : hiddenState.getPlayers()) {
			for(SKCard card : player.getHand()) {
				card.setHidden(!player.getName().equals(viewerName));
			}
		}
		return hiddenState;
	}
}
