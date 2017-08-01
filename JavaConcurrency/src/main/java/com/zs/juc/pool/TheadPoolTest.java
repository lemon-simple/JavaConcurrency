package com.zs.juc.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TheadPoolTest {
    private static final int COUNT_BITS = Integer.SIZE - 3;

    private static final int CAPACITY = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    private static final int RUNNING = -1 << COUNT_BITS;

    private static final int SHUTDOWN = 0 << COUNT_BITS;

    private static final int STOP = 1 << COUNT_BITS;

    private static final int TIDYING = 2 << COUNT_BITS;

    private static final int TERMINATED = 3 << COUNT_BITS;

    public static void main(String[] args) throws InterruptedException {

        ExecutorService service = Executors.newFixedThreadPool(2);
        // ExecutorService service = Executors.newCachedThreadPool();
        // ExecutorService service = Executors.newWorkStealingPool();

        for (int i = 0; i < 10; i++) {
            service.submit(getTask());
        }
    }

    private static Runnable getTask() {
        return new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println(Thread.currentThread().getId());
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
