	package com.PsichiX.JustIDS.comm;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;

import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo;
import com.google.protobuf.InvalidProtocolBufferException;



public class UDPBroadCastManager implements BroadcastManagerInterface {

    private static final String TAG = UDPBroadCastManager.class.getName();

    public static interface ChainedReceiver {
        void forwardReceivedMessage(PlayerBroadcastInfo pbi);
    }

	private static int PORT = 14444;
	DatagramSocket socket;
	private Context context;

    private LinkedList<ChainedReceiver> chainedReceiverList = new LinkedList<ChainedReceiver>();
	
	public UDPBroadCastManager(Context context) {
		this.context = context;
		try {
			Log.v(TAG, "Opening datagram socket");
			socket = new DatagramSocket(PORT);
			socket.setSoTimeout(200);
		} catch (SocketException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	private InetAddress getBroadcastAddress(Context context) throws IOException {
	    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    MulticastLock ml = wifi.createMulticastLock("some tag");
	    ml.acquire();
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    // handle null somehow

	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}
	
	private void sendBroadcast(final Context context, final byte [] data) {
			new Thread() { 
				@Override
				public void run() {
					try {
						socket.setBroadcast(true);
						DatagramPacket packet = new DatagramPacket(data, data.length,
							    getBroadcastAddress(context), PORT);
							socket.send(packet);
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}.start();
	}
	
	private byte [] receiveBroadCast(Context context)  {
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(packet);
		} catch (InterruptedIOException ioe) {
			Log.v(TAG, "Timeout");
			return null;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		int len = packet.getLength();
		byte [] newArray = new byte[len];
		byte [] oldArray = packet.getData();
		for (int i=0; i<len; i++) {
			newArray[i] = oldArray[i];
		}
		return newArray;
	}

	/* (non-Javadoc)
	 * @see com.PsichiX.JustIDS.comm.BroadcastManagerInterface#stopGameManager()
	 */
	@Override
	public void destroy() {
		socket.close();
	}

	/* (non-Javadoc)
	 * @see com.PsichiX.JustIDS.comm.BroadcastManagerInterface#sendBroadcast(com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo)
	 */
	@Override
	public void sendBroadcast(PlayerBroadcastInfo info) {
		byte[] message = info.toByteArray();
		sendBroadcast(context, message);		
	}

	/* (non-Javadoc)
	 * @see com.PsichiX.JustIDS.comm.BroadcastManagerInterface#receiveBroadCast()
	 */
	@Override
	public PlayerBroadcastInfo receiveBroadCast() {
		final byte[] message = receiveBroadCast(context);
		if (message == null)  {
			return null;
		}
		try {
			PlayerBroadcastInfo pbi = PlayerBroadcastInfo.parseFrom(message);
            for (ChainedReceiver receiver : chainedReceiverList) {
                receiver.forwardReceivedMessage(pbi);
            }
			return pbi;
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		}
	}

    public void addChainedReceiver(ChainedReceiver receiver) {
        chainedReceiverList.add(receiver);
    }
	
}
