package com.akkafun.common.scheduler;

import com.akkafun.base.exception.BaseException;
import com.akkafun.common.spring.ApplicationContextHolder;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.*;

/**
 * 这个类的作用是在执行定时任务方法之前, 统一进行一些操作:
 * 1. 判断当前实例是否为leader, 不是的话不执行任务
 * 2. 输出时间统计日志
 *
 * Created by liubin on 2016/4/20.
 */
public class ZkCoordinateScheduledExecutor extends ScheduledThreadPoolExecutor {

    private static Logger logger = LoggerFactory.getLogger(ZkCoordinateScheduledExecutor.class);

    public ZkCoordinateScheduledExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public ZkCoordinateScheduledExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {

        if(!(runnable instanceof DelegatingErrorHandlingRunnable)) {
            throw new BaseException("runnable预期类型是DelegatingErrorHandlingRunnable的子类, 实际是: " +
                    runnable.getClass().getName());
        }

        if(!(task instanceof FutureTask)) {
            throw new BaseException("task预期类型是FutureTask的子类, 实际是: " + task.getClass().getName());
        }

        //得到DelegatingErrorHandlingRunnable的delegate属性
        Field delegateRunnableField = ReflectionUtils.findField(DelegatingErrorHandlingRunnable.class, "delegate");
        if(delegateRunnableField == null) {
            throw new BaseException("DelegatingErrorHandlingRunnable类型中没有找到delegate属性");
        }
        delegateRunnableField.setAccessible(true);
        ScheduledMethodRunnable delegateRunnable = (ScheduledMethodRunnable) ReflectionUtils.getField(delegateRunnableField, runnable);

        //得到FutureTask的callable属性
        Field field = ReflectionUtils.findField(FutureTask.class, "callable");
        if(field == null) {
            throw new BaseException("FutureTask类型中没有找到callable属性");
        }
        field.setAccessible(true);
        final Callable<V> callable = (Callable<V>)ReflectionUtils.getField(field, task);

        Callable<V> decorateCallable = new ZkCoordinateTask(delegateRunnable, callable);

        //修改FutureTask的callable属性, 用ZkCoordinateTask替换
        ReflectionUtils.setField(field, task, decorateCallable);

        return task;

    }

    static class ZkCoordinateTask<V> implements Callable<V> {

        ScheduledMethodRunnable scheduledMethodRunnable;

        Callable<V> target;

        String name;

        ZkSchedulerCoordinator zkSchedulerCoordinator;

        public ZkCoordinateTask(ScheduledMethodRunnable scheduledMethodRunnable, Callable<V> target) {
            this.scheduledMethodRunnable = scheduledMethodRunnable;
            this.target = target;

            name = String.format("Class: %s, method: %s", scheduledMethodRunnable.getTarget().getClass().getName(),
                    scheduledMethodRunnable.getMethod().getName());
        }

        @Override
        public V call() throws Exception {
            if(zkSchedulerCoordinator == null) {
                zkSchedulerCoordinator = ApplicationContextHolder.context.getBean(ZkSchedulerCoordinator.class);
            }
            //如果当前实例不是leader, 不执行任务
            if(zkSchedulerCoordinator.isLeader()) {
//                Stopwatch stopwatch = Stopwatch.createStarted();
                try {
                    //执行任务
                    return target.call();
                } catch (Throwable e) {
                    logger.error(String.format("定时任务[%s]运行的时候发生错误: %s", name, e.getMessage()), e);
                    throw e;
                } finally {
//                    stopwatch.stop();
//                    logger.info(String.format("定时任务[%s]运行结束, 耗时: %s", name, stopwatch.toString()));
                }
            }
            return null;
        }
    }

}
