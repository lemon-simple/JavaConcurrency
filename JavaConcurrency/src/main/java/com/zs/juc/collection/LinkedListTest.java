package com.zs.juc.collection;

import java.util.LinkedList;

public class LinkedListTest {

	public static void main(String[] args) {

		LinkedList<String> li = new LinkedList<String>();

		for (int i = 0; i < 100; i++) {
			li.add(i + "a");
		}

		System.out.println(li.get(3));

		for (String s : li) {
			System.out.println(s);
		}

	}

}
