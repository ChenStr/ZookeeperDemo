package com.zk;


import com.zk.tools.ZkUtools;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.zk.tools.ZkUtools.connect;

@SpringBootApplication
public class ZkApplication {



	public static void main(String[] args) throws Exception {
		SpringApplication.run(ZkApplication.class, args);
	}


}
