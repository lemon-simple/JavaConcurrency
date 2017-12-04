package com.zs.juc.atomicType;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicClassTest {
	static AtomicInteger ai = new AtomicInteger(1);

	static int[] intArray = { 1, 2 };
	static AtomicIntegerArray aiArray = new AtomicIntegerArray(intArray);

	static User july = new User(2, "july");
	static User[] userArray = new User[] { new User(1, "jason"), july };
	static AtomicReferenceArray<User> referenceArray = new AtomicReferenceArray<>(userArray);

	static AtomicReference<User> atomicUserRef = new AtomicReference<User>();

	public static void main(String[] args) {
		// atomicInterTest();

		// AtomicArrayTest();

		// AtomicReferenceArrayTest();

		// AtomicReferenceTest();

		AtomicUpdater();

	}

	private static void AtomicUpdater() {
		// ·AtomicIntegerFieldUpdater：原子更新整型的字段的更新器。
		// ·AtomicLongFieldUpdater：原子更新长整型字段的更新器。
		// ·AtomicStampedReference：原子更新带有版本号的引用类型。该类将整数值与引用关联起
		// 来，可用于原子的更新数据和数据的版本号，可以解决使用CAS进行原子更新时可能出现的
		// ABA问题。

		//需要更新引用對象的內部字段必須是public volatile修飾的	
		AtomicIntegerFieldUpdater fieldUpdater = AtomicIntegerFieldUpdater.newUpdater(User.class, "id");

		User user = new User(99, "rose");
		fieldUpdater.addAndGet(user, 1);
		System.out.println(user);
	}

	private static void AtomicReferenceTest() {
		User user = new User(12, "jack");
		atomicUserRef.set(user);
		User updateUser = new User(2, "jack");
		atomicUserRef.compareAndSet(user, updateUser);
		System.out.println(atomicUserRef.get().getName());
		System.out.println(atomicUserRef.get().getId());
	}

	private static void AtomicArrayTest() {
		for (int i = 0; i < aiArray.length(); i++)
			aiArray.addAndGet(i, 1);

		System.out.println("aiArray	" + aiArray);
		for (int i = 0; i < intArray.length; i++) {
			System.out.println("intArray	" + intArray[i]);
		}
	}

	private static void AtomicReferenceArrayTest() {
		for (int i = 0; i < referenceArray.length(); i++) {
			referenceArray.compareAndSet(i, new User(1, "jason"), new User(3, "jason"));
			referenceArray.compareAndSet(i, july, new User(3, "july"));
		}
		System.out.println("referenceArray	" + referenceArray);
	}

	private static void atomicInterTest() {
		System.out.println(ai.getAndIncrement());
		System.out.println(ai.get());
	}

	static class User {
		public volatile int id;
		private String name;

		public User(int id, String name) {
			super();
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return "User [id=" + id + ", name=" + name + "]";
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}