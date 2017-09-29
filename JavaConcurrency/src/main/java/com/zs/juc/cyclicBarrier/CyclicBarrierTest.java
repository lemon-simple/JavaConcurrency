package com.zs.juc.cyclicBarrier;

import java.util.concurrent.TimeUnit;

public class CyclicBarrierTest {
    static CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cyclicBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(1);
            }
        });
        t1.start();

        TimeUnit.SECONDS.sleep(3);
        t1.interrupt();

        System.out.println(2);
    }
}