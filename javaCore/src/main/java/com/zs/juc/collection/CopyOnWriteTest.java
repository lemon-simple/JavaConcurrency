package com.zs.juc.collection;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteTest {

	public static void main(String[] args) {

		People[] pp = { new People(1, "AB"), new People(2, "ABxF") };
		Object[] newElements = Arrays.copyOf(pp, 1 + 1);
		System.out.println(pp);
		for (People p : pp) {
			System.out.println(p);
		}
		for (Object p : newElements) {
			System.out.println(p);
		}

		//
		// CopyOnWriteArrayList<People> arrayList = new
		// CopyOnWriteArrayList<People>();
		// for (int i = 0; i < 10; i++) {
		// People p= new People(i,"ABC");
		// System.out.println(p);
		// arrayList.add(p);
		// }
		//
		// for (People s : arrayList) {
		// System.out.println(s);
		// }

	}

	static class People {
		private int i;
		private String name;

		public People(int i, String name) {
			this.i = i;
			this.name = name;
		}

	}

}
