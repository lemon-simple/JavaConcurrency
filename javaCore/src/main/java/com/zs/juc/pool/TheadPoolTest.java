package com.zs.juc.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TheadPoolTest {
	private static final int COUNT_BITS = Integer.SIZE - 3;

	private static final int CAPACITY = (1 << COUNT_BITS) - 1;

	// runState is stored in the high-order bits
	private static final int RUNNING = -1 << COUNT_BITS;

	private static final int SHUTDOWN = 0 << COUNT_BITS;

	private static final int STOP = 1 << COUNT_BITS;

	private static final int TIDYING = 2 << COUNT_BITS;

	private static final int TERMINATED = 3 << COUNT_BITS;

	public static void main(String[] args) throws Exception {

		ExecutorService service = Executors.newFixedThreadPool(2);
		// ExecutorService service = Executors.newCachedThreadPool();
		// ExecutorService service = Executors.newWorkStealingPool();
		List<Callable<String>> callables = new ArrayList<Callable<String>>();
		for (int i = 0; i < 10; i++) {
			callables.add(Executors.callable(getTask(i), "s"));
		}

		for (Callable<String> call : callables) {
			service.submit(call);
		}
	}

	private static Runnable getTask(int i) {
		return new Runnable() {
			@Override
			public void run() {

				System.out.println("i	" + i + Thread.currentThread().getId());
				try {
					TimeUnit.SECONDS.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	}
}
