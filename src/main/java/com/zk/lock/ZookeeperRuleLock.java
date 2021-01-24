package com.zk.lock;

import org.I0Itec.zkclient.IZkDataListener;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper 分布式锁的实现类  (公平锁)
 */
public class ZookeeperRuleLock extends ZookeeperLockFather {

    //发令枪
    private CountDownLatch countDownLatch = null;

    //当前请求的节点前一个节点
    private String beforePath;

    //当前请求的节点
    private String currentPath;

    //初始化方法
    public ZookeeperRuleLock() {
        //判断当前的锁有没有创建节点，没有创建节点的化后面的有序节点的创建将无法继续
        if (!this.zkClient.exists(PATH2)) {
            this.zkClient.createPersistent(PATH2);
        }
    }

    @Override
    public boolean tryLock() {
        //判断当前节点是否有创建有序节点，如果没有创建有序节点就相当于没有排进队伍
        if (currentPath == null || currentPath.length() <=0){
            //如果没有加入队列，那么将其加入队列，创建一个有序临时节点
            currentPath = this.zkClient.createEphemeralSequential(PATH2 + '/',"lock");
        }
        //获取所有的子节点(临时节点)
        List<String> childrens = this.zkClient.getChildren(PATH2);
        //对临时节点进行排序
        Collections.sort(childrens);

        //判断当前节点是否是当前队列的最小(前面的节点)
        if (currentPath.equals(PATH2+'/' + childrens.get(0))){
            //如果是，抢锁成功返回 true
            return true;
        }
        //如果当前节点不是最小(最前面的节点)，就将自己前面的节点赋值给 beforePath
        else{
            //因为有序临时节点的名称一般都是 0000000401 (前几位都是 0 的情况)这样的所以我们这里从下标 7 截取到最后
            int wz = Collections.binarySearch(childrens,currentPath.substring(7));
            //获取到了自身前面的一个节点的名字
            beforePath = PATH2 + '/' + childrens.get(wz-1);
            return false;
        }
    }

    /**
     * 等待方法
     *
     */
    @Override
    public void waitLock() {
        //创建一个 zookeeper 的监听器
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            //当删除时会触发的方法
            @Override
            public void handleDataDeleted(String s) throws Exception {
                //唤醒被等待的线程
                if (countDownLatch!=null){
                    countDownLatch.countDown();
                }
            }
        };

        //给你前面的节点绑定监听事件
        this.zkClient.subscribeDataChanges(beforePath,listener);

        //判断你前面的节点是否存在
        if (this.zkClient.exists(beforePath)){
            //存在时
            countDownLatch = new CountDownLatch(1);
            try {
                // 继续等待
                countDownLatch.await();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //不存在时
        else{
            //删除监听
            this.zkClient.unsubscribeDataChanges(beforePath,listener);
        }
    }

    //释放锁的方法，删除节点
    @Override
    public void unlock() {
        //删除当前临时节点
        zkClient.delete(currentPath);
        //关闭 zookeeper 连接
        zkClient.close();
    }
}
