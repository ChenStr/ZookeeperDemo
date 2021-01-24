package com.zk.lock;

import org.I0Itec.zkclient.ZkClient;

/**
 * zookeeper 分布式锁的实现父类
 * 主要用于少写重复代码
 */
public abstract class ZookeeperLockFather extends AbstractLock {

    // zk连接地址
    private static final String CONNECTSTRING = "127.0.0.1:2181";
    // 创建zk连接
    protected ZkClient zkClient = new ZkClient(CONNECTSTRING);
    //新建锁的地址
    protected static final String PATH = "/lock1";

    //新建锁的地址
    protected static final String PATH2 = "/lock2";
}
