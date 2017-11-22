package com.zs.juc.queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentLinkedQueueTest {

	public static void main(String[] args) throws InterruptedException {

		ConcurrentLinkedQueue<Runnable> clQueue = new ConcurrentLinkedQueue<Runnable>();
	}

	static class CustomizedTask implements Runnable {
		@Override
		public void run() {
			System.out.println(System.currentTimeMillis());
		}
	}
}
