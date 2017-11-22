public class ConditionTest {
	volatile int state = 0;

	public static void main(String[] args) throws InterruptedException {
		char a = 'A';
		for (int i = 0; i < 100; i++) {
			System.out.print(i+"---");
			System.out.print(String.valueOf((i + a + 1)).hashCode() & 64);
			System.out.print("----");
			System.out.println(String.valueOf((i + a + 1)).hashCode() & 63);
		}

	}

	//
	// ReadWriteLock lock = new ReentrantReadWriteLock();
	// Lock writeLock = lock.writeLock();
	// Condition condition = lock.writeLock().newCondition();
	//
	// ExecutorService ex = Executors.newFixedThreadPool(10);
	//
	// for (int i = 0; i < 3; i++){
	// System.out.println(i);
	// ex.submit(getRunnable(writeLock, condition));}
	//
	// TimeUnit.SECONDS.sleep(10);
	// writeLock.lock();
	//
	// try {
	// condition.signalAll();
	// } finally {
	// writeLock.unlock();
	//
	// }
	// System.out.println("ready");
	//
	// }
	//
	// private static Runnable getRunnable(Lock writeLock, Condition condition)
	// {
	// return new Runnable() {
	// @Override
	// public void run() {
	// writeLock.lock();
	// try {
	// condition.await();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// } finally {
	// writeLock.unlock();
	// }
	// System.out.println(Thread.currentThread().getName() + " [active] ");
	// }
	// };
	//
	// }
	//
	// public int getState() {
	// return state;
	// }
	//
	// public void setState(int state) {
	// this.state = state;
	// }

}