package com.yejunyu.rapid.common.concurrent.queue.mpmc;

/**
 * @author : YeJunyu
 * @description : 阻塞的自旋锁抽象类
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/6
 */
public abstract class AbstractConditionSpinning implements ConcurrentCondition {

    @Override
    public void awaitNanos(long timeout) throws InterruptedException {
        long now = System.nanoTime();
        final long expire = now + timeout;
        final Thread thread = Thread.currentThread();
        while (test() && expire > now && !thread.isInterrupted()) {
            now = System.nanoTime();
            ConcurrentCondition.onSpinWait();
        }
        if (thread.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void await() throws InterruptedException {
        final Thread thread = Thread.currentThread();
        while (test() && !thread.isInterrupted()) {
            ConcurrentCondition.onSpinWait();
        }
        if (thread.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void signal() {
        // ignore
    }
}
