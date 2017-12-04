package com.zs.juc.SemaphoreExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExchangerTest {

    private static final Exchanger<List<String>> exgr = new Exchanger<List<String>>();

    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    private static Random rand = new Random(47);

    private static Runnable producerProductMsg() {
        return new Runnable() {

            @Override
            public void run() {
                List<String> producerMsgX = new ArrayList<String>();
                for (int i = 0; i < 10; i++) {
                    producerMsgX.add(i + rand.nextInt(100) + "");
                }
                try {
                    // 生产,阻塞
                    // Waits for another thread to arrive at this exchange
                    // point
                    exgr.exchange(producerMsgX);
                    System.out.println("	ThreadName:" + Thread.currentThread().getName() + "	producerProductMsg："
                            + producerMsgX);

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        };
    }

    private static Runnable consumerConsumeMsg() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> getMsgFromProducer = exgr.exchange(new ArrayList<String>());
                    System.out.println("	ThreadName:" + Thread.currentThread().getName() + "	consumerConsumeMsg："
                            + getMsgFromProducer);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        };
    }

    public static void main(String[] args) {

        // 消息生成者
        threadPool.execute(producerProductMsg());
        // 消费者 消费
        threadPool.execute(consumerConsumeMsg());
        threadPool.shutdown();
    }
}