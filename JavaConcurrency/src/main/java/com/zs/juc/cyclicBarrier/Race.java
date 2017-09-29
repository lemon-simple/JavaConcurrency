package com.zs.juc.cyclicBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Player implements Runnable {

    private static int counter = 0;

    private int id = counter++;

    private int moveDistance;

    private static CyclicBarrier barrier;

    private static Random random = new Random(47);

    public Player(CyclicBarrier b) {
        barrier = b;
    }

    public int getMoveDistance() {
        return moveDistance;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            moveDistance += random.nextInt(3);
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "" + id;
    }

}

public class Race {
    private int playerNum = 5;

    private int finalLine = 20;

    private CyclicBarrier barrier;// 唯一的CyclicBarrier对象

    private List<Player> players = new ArrayList<Player>();

    private ExecutorService es = Executors.newCachedThreadPool();

    public Race() {

        // 1 构建CyclicBarrier(int parties, Runnable barrierAction)
        // 当n个player都在这个barrier上调用了barrier.await()，这个barrier
        // 对象将重置，并使用最后一个调用await的线程去运行barrierAction，
        // 唤醒所有wait的线程,
        // 然后再一次等待nHourse全部执行await....周而复始；直到shutdownNow
        // 查看CyclicBarrier构造器的JDoc，有详细描述
        barrier = new CyclicBarrier(playerNum, new Runnable() {
            @Override
            public void run() {
                // 1.打印赛道
                StringBuffer raceLine = new StringBuffer();
                raceLine.append(" ");
                for (int i = 0; i < finalLine; i++) {
                    raceLine.append("=");
                }
                System.out.println(" 100米男子赛道");
                System.out.println(raceLine);

                // 2.打印当前参赛者行进位置
                for (Player player : players) {
                    StringBuffer playerLocation = new StringBuffer();
                    playerLocation.append(player);
                    for (int i = 0; i < player.getMoveDistance(); i++) {
                        playerLocation.append("*");
                    }
                    System.out.println(playerLocation);

                }
                // 3.检查是否有参赛者完成比赛，如果是，结束比赛;
                // (会存在多个player同时越过终点)
                for (Player player : players) {
                    if (player.getMoveDistance() >= finalLine) {
                        System.out.println("bravo: player" + player + " won!!! ");
                        es.shutdownNow();// 该方法并不保证池中所有线程退出
                        return;
                    }

                }
                try {
                    TimeUnit.MILLISECONDS.sleep(80);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // 2.任务执行:所有player开始跑
        for (int i = 0; i < playerNum; i++) {
            // 每个Player中传入同一个CyclicBarrier对象
            Player p = new Player(barrier);
            players.add(p);
            es.execute(p);
        }
    }

    public static void main(String[] args) {
        new Race();

    }

}