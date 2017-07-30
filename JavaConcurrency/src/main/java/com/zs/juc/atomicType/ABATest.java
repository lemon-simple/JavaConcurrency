package com.zs.juc.atomicType;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

public class ABATest {

	static java.util.concurrent.atomic.AtomicInteger atomicInteger = new java.util.concurrent.atomic.AtomicInteger(100);
	static AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<Integer>(100, 1);

	public static void main(String[] args) throws InterruptedException {

		// AtomicInteger
		Thread at1 = new Thread(new Runnable() {
			@Override
			public void run() {
				// 100——>110——>100
				atomicInteger.compareAndSet(100, 110);
				atomicInteger.compareAndSet(110, 100);
			}
		});

		Thread at2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(1); // at1,执行完
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("AtomicInteger ABA:" + atomicInteger.compareAndSet(100, 120));
			}
		});

		at1.start();
		at2.start();

		// AtomicStampedReference

		Thread tsf1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 让 tsf2先获取stamp，导致预期时间戳不一致
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// <100,stampA>——><110,stampA+1>
				atomicStampedReference.compareAndSet(100, 110, atomicStampedReference.getStamp(),
						atomicStampedReference.getStamp() + 1);
				// <110,stampA+1>——><100,stampA+1+1>
				atomicStampedReference.compareAndSet(110, 100, atomicStampedReference.getStamp(),
						atomicStampedReference.getStamp() + 1);
			}
		});

		Thread tsf2 = new Thread(new Runnable() {
			@Override
			public void run() {
				int stamp = atomicStampedReference.getStamp();
				System.out.println("tsf2  stamp" + stamp);

				try {
					TimeUnit.SECONDS.sleep(2); // 线程tsf1执行完
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("AtomicStampedReference ABA:"
						+ atomicStampedReference.compareAndSet(100, 120, stamp, stamp + 1));
			}
		});

		tsf1.start();
		tsf2.start();
	}

}