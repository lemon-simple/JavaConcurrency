import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConditionTest {
	volatile int state = 0;

	public static void main(String[] args) throws InterruptedException {
		
	
	
	 ReadWriteLock lock = new ReentrantReadWriteLock();
	 Lock writeLock = lock.writeLock();
	 Condition condition = lock.writeLock().newCondition();
	
	 ExecutorService ex = Executors.newFixedThreadPool(10);
	
	 for (int i = 0; i < 3; i++){
		 System.out.println(i);
	 ex.submit(getRunnable(writeLock, condition));}
	
	 TimeUnit.SECONDS.sleep(10);
	 writeLock.lock();
	
	 try {
	 condition.signalAll();
	 } finally {
	 writeLock.unlock();
	
	 }
	 System.out.println("ready");
	
	 }

	private static Runnable getRunnable(Lock writeLock, Condition condition) {
		return new Runnable() {
			@Override
			public void run() {
				writeLock.lock();
				try {
					condition.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					writeLock.unlock();
				}
				System.out.println(Thread.currentThread().getName() + " [active] ");
			}
		};

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}