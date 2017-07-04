/*
 * Copyright (C) 2014-2016 Omniprime All rights reserved
 * Author: zhangsh
 * Date: 2017年7月4日
 * Description:DefaultThreadPool.java 
 */
package com.zs.juc.threadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangsh
 *
 */
public class DefaultThreadPool<Job extends Runnable> implements ThreadPool<Job> {
    private static final int MAX_WORKERS_NUMBERS = 10;

    private static final int DEFAULT_WORKER_NUMBERS = 5;

    private static final int MIN_WORKER_NUMBERS = 1;

    private final LinkedList<Job> jobs = new LinkedList<Job>();

    // not good
    AtomicInteger id = new AtomicInteger(0);

    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());

    /**
     * 
     */
    public DefaultThreadPool() {
        new DefaultThreadPool<Job>(DEFAULT_WORKER_NUMBERS);
    }

    public DefaultThreadPool(int num) {
        num = num > MAX_WORKERS_NUMBERS ? MAX_WORKERS_NUMBERS : num < MIN_WORKER_NUMBERS ? MIN_WORKER_NUMBERS : num;

        initializeWorkers(num);
    }

    /**
     * @param num
     */
    private void initializeWorkers(int num) {
        for (int i = 0; i < num; i++) {
            Worker w = new Worker();
            workers.add(w);
            new Thread(w, id.getAndIncrement() + "").start();
        }
    }

    @Override
    public void execute(Job job) {
        if (null != job) {
            synchronized (jobs) {
                jobs.add(job);
                jobs.notify();
            }
        }

    }

    @Override
    public void shutDown() {

        for (Worker worker : workers) {
            worker.shutDown();
        }

    }

    @Override
    public void addWorkers(int num) {
        synchronized (jobs) {
            for (int i = 0; i < num; i++) {
                if (workers.size() <= MAX_WORKERS_NUMBERS) {
                    Worker w = new Worker();
                    workers.add(w);
                    new Thread(w, id.getAndIncrement() + "").start();
                }
            }
        }

    }

    @Override
    public void removeWorker(int num) {
        synchronized (jobs) {
            if (num <= workers.size()) {
                for (int i = 0; i < num; i++) {
                    Worker w = workers.get(0);
                    workers.remove(w);
                    w.shutDown();
                }
            }
        }
    }

    @Override
    public synchronized int getJobSize() {
        return jobs.size();
    }

    class Worker implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            while (running) {
                Job job = null;
                synchronized (jobs) {
                    while (jobs.isEmpty()) {
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    job = jobs.removeFirst();
                }
                if (null != job) {
                    job.run();
                }
            }
        }

        public void shutDown() {
            running = false;
        }

    }

}
