package com.zs.juc.Semaphore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SemaphoreTest {
	private static final int THREAD_COUNT = 3;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
	private static Semaphore semaphore = new Semaphore(2);

	public static void main(String[] args) {
		for (int i = 0; i < THREAD_COUNT; i++) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println(Thread.currentThread().getId() + "	ready");

						semaphore.acquire();
						System.out.println(Thread.currentThread().getId() + "	save data");
						semaphore.release();

					} catch (InterruptedException e) {
					}
				}
			});
		}
		threadPool.shutdown();
	}
}