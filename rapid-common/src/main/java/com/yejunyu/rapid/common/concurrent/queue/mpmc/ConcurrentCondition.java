package com.yejunyu.rapid.common.concurrent.queue.mpmc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author : YeJunyu
 * @description : <code>java.util.concurrent.locks.Condition</code>
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/2
 */
public interface ConcurrentCondition {

    long PARK_TIMEOUT = 50L;

    int MAX_PROG_YIELD = 2000;

    /**
     * 判断 queue 的 条件是否满足 (空或者满)
     *
     * @return
     */
    boolean test();

    /**
     * wake 当 condition 满足的时候 or timeout
     *
     * @param timeout
     * @throws InterruptedException
     */
    void awaitNanos(final long timeout) throws InterruptedException;


    /**
     * wake 假如 signal 取消
     *
     * @throws InterruptedException
     */
    void await() throws InterruptedException;

    /**
     * condition to wake up
     */
    void signal();

    /**
     * 空转让出到 yield
     *
     * @param n
     * @return
     */
    static int progressiveYield(final int n) {
        if (n > 500) {
            if (n < 1000) {
                // 后三位都为 0 时, 概率为 1/8
                if ((n & 0x7) == 0) {
                    // 线程禁用 50 ns
                    LockSupport.parkNanos(PARK_TIMEOUT);
                } else {
                    // 7/8概率 空转
                    onSpinWait();
                }
            } else if (n < MAX_PROG_YIELD) {
                // 大于 1000 小于 2000 的时候
                // 后两位都为 0 时 1/4 概率
                if ((n & 0x3) == 0) {
                    Thread.yield();
                } else {
                    onSpinWait();
                }
            } else {
                // 大于 2000 时
                Thread.yield();
                return n;
            }
        }
        return n + 1;
    }

    static void onSpinWait() {
        // Java 9 hint for spin waiting PAUSE instruction
        //http://openjdk.java.net/jeps/285
        // Thread.onSpinWait();
    }

    static boolean waitStatus(final long timeout, final TimeUnit timeUnit,
                              final ConcurrentCondition concurrentCondition) throws InterruptedException {
        // until condition is signaled
        final long nanosTimeout = TimeUnit.NANOSECONDS.convert(timeout, timeUnit);
        final long expire = System.nanoTime() + nanosTimeout;
        // the queue is empty or full wait for something to change
        while (concurrentCondition.test()) {
            final long now = System.nanoTime();
            if (now > expire) {
                return false;
            }
            concurrentCondition.awaitNanos(expire - now - PARK_TIMEOUT);
        }
        return true;
    }
}
