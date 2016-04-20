package com.akkafun.common.scheduler;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 定时任务父类, 注意子类要实现成单例类
 * Created by liubin on 15-6-4.
 */
public abstract class SchedulerTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(SchedulerTask.class);

    private AtomicBoolean running = new AtomicBoolean(false);

    protected SchedulerTask() {}

    @Override
    public void run() {
        String className = getClass().getSimpleName();
        if(running.compareAndSet(false, true)) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                doRun();
            } catch (Throwable e) {
                logger.error(className + "运行的时候发生错误: " + e.getMessage(), e);
            } finally {
                running.set(false);
                stopwatch.stop();
                logger.info(className + "定时任务运行结束, 耗时: " + stopwatch.toString());
            }
        } else {
            logger.warn(className + "定时任务已经在运行...");
        }
    }


    protected abstract void doRun();


}
