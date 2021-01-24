package com.zk.controller;

import com.zk.lock.AbstractLock;
import com.zk.lock.LockThread;
import com.zk.lock.ZookeeperLock;
import com.zk.tools.MyZkSerializer;
import com.zk.tools.ZkUtools;
import com.zk.zookeeper.ZKWatcher;
import org.I0Itec.zkclient.*;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.PrepRequestProcessor;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/test")
public class TestController {

    //获取 Zookeeper 服务地址
    private static final String SERVER = "127.0.0.1:2181";

    //会话超时时间
    private final int SESSION_TIMEOUT = 30000;

    //发令枪
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 获得 session 的方式，这种方式可能会在 Zookeeper 还没有获得连接的时候就已经对 ZK 进行访问了
     * @return
     */
    @GetMapping("/test1")
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

    /**
     * 创建节点
     * @throws Exception
     */
    @GetMapping("/create")
    public void Create(String path) throws Exception {
//        //创建权限
//        ACL acl = new ACL();
//        //创建ACL id  参数1 : 方式    参数2 : ip地址
//        Id id = new Id("ip","127.0.0.1");
//        //设置id
//        acl.setId(id);
//        //设置权限
//        acl.setPerms(ZooDefs.Perms.ALL);
//        //设置权限策略组
//        List<ACL> acls = new ArrayList<ACL>();
//        acls.add(acl);

//        final String path = "/test2/test_002";
        //本地访问 (如果是集群的话地址可以用 , 隔开)
        ZooKeeper connect = ZkUtools.connect("127.0.0.1:2181");
        ZkUtools.create(connect,path,"test".getBytes());
        //为新建的 zookeeper 节点设置权限
//        connect.create(path,"test".getBytes(),acls, CreateMode.PERSISTENT);
        //保持连接存活
        Thread.sleep(5*1000);
    }

    /**
     * 修改节点权限 (ip)
     * @throws Exception
     */
    @GetMapping("/ipauth")
    public void ipauth() throws Exception {
        //创建权限
        ACL acl = new ACL();
        //创建ACL id  参数1 : 方式    参数2 : ip地址
        Id id = new Id("ip","127.0.0.1");
        //设置id
        acl.setId(id);
        //设置权限
        acl.setPerms(ZooDefs.Perms.ALL);
        //设置权限策略组
        List<ACL> acls = new ArrayList<ACL>();
        acls.add(acl);


        final String path = "/test";
        ZooKeeper connect = ZkUtools.connect("127.0.0.1:2181");
        connect.setACL(path,acls,connect.exists(path,false).getVersion());
        List<ACL> acls2 = connect.getACL(path,connect.exists(path,false));
        Thread.sleep(5*1000);
        for (ACL acl1 : acls2){
            System.out.println(acl1.getPerms());
            System.out.println(acl1.getId());
        }
    }

    /**
     * 修改节点的权限(用户)
     * @throws Exception
     */
    @GetMapping("/roleauth")
    public void roleauth() throws Exception {
        //创建权限
        ACL acl = new ACL();
        //创建ACL id  参数1 : 方式    参数2 : 用户名与密码
        Id id = new Id("digest","user:447R1Szes7vlvFpju+mOXmUr/WE=");
        //设置id
        acl.setId(id);
        //设置权限
        acl.setPerms(ZooDefs.Perms.ALL);
        //设置权限策略组
        List<ACL> acls = new ArrayList<ACL>();
        acls.add(acl);

        final String path = "/test";
        ZooKeeper connect = ZkUtools.connect("127.0.0.1:2181");
        //修改权限
        connect.setACL(path,acls,-1);
        Thread.sleep(5*1000);
        System.out.println("修改成功");

    }

    /**
     * 读取节点内容与权限(用户)
     * @throws Exception
     */
    @GetMapping("/getAcl")
    public void getAcl() throws Exception {
        final String path = "/test";
        ZooKeeper connect = ZkUtools.connect("127.0.0.1:2181");

        //添加用户信息
        connect.addAuthInfo("digest","user:12345678".getBytes());

        byte[] data = connect.getData(path,false,null);
        System.out.println(new String(data,"UTF-8"));
    }

    /**
     * 仿制 zookeeper 监听器
     * @throws Exception
     */
    @GetMapping("/watch")
    public void testWatch() throws Exception {

//        /**
//         * 连接策略
//         * 参数1：初始的重试等待时间
//         * 参数2：最多重试次数
//         *
//         * ExponentialBackoffRetry：重试一定次数，每次重试时间依次递增
//         * RetryNTimes：重试 N 次
//         * RetryOneTime：重试一次
//         * RetryUntilElapsed：重试一定时间
//         */
//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
//
//        //创建 CuratorFrameworkImpl 实例 参数1：Zookeeper 服务地址 参数2：会话超时事件 参数3：连接超时时间 参数4：连接策略
//        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181",1000,1000,retryPolicy);
//
//        client.start();
//
//        //创建持久节点
//        client.create().forPath("/test","test".getBytes());
//
//        //创建永久有序节点
//        client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/test","test".getBytes());
//
//        //创建临时节点
//        client.create().withMode(CreateMode.EPHEMERAL).forPath("/test","test".getBytes());
//
//        //创建临时有序节点
//        client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/test","test".getBytes());
//
//        //判断节点是否存在
//        Stat stat1 = client.checkExists().forPath("/test");
//
//        //创建监听器
//        CuratorListener listener = new CuratorListener() {
//            @Override
//            public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
//
//            }
//        };
//
//        //添加监听器
//        client.getCuratorListenable().addListener(listener);
//
//        //异步设置某个节点的数据
//        client.setData().inBackground().forPath("/test","new".getBytes());
//
//        //异步创建监听器
//        BackgroundCallback callback = new BackgroundCallback() {
//            @Override
//            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
//
//            }
//        };
//
//        //异步设置节点数据
//        client.setData().inBackground(callback).forPath("/test");
//
//        //删除节点
//        client.delete().forPath("/test");
//
//        //级联删除子节点
//        client.delete().guaranteed().deletingChildrenIfNeeded().forPath("/test");
//
//        //事务操纵
//        CuratorOp createOp = client.transactionOp().create().forPath("/test","test".getBytes());
//        CuratorOp setDataOp = client.transactionOp().setData().forPath("/test","new".getBytes());
//        CuratorOp deleteOp = client.transactionOp().delete().forPath("/test");
//        //执行事务
//        List<CuratorTransactionResult> results = client.transaction().forOperations(createOp,setDataOp,deleteOp);



        ZKWatcher watcher = new ZKWatcher();
        watcher.test();
    }

    /**
     * 获取 zookeeper 节点的值
     */
    @GetMapping("/getdata")
    public String getdata(String string) throws Exception {

        // 创建zk连接
        ZkClient zkClient = new ZkClient("127.0.0.1:2181");
        //设置序列化
        zkClient.setZkSerializer(new MyZkSerializer());
//        ZooKeeper connect = ZkUtools.connect("127.0.0.1:2181");


//        byte[] data = connect.getData(string,false,null);
        String data = zkClient.readData(string);
        zkClient.close();
        return data;
    }

    /**
     * zookeeper 分布式锁
     * @throws Exception
     */
    @GetMapping("/lock")
    public void lock() throws Exception {
        System.out.println("##生成唯一单号##");
        for (int i = 0 ; i < 10 ; i++){
            new Thread( new LockThread() ).start();
        }
    }
}
