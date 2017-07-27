package com.zs.juc.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ArrayBlockingQueueTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService ex = Executors.newFixedThreadPool(50);

        ArrayBlockingQueue<CustomizedTask> tasksQueue = new ArrayBlockingQueue<CustomizedTask>(10);
        // 生产者线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        tasksQueue.put(new CustomizedTask());
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
                        TimeUnit.SECONDS.sleep(1);
                        ex.submit(task);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println(
                            "---------------------------------------------------------------------------------------");
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (Object o : tasksQueue.toArray()) {
                        System.out.print(o + "    ");
                    }
                    System.out.println(
                            "---------------------------------------------------------------------------------------");

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
