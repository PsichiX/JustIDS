package com.PsichiX.JustIDS.game;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

import com.PsichiX.JustIDS.comm.BroadcastManagerInterface;
import com.PsichiX.JustIDS.display.PrintCurrentState;
import com.PsichiX.JustIDS.game.GameStateMachine.GameStateChangeListener;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo.BroadcastType;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerState;

/**
 * The Game Manager is responsible for gluing together various components inside game processing engine.
 * It is the main object, the remaining parts of the game (UI should interact with). It provides methods to 
 * start the game, pause and resume as well as joining the game and attacking the opponent.
 * 
 * It uses GameStateMachine object to get information about the state the game is in as well as broadcast manager 
 * which is responsible for communication.
 * 
 * @author potiuk
 *
 */
public class GameManager {

    private final GameStateChangeListener listener;
    // Here we are using java logger not the android logger on purpose...
    // We want to test the game manager logic outside of android
	Logger logger = Logger.getLogger(GameManager.class.getName());

	private static final double MIN_LIFE_POINTS = 0.01;
	static int UNIT_OF_TIME_MILLIS = 100;

	private HashMap<String, Player> players = new HashMap<String, Player>();
	
	private volatile double lifePointsOfMine;
	BroadcastManagerInterface bcm;
	private GameStateMachine gameStateMachine;
	
	private Thread readingThread;
	private PingThread pingThread;

	private String androidId;
	private String name;
	boolean paused;

    public GameManager(BroadcastManagerInterface broadcastManager, String androidId,
                       String name, GameStateChangeListener listener) {
		this.bcm = broadcastManager;
		this.androidId = androidId;
		this.gameStateMachine = new GameStateMachine(androidId, listener);
        this.name = name;
        this.listener = listener;
	}

    public void startGameManager() {
        resetGame();
        resume();
    }

	/**
	 * Determines whether the broadcast message received is one that we are interested in.
	 * 
	 * @param pbi broadcast message
	 * @return true if this is something we should handle
	 */
	public boolean shouldICareAboutThisBroadcast(PlayerBroadcastInfo pbi) {
		if (hasFinished()) {
			logger.fine(name + ":Skipping message (I finished already): " + pbi);
			return false;
		}
		boolean mine = pbi.getMyPlayer().getId().equals(androidId);
		if (mine) {
			logger.fine(name + ":Skipping message (It's message from self): " + pbi);
		}
		return !mine;
	}

	/**
	 * Handler for receiving a message. That message is already filtered - so we only see messages that 
	 * should be handled by us.
	 * 
	 * @param pbi
	 */
	synchronized boolean receiveMessage(PlayerBroadcastInfo pbi) {
        boolean somethingHasChanged = false;
        for (Player newPlayer: pbi.getAllPlayersList()) {
            String playerId = newPlayer.getId();
            if (playerId.equals(getMyPlayer().getId())) {
                //skip myself
                continue;
            }
            Player oldPlayer = players.get(playerId);
            if (oldPlayer != null) {
                byte oldPlayerAsByteArray[] = oldPlayer.toByteArray();
                byte newPlayerAsByteArray[] = newPlayer.toByteArray();
                if (!Arrays.equals(oldPlayerAsByteArray, newPlayerAsByteArray)) {
                    somethingHasChanged = true;
                    players.put(playerId, newPlayer);
                }
            } else {
                somethingHasChanged = true;
                players.put(playerId, newPlayer);
            }
        }
		if (pbi.getType() == BroadcastType.STATE) {
			gameStateMachine.startGameIfAllReady(players.values());
		} else if (pbi.getType() == BroadcastType.ATTACK) {
            if (isAttackSuccessfull(pbi)) {
                logger.info("Attack successful " + pbi + " to " + getMyPlayer());
                decreaseLifePointsOfMine(pbi.getAttackStrength());
                gameStateMachine.hit();
                gameStateMachine.successfulAttack();
    		} else {
                logger.info("Attack observed " + pbi + " from " + pbi.getMyPlayer());
                Player attacking = pbi.getMyPlayer();
                listener.notifyHitSeen(attacking);
            }
        }
        return somethingHasChanged;
	}

