package com.zs.juc.lock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class FairAndUnfairTest {
	private static Lock fairLock = new ReentrantLock2(true);
	private static Lock unfairLock = new ReentrantLock2(false);

	public void fair() {
		testLock(fairLock);
	}

	public void unfair() {
		testLock(unfairLock);
	}

	private void testLock(Lock lock) {
		// 启动5个Job（略）
	}

	private static class Job extends Thread {
		private Lock lock;

		public Job(Lock lock) {
			this.lock = lock;
		}

//		private void run() {// 连续2次打印当前的Thread和等待队列中的Thread（略）
//		}
	}

	private static class ReentrantLock2 extends ReentrantLock {
		public ReentrantLock2(boolean fair) {
			super(fair);
		}

		protected Collection<Thread> getQueuedThreads() {
			List<Thread> arrayList = new ArrayList<Thread>(super.getQueuedThreads());
			Collections.reverse(arrayList);
			return arrayList;
		}
	}
}