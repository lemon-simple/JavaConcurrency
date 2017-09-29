package test;

import java.util.LinkedList;

public class Test {
    public static void main(String[] args) {

        LinkedList<String> linkedList = new LinkedList<String>();

        for (int i = 0; i < 10; i++) {
            linkedList.add(i + "ABCDE" + "FG");
            linkedList.get(i);
        }
    }
}