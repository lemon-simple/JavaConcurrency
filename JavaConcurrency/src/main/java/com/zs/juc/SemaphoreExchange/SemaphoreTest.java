package com.zs.juc.SemaphoreExchange;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SemaphoreTest {
    private static final int THREAD_COUNT = 50;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

    private static Semaphore semaphore = new Semaphore(5);

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            int integer = i;
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire();
                        System.out.println(integer + "  enter" + "availablePermits" + semaphore.availablePermits());

                        TimeUnit.SECONDS.sleep(60);
                        semaphore.release();

                    } catch (InterruptedException e) {
                    }
                }
            });
        }
        threadPool.shutdown();
    }
}