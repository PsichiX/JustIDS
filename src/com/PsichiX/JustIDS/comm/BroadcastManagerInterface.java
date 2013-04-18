package com.PsichiX.JustIDS.comm;

import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo;

public interface BroadcastManagerInterface {

	public abstract void destroy();

	public abstract void sendBroadcast(PlayerBroadcastInfo info);

	public abstract PlayerBroadcastInfo receiveBroadCast();

}