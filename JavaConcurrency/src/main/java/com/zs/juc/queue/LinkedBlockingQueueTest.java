package com.zs.juc.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LinkedBlockingQueueTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService ex = Executors.newFixedThreadPool(50);

        LinkedBlockingQueue<CustomizedTask> tasksQueue = new LinkedBlockingQueue<CustomizedTask>(100);
        // 生产者线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        tasksQueue.put(new CustomizedTask());
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // 消费者线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomizedTask task;
                try {
                    while ((task = tasksQueue.take()) != null && !Thread.currentThread().isInterrupted()) {
                        ex.submit(task);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        System.out.println("Main Thread is terminated");
    }

    static class CustomizedTask implements Runnable {
        @Override
        public void run() {
            System.out.println(System.currentTimeMillis());
        }
    }
}
