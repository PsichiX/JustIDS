package com.PsichiX.JustIDS;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;



public class BroadCastManager {

	private static int PORT = 14444;
	DatagramSocket socket;
	
	public BroadCastManager() {
		try {
			Log.i("INFO", "Opening datagram socket");
			socket = new DatagramSocket(PORT);
			socket.setSoTimeout(200);
		} catch (SocketException e) {
			Log.e("SOCKET", e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	public InetAddress getBroadcastAddress(Context context) throws IOException {
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
	
	public void sendBroadcast(final Context context, final byte [] data) {
			new Thread() { 
				@Override
				public void run() {
					try {
						socket.setBroadcast(true);
						DatagramPacket packet = new DatagramPacket(data, data.length,
							    getBroadcastAddress(context), PORT);
							socket.send(packet);
					} catch (IOException e) {
						Log.e("SOCKET", e.getMessage());
					}
				}
			}.start();
	}
	
	public byte [] receiveBroadCast(Context context)  {
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(packet);
		} catch (InterruptedIOException ioe) {
			Log.i("SOCKET", "Timeout");
			return null;
		} catch (IOException e) {
			Log.e("SOCKET", e.getMessage());
		}
		int len = packet.getLength();
		return Arrays.copyOfRange(packet.getData(), 0, len);
	}

	public void destroy() {
		socket.close();
	}
	
}
