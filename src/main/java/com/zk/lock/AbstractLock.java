package com.zk.lock;

/**
 * 分布式锁的抽象类类
 */
public abstract class AbstractLock implements Lock {

    public void getLock() {
        //尝试获取锁资源
        if (tryLock()){
            System.out.println("##获取 lock 锁的资源 ####");
        }
        // 如果没有获取到那么就进行等待
        else {
            //等待
            waitLock();
            //重新获取锁资源
            getLock();
        }

    }

    // 尝试获取锁资源方法
    public abstract boolean tryLock();

    // 等待方法
    public abstract void waitLock();


}
