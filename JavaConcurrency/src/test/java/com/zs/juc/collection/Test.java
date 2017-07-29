package com.zs.juc.collection;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        Map<Integer, Integer> keyToValue = new HashMap<Integer, Integer>();

        for (int i = 0; i < 100000; i++) {
            keyToValue.put(i, i);
            System.out.println(keyToValue);
        }
    }
}
