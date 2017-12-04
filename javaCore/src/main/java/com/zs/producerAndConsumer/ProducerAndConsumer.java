package com.zs.producerAndConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.zs.juc.pool.Executors;

public class ProducerAndConsumer {

	static List<String> collection = new ArrayList<String>();
	private static final int maxSize = 10;
	private static int count = 0;

	public static void main(String[] args) {
		ExecutorService producerThreadPool = Executors.newFixedThreadPool(5);
		ExecutorService consumerThreadPool = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 5; i++) {
			producerThreadPool.execute(new Producer());
		}
		for (int i = 0; i < 5; i++) {
			consumerThreadPool.execute(new Consumer());

		}
	}

	static class Producer implements Runnable {
		private void produce() {
			synchronized (collection) {
				if (collection.size() <= maxSize) {
					collection.add("product" + count++);
					System.out.println(Thread.currentThread() + "	produce:	product_" + count);
					if (collection.size() == 1) {
						System.out.println(Thread.currentThread() + "	notify");
						collection.notifyAll();
					}
				} else {
					try {
						System.out.println(Thread.currentThread() + "	wait");
						collection.wait();// wait for a changed condition
						System.out.println(Thread.currentThread() + "	run by notified");

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted())
				produce();
		}
	}

	static class Consumer implements Runnable {
		public void comsume() {
			synchronized (collection) {
				if (collection.size() > 0) {
					String removedOne = collection.remove(0);
					System.out.println(Thread.currentThread() + "	remove:	" + removedOne);
					if (collection.size() == maxSize - 1) {
						System.out.println(Thread.currentThread() + "	notify");
						collection.notifyAll();
					}
				} else {
					try {
						System.out.println(Thread.currentThread() + "	wait");
						collection.wait();// wait for a changed condition
						System.out.println(Thread.currentThread() + "	run by notified");

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted())
				comsume();
		}
	}
}