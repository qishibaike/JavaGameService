package org.tont.core;

public class GlobalTime implements Runnable{

	private static volatile long curTime;
	
	public static long getCurTime() {
		return curTime;
	}
	
	public static void start() {
		new Thread(new GlobalTime()).start();
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				GlobalTime.curTime = System.currentTimeMillis() / 1000;
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//
			}
		}
	}
	
}
