package com.yejunyu.rapid.common.concurrent.queue.mpmc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : YeJunyu
 * @description : user java sync to signal
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/6
 */
public abstract class AbstractCondition implements ConcurrentCondition {

    private final ReentrantLock queLock = new ReentrantLock();

    private final Condition condition = queLock.newCondition();

    @Override
    public void awaitNanos(long timeout) throws InterruptedException {
        long remaining = timeout;
        queLock.lock();
        try {
            while (test() && remaining > 0) {
                remaining = condition.awaitNanos(remaining);
            }
        } finally {
            queLock.unlock();
        }
    }

    @Override
    public void await() throws InterruptedException {
        queLock.lock();
        try {
            while (test()) {
                condition.await();
            }
        } finally {
            queLock.unlock();
        }
    }

    @Override
    public void signal() {
        queLock.lock();
        try {
            // Wakes up all waiting threads.
            condition.signalAll();
        } finally {
            queLock.unlock();
        }
    }
}
