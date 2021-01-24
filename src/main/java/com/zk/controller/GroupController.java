package com.zk.controller;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/group")
public class GroupController {

    //获取 Zookeeper 服务地址
    private static final String SERVER = "192.168.234.130:2181,192.168.234.131:2181,192.168.234.132:2181";

    //会话超时时间
    private final int SESSION_TIMEOUT = 30000;

    //发令枪
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 获得 session 的方式，这种方式可能会在 Zookeeper 还没有获得连接的时候就已经对 ZK 进行访问了
     * @return
     */
    @GetMapping("/test")
    public void Test1() throws Exception {
        //添加了 watcher 监听
        ZooKeeper zooKeeper = new ZooKeeper(SERVER, SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                //判断是否有进行连接
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    //确认连接完毕后再进行操作
                    countDownLatch.countDown();
                    System.out.println("已经取得连接");
                }
            }
        });

        //连接完成前先进行等待
        countDownLatch.await();
        System.out.println(zooKeeper.getState());
    }

}
