package com.akkafun.common.scheduler;

import com.akkafun.common.spring.ApplicationConstant;
import com.akkafun.common.utils.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.CountDownLatch;

/**
 * 使用zk的leader选举功能, 实现一个服务部署多个实例时, 只允许一个实例下的定时任务是运行状态
 * Created by liubin on 2016/4/20.
 */
public class ZkSchedulerCoordinator implements InitializingBean, DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(ZkSchedulerCoordinator.class);

    private ApplicationConstant applicationConstant;

    //是否为leader, 只有leader才能执行定时任务
    private volatile boolean leader = false;

    private CountDownLatch latch = new CountDownLatch(1);

    private CuratorFramework client = null;

    private LeaderSelector selector = null;

    public ZkSchedulerCoordinator(ApplicationConstant applicationConstant) {
        this.applicationConstant = applicationConstant;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        String zkAddress = applicationConstant.zkAddress;
        if(StringUtils.isBlank(zkAddress)) {
            logger.warn("zkAddress 为空, ZkSchedulerCoordinator 停止运行");
            return;
        }
        String applicationName = applicationConstant.applicationName;
        String path = ZkUtils.createZkSchedulerLeaderPath(applicationName);

        logger.info("开始连接zookeeper, 进行scheduler leader选举, zkAddress:{}, path:{}", zkAddress, path);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        client.start();

        LeaderSelectorListener listener = new SchedulerLeaderSelector();
        selector = new LeaderSelector(client, path, listener);
//        selector.autoRequeue();  // not required, but this is behavior that you will probably expect
        selector.start();

    }

    @Override
    public void destroy() throws Exception {
        if(selector != null) {
            selector.close();
        }
        if(client != null) {
            client.close();
        }
        latch.countDown();
    }

    public boolean isLeader() {
        return leader;
    }

    class SchedulerLeaderSelector extends LeaderSelectorListenerAdapter {

        @Override
        public void takeLeadership(CuratorFramework client) throws Exception {
            logger.info("获取 scheduler leader");
            leader = true;
            latch.await();
            logger.info("主动放弃 scheduler leader");
            leader = false;
        }
    }

}
