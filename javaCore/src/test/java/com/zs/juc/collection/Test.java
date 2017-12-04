package com.zs.juc.collection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Test {

    public static void main(String[] args) {
        Map<String, String> keyToValue = new HashMap<String, String>();

        for (int i = 0; i < 50; i++) {
            keyToValue.put(i+"abcde", i+"abcde");
            System.out.println(i+"abcde");
        }
   	  System.out.println("-------------------------------------");

        Iterator<Entry<String, String>> iterator=keyToValue.entrySet().iterator();
        
        while(iterator.hasNext()){
      	  System.out.println(iterator.next().getKey());
        }
   	  System.out.println("-------------------------------------");

        for(Entry<String, String> entrySetItem:keyToValue.entrySet()){
      	  System.out.println(entrySetItem);
        }
        
        
        
    }
}
