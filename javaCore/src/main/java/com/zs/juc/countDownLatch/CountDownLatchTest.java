package com.zs.juc.countDownLatch;

public class CountDownLatchTest {
    int count=
    static CountDownLatch countDownLatch = new CountDownLatch(2);

    public static void main(String[] args) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("-1");
                countDownLatch.countDown();
                System.out.println("-1");
                countDownLatch.countDown();
            }
        }).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("others terminate");
            }
        }).start();
        countDownLatch.await();
        System.out.println("main terminate");
    }
}