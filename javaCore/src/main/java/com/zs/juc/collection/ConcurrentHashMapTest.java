package com.zs.juc.collection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConcurrentHashMapTest {
    public static void main(String[] args) throws InterruptedException {

        int ssize = 1;
        while (ssize < 16) {
            ssize <<= 1;
        }

        System.out.println("ConcurrentHashMap 1.7 default segement size: " + ssize);

        // 阅读初始化源码
        final ConcurrentHashMap<String, String> cm = new ConcurrentHashMap<String, String>();
        // printSegementSize(16);
        TimeUnit.SECONDS.sleep(5);
        for (int i = 0; i < 1000000; i++) {
            cm.put("key_" + i, "value_" + i);
            System.err.println(i);
        }
        System.out.println(cm);
        TimeUnit.SECONDS.sleep(5);

        new Thread(new Runnable() {
            @Override
            public void run() {
                cm.put("1fff", "1");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                cm.put("2zzzz", "1");
            }
        }).start();

        //
        // for (int i = 0; i < 50; i++) {
        // //阅读put操作源码
        // cm.put(i+"", i+"");
        //
        // if(cm.size()==11){
        // System.out.println("sleep"+cm);
        // TimeUnit.SECONDS.sleep(7);
        // };
        // }
        ////

        for (int i = 0; i < 12; i++) {
            // 阅读get操作源码
            System.out.println(cm.get("key_" + i));
        }
    }

    public static void printSegementSize(int concurrencyLevel) {
        int ssize = 1;
        int sshift = 0;

        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        System.out.println(ssize);
    }
}
