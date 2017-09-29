package com.zs.juc.cyclicBarrier;

import java.util.concurrent.BrokenBarrierException;

public class CyclicBarrierTest2 {
    static int times = 1;

    public static void main(String[] args) {
        int cyclicTimes = 4;
        CyclicBarrier barrier = new CyclicBarrier(cyclicTimes, new Runnable() {

            @Override
            public void run() {
                System.out.println("    Thread Name : " + Thread.currentThread().getName() + "    times : "
                        + CyclicBarrierTest2.times++);

            }
        });

        for (int i = 0; i < cyclicTimes; i++)
            new Writer(barrier, "thread-" + i).start();

        // cyclic重用
        // System.out.println("CyclicBarrier重用");
        //
        // for (int i = 0; i < cyclicTimes; i++)
        // new Writer(barrier).start();

    }

    static class Writer extends Thread {
        private CyclicBarrier cyclicBarrier;

        public Writer(CyclicBarrier cyclicBarrier, String name) {
            super(name);
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            System.out.println("线程" + Thread.currentThread().getName() + "正在写入数据...");
            try {
                Thread.sleep(1); // 以睡眠来模拟写入数据操作
                System.out.println("线程" + Thread.currentThread().getName() + "写入数据完毕，等待其他线程写入完毕");
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println("所有线程写入完毕，继续处理其他任务...");
        }
    }
}