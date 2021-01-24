package com.zk.lock;

/**
 * lock 锁的接口类
 * zookeeper 分布式锁案例
 */
public interface Lock {

    // 获取锁的方法
    public void getLock();

    // 释放锁的方法
    public void unlock();

}
