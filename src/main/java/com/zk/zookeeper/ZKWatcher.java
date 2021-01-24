package com.zk.zookeeper;


import com.zk.tools.ZkUtools;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 模仿 zookeeper 的监听器
 */
public class ZKWatcher {

    //获取 Zookeeper 服务地址
    private static final String SERVER = "127.0.0.1:2181";

    //会话超时时间
    private final int SESSION_TIMEOUT = 30000;

    //发令枪
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public static ZooKeeper zoo;


    public ZKWatcher() {
    }

    public ZKWatcher(ZooKeeper zoo) throws Exception {
        this.zoo = ZkUtools.connect(SERVER);
    }

    /**
     * 客户端的监听器
     * @param host
     * @throws Exception
     */
    public void getClient(String host) throws Exception{
        this.zoo = new ZooKeeper(host, 5000, new Watcher() {
            /**
             * 当被监听的对象的节点发生改变时就会调用此方法
             * @param event
             */
            @Override
            public void process(WatchedEvent event) {
                System.out.println(event.getPath() + "已被修改。");
                //参数2 : 是否需要事件注册
                try {
                    //监听器的注册(递归调用)
                    zoo.getChildren("/test2",true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        countDownLatch.await();
        System.out.println("zk 连接状态：" + zoo.getState());
    }

    public ZooKeeper connect(String host) throws Exception {
        // 连接的 IP 地址 , 默认的超时时间 , 添加监听器
        this.zoo = new ZooKeeper(host, 5000, new Watcher() {
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
        return this.zoo;
    }

    public void test() throws Exception {
        this.getClient(this.SERVER);
        //监听器的注册
        List<String> children = zoo.getChildren("/test2",true);
        for (String string : children){
            System.out.println("子节点"+string);
        }

        Thread.sleep(5*1000);
    }



}
