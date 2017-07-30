package com.zs.juc.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TheadPoolTest {

	public static void main(String[] args) {

		ExecutorService service = Executors.newFixedThreadPool(2);

		for (int i = 0; i < 5; i++)
			service.submit(getTask());
		
	
		service.shutdown();

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
