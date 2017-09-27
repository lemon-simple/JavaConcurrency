
package com.zs.juc.lock;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * An implementation of {@link ReadWriteLock} supporting similar semantics to
 * {@link ReentrantLock}.
 * <p>
 * This class has the following properties:
 *
 * <ul>
 * <li><b>Acquisition order</b>
 *
 * <p>
 * This class does not impose a reader or writer preference ordering for lock
 * access. However, it does support an optional <em>fairness</em> policy.
 *
 * 非公平模式(默认)当作为非公平模式构建时，读锁、写锁的进入顺序没有指定，为了满足重入的要求。
 * 一个非公平锁连续不断地竞争state也许会不预期的导致其他读、写线程的延迟，但是非公平锁会有更好的吞吐量。
 * 
 *
 * <dt><b><i>Fair mode</i></b>
 * <dd>When constructed as fair, threads contend for entry using an
 * approximately arrival-order policy. When the currently held lock is released,
 * either the longest-waiting single writer thread will be assigned the write
 * lock, or if there is a group of reader threads waiting longer than all
 * waiting writer threads, that group will be assigned the read lock.
 * 
 * 公平模式 当构建为公平锁，线程竞争使用了近乎先等先得的策略。当当前线程释放了锁，等待最久的写线程将会获取到写锁，
 * 或者当多个读线程都排队在任何一个写线程之前， 那么这些读线程都会获取到读锁。
 * 
 * 当写锁被占用，或者存在一个等待的写线程(排在队列最靠前)？？？？，
 * 那么其他再要尝试获取公平读锁的读线程将会被阻塞，直到排在它前边的写线程获取、释放掉了写锁。
 * 当然，当排在读线程之前的写线程取消了对写锁的获取，从队列中移除，
 * 并且这个时候写锁是空闲的， 这些读线程将会允许共享式获取读锁。
 * 
 *
 * 公平模式中，一个写线程获取锁成功的前提是：当前写锁、读锁都是空闲的。
 * 要注意：同ReentrantLock类似，即使在公平模式中，tryLock方法都会直接尝试获取锁(barge CAS)。
 * 
 *
 * <li><b>Reentrancy</b>
 *
 * <p>
 * This lock allows both readers and writers to reacquire read or write locks in
 * the style of a {@link ReentrantLock}. Non-reentrant readers are not allowed
 * until all write locks held by the writing thread have been released.
 * 重入性与ReentrantLock类似。
 *
 * <p>
 * Additionally, a writer can acquire the read lock, but not vice-versa. Among
 * other applications, reentrancy can be useful when write locks are held during
 * calls or callbacks to methods that perform reads under read locks. If a
 * reader tries to acquire the write lock it will never succeed.
 * 一个已经获取到写锁的写线程可以再次(重入)获取读锁，但是一个获取到读锁的读线程并不能重入获取写锁。？？
 *
 * <li><b>Lock downgrading</b>
 * <p>
 * Reentrancy also allows downgrading from the write lock to a read lock, by
 * acquiring the write lock, then the read lock and then releasing the write
 * lock. However, upgrading from a read lock to the write lock is <b>not</b>
 * possible. 重入性允许一个写锁降级为一个读锁（获取写锁，获取读锁，然后释放写锁），但是读锁不能升级为一个写锁。？？
 *
 * <li><b>Interruption of lock acquisition</b>
 * <p>
 * The read lock and write lock both support interruption during lock
 * acquisition. 读写锁均支持对中断的响应
 *
 * <li><b>{@link Condition} support</b>
 * <p>
 * The write lock provides a {@link Condition} implementation that behaves in
 * the same way, with respect to the write lock, as the {@link Condition}
 * implementation provided by {@link ReentrantLock#newCondition} does for
 * {@link ReentrantLock}. This {@link Condition} can, of course, only be used
 * with the write lock.
 * 
 * <p>
 * The read lock does not support a {@link Condition} and
 * {@code readLock().newCondition()} throws
 * {@code UnsupportedOperationException}.
 *
 * Condition只能在写锁中使用，读锁无法使用。具体使用与ReentrantLock类似。
 * <li><b>Instrumentation</b>
 * <p>
 * This class supports methods to determine whether locks are held or contended.
 * These methods are designed for monitoring system state, not for
 * synchronization control.
 * </ul>
 *
 * <p>
 * Serialization of this class behaves in the same way as built-in locks: a
 * deserialized lock is in the unlocked state, regardless of its state when
 * serialized.
 *
 * <p>
 * <b>Sample usages</b>. Here is a code sketch showing how to perform lock
 * downgrading after updating a cache (exception handling is particularly tricky
 * when handling multiple locks in a non-nested fashion):
 * 
 * <pre>
 *  {@code
 * class CachedData {
 *   Object data;
 *   volatile boolean cacheValid;
 *   final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 *
 *   void processCachedData() {
 *     rwl.readLock().lock();
 *     if (!cacheValid) {
 *       // Must release read lock before acquiring write lock
 *       rwl.readLock().unlock();
 *       rwl.writeLock().lock();
 *       try {
 *         // Recheck state because another thread might have
 *         // acquired write lock and changed state before we did.
 *         if (!cacheValid) {
 *           data = ...
 *           cacheValid = true;
 *         }
 *         // Downgrade by acquiring read lock before releasing write lock
 *         rwl.readLock().lock();//锁降级(1获取写锁 2获取读锁 3释放写锁 4释放读锁)
 *       } finally {
 *         rwl.writeLock().unlock(); // Unlock write, still hold read
 *       }
 *     }
 *
 *     try {
 *       use(data);
 *     } finally {
 *       rwl.readLock().unlock();
 *     }
 *   }
 * }}
 * </pre>
 *
 * ReentrantReadWriteLocks can be used to improve concurrency in some uses of
 * some kinds of Collections. This is typically worthwhile only when the
 * collections are expected to be large, accessed by more reader threads than
 * writer threads, and entail operations with overhead that outweighs
 * synchronization overhead. For example, here is a class using a TreeMap that
 * is expected to be large and concurrently accessed.
 * 重入读写锁ReentrantReadWriteLock 在对JDK容器的使用中可改善并发性提高吞吐量。特別是在大容器的場景
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	class RWDictionary {
 * 		private final Map<String, Data> m = new TreeMap<String, Data>();
 * 		private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 * 		private final Lock r = rwl.readLock();
 * 		private final Lock w = rwl.writeLock();
 *
 * 		public Data get(String key) {
 * 			r.lock();
 * 			try {
 * 				return m.get(key);
 * 			} finally {
 * 				r.unlock();
 * 			}
 * 		}
 * 
 * 		public String[] allKeys() {
 * 			r.lock();
 * 			try {
 * 				return m.keySet().toArray();
 * 			} finally {
 * 				r.unlock();
 * 			}
 * 		}
 * 
 * 		public Data put(String key, Data value) {
 * 			w.lock();
 * 			try {
 * 				return m.put(key, value);
 * 			} finally {
 * 				w.unlock();
 * 			}
 * 		}
 * 
 * 		public void clear() {
 * 			w.lock();
 * 			try {
 * 				m.clear();
 * 			} finally {
 * 				w.unlock();
 * 			}
 * 		}
 * 	}
 * }
 * </pre>
 *
 * <h3>Implementation Notes</h3>
 *
 * <p>
 * This lock supports a maximum of 65535 recursive write locks and 65535 read
 * locks. Attempts to exceed these limits result in {@link Error} throws from
 * locking methods.
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ReentrantReadWriteLock implements ReadWriteLock, java.io.Serializable {
	private static final long serialVersionUID = -6992448646407690164L;
	/** Inner class providing readlock */
	private final ReentrantReadWriteLock.ReadLock readerLock;
	/** Inner class providing writelock */
	private final ReentrantReadWriteLock.WriteLock writerLock;
	/** Performs all synchronization mechanics */
	final Sync sync;

	/**
	 * Creates a new {@code ReentrantReadWriteLock} with default (nonfair)
	 * ordering properties.
	 */
	public ReentrantReadWriteLock() {
		this(false);
	}

	/**
	 * Creates a new {@code ReentrantReadWriteLock} with the given fairness
	 * policy.
	 *
	 * @param fair
	 *            {@code true} if this lock should use a fair ordering policy
	 */
	public ReentrantReadWriteLock(boolean fair) {
		sync = fair ? new FairSync() : new NonfairSync();
		readerLock = new ReadLock(this);
		writerLock = new WriteLock(this);
	}

	public ReentrantReadWriteLock.WriteLock writeLock() {
		return writerLock;
	}

	public ReentrantReadWriteLock.ReadLock readLock() {
		return readerLock;
	}

	/**
	 * Synchronization implementation for ReentrantReadWriteLock. Subclassed
	 * into fair and nonfair versions.
	 */
	abstract static class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = 6317671515068378041L;

		/*
		 * Read vs write count extraction constants and functions. Lock state is
		 * logically divided into two unsigned shorts: The lower one
		 * representing the exclusive (writer) lock hold count, and the upper
		 * the shared (reader) hold count.
		 * 
		 * 以下是读写锁的计数器提取后常量、函数。
		 * 
		 * 锁的state状态(int 32bit)被逻辑上分为两个无符号short数(16bit)：
		 * 这低16bit代表互斥的排他锁的持有状态(写锁), 高16bit代表共享锁的持有状态(读锁)。
		 * 
		 * 
		 */

		static final int SHARED_SHIFT = 16;
		static final int SHARED_UNIT = (1 << SHARED_SHIFT);
		static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
		static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

		/** Returns the number of shared holds represented in count */
		static int sharedCount(int c) {
			return c >>> SHARED_SHIFT;
		}

		/** Returns the number of exclusive holds represented in count */
		static int exclusiveCount(int c) {
			return c & EXCLUSIVE_MASK;
		}

		static final class HoldCounter {
			int count = 0;
			// Use id, not reference, to avoid garbage retention
			final long tid = getThreadId(Thread.currentThread());
		}

		/**
		 */
		static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
			public HoldCounter initialValue() {
				return new HoldCounter();
			}
		}

		/**
		 * 用来记录每个获取读锁的线程id以及线程重入情况。 当前线程持有的读锁的重入次数，
		 * 每个线程获取的读锁状态保存在ThreadLocal中
		 * 仅在构造器和readObject时完成初始化。当一个读锁的重入数量为0时清除
		 */
		private transient ThreadLocalHoldCounter readHolds;

		/**
		 * 共享锁的计数器缓存（记录最后一个获取读锁成功的线程情况:线程ID，线程重入次数)
		 */
		private transient HoldCounter cachedHoldCounter;

		/**
		 * 用來引用首个获取共享读锁的线程(CAS 0——>1).
		 */
		private transient Thread firstReader = null;
		private transient int firstReaderHoldCount;

		Sync() {
			readHolds = new ThreadLocalHoldCounter();// 当前线程(ThreadLocal存储)的锁的计数器，初始为0
			setState(getState()); // ensures visibility of readHolds
		}

		/*
		 * Acquires and releases use the same code for fair and nonfair locks,
		 * but differ in whether/how they allow barging when queues are
		 * non-empty.
		 */

		/**
		 * Returns true if the current thread, when trying to acquire the read
		 * lock, and otherwise eligible to do so, should block because of policy
		 * for overtaking other waiting threads.
		 */
		abstract boolean readerShouldBlock();

		/**
		 * Returns true if the current thread, when trying to acquire the write
		 * lock, and otherwise eligible to do so, should block because of policy
		 * for overtaking other waiting threads.
		 */
		abstract boolean writerShouldBlock();

		/*
		 * Note that tryRelease and tryAcquire can be called by Conditions. So
		 * it is possible that their arguments contain both read and write holds
		 * that are all released during a condition wait and re-established in
		 * tryAcquire. 注意：tryRelease、tryAcquire方法可以被Condition调用。
		 */

		protected final boolean tryRelease(int releases) {
			if (!isHeldExclusively())// ！当前锁是否排他模式被占有？
				throw new IllegalMonitorStateException();
			int nextc = getState() - releases;// state计数器准备释放一次release
			boolean free = exclusiveCount(nextc) == 0;// 获取排他模式下的计数器个数
			if (free)// 当前写锁是否空闲
				setExclusiveOwnerThread(null);
			setState(nextc);
			return free;
		}

		protected final boolean tryAcquire(int acquires) {
			/*
			 * Walkthrough: 1. If read count nonzero or write count nonzero and
			 * owner is a different thread, fail. 2. If count would saturate,
			 * fail. (This can only happen if count is already nonzero.) 3.
			 * Otherwise, this thread is eligible for lock if it is either a
			 * reentrant acquire or queue policy allows it. If so, update state
			 * and set owner.
			 * 
			 * 1.当读锁被其他线程占用则失败;写锁被其他线程占用则失败。(写锁的排他性决定了获取成功的前提是:读写锁都空闲) 
			 * 2.写锁被当前线程重入，在count不为0的前提下，如果计数出现饱和，失败
			 * 3.否则，当前线程有资格CAS获取锁(考虑公平、非公平模式)。
			 */
			Thread current = Thread.currentThread();
			int c = getState();
			int w = exclusiveCount(c);// 获取锁的提取数
			// 以下if分支中,锁被占用,重入情況下才有可能會成功,否者失败或者抛出异常。
			if (c != 0) {// state!=0？
				// (Note: if c != 0 and w == 0 then shared count != 0)
				// 注意:state！=0 并且写锁提取数=0那么读锁提取数肯定不为0
				if (w == 0 || current != getExclusiveOwnerThread())// 如果读锁被占用写锁空闲？||占用线程不是当前线程？
					return false;// 写锁空闲读锁占用 或者 占用锁线程不是当前线程 那么失败
				if (w + exclusiveCount(acquires) > MAX_COUNT)// 饱和，失败
					throw new Error("Maximum lock count exceeded");
				// Reentrant acquire
				setState(c + acquires);// c!=0&&w!=0&&当前线程占用&&重入次数未饱和。也就是当前线程持有但是计数器不饱和,可以继续重入
				return true;
			}	
			if (writerShouldBlock() || !compareAndSetState(c, c + acquires))// 当前模式是否允许获取锁||CAS失败
				return false;
			setExclusiveOwnerThread(current);
			return true;
		}

		protected final boolean tryReleaseShared(int unused) {
			Thread current = Thread.currentThread();
			if (firstReader == current) {// 当前线程是否是共享式读线程中的第一个获取到读锁的线程
				// assert firstReaderHoldCount > 0;
				if (firstReaderHoldCount == 1)// 确认是首个获取到读锁的线程
					firstReader = null;// 释放firstReader引用
				else
					firstReaderHoldCount--;// 否者第一次获取锁的线程存在重入的情况
			} else {// 如果不是首次获取锁的线程的释放共享锁操作
				HoldCounter rh = cachedHoldCounter;// 锁缓存的计数器情况
				if (rh == null || rh.tid != getThreadId(current))// ？？？||当前线程并不是最后的缓存线程(最后一个获取到共享锁的线程)
					rh = readHolds.get();// 获取当前线程缓存的锁计数情况
				int count = rh.count;
				if (count <= 1) {// 1表非首线程的其他线程示唯一一次获得了锁（非首线程），清除threadLocal
					readHolds.remove();
					if (count <= 0)
						throw unmatchedUnlockException();
				}
				--rh.count;
			}
			for (;;) {
				int c = getState();
				int nextc = c - SHARED_UNIT;
				if (compareAndSetState(c, nextc))
					// Releasing the read lock has no effect on readers,
					// but it may allow waiting writers to proceed if
					// both read and write locks are now free.
					return nextc == 0;// true:读写锁均空闲，这时才需要唤醒后继结点(应该是写线程)
			}
		}

		private IllegalMonitorStateException unmatchedUnlockException() {
			return new IllegalMonitorStateException("attempt to unlock read lock, not locked by current thread");
		}

		protected final int tryAcquireShared(int unused) {
			/*
			 * 此方法实现用于读锁。
			 * 1.写锁被其他线程占有，失败
			 * 2.当前是否满足排队策略？如果满足排队策略，并且不需要等待，那么CAS.注意此步骤没有检查重入性获取。
			 * 3.如果2因为CAS失败或者饱和异常或者没有资格，那么绑定版本号循环再试
			 */
			Thread current = Thread.currentThread();
			int c = getState();
			// 如果：写锁被占用 && 当前线程不是占有锁的线程——>写锁被其他线程占用时，读锁的获取被阻塞
			if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current)
				return -1;// -1表示失败
			int r = sharedCount(c);
			// 如果：读线程不需要被阻塞(当头结点的后继结点为独占模式 将会阻塞，为了不产生脏读，要考虑之前进入的写操作) &&
			// 读锁提取数没有饱和 && CAS(注意此处 c+SHARED_UNIT)
			if (!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) {
				if (r == 0) {// 读锁的提取数为0（当前线程占用之前，读锁未被占用）
					firstReader = current;// 当前线程称为第一个读线程
					firstReaderHoldCount = 1;
				} else if (firstReader == current) {// (这个锁已经被其他读线程共享了)当前线程是否是第一个共享读写线程
					firstReaderHoldCount++;// 首读线程计数+1，代表重入！
				} else {// 读锁之前已经被共享了，当前线程不是获取读锁的首个线程
					HoldCounter rh = cachedHoldCounter;
					if (rh == null || rh.tid != getThreadId(current))// cachedHoldCounter不为空||
																		// 当前线程不是缓存的线程
						cachedHoldCounter = rh = readHolds.get();
					// 不为空且是当前线程，并且cahce的锁记录为0，说明当前仅有首线程获取到了锁(可能其他线程已经释放了读锁)
					else if (rh.count == 0)
						readHolds.set(rh);
					rh.count++;// 锁重入情况
				}
				return 1;
			}
			// CAS失败后再次尝试读锁获取
			return fullTryAcquireShared(current);
		}

		/**
		 * Full version of acquire for reads, that handles CAS misses and
		 * reentrant reads not dealt with in tryAcquireShared.
		 */
		final int fullTryAcquireShared(Thread current) {
			/*
			 * This code is in part redundant with that in tryAcquireShared but
			 * is simpler overall by not complicating tryAcquireShared with
			 * interactions between retries and lazily reading hold counts.
			 * 
			 * 
			 */
			HoldCounter rh = null;
			for (;;) {
				int c = getState();
				if (exclusiveCount(c) != 0) {
					if (getExclusiveOwnerThread() != current)
						return -1;
					// else we hold the exclusive lock; blocking here
					// would cause deadlock.
				} else if (readerShouldBlock()) {
					// Make sure we're not acquiring read lock reentrantly
					if (firstReader == current) {
						// assert firstReaderHoldCount > 0;
					} else {// 写锁没有被占用 && 当前读线程需要被阻塞 && 当前线程不是firstReader
						if (rh == null) {// 非重入
							rh = cachedHoldCounter;// rh引用指向把这个锁缓存的holdCounter
							if (rh == null || rh.tid != getThreadId(current)) {// rh==null??
																				// ||
																				// 当前线程的计数器情况没有被缓存过
								rh = readHolds.get();// 获取当前线程ThreadLocal中的计数器情况
								if (rh.count == 0)// 表示当前线程非重入
									readHolds.remove();// 清除当前ThreadLocal存储的值
							}
						}
						if (rh.count == 0)// 首次进入并且需要被阻塞
							return -1;
					}
				}
				if (sharedCount(c) == MAX_COUNT)
					throw new Error("Maximum lock count exceeded");
				if (compareAndSetState(c, c + SHARED_UNIT)) {
					if (sharedCount(c) == 0) {
						firstReader = current;
						firstReaderHoldCount = 1;
					} else if (firstReader == current) {
						firstReaderHoldCount++;
					} else {
						if (rh == null)
							rh = cachedHoldCounter;
						if (rh == null || rh.tid != getThreadId(current))
							rh = readHolds.get();
						else if (rh.count == 0)
							readHolds.set(rh);
						rh.count++;
						cachedHoldCounter = rh; // cache for release
					}
					return 1;
				}
			}
		}

		/**
		 * Performs tryLock for write, enabling barging in both modes. This is
		 * identical in effect to tryAcquire except for lack of calls to
		 * writerShouldBlock.
		 */
		final boolean tryWriteLock() {
			Thread current = Thread.currentThread();
			int c = getState();
			if (c != 0) {
				int w = exclusiveCount(c);
				if (w == 0 || current != getExclusiveOwnerThread())
					return false;
				if (w == MAX_COUNT)
					throw new Error("Maximum lock count exceeded");
			}
			if (!compareAndSetState(c, c + 1))
				return false;
			setExclusiveOwnerThread(current);
			return true;
		}

		/**
		 * Performs tryLock for read, enabling barging in both modes. This is
		 * identical in effect to tryAcquireShared except for lack of calls to
		 * readerShouldBlock.
		 */
		final boolean tryReadLock() {
			Thread current = Thread.currentThread();
			for (;;) {
				int c = getState();
				if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current)
					return false;
				int r = sharedCount(c);
				if (r == MAX_COUNT)
					throw new Error("Maximum lock count exceeded");
				if (compareAndSetState(c, c + SHARED_UNIT)) {
					if (r == 0) {
						firstReader = current;
						firstReaderHoldCount = 1;
					} else if (firstReader == current) {
						firstReaderHoldCount++;
					} else {
						HoldCounter rh = cachedHoldCounter;
						if (rh == null || rh.tid != getThreadId(current))
							cachedHoldCounter = rh = readHolds.get();
						else if (rh.count == 0)
							readHolds.set(rh);
						rh.count++;
					}
					return true;
				}
			}
		}

		protected final boolean isHeldExclusively() {
			// While we must in general read state before owner,
			// we don't need to do so to check if current thread is owner
			return getExclusiveOwnerThread() == Thread.currentThread();
		}

		// Methods relayed to outer class

		final ConditionObject newCondition() {
			return new ConditionObject();
		}

		final Thread getOwner() {
			// Must read state before owner to ensure memory consistency
			return ((exclusiveCount(getState()) == 0) ? null : getExclusiveOwnerThread());
		}

		final int getReadLockCount() {
			return sharedCount(getState());
		}

		final boolean isWriteLocked() {
			return exclusiveCount(getState()) != 0;
		}

		final int getWriteHoldCount() {
			return isHeldExclusively() ? exclusiveCount(getState()) : 0;
		}

		final int getReadHoldCount() {
			if (getReadLockCount() == 0)
				return 0;

			Thread current = Thread.currentThread();
			if (firstReader == current)
				return firstReaderHoldCount;

			HoldCounter rh = cachedHoldCounter;
			if (rh != null && rh.tid == getThreadId(current))
				return rh.count;

			int count = readHolds.get().count;
			if (count == 0)
				readHolds.remove();
			return count;
		}

		/**
		 * Reconstitutes the instance from a stream (that is, deserializes it).
		 */
		private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
			s.defaultReadObject();
			readHolds = new ThreadLocalHoldCounter();
			setState(0); // reset to unlocked state
		}

		final int getCount() {
			return getState();
		}
	}

	/**
	 * Nonfair version of Sync
	 */
	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = -8159625535654395037L;

		final boolean writerShouldBlock() {
			return false; // writers can always barge
		}

		final boolean readerShouldBlock() {
			/*
			 * As a heurireaderShouldBlock return (h = head) != null && (s =
			 * h.next) != null && !s.isShared() && s.thread != null; 如果头结点不为空
			 * &&头结点后继结点也不为空 &&后继结点不是共享模式的(读线程)
			 * &&后继结点线程不空——>总之：也就是要满足队列的第二个结点是独占模式的（写线程）则阻塞写线程；
			 * 
			 */
			// return apparentlyFirstQueuedIsExclusive();
			return false;// !!!
		}
	}

	/**
	 * Fair version of Sync
	 */
	static final class FairSync extends Sync {
		private static final long serialVersionUID = -2274990926593161451L;

		final boolean writerShouldBlock() {
			return hasQueuedPredecessors();// 公平模式中，判断是否有等待更久的结点
		}

		final boolean readerShouldBlock() {
			return hasQueuedPredecessors();
		}
	}

	/**
	 * The lock returned by method {@link ReentrantReadWriteLock#readLock}.
	 */
	public static class ReadLock implements Lock, java.io.Serializable {
		private static final long serialVersionUID = -5992448646407690164L;
		private final Sync sync;

		/**
		 * Constructor for use by subclasses
		 *
		 * @param lock
		 *            the outer lock object
		 * @throws NullPointerException
		 *             if the lock is null
		 */
		protected ReadLock(ReentrantReadWriteLock lock) {
			sync = lock.sync;
		}

		/**
		 * Acquires the read lock.
		 *
		 * <p>
		 * Acquires the read lock if the write lock is not held by another
		 * thread and returns immediately.
		 *
		 * <p>
		 * If the write lock is held by another thread then the current thread
		 * becomes disabled for thread scheduling purposes and lies dormant
		 * until the read lock has been acquired.
		 */
		public void lock() {
			sync.acquireShared(1);
		}

		/**
		 * Acquires the read lock unless the current thread is
		 * {@linkplain Thread#interrupt interrupted}.
		 *
		 * <p>
		 * Acquires the read lock if the write lock is not held by another
		 * thread and returns immediately.
		 *
		 * <p>
		 * If the write lock is held by another thread then the current thread
		 * becomes disabled for thread scheduling purposes and lies dormant
		 * until one of two things happens:
		 *
		 * <ul>
		 *
		 * <li>The read lock is acquired by the current thread; or
		 *
		 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
		 * current thread.
		 *
		 * </ul>
		 *
		 * <p>
		 * If the current thread:
		 *
		 * <ul>
		 *
		 * <li>has its interrupted status set on entry to this method; or
		 *
		 * <li>is {@linkplain Thread#interrupt interrupted} while acquiring the
		 * read lock,
		 *
		 * </ul>
		 *
		 * then {@link InterruptedException} is thrown and the current thread's
		 * interrupted status is cleared.
		 *
		 * <p>
		 * In this implementation, as this method is an explicit interruption
		 * point, preference is given to responding to the interrupt over normal
		 * or reentrant acquisition of the lock.
		 *
		 * @throws InterruptedException
		 *             if the current thread is interrupted
		 */
		public void lockInterruptibly() throws InterruptedException {
			sync.acquireSharedInterruptibly(1);
		}

		/**
		 * Acquires the read lock only if the write lock is not held by another
		 * thread at the time of invocation.
		 *
		 * <p>
		 * Acquires the read lock if the write lock is not held by another
		 * thread and returns immediately with the value {@code true}. Even when
		 * this lock has been set to use a fair ordering policy, a call to
		 * {@code tryLock()} <em>will</em> immediately acquire the read lock if
		 * it is available, whether or not other threads are currently waiting
		 * for the read lock. This &quot;barging&quot; behavior can be useful in
		 * certain circumstances, even though it breaks fairness. If you want to
		 * honor the fairness setting for this lock, then use
		 * {@link #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) } which
		 * is almost equivalent (it also detects interruption).
		 *
		 * <p>
		 * If the write lock is held by another thread then this method will
		 * return immediately with the value {@code false}.
		 *
		 * @return {@code true} if the read lock was acquired
		 */
		public boolean tryLock() {
			return sync.tryReadLock();
		}

		/**
		 * Acquires the read lock if the write lock is not held by another
		 * thread within the given waiting time and the current thread has not
		 * been {@linkplain Thread#interrupt interrupted}.
		 *
		 * <p>
		 * Acquires the read lock if the write lock is not held by another
		 * thread and returns immediately with the value {@code true}. If this
		 * lock has been set to use a fair ordering policy then an available
		 * lock <em>will not</em> be acquired if any other threads are waiting
		 * for the lock. This is in contrast to the {@link #tryLock()} method.
		 * If you want a timed {@code tryLock} that does permit barging on a
		 * fair lock then combine the timed and un-timed forms together:
		 *
		 * <pre>
		 *  {@code
		 * if (lock.tryLock() ||
		 *     lock.tryLock(timeout, unit)) {
		 *   ...
		 * }}
		 * </pre>
		 *
		 * <p>
		 * If the write lock is held by another thread then the current thread
		 * becomes disabled for thread scheduling purposes and lies dormant
		 * until one of three things happens:
		 *
		 * <ul>
		 *
		 * <li>The read lock is acquired by the current thread; or
		 *
		 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
		 * current thread; or
		 *
		 * <li>The specified waiting time elapses.
		 *
		 * </ul>
		 *
		 * <p>
		 * If the read lock is acquired then the value {@code true} is returned.
		 *
		 * <p>
		 * If the current thread:
		 *
		 * <ul>
		 *
		 * <li>has its interrupted status set on entry to this method; or
		 *
		 * <li>is {@linkplain Thread#interrupt interrupted} while acquiring the
		 * read lock,
		 *
		 * </ul>
		 * then {@link InterruptedException} is thrown and the current thread's
		 * interrupted status is cleared.
		 *
		 * <p>
		 * If the specified waiting time elapses then the value {@code false} is
		 * returned. If the time is less than or equal to zero, the method will
		 * not wait at all.
		 *
		 * <p>
		 * In this implementation, as this method is an explicit interruption
		 * point, preference is given to responding to the interrupt over normal
		 * or reentrant acquisition of the lock, and over reporting the elapse
		 * of the waiting time.
		 *
		 * @param timeout
		 *            the time to wait for the read lock
		 * @param unit
		 *            the time unit of the timeout argument
		 * @return {@code true} if the read lock was acquired
		 * @throws InterruptedException
		 *             if the current thread is interrupted
		 * @throws NullPointerException
		 *             if the time unit is null
		 */
		public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
			return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
		}

		/**
		 * Attempts to release this lock.
		 *
		 * <p>
		 * If the number of readers is now zero then the lock is made available
		 * for write lock attempts.
		 */
		public void unlock() {
			sync.releaseShared(1);
		}

		/**
		 * Throws {@code UnsupportedOperationException} because
		 * {@code ReadLocks} do not support conditions.
		 *
		 * @throws UnsupportedOperationException
		 *             always
		 */
		public Condition newCondition() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Returns a string identifying this lock, as well as its lock state.
		 * The state, in brackets, includes the String {@code "Read locks ="}
		 * followed by the number of held read locks.
		 *
		 * @return a string identifying this lock, as well as its lock state
		 */
		public String toString() {
			int r = sync.getReadLockCount();
			return super.toString() + "[Read locks = " + r + "]";
		}
	}

	/**
	 * The lock returned by method {@link ReentrantReadWriteLock#writeLock}.
	 */
	public static class WriteLock implements Lock, java.io.Serializable {
		private static final long serialVersionUID = -4992448646407690164L;
		private final Sync sync;

		/**
		 * Constructor for use by subclasses
		 *
		 * @param lock
		 *            the outer lock object
		 * @throws NullPointerException
		 *             if the lock is null
		 */
		protected WriteLock(ReentrantReadWriteLock lock) {
			sync = lock.sync;
		}

		/**
		 * Acquires the write lock.
		 *
		 * <p>
		 * Acquires the write lock if neither the read nor write lock are held
		 * by another thread and returns immediately, setting the write lock
		 * hold count to one.
		 *
		 * <p>
		 * If the current thread already holds the write lock then the hold
		 * count is incremented by one and the method returns immediately.
		 *
		 * <p>
		 * If the lock is held by another thread then the current thread becomes
		 * disabled for thread scheduling purposes and lies dormant until the
		 * write lock has been acquired, at which time the write lock hold count
		 * is set to one.
		 */
		public void lock() {
			sync.acquire(1);
		}

		/**
		 * Acquires the write lock unless the current thread is
		 * {@linkplain Thread#interrupt interrupted}.
		 *
		 * <p>
		 * Acquires the write lock if neither the read nor write lock are held
		 * by another thread and returns immediately, setting the write lock
		 * hold count to one.
		 *
		 * <p>
		 * If the current thread already holds this lock then the hold count is
		 * incremented by one and the method returns immediately.
		 *
		 * <p>
		 * If the lock is held by another thread then the current thread becomes
		 * disabled for thread scheduling purposes and lies dormant until one of
		 * two things happens:
		 *
		 * <ul>
		 *
		 * <li>The write lock is acquired by the current thread; or
		 *
		 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
		 * current thread.
		 *
		 * </ul>
		 *
		 * <p>
		 * If the write lock is acquired by the current thread then the lock
		 * hold count is set to one.
		 *
		 * <p>
		 * If the current thread:
		 *
		 * <ul>
		 *
		 * <li>has its interrupted status set on entry to this method; or
		 *
		 * <li>is {@linkplain Thread#interrupt interrupted} while acquiring the
		 * write lock,
		 *
		 * </ul>
		 *
		 * then {@link InterruptedException} is thrown and the current thread's
		 * interrupted status is cleared.
		 *
		 * <p>
		 * In this implementation, as this method is an explicit interruption
		 * point, preference is given to responding to the interrupt over normal
		 * or reentrant acquisition of the lock.
		 *
		 * @throws InterruptedException
		 *             if the current thread is interrupted
		 */
		public void lockInterruptibly() throws InterruptedException {
			sync.acquireInterruptibly(1);
		}

		/**
		 * Acquires the write lock only if it is not held by another thread at
		 * the time of invocation.
		 *
		 * <p>
		 * Acquires the write lock if neither the read nor write lock are held
		 * by another thread and returns immediately with the value
		 * {@code true}, setting the write lock hold count to one. Even when
		 * this lock has been set to use a fair ordering policy, a call to
		 * {@code tryLock()} <em>will</em> immediately acquire the lock if it is
		 * available, whether or not other threads are currently waiting for the
		 * write lock. This &quot;barging&quot; behavior can be useful in
		 * certain circumstances, even though it breaks fairness. If you want to
		 * honor the fairness setting for this lock, then use
		 * {@link #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) } which
		 * is almost equivalent (it also detects interruption).
		 *
		 * <p>
		 * If the current thread already holds this lock then the hold count is
		 * incremented by one and the method returns {@code true}.
		 *
		 * <p>
		 * If the lock is held by another thread then this method will return
		 * immediately with the value {@code false}.
		 *
		 * @return {@code true} if the lock was free and was acquired by the
		 *         current thread, or the write lock was already held by the
		 *         current thread; and {@code false} otherwise.
		 */
		public boolean tryLock() {
			return sync.tryWriteLock();
		}

		/**
		 * Acquires the write lock if it is not held by another thread within
		 * the given waiting time and the current thread has not been
		 * {@linkplain Thread#interrupt interrupted}.
		 *
		 * <p>
		 * Acquires the write lock if neither the read nor write lock are held
		 * by another thread and returns immediately with the value
		 * {@code true}, setting the write lock hold count to one. If this lock
		 * has been set to use a fair ordering policy then an available lock
		 * <em>will not</em> be acquired if any other threads are waiting for
		 * the write lock. This is in contrast to the {@link #tryLock()} method.
		 * If you want a timed {@code tryLock} that does permit barging on a
		 * fair lock then combine the timed and un-timed forms together:
		 *
		 * <pre>
		 *  {@code
		 * if (lock.tryLock() ||
		 *     lock.tryLock(timeout, unit)) {
		 *   ...
		 * }}
		 * </pre>
		 *
		 * <p>
		 * If the current thread already holds this lock then the hold count is
		 * incremented by one and the method returns {@code true}.
		 *
		 * <p>
		 * If the lock is held by another thread then the current thread becomes
		 * disabled for thread scheduling purposes and lies dormant until one of
		 * three things happens:
		 *
		 * <ul>
		 *
		 * <li>The write lock is acquired by the current thread; or
		 *
		 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
		 * current thread; or
		 *
		 * <li>The specified waiting time elapses
		 *
		 * </ul>
		 *
		 * <p>
		 * If the write lock is acquired then the value {@code true} is returned
		 * and the write lock hold count is set to one.
		 *
		 * <p>
		 * If the current thread:
		 *
		 * <ul>
		 *
		 * <li>has its interrupted status set on entry to this method; or
		 *
		 * <li>is {@linkplain Thread#interrupt interrupted} while acquiring the
		 * write lock,
		 *
		 * </ul>
		 *
		 * then {@link InterruptedException} is thrown and the current thread's
		 * interrupted status is cleared.
		 *
		 * <p>
		 * If the specified waiting time elapses then the value {@code false} is
		 * returned. If the time is less than or equal to zero, the method will
		 * not wait at all.
		 *
		 * <p>
		 * In this implementation, as this method is an explicit interruption
		 * point, preference is given to responding to the interrupt over normal
		 * or reentrant acquisition of the lock, and over reporting the elapse
		 * of the waiting time.
		 *
		 * @param timeout
		 *            the time to wait for the write lock
		 * @param unit
		 *            the time unit of the timeout argument
		 *
		 * @return {@code true} if the lock was free and was acquired by the
		 *         current thread, or the write lock was already held by the
		 *         current thread; and {@code false} if the waiting time elapsed
		 *         before the lock could be acquired.
		 *
		 * @throws InterruptedException
		 *             if the current thread is interrupted
		 * @throws NullPointerException
		 *             if the time unit is null
		 */
		public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
			return sync.tryAcquireNanos(1, unit.toNanos(timeout));
		}

		/**
		 * Attempts to release this lock.
		 *
		 * <p>
		 * If the current thread is the holder of this lock then the hold count
		 * is decremented. If the hold count is now zero then the lock is
		 * released. If the current thread is not the holder of this lock then
		 * {@link IllegalMonitorStateException} is thrown.
		 *
		 * @throws IllegalMonitorStateException
		 *             if the current thread does not hold this lock
		 */
		public void unlock() {
			sync.release(1);
		}

		/**
		 * Returns a {@link Condition} instance for use with this {@link Lock}
		 * instance.
		 * <p>
		 * The returned {@link Condition} instance supports the same usages as
		 * do the {@link Object} monitor methods ({@link Object#wait() wait},
		 * {@link Object#notify notify}, and {@link Object#notifyAll notifyAll})
		 * when used with the built-in monitor lock.
		 *
		 * <ul>
		 *
		 * <li>If this write lock is not held when any {@link Condition} method
		 * is called then an {@link IllegalMonitorStateException} is thrown.
		 * (Read locks are held independently of write locks, so are not checked
		 * or affected. However it is essentially always an error to invoke a
		 * condition waiting method when the current thread has also acquired
		 * read locks, since other threads that could unblock it will not be
		 * able to acquire the write lock.)
		 *
		 * <li>When the condition {@linkplain Condition#await() waiting} methods
		 * are called the write lock is released and, before they return, the
		 * write lock is reacquired and the lock hold count restored to what it
		 * was when the method was called.
		 *
		 * <li>If a thread is {@linkplain Thread#interrupt interrupted} while
		 * waiting then the wait will terminate, an {@link InterruptedException}
		 * will be thrown, and the thread's interrupted status will be cleared.
		 *
		 * <li>Waiting threads are signalled in FIFO order.
		 *
		 * <li>The ordering of lock reacquisition for threads returning from
		 * waiting methods is the same as for threads initially acquiring the
		 * lock, which is in the default case not specified, but for
		 * <em>fair</em> locks favors those threads that have been waiting the
		 * longest.
		 *
		 * </ul>
		 *
		 * @return the Condition object
		 */
		public Condition newCondition() {
			return sync.newCondition();
		}

		/**
		 * Returns a string identifying this lock, as well as its lock state.
		 * The state, in brackets includes either the String {@code "Unlocked"}
		 * or the String {@code "Locked by"} followed by the
		 * {@linkplain Thread#getName name} of the owning thread.
		 *
		 * @return a string identifying this lock, as well as its lock state
		 */
		public String toString() {
			Thread o = sync.getOwner();
			return super.toString() + ((o == null) ? "[Unlocked]" : "[Locked by thread " + o.getName() + "]");
		}

		/**
		 * Queries if this write lock is held by the current thread. Identical
		 * in effect to
		 * {@link ReentrantReadWriteLock#isWriteLockedByCurrentThread}.
		 *
		 * @return {@code true} if the current thread holds this lock and
		 *         {@code false} otherwise
		 * @since 1.6
		 */
		public boolean isHeldByCurrentThread() {
			return sync.isHeldExclusively();
		}

		/**
		 * Queries the number of holds on this write lock by the current thread.
		 * A thread has a hold on a lock for each lock action that is not
		 * matched by an unlock action. Identical in effect to
		 * {@link ReentrantReadWriteLock#getWriteHoldCount}.
		 *
		 * @return the number of holds on this lock by the current thread, or
		 *         zero if this lock is not held by the current thread
		 * @since 1.6
		 */
		public int getHoldCount() {
			return sync.getWriteHoldCount();
		}
	}

	// Instrumentation and status

	/**
	 * Returns {@code true} if this lock has fairness set true.
	 *
	 * @return {@code true} if this lock has fairness set true
	 */
	public final boolean isFair() {
		return sync instanceof FairSync;
	}

	/**
	 * Returns the thread that currently owns the write lock, or {@code null} if
	 * not owned. When this method is called by a thread that is not the owner,
	 * the return value reflects a best-effort approximation of current lock
	 * status. For example, the owner may be momentarily {@code null} even if
	 * there are threads trying to acquire the lock but have not yet done so.
	 * This method is designed to facilitate construction of subclasses that
	 * provide more extensive lock monitoring facilities.
	 *
	 * @return the owner, or {@code null} if not owned
	 */
	protected Thread getOwner() {
		return sync.getOwner();
	}

	/**
	 * Queries the number of read locks held for this lock. This method is
	 * designed for use in monitoring system state, not for synchronization
	 * control.
	 * 
	 * @return the number of read locks held
	 */
	public int getReadLockCount() {
		return sync.getReadLockCount();
	}

	/**
	 * Queries if the write lock is held by any thread. This method is designed
	 * for use in monitoring system state, not for synchronization control.
	 *
	 * @return {@code true} if any thread holds the write lock and {@code false}
	 *         otherwise
	 */
	public boolean isWriteLocked() {
		return sync.isWriteLocked();
	}

	/**
	 * Queries if the write lock is held by the current thread.
	 *
	 * @return {@code true} if the current thread holds the write lock and
	 *         {@code false} otherwise
	 */
	public boolean isWriteLockedByCurrentThread() {
		return sync.isHeldExclusively();
	}

	/**
	 * Queries the number of reentrant write holds on this lock by the current
	 * thread. A writer thread has a hold on a lock for each lock action that is
	 * not matched by an unlock action.
	 *
	 * @return the number of holds on the write lock by the current thread, or
	 *         zero if the write lock is not held by the current thread
	 */
	public int getWriteHoldCount() {
		return sync.getWriteHoldCount();
	}

	/**
	 * Queries the number of reentrant read holds on this lock by the current
	 * thread. A reader thread has a hold on a lock for each lock action that is
	 * not matched by an unlock action.
	 *
	 * @return the number of holds on the read lock by the current thread, or
	 *         zero if the read lock is not held by the current thread
	 * @since 1.6
	 */
	public int getReadHoldCount() {
		return sync.getReadHoldCount();
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire
	 * the write lock. Because the actual set of threads may change dynamically
	 * while constructing this result, the returned collection is only a
	 * best-effort estimate. The elements of the returned collection are in no
	 * particular order. This method is designed to facilitate construction of
	 * subclasses that provide more extensive lock monitoring facilities.
	 *
	 * @return the collection of threads
	 */
	protected Collection<Thread> getQueuedWriterThreads() {
		return sync.getExclusiveQueuedThreads();
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire
	 * the read lock. Because the actual set of threads may change dynamically
	 * while constructing this result, the returned collection is only a
	 * best-effort estimate. The elements of the returned collection are in no
	 * particular order. This method is designed to facilitate construction of
	 * subclasses that provide more extensive lock monitoring facilities.
	 *
	 * @return the collection of threads
	 */
	protected Collection<Thread> getQueuedReaderThreads() {
		return sync.getSharedQueuedThreads();
	}

	/**
	 * Queries whether any threads are waiting to acquire the read or write
	 * lock. Note that because cancellations may occur at any time, a
	 * {@code true} return does not guarantee that any other thread will ever
	 * acquire a lock. This method is designed primarily for use in monitoring
	 * of the system state.
	 *
	 * @return {@code true} if there may be other threads waiting to acquire the
	 *         lock
	 */
	public final boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}

	/**
	 * Queries whether the given thread is waiting to acquire either the read or
	 * write lock. Note that because cancellations may occur at any time, a
	 * {@code true} return does not guarantee that this thread will ever acquire
	 * a lock. This method is designed primarily for use in monitoring of the
	 * system state.
	 *
	 * @param thread
	 *            the thread
	 * @return {@code true} if the given thread is queued waiting for this lock
	 * @throws NullPointerException
	 *             if the thread is null
	 */
	public final boolean hasQueuedThread(Thread thread) {
		return sync.isQueued(thread);
	}

	/**
	 * Returns an estimate of the number of threads waiting to acquire either
	 * the read or write lock. The value is only an estimate because the number
	 * of threads may change dynamically while this method traverses internal
	 * data structures. This method is designed for use in monitoring of the
	 * system state, not for synchronization control.
	 *
	 * @return the estimated number of threads waiting for this lock
	 */
	public final int getQueueLength() {
		return sync.getQueueLength();
	}

	/**
	 * Returns a collection containing threads that may be waiting to acquire
	 * either the read or write lock. Because the actual set of threads may
	 * change dynamically while constructing this result, the returned
	 * collection is only a best-effort estimate. The elements of the returned
	 * collection are in no particular order. This method is designed to
	 * facilitate construction of subclasses that provide more extensive
	 * monitoring facilities.
	 *
	 * @return the collection of threads
	 */
	protected Collection<Thread> getQueuedThreads() {
		return sync.getQueuedThreads();
	}

	/**
	 * Queries whether any threads are waiting on the given condition associated
	 * with the write lock. Note that because timeouts and interrupts may occur
	 * at any time, a {@code true} return does not guarantee that a future
	 * {@code signal} will awaken any threads. This method is designed primarily
	 * for use in monitoring of the system state.
	 *
	 * @param condition
	 *            the condition
	 * @return {@code true} if there are any waiting threads
	 * @throws IllegalMonitorStateException
	 *             if this lock is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this lock
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public boolean hasWaiters(Condition condition) {
		if (condition == null)
			throw new NullPointerException();
		if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
			throw new IllegalArgumentException("not owner");
		return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) condition);
	}

	/**
	 * Returns an estimate of the number of threads waiting on the given
	 * condition associated with the write lock. Note that because timeouts and
	 * interrupts may occur at any time, the estimate serves only as an upper
	 * bound on the actual number of waiters. This method is designed for use in
	 * monitoring of the system state, not for synchronization control.
	 *
	 * @param condition
	 *            the condition
	 * @return the estimated number of waiting threads
	 * @throws IllegalMonitorStateException
	 *             if this lock is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this lock
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	public int getWaitQueueLength(Condition condition) {
		if (condition == null)
			throw new NullPointerException();
		if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
			throw new IllegalArgumentException("not owner");
		return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) condition);
	}

	/**
	 * Returns a collection containing those threads that may be waiting on the
	 * given condition associated with the write lock. Because the actual set of
	 * threads may change dynamically while constructing this result, the
	 * returned collection is only a best-effort estimate. The elements of the
	 * returned collection are in no particular order. This method is designed
	 * to facilitate construction of subclasses that provide more extensive
	 * condition monitoring facilities.
	 *
	 * @param condition
	 *            the condition
	 * @return the collection of threads
	 * @throws IllegalMonitorStateException
	 *             if this lock is not held
	 * @throws IllegalArgumentException
	 *             if the given condition is not associated with this lock
	 * @throws NullPointerException
	 *             if the condition is null
	 */
	protected Collection<Thread> getWaitingThreads(Condition condition) {
		if (condition == null)
			throw new NullPointerException();
		if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
			throw new IllegalArgumentException("not owner");
		return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject) condition);
	}

	/**
	 * Returns a string identifying this lock, as well as its lock state. The
	 * state, in brackets, includes the String {@code "Write locks ="} followed
	 * by the number of reentrantly held write locks, and the String
	 * {@code "Read locks ="} followed by the number of held read locks.
	 *
	 * @return a string identifying this lock, as well as its lock state
	 */
	public String toString() {
		int c = sync.getCount();
		int w = Sync.exclusiveCount(c);
		int r = Sync.sharedCount(c);

		return super.toString() + "[Write locks = " + w + ", Read locks = " + r + "]";
	}

	/**
	 * Returns the thread id for the given thread. We must access this directly
	 * rather than via method Thread.getId() because getId() is not final, and
	 * has been known to be overridden in ways that do not preserve unique
	 * mappings.
	 */
	static final long getThreadId(Thread thread) {
		return UNSAFE.getLongVolatile(thread, TID_OFFSET);
	}

	// Unsafe mechanics
	private static final sun.misc.Unsafe UNSAFE;
	private static final long TID_OFFSET;
	static {
		try {
			UNSAFE = sun.misc.Unsafe.getUnsafe();
			Class<?> tk = Thread.class;
			TID_OFFSET = UNSAFE.objectFieldOffset(tk.getDeclaredField("tid"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}
