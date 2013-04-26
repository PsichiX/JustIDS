package com.PsichiX.JustIDS.game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.PsichiX.JustIDS.message.PlayerInformation;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.PsichiX.JustIDS.comm.MockBroadCastManager;
import com.PsichiX.JustIDS.game.GameStateMachine.GameStateChangeListener;
import com.PsichiX.JustIDS.game.GameStateMachine.GameStateNotificationEnum;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerState;

public class GameTest extends TestCase {
    // Here we are using java logger not the android logger on purpose...
    // We want to test the game manager logic outside of android
	private static Logger logger = Logger.getLogger(GameTest.class.getName());
	
	private static final int TIME_UNIT = 50;

	private class MockListener implements GameStateChangeListener {

		private int number;
        private boolean hitSeen;

        public MockListener(int number) {
			this.number = number;
		}
		
		private List<GameStateNotificationEnum> notifications = new LinkedList<GameStateNotificationEnum>();
		@Override
		public void notifyStateChange(
				GameStateNotificationEnum gameStateNotification) {
			notifications.add(gameStateNotification);
			logger.fine("Listener:" + number + ":Received notification " + gameStateNotification);
		}

        @Override
        public void notifyHitSeen(Player attacking) {
            this.hitSeen = true;
            logger.info("Hit Seen. Attacking:" + attacking);
        }

        @Override
		public String toString() {
			return Arrays.toString(notifications.toArray()) + ", hit seen: " + this.hitSeen;
		}
	}

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		GameManager.overrideUnitTestMilliseconds(TIME_UNIT);
	}

	private static class PlayerIdComparator implements Comparator<Player> {
		@Override
		public int compare(Player lhs, Player rhs) {
			return lhs.getId().compareTo(rhs.getId());
		}
		
	}
 	private void expectedPlayerLifeEquals(GameManager gm, double expectedPoints[]) {
		Player players[] = gm.getPlayers();
		SortedSet<Player> sortedPlayers = new TreeSet<Player>(new PlayerIdComparator());
		for (Player player: players) {
			sortedPlayers.add(player);
		}
		double points[] = new double[sortedPlayers.size()];
		int index=0;
		for (Player player: sortedPlayers) {
			points[index++] = player.getLifePoints();
		}
		for(int i=0; i<points.length; i++) {
			String message = "Array element: "+ i + " differ. Actual: " + Arrays.toString(points) + 
					", expected:" + Arrays.toString(expectedPoints);
			assertEquals(message, expectedPoints[i], points[i],0.01);
		}
	}

    private GameManager [] createAndStartGameManagers(int numberOfGameManagers){
        GameManager gameManagers[] = new GameManager[numberOfGameManagers];
        for (int i=0; i< numberOfGameManagers; i++) {
            gameManagers[i] = new GameManager(new MockBroadCastManager(), "id" + i, "Name" + i, new MockListener(i));
            gameManagers[i].startGameManager();
        }
        return gameManagers;
    }

    private void stopGameManagers(GameManager[] gameManagers) {
        for (int i=0; i<gameManagers.length; i++) {
            gameManagers[i].stopGameManager();
        }
    }

    private void printGameManagers(GameManager[] gameManagers) {
        for (int i=0; i<gameManagers.length; i++) {
            logger.info("GM" + i + ":" + gameManagers[i].getGameStateMachine().getNotificationStateListener().toString());
        }
    }

	@Test
	public void testSimpleGameWithTwoPeopleOneWinning() throws Exception {
        GameManager[] gms = createAndStartGameManagers(2);

		assertEquals(PlayerState.WAITING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);
        gms[0].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
        gms[1].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gms[1].getGameStateMachine().getMyState());
        gms[0].attackWithStrength(100.0);
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gms[1].getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);

        printGameManagers(gms);
        stopGameManagers(gms);
	}

	@Test
	public void testSimpleGameWithThreePeopleOneWinning() throws Exception {
        GameManager[] gms = createAndStartGameManagers(3);

		assertEquals(PlayerState.WAITING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[2].getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);
		gms[0].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[2].getGameStateMachine().getMyState());
		gms[1].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[2].getGameStateMachine().getMyState());
		gms[0].attackWithStrength(100.0);
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gms[2].getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);

        printGameManagers(gms);
        stopGameManagers(gms);
	}

	@Test
	public void testSimpleGameWithFourPeopleOneWinning() throws Exception {
        GameManager[] gms = createAndStartGameManagers(4);

		assertEquals(PlayerState.WAITING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[3].getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);
		gms[0].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[3].getGameStateMachine().getMyState());
		gms[1].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[3].getGameStateMachine().getMyState());
		gms[0].attackWithStrength(100.0);
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gms[3].getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);

        printGameManagers(gms);
        stopGameManagers(gms);
	}

	@Test
	public void testSimpleGameWithFourPeoplePartialAttacks() throws Exception {
        GameManager[] gms = createAndStartGameManagers(4);

		double expectedPoints[] = {100.0,100.0,100.0,100.0};
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WAITING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[3].getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gms[0], expectedPoints);
		expectedPlayerLifeEquals(gms[1], expectedPoints);
		expectedPlayerLifeEquals(gms[2], expectedPoints);
		expectedPlayerLifeEquals(gms[3], expectedPoints);
		gms[0].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gms[3].getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gms[0], expectedPoints);
		expectedPlayerLifeEquals(gms[1], expectedPoints);
		expectedPlayerLifeEquals(gms[2], expectedPoints);
		expectedPlayerLifeEquals(gms[3], expectedPoints);
		gms[1].joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[3].getGameStateMachine().getMyState());
		gms[0].attackWithStrength(30.0);
		expectedPoints[1] = 70.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[3].getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gms[0], expectedPoints);
		expectedPlayerLifeEquals(gms[1], expectedPoints);
		expectedPlayerLifeEquals(gms[2], expectedPoints);
		expectedPlayerLifeEquals(gms[3], expectedPoints);
		gms[0].attackWithStrength(60.0);
		expectedPoints[1] = 10.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[3].getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gms[0], expectedPoints);
		expectedPlayerLifeEquals(gms[1], expectedPoints);
		expectedPlayerLifeEquals(gms[2], expectedPoints);
		expectedPlayerLifeEquals(gms[3], expectedPoints);
		gms[1].attackWithStrength(50.0);
		expectedPoints[0] = 50.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gms[3].getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gms[0], expectedPoints);
		expectedPlayerLifeEquals(gms[1], expectedPoints);
		expectedPlayerLifeEquals(gms[2], expectedPoints);
		expectedPlayerLifeEquals(gms[3], expectedPoints);
		gms[0].attackWithStrength(30.0);
		expectedPoints[1] = 0.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gms[0].getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gms[1].getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gms[2].getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gms[3].getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gms[0], expectedPoints);
		expectedPlayerLifeEquals(gms[1], expectedPoints);
		expectedPlayerLifeEquals(gms[2], expectedPoints);
		expectedPlayerLifeEquals(gms[3], expectedPoints);
		Thread.sleep(4*TIME_UNIT);
		expectedPlayerLifeEquals(gms[0], expectedPoints);
		expectedPlayerLifeEquals(gms[1], expectedPoints);
		expectedPlayerLifeEquals(gms[2], expectedPoints);
		expectedPlayerLifeEquals(gms[3], expectedPoints);

        printGameManagers(gms);
        stopGameManagers(gms);
	}
	
	
}
