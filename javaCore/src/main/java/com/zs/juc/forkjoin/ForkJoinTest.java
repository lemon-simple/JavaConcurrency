package com.zs.juc.forkjoin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class ForkJoinTest {

	static class CountTask extends RecursiveTask<Integer> {
		private static final long serialVersionUID = 1L;
		private static final int THRESHOLD = 2;
		private int start;
		private int end;

		public CountTask(int start, int end) {
			this.start = start;
			this.end = end;
		}

		protected Integer compute() {
			int sum = 0;
			// 任务足够小 不需要继续分解 则执行计算
			if ((end - start) <= THRESHOLD) {
				for (int i = start; i <= end; i++) {
					sum += i;
				}
			} else {
				// 需要继续分解任务
				int middle = (start + end) / 2;
				CountTask leftTask = new CountTask(start, middle);
				CountTask rightTask = new CountTask(middle + 1, end);

				// 执行子任务
				leftTask.fork();
				rightTask.fork();
				// 等待子任务执行完毕，得到结果
				int leftResult = leftTask.join();
				int rightResult = rightTask.join();
				// 合并子任務
				sum = leftResult + rightResult;
			}
			return sum;
		}

		public static void main(String[] args) throws InterruptedException, ExecutionException {
			ForkJoinPool forkJoinPool = new ForkJoinPool();
			CountTask task = new CountTask(1,4);
			// 执行
			Future<Integer> result = forkJoinPool.submit(task);

			if (task.isCompletedAbnormally()) {
				System.out.println(task.getException());
			} else {
				System.out.println("Result:" + result.get());
			}
		}
	}

}
