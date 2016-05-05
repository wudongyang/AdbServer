package com.caibou.adbserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author ZWF
 *
 */
public class SocketConnect {
	
	private Socket mSocket;
	private ConnectCallBack mCallback;
	private boolean mRunning = false;
	private final int BUFFER_SIZE = 1024;
	
	public void connect(final String ip, final int port){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mSocket = new Socket(ip, port);
					if (mCallback != null) {
						mCallback.connected();
					}
					onReceive();
				} catch (UnknownHostException e) {
					onError();
					e.printStackTrace();
				} catch (IOException e) {
					onError();
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private void onReceive(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream is = mSocket.getInputStream();
					byte[] buffer = new byte[BUFFER_SIZE];
					int length = 0;
					mRunning = true;
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					while (mRunning) {
						length = is.read(buffer, 0, buffer.length);
						if (length == -1) {
							disconnct();
							mRunning = false;
							break;
						}
						
						bos.write(buffer, 0, length);
						if (length < BUFFER_SIZE) {
							if (mCallback != null) {
								mCallback.receive(bos.toByteArray());
								bos.reset();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void disconnct(){
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setConnectListener(ConnectCallBack callback){
		this.mCallback = callback;
	}
	
	private void onError(){
		if (mCallback != null) {
			mCallback.error();
		}
	}
	
	public void send(byte[] buffer){
		OutputStream os;
		try {
			os = mSocket.getOutputStream();
			if (os != null) {
				os.write(buffer);
				os.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public interface ConnectCallBack{
		void connected();
		void receive(byte[] buffer);
		void disconnect();
		void error();
	}
}
