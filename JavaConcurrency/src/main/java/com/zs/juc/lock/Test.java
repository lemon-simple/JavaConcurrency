/*
 * Copyright (C) 2014-2016 Omniprime All rights reserved
 * Author: zhangsh
 * Date: 2017年7月4日
 * Description:Test.java 
 */
package com.zs.juc.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhangsh
 *
 */
public class Test {
    static ReentrantLock lock = new ReentrantLock();

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

    private static final int COUNT_BITS = Integer.SIZE - 3;

    public static void main(String[] args) throws InterruptedException {

        retry:

        for (int i = 0; i < 10; i++) {

            for (int j = 11; j < 15; j++) {

                if (j == 11) {
                    break retry;
                }
                System.out.println("j" + j);

            }
            System.out.println("i" + i);
        }

        // test();
        // testExtractCounter();

        // testThreadLocalReference();

        // 构造FIFO队列：|readLock-lock|writeLock-lock|readLock-lock|readLock-lock|
        ReadWriteLock wrLock = new ReentrantReadWriteLock();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("first");
                wrLock.readLock().lock();
            }
        }, "01 to readLock lock").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("first");
                wrLock.readLock().lock();
            }
        }, "02 to readLock lock").start();
        TimeUnit.SECONDS.sleep(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                wrLock.writeLock().lock();
            }
        }, "03 to writeLock lock").start();
        TimeUnit.SECONDS.sleep(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                wrLock.readLock().lock();
            }
        }, "04 to readLock lock").start();
        TimeUnit.SECONDS.sleep(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                wrLock.readLock().lock();
            }
        }, "05 to readLock lock").start();
        TimeUnit.SECONDS.sleep(1);

    }

    private static void testThreadLocalReference() {
        HoldCounter b = null;
        ThreadLocalHoldCounter threadHolder = new ThreadLocalHoldCounter();
        HoldCounter c = threadHolder.get();
        b = c;
        threadHolder.remove();
        System.out.println("HoldCounter c.count " + c.count);
        c.count++;
        System.out.println("HoldCounter c.count " + c.count);

        System.out.println("HoldCounter b.count " + b.count);

    }

    static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
        public HoldCounter initialValue() {
            return new HoldCounter();
        }
    }

    static final class HoldCounter {
        int count = 0;
    }

    public static Runnable getRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                lock.lock();
                try {
                    System.out.println("locked by " + Thread.currentThread().getName());
                } finally {
                    lock.unlock();
                }
            }
        };
    }

    private static void test() {
        ExecutorService es = Executors.newFixedThreadPool(5);

        lock.lock();
        for (int i = 0; i < 5; i++) {
            es.execute(getRunnable());
        }
    }

    private static void testExtractCounter() {
        System.out.println(sharedCount(SHARED_UNIT));
        System.out.println(exclusiveCount(SHARED_UNIT));
        System.out.println(SHARED_UNIT);
    }
}
