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
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhangsh
 *
 */
public class Test {
    static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 5; i++) {
            es.execute(getRunnable());
        }
        TimeUnit.SECONDS.sleep(30);
    }

    public static Runnable getRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                lock.lock();
                try {
                    while (true) {
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        };
    }

}
