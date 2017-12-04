/*
 * Copyright (C) 2014-2016 Omniprime All rights reserved
 * Author: zhangsh
 * Date: 2017年7月4日
 * Description:TY.java 
 */
package com.zs.juc.threadPool;

/**
 * @author zhangsh
 *
 */
public interface ThreadPool<Job extends Runnable> {

    void execute(Job job);

    void shutDown();

    void addWorkers(int num);

    void removeWorker(int num);

    int getJobSize();

}
