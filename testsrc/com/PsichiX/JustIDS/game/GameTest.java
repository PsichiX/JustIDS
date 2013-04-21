package com.PsichiX.JustIDS.game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.PsichiX.JustIDS.comm.MockBroadCastManager;
import com.PsichiX.JustIDS.game.GameStateMachine.GameStateChangeListener;
import com.PsichiX.JustIDS.game.GameStateMachine.GameStateNotificationEnum;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerId;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerState;

public class GameTest extends TestCase {
	private static Logger logger = Logger.getLogger(GameTest.class.getName());
	
	private static final int TIME_UNIT = 10;

	private class MockListener implements GameStateChangeListener {

		private int number;
		public MockListener(int number) {
			this.number = number;
		}
		
		private List<GameStateNotificationEnum> notifications = new LinkedList<GameStateNotificationEnum>();
		@Override
		public void notifyStateChange(
				GameStateNotificationEnum gameStateNotification) {
			notifications.add(gameStateNotification);
			logger.info("Listener:" + number + ":Received notification " + gameStateNotification);
		}
		
		@Override
		public String toString() {
			return Arrays.toString(notifications.toArray());
		}
	}

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		GameManager.overrideUnitTestMilliseconds(TIME_UNIT);
	}

	private static class PlayerIdComparator implements Comparator<PlayerId> {
		@Override
		public int compare(PlayerId lhs, PlayerId rhs) {
			return lhs.getId().compareTo(rhs.getId());
		}
		
	}
 	private void expectedPlayerLifeEquals(GameManager gm, double expectedPoints[]) {
		PlayerId players[] = gm.getPlayers();
		SortedSet<PlayerId> sortedPlayers = new TreeSet<PlayerId>(new PlayerIdComparator());
		for (PlayerId player: players) {
			sortedPlayers.add(player);
		}
		double points[] = new double[sortedPlayers.size()];
		int index=0;
		for (PlayerId player: sortedPlayers) {
			points[index++] = player.getLifePoints();
		}
		for(int i=0; i<points.length; i++) {
			String message = "Array element: "+ i + " differ. Actual: " + Arrays.toString(points) + 
					", expected:" + Arrays.toString(expectedPoints);
			assertEquals(message, expectedPoints[i], points[i],0.01);
		}
	}
	
	@Test
	public void testSimpleGameWithTwoPeopleOneWinning() throws Exception {
		GameManager gm1 = new GameManager(new MockBroadCastManager(), "id1", new MockListener(1));
        gm1.readyToPlay("Name 1");
		GameManager gm2 = new GameManager(new MockBroadCastManager(), "id2", new MockListener(2));
        gm2.readyToPlay("Name 2");
		assertEquals(PlayerState.WAITING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);
		gm1.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		gm2.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gm2.getGameStateMachine().getMyState());
		gm1.attackWithStrength(100.0);
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gm2.getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);

		logger.info("GM1:" + gm1.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM2:" + gm2.getGameStateMachine().getNotificationStateListener().toString());
		gm1.destroy();
		gm2.destroy();
	}

	@Test
	public void testSimpleGameWithThreePeopleOneWinning() throws Exception {
		GameManager gm1 = new GameManager(new MockBroadCastManager(), "id1", new MockListener(1));
        gm1.readyToPlay("Name 1");
		GameManager gm2 = new GameManager(new MockBroadCastManager(), "id2", new MockListener(2));
        gm2.readyToPlay("Name 2");
		GameManager gm3 = new GameManager(new MockBroadCastManager(), "id3", new MockListener(3));
        gm3.readyToPlay("Name 3");
		assertEquals(PlayerState.WAITING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm3.getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);
		gm1.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm3.getGameStateMachine().getMyState());
		gm2.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm3.getGameStateMachine().getMyState());
		gm1.attackWithStrength(100.0);
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gm3.getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);

		logger.info("GM1:" + gm1.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM2:" + gm2.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM3:" + gm3.getGameStateMachine().getNotificationStateListener().toString());
		gm1.destroy();
		gm2.destroy();
		gm3.destroy();
	}

	@Test
	public void testSimpleGameWithFourPeopleOneWinning() throws Exception {
		GameManager gm1 = new GameManager(new MockBroadCastManager(), "id1", new MockListener(1));
        gm1.readyToPlay("Name 1");
		GameManager gm2 = new GameManager(new MockBroadCastManager(), "id2", new MockListener(2));
        gm2.readyToPlay("Name 2");
		GameManager gm3 = new GameManager(new MockBroadCastManager(), "id3", new MockListener(3));
        gm3.readyToPlay("Name 3");
		GameManager gm4 = new GameManager(new MockBroadCastManager(), "id4", new MockListener(4));
        gm4.readyToPlay("Name 4");
		assertEquals(PlayerState.WAITING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm4.getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);
		gm1.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm4.getGameStateMachine().getMyState());
		gm2.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm4.getGameStateMachine().getMyState());
		gm1.attackWithStrength(100.0);
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gm4.getGameStateMachine().getMyState());
		Thread.sleep(4*TIME_UNIT);

		logger.info("GM1:" + gm1.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM2:" + gm2.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM3:" + gm3.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM4:" + gm3.getGameStateMachine().getNotificationStateListener().toString());
		gm1.destroy();
		gm2.destroy();
		gm3.destroy();
		gm4.destroy();
	}

	@Test
	public void testSimpleGameWithFourPeoplePartialAttacks() throws Exception {
		GameManager gm1 = new GameManager(new MockBroadCastManager(), "id1", new MockListener(1));
        gm1.readyToPlay("Name 1");
		GameManager gm2 = new GameManager(new MockBroadCastManager(), "id2", new MockListener(2));
        gm2.readyToPlay("Name 2");
		GameManager gm3 = new GameManager(new MockBroadCastManager(), "id3", new MockListener(3));
        gm3.readyToPlay("Name 3");
		GameManager gm4 = new GameManager(new MockBroadCastManager(), "id4", new MockListener(4));
        gm4.readyToPlay("Name 4");
		double expectedPoints[] = {100.0,100.0,100.0,100.0};
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WAITING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm4.getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gm1, expectedPoints);
		expectedPlayerLifeEquals(gm2, expectedPoints);
		expectedPlayerLifeEquals(gm3, expectedPoints);
		expectedPlayerLifeEquals(gm4, expectedPoints);
		gm1.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.IN_GAME,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.WAITING,gm4.getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gm1, expectedPoints);
		expectedPlayerLifeEquals(gm2, expectedPoints);
		expectedPlayerLifeEquals(gm3, expectedPoints);
		expectedPlayerLifeEquals(gm4, expectedPoints);
		gm2.joinGame();
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm4.getGameStateMachine().getMyState());
		gm1.attackWithStrength(30.0);
		expectedPoints[1] = 70.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm4.getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gm1, expectedPoints);
		expectedPlayerLifeEquals(gm2, expectedPoints);
		expectedPlayerLifeEquals(gm3, expectedPoints);
		expectedPlayerLifeEquals(gm4, expectedPoints);
		gm1.attackWithStrength(60.0);
		expectedPoints[1] = 10.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm4.getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gm1, expectedPoints);
		expectedPlayerLifeEquals(gm2, expectedPoints);
		expectedPlayerLifeEquals(gm3, expectedPoints);
		expectedPlayerLifeEquals(gm4, expectedPoints);
		gm2.attackWithStrength(50.0);
		expectedPoints[0] = 50.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.PLAYING,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.PLAYING,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.OBSERVER,gm4.getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gm1, expectedPoints);
		expectedPlayerLifeEquals(gm2, expectedPoints);
		expectedPlayerLifeEquals(gm3, expectedPoints);
		expectedPlayerLifeEquals(gm4, expectedPoints);
		gm1.attackWithStrength(30.0);
		expectedPoints[1] = 0.0;
		Thread.sleep(4*TIME_UNIT);
		assertEquals(PlayerState.WON,gm1.getGameStateMachine().getMyState());
		assertEquals(PlayerState.LOST,gm2.getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gm3.getGameStateMachine().getMyState());
		assertEquals(PlayerState.GAME_FINISHED,gm4.getGameStateMachine().getMyState());
		expectedPlayerLifeEquals(gm1, expectedPoints);
		expectedPlayerLifeEquals(gm2, expectedPoints);
		expectedPlayerLifeEquals(gm3, expectedPoints);
		expectedPlayerLifeEquals(gm4, expectedPoints);
		Thread.sleep(4*TIME_UNIT);
		expectedPlayerLifeEquals(gm1, expectedPoints);
		expectedPlayerLifeEquals(gm2, expectedPoints);
		expectedPlayerLifeEquals(gm3, expectedPoints);
		expectedPlayerLifeEquals(gm4, expectedPoints);
		logger.info("GM1:" + gm1.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM2:" + gm2.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM3:" + gm3.getGameStateMachine().getNotificationStateListener().toString());
		logger.info("GM4:" + gm4.getGameStateMachine().getNotificationStateListener().toString());
		gm1.destroy();
		gm2.destroy();
		gm3.destroy();
		gm4.destroy();
	}
	
	
}
