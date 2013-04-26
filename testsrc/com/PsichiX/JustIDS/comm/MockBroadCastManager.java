package com.PsichiX.JustIDS.comm;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo;


public class MockBroadCastManager implements BroadcastManagerInterface {

    // Here we are using java logger not the android logger on purpose...
    // We want to test the game manager logic outside of android
	Logger logger = Logger.getLogger(MockBroadCastManager.class.getName());

	private static List<BlockingQueue<PlayerBroadcastInfo>> queues = new LinkedList<BlockingQueue<PlayerBroadcastInfo>>();
		
	private BlockingQueue<PlayerBroadcastInfo> myQueue;
	
	
	public MockBroadCastManager() {
		this.myQueue = new LinkedBlockingQueue<PlayerBroadcastInfo>();
		queues.add(myQueue);
	}

	@Override
	public void destroy() {
		for (BlockingQueue <PlayerBroadcastInfo> queue: queues) {
            queue.clear();
        }
        queues.clear();
        this.myQueue.clear();
	}

	@Override
	public void sendBroadcast(PlayerBroadcastInfo info) {
		logger.fine("Sending message "+ info);
		for (BlockingQueue<PlayerBroadcastInfo> queue : queues) {
			queue.add(info);
		}
	}

	@Override
	public PlayerBroadcastInfo receiveBroadCast() {
		try {
			return myQueue.take();
		} catch (InterruptedException e) {
			return null;
		}		
	}

}
