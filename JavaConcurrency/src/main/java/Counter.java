import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
	volatile int state = 0;

	public static void main(String[] args) throws InterruptedException {
		//
		// Counter c = new Counter();
		// c.getRunnable(c);
		//
		// ExecutorService ex = Executors.newFixedThreadPool(100);
		// for (int i = 0; i < 130; i++)
		// ex.execute(c.getRunnable(c));

		// System.out.println(c.getState());

		ReentrantLock lock = new ReentrantLock(false);
		for (int i = 0; i < 130; i++) {

			lock.lock();
		}
		
		TimeUnit.SECONDS.sleep(10);
		
		

	}

	private Runnable getRunnable(Counter c) {
		return new Runnable() {
			@Override
			public void run() {
				int nowState = c.getState();
				int next = nowState + 1;
				c.setState(next);
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