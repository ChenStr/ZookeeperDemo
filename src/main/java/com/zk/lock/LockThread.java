package com.zk.lock;

/**
 * 模仿多线程的方法类
 */
public class LockThread implements Runnable {

    //全局订单id
    public static int count = 0;

    @Override
    public void run() {
        //非公平锁
        ZookeeperLock zookeeperLock = new ZookeeperLock();
        //公平锁
//        ZookeeperRuleLock zookeeperLock = new ZookeeperRuleLock();
        try{
            zookeeperLock.getLock();
            count++;
            System.out.println(Thread.currentThread().getName() + "生成订单ID:" + count);
        }catch (Exception e){
//            e.printStackTrace();
        }finally {
            zookeeperLock.unlock();
        }
    }
}
