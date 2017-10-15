package com.zs.juc.collection;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapInfiniteLoop {
	private static int RESIZE_STAMP_BITS = 16;

	private final static int[] ints = new int[10];

	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	private static HashMap<Integer, String> map = new HashMap<Integer, String>(2, 0.75f);
	private static ConcurrentHashMap<Integer, String> concurrentHashMap = new ConcurrentHashMap<Integer, String>(2,
			0.75f);

	public static void main(String[] args) {
		System.out.println(ints[0]);
		String s=new String("2");
		ints[0] = 10;
		System.out.println(ints[0]);

	}

	static final int resizeStamp(int n) {
		return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
	}
}