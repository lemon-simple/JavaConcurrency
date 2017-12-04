package com.zs.juc.lock.aqs;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 总的来说这种实现的好处是保证所有等待线程的公平竞争，而且没有竞争同一个变量，因为每个线程只要等待自己的前继释放就好了。
 * 而自旋的好处是线程不需要睡眠和唤醒，减小了系统调用的开销。 
 * 
 * https://segmentfault.com/a/1190000007094429
 */

public class CLHLock {
	private final ThreadLocal<Node> prev;
	private final ThreadLocal<Node> node;
	private final AtomicReference<Node> tail = new AtomicReference<Node>(new Node("head"));

	public CLHLock() {
		this.node = new ThreadLocal<Node>() {
			protected Node initialValue() {
				return new Node();
			}
		};

		this.prev = new ThreadLocal<Node>() {
			protected Node initialValue() {
				return null;
			}
		};
	}

	public void lock() {
		final Node node = this.node.get();
		System.out.println(Thread.currentThread().getName());

		node.locked = true;
		// 一个CAS操作即可将当前线程对应的节点加入到队列中，
		// 并且同时获得了前继节点的引用，然后就是等待前继释放锁
		Node pred = this.tail.getAndSet(node);
		this.prev.set(pred);
		System.out.println();
		System.out.println("this.node" + this.node.get());
		System.out.println("this.prev" + this.prev.get());
		System.out.println("this.tail" + this.tail.get());

		while (pred.locked) {// 进入自旋
		}
	}

	public void unlock() {
		
		System.out.println();
		System.out.println(Thread.currentThread().getName()+"start-----------unlock-------------");
		System.out.println("unlock this.node" + this.node.get());
		System.out.println("unlock this.prev" + this.prev.get());
		System.out.println("unlock this.tail" + this.tail.get());
		final Node node = this.node.get();
		node.locked = false;
		this.node.set(this.prev.get());
		System.out.println(Thread.currentThread().getName()+"-----------unlock-------------");
		System.out.println("unlock this.node" + this.node.get());
		System.out.println("unlock this.prev" + this.prev.get());
		System.out.println("unlock this.tail" + this.tail.get());
		System.out.println(Thread.currentThread().getName()+"end-----------unlock-------------");
		System.out.println();

	}

	private static class Node {
		private volatile boolean locked;
		private String name;
		
		public Node() {
		}
		public Node(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return "Node [locked=" + locked + ", name=" + name + "]";
		}
		
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
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}).start();
			Thread.sleep(100);
		}
		Thread.sleep(1000);
		System.out.println("main thread unlock!");
		lock.unlock();
	}
}