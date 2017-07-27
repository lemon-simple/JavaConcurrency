package com.zs.juc.collection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConcurrentHashMapTest {
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    public static void main(String[] args) throws InterruptedException {
        System.out.println((16 >>> 2));
        //
        @SuppressWarnings("unused")
        Map<String, String> cm = new ConcurrentHashMap<String, String>();
        //
        // System.out.println(cm.get("java.lang.ThreadGroup"));
        //
        // // cm.put(null, "1");
        //
        int i = 0;
        for (; i < Integer.MAX_VALUE; i++) {
            System.out.println(cm.get("java.lang.ThreadGroup"));
            System.out.println(i);
            cm.put("key_" + i, "huaizuo_" + i);
        }
        cm.put("key_" + i, "huaizuo_" + i);

    }

    /**
     * Returns a power of two table size for the given desired capacity. See
     * Hackers Delight, sec 3.2
     */
    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
}