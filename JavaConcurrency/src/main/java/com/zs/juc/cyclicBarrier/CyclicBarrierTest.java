package com.zs.juc.cyclicBarrier;

import java.util.concurrent.TimeUnit;

public class CyclicBarrierTest {
	static CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

	public static void main(String[] args) throws InterruptedException {
	Thread t=new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					cyclicBarrier.await();
					
					System.out.println(22);

				} catch (Exception e) {
				}
				System.out.println(1);
			}
		});
	t.start();
	
	TimeUnit.SECONDS.sleep(3);
	t.interrupt();

		System.out.println(2);
	}
}