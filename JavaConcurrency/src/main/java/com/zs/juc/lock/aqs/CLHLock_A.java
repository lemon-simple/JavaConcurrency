package com.zs.juc.lock.aqs;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * CLH锁也是一种基于链表的可扩展、高性能、公平的自旋锁，申请线程只在本地变量上自旋，它不断轮询前驱的状态，如果发现前驱释放了锁就结束自旋。
 */
public class CLHLock_A {
	private static class CLHNode {
		private static volatile boolean isLocked;
	}

	private final ThreadLocal<CLHNode> node;
	private final ThreadLocal<CLHNode> prev;
	private final AtomicReference<CLHNode> tail = new AtomicReference<CLHNode>(new CLHNode());

	public CLHLock_A() {
		node = new ThreadLocal<CLHNode>() {
			protected CLHNode initialValue() {
				return new CLHNode();
			}
		};
		prev = new ThreadLocal<CLHNode>() {
			protected CLHNode initialValue() {
				return null;
			}
		};
	}
	
	public void lock(){
		CLHNode node=this.node.get();
		node.isLocked=true;
		CLHNode prev=tail.getAndSet(node);
		while(prev.isLocked){
		}
		
	}
	
	public void unLock() {
		CLHNode node = this.node.get();
		node.isLocked = false;
	}
	
	public static void main(String[] args) throws InterruptedException {
		final CLHLock lock = new CLHLock();
		lock.lock();

		for (int i = 0; i < 3; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					lock.lock();
					System.out.println(Thread.currentThread().getId() + " acquired the lock!");
					lock.unlock();
				}
			}).start();
			Thread.sleep(100);
		}

		System.out.println("main thread unlock!");
		lock.unlock();
	}

}