package com.zs.juc.lock;

import java.util.concurrent.TimeUnit;

public class ReadWriteLockTest {
	public static void main(String[] args) throws InterruptedException {

		// test();
		// testExtractCounter();
		// testThreadLocalReference();

		// 构造FIFO队列：|readLock-lock|writeLo-ck-lock|readLock-lock|readLock-lock|
		java.util.concurrent.locks.ReentrantReadWriteLock wrLock = new java.util.concurrent.locks.ReentrantReadWriteLock();

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("01");
				wrLock.readLock().lock();
				wait_time();
				wrLock.readLock().unlock();
				System.out.println("01 end");
			}
		}, "01 to readLock lock").start();

		wait_time(1000);

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("02");
				wrLock.writeLock().lock();
				wait_time(1000);
				wrLock.writeLock().unlock();
				System.out.println("02 end");
			}
		}, "02 to writeLock lock").start();

		wait_time();

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("03");
				wrLock.readLock().lock();
				wait_time();
				wrLock.readLock().unlock();
				System.out.println("03 end");
			}
		}, "03 to readLock lock").start();

		wait_time();

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("04");
				wrLock.readLock().lock();
				wait_time();
				wrLock.readLock().unlock();
				System.out.println("04 end");
			}
		}, "04 to readLock lock").start();

		wait_time();

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("05");
				wrLock.readLock().lock();
				wait_time(100);
				wrLock.readLock().unlock();
				System.out.println("05 end");
			}
		}, "05 to readLock lock").start();

		wait_time();

		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("06");
				wrLock.writeLock().lock();
				wait_time();
				wrLock.writeLock().unlock();
				System.out.println("06 end");
			}
		}, "06 to writeLock lock").start();
		System.out.println("main thread end");
	}

	private static void wait_time(int s) {
		System.out.println("[" + Thread.currentThread().getName() + Thread.currentThread().getId() + "   ]sleep start");
		try {
			TimeUnit.MILLISECONDS.sleep(s);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("[" + Thread.currentThread().getName() + Thread.currentThread().getId() + "   ]sleep end");
	}

	private static void wait_time() {
		wait_time(30);
	}

}
