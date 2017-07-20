/*
 * Copyright (C) 2014-2016 Omniprime All rights reserved
 * Author: zhangsh
 * Date: 2017年7月7日
 * Description:TwinsLocks.java 
 */
package com.zs.juc.lock.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author zhangsh
 *
 */
public class TwinsLocks_exclusive implements Lock {

    private final Sync sync = new Sync(2);

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        Sync(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("count must large  than zero.");
            }
            setState(count);
        }

        // @Override
        // protected int tryAcquireShared(int arg) {
        // while (0 < getState()) {
        // return (compareAndSetState(2, 1) || compareAndSetState(1, 0)) ? 0 :
        // -1;
        // // System.out.println(Thread.currentThread().getName() + " i: "
        // // + i);
        // // return i;
        // }
        // // System.out.println(Thread.currentThread().getName() + " i: " +
        // // -1);
        //
        // return -1;
        // }
        @Override
        protected boolean tryAcquire(int arg) {
            while (0 < getState()) {
                return (compareAndSetState(2, 1) || compareAndSetState(1, 0));
                // System.out.println(Thread.currentThread().getName() + " i: "
                // + i);
                // return i;
            }
            // System.out.println(Thread.currentThread().getName() + " i: " +
            // -1);

            return false;
        }

        protected boolean tryRelease(int arg) {
            while (2 > getState()) {// 有锁被获取中
                return (compareAndSetState(0, 1) || compareAndSetState(1, 2));
            }
            return false;
        }
    }

    public void lock() {
        sync.acquire(1);

    }

    public void unlock() {
        sync.release(1);

    }

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        test();
    }

    public static void test() throws InterruptedException {
        final Lock lock = new TwinsLocks_exclusive();

        for (int i = 0; i < 100; i++)
            new Thread(getRunnable(lock)).start();

    }

    /**
     * @param lock
     * @return
     */
    private static Runnable getRunnable(final Lock lock) {
        return new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    lock.lock();
                    System.out.println("[" + Thread.currentThread().getName() + "]----------something----------"
                            + System.currentTimeMillis());
                    try {
                        TimeUnit.SECONDS.sleep(6);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lock.unlock();
                }
            }
        };
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
