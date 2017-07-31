package com.zs.juc.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TheadPoolTest {

	public static void main(String[] args) throws InterruptedException {

		ExecutorService service = Executors.newCachedThreadPool();

		TimeUnit.SECONDS.sleep(15);

		for (int i = 0; i < 50; i++){
		service.submit(getTask());}

		TimeUnit.SECONDS.sleep(1500);

	}

	private static Runnable getTask() {
		return new Runnable() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getId());
			}
		};
	}

}
