package com.zk.lock;

import java.util.concurrent.CountDownLatch;
import org.I0Itec.zkclient.IZkDataListener;

/**
 * zookeeper 分布式锁的实现类  (非公平锁)
 */
public class ZookeeperLock extends ZookeeperLockFather {

    //发令枪
    private CountDownLatch countDownLatch = null;

    //尝试获取锁的方法，创建节点
    @Override
    public boolean tryLock(){
        // 创建节点，试图去抢锁
        try{
            // zookeeper 创建了一个名为 "/lock" 临时节点
            this.zkClient.createEphemeral(PATH);
            return true;
        }
        // 创建节点失败 (抢锁失败)
        catch (Exception e){
//            e.printStackTrace();
            return false;
        }
    }

    /**
     * 等待方法
     * 使用监听器来监听节点的删除方法来判断是否可以抢锁，如果可以那么就将监听器删除
     */
    @Override
    public void waitLock() {
        // 添加数据监听器
        IZkDataListener iZkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            //当节点删除的时候会触发的方法
            @Override
            public void handleDataDeleted(String s) throws Exception {
                //唤醒被等待的线程
                if (countDownLatch!=null){
                    countDownLatch.countDown();
                }
            }
        };

        //给这个节点绑定监听事件
        this.zkClient.subscribeDataChanges(PATH,iZkDataListener);

        //判断节点是否存在
        if (this.zkClient.exists(PATH)){
            //存在时
            countDownLatch = new CountDownLatch(1);
            try {
                // 继续等待
                countDownLatch.await();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            //删除监听
            this.zkClient.unsubscribeDataChanges(PATH,iZkDataListener);
        }

    }

    //释放锁的方法，删除节点
    @Override
    public void unlock() {
        //判断是否有连接上 zookeeper
        if (this.zkClient!=null){
            //删除节点
            this.zkClient.delete(PATH);
            //关闭 zookeeper 连接
            this.zkClient.close();
            System.out.println("释放锁资源…");
        }
    }
}
