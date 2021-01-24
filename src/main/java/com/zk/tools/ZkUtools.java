package com.zk.tools;

import lombok.Data;
import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

@Data
public class ZkUtools {

    private static ZooKeeper zoo;

    //发令枪 zookeeper 需要于发令枪一同使用
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    // zookeeper Java 客户端的基本连接示例
    public static ZooKeeper connect(String host) throws Exception {
        // 连接的 IP 地址 , 默认的超时时间 , 添加监听器
        zoo = new ZooKeeper(host, 5000, new Watcher() {
            /**
             * 收到来自 Server 的 Watcher 通知后的处理
             * @param watchedEvent
             */
            @Override
            public void process(WatchedEvent watchedEvent) {
                //判断是否访问成功
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    countDownLatch.countDown();
                    System.out.println("获取 ZK 服务器的连接");
                }
            }
        });
        countDownLatch.await();
        System.out.println("zk 连接状态：" + zoo.getState());
        return zoo;
    }

    // zookeeper Java 客户端的基本关闭示例
    public void close(ZooKeeper zooKeeper) throws InterruptedException {
        zooKeeper.close();
    }

    /**
     * zookeeper 创建节点基本示例
     * @param path 节点的名称
     * @param data 节点的数据
     */
    public static void create(ZooKeeper zooKeeper,String path,byte[] data) throws Exception {
        /**
         * 四个参数分别是 path : 节点名称  data : 节点数据  ZooDefs : 节点的权限(示例中为 world)  CreateMode : 节点类型(默认为持久节点)
         * 节点类型介绍 PERSISTENT : 持久节点 , EPHEMERAL : 临时节点 , PERSISTENT_SEQUENTIAL : 有序持久节点 , EPHEMERAL_SEQUENTIAL : 有序临时节点
         */
        zooKeeper.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

    }

}