	/**
	 * Sleep for time (in milliseconds) specified.
	 * 
	 * @param time time to sleep
	 */
	void goToSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			logger.warning(name + ": " + e.getMessage());
		}
	}
	
	/**
	 * Return true if I lost the game.
	 * 
	 * @return result
	 */
	synchronized boolean didILoose() {
		return gameStateMachine.amIInGame()	&& lifePointsOfMine <= MIN_LIFE_POINTS;
	}

	/**
	 * Return true if I won the game.
	 * 
	 * @return result
	 */
	public synchronized boolean didIWin() {
		if (gameStateMachine.amIInGame()) {
			if (players.keySet().size() == 0) {
				return false;
			}
			for (Player player : players.values()) {
				if (player.getState() == PlayerState.LOST && !gameStateMachine.isMyself(player)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	/**
	 * Checks if game is finished.
	 * 
	 * @return result of the check
	 */
	public synchronized boolean isGameFinished() {
		if (players.keySet().size() == 0) {
			return false;
		}
		for (Player player : getAllPlayersInfo()) {
			if (player.getState() == PlayerState.WON || player.getState() == PlayerState.LOST) {
				return true;
			}
		}
		return false;
	}
	
	public Player getMyPlayer() {
		return Player.newBuilder().setId(androidId).setName(name)
				.setLifePoints(lifePointsOfMine).setState(gameStateMachine.getMyState())
				.build();
	}
	
	private Player copyPlayerIdAndSetState(Player originalPlayerId, PlayerState playerState) {
		return Player.newBuilder().setId(originalPlayerId.getId()).setName(originalPlayerId.getName())
				.setLifePoints(originalPlayerId.getLifePoints()).setState(playerState)
                .build();
	}
	
	private boolean isAttackSuccessfull(PlayerBroadcastInfo pbi) {
		boolean res = gameStateMachine.amIInGame() && pbi.getAttackStrength() > 0.01;
		logger.fine("Checked if attack is successful:" + pbi + ":" + res);
		return res;
	}

	private synchronized double decreaseLifePointsInternally(double decreaseBy) {
		lifePointsOfMine -= decreaseBy;
		if (lifePointsOfMine < 0.01) {
			lifePointsOfMine = 0.0;
		}
		return lifePointsOfMine;
	}

	public void decreaseLifePointsOfMine(double decreaseBy) {
		decreaseLifePointsInternally(decreaseBy);
        sendMyState();
	}

	public synchronized void sendMyState() {
		Player newPlayer = getMyPlayer();
        String playerId = newPlayer.getId();
        Player oldPlayer = players.get(playerId);
        boolean somethingHasChanged = false;
        if (oldPlayer != null) {
            if (!Arrays.equals(oldPlayer.toByteArray(),newPlayer.toByteArray())){
                somethingHasChanged = true;
                players.put(playerId, newPlayer);
            }
        } else {
            somethingHasChanged = true;
            players.put(playerId, newPlayer);
        }
		PlayerBroadcastInfo info = PlayerBroadcastInfo.newBuilder().
				setMyPlayer(newPlayer).
				setType(BroadcastType.STATE).
				addAllAllPlayers(getAllPlayersInfo()).build();
		this.bcm.sendBroadcast(info);
        if (somethingHasChanged) {
            logger.info("Sending state change:" +
                    PrintCurrentState.getCurrentStateAsString(getMyPlayer(), getPlayers()));
            gameStateMachine.runSomethingChangedListener();
        }
	}

	public boolean isMyself(Player player) {
		return player.getId().equals(androidId);
	}

	private Collection<Player> getAllPlayersInfo() {
		Collection<Player> playerValues =  players.values();
		if (gameStateMachine.amIInGame()) {
			Collection<Player> newList = new LinkedList<Player>();
			for (Player player : playerValues) {
				if (GameStateMachine.isInGame(player.getState()) && !isMyself(player)) {
					if (didILoose() ) {
						newList.add(copyPlayerIdAndSetState(player, PlayerState.WON));
					} else if (didIWin()){
						newList.add(copyPlayerIdAndSetState(player, PlayerState.LOST));						
					} else {
						newList.add(player);
					}
				} else {
					newList.add(player);
				}
			}
			return newList;
		} else {
			return playerValues;
		}
	}

	public boolean hasFinished() {
		return (didILoose() || didIWin());
	}

	public void attackWithStrength(double strength) {
		if (hasFinished()) {
			// do nothing
			return;
		}
		PlayerBroadcastInfo info = PlayerBroadcastInfo.newBuilder()
				.setMyPlayer(getMyPlayer()).setAttackStrength(strength)
				.setType(BroadcastType.ATTACK).build();
		this.bcm.sendBroadcast(info);
	}

	/**
	 * Resets the game. It should be executed after the game is in "finished" state.
	 */
	public synchronized void resetGame() {
		this.lifePointsOfMine = 100.0;
		this.players.clear();
		this.gameStateMachine.resetStateMachine();
	}

	/**
	 * Joins the game. In case the player is not in "Waiting" state It will
	 * return false and will not start the game.
	 * 
	 * @return
	 */
	public synchronized boolean joinGame() {
		boolean res = gameStateMachine.joinGame();
		gameStateMachine.startGameIfAllReady(players.values());
        sendMyState();
		return res;
	}	
	
	/**
	 * Pause the game. It should be called when application's activity is paused.
	 */
	public synchronized void pause() {
		paused = true;
		this.readingThread = null;
		this.pingThread = null;
	}

	/**
	 * Resumes the game. It should be called when application's activity is resumed.
	 */
	public synchronized void resume() {
		paused = false;
		readingThread = new ReadingThread(this, gameStateMachine);
		readingThread.start();
		pingThread = new PingThread(this, gameStateMachine);
		pingThread.start();
	}


	/**
	 * Destroys the object. Should be called whenever we stop needing the game manager.
	 * 
	 */
	public void stopGameManager() {
		pause();
		bcm.destroy();
	}

	// Getters
	public GameStateMachine getGameStateMachine() {
		return gameStateMachine;
	}
	
	public String getName() {
		return name;
	}

	public synchronized Player[] getPlayers() {
        Collection<Player> values = players.values();
        return values.toArray(new Player[values.size()]);
	}
	
	/**
	 * For tests only. allows to decrease the time of tests
	 * 
	 * @param unit
	 */
    static void overrideUnitTestMilliseconds(int unit) {
		UNIT_OF_TIME_MILLIS = unit;
	}

}
