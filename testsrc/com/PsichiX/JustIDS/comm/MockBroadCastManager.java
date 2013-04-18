package com.PsichiX.JustIDS.comm;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo;




public class MockBroadCastManager implements BroadcastManagerInterface {

	Logger logger = Logger.getLogger(MockBroadCastManager.class.getName());

	private static List<BlockingQueue<PlayerBroadcastInfo>> queues = new LinkedList<BlockingQueue<PlayerBroadcastInfo>>();
		
	private BlockingQueue<PlayerBroadcastInfo> myQueue;
	
	
	public MockBroadCastManager() {
		this.myQueue = new LinkedBlockingQueue<PlayerBroadcastInfo>();
		queues.add(myQueue);
	}

	@Override
	public void destroy() {
		queues.clear();
	}

	@Override
	public void sendBroadcast(PlayerBroadcastInfo info) {
		logger.info("Sending message "+ info);
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
