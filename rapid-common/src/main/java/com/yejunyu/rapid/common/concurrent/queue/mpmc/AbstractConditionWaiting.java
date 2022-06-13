package com.yejunyu.rapid.common.concurrent.queue.mpmc;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/7
 */
public abstract class AbstractConditionWaiting implements ConcurrentCondition {

    private static final int CACHE_LINE_REFS = Padding.CACHE_LINE / Long.BYTES;

    private static final int MAX_WAITERS = 8;

    private static final long WAITER_MASK = MAX_WAITERS - 1L;

    private static final long WAIT_TIME = PARK_TIMEOUT;

    private final LongAdder waitCount = new LongAdder();

    private final AtomicReferenceArray<Thread> waiter = new AtomicReferenceArray<>(MAX_WAITERS + 2 * CACHE_LINE_REFS);

    private long waitCache = 0L;

    @Override
    public void awaitNanos(long timeout) throws InterruptedException {
        for (; ; ) {
            try {
                final long waitCount = this.waitCount.longValue();
                long waitSequence = waitCount;
                this.waitCount.increment();
                this.waitCache = waitCount + 1;

                long timeNow = System.nanoTime();
                final long expire = timeNow + timeout;
                final Thread thread = Thread.currentThread();

                int spin = 0;
                if (waitCount == 0) {
                    // first thread
                    while (test() && expire > timeNow && !thread.isInterrupted()) {
                        spin = ConcurrentCondition.progressiveYield(spin);
                        timeNow = System.nanoTime();
                    }
                    if (thread.isInterrupted()) {
                        throw new InterruptedException();
                    }
                    return;
                } else {
                    // wait to become a waiter
                    while (test() && !waiter.compareAndSet((int) (waitSequence++ & WAITER_MASK) + CACHE_LINE_REFS, null, thread) &&
                            expire > timeNow) {
                        if (spin < ConcurrentCondition.MAX_PROG_YIELD) {
                            spin = ConcurrentCondition.progressiveYield(spin);
                        } else {
                            LockSupport.parkNanos(MAX_WAITERS * ConcurrentCondition.PARK_TIMEOUT);
                        }
                        timeNow = System.nanoTime();
                    }
                    while (test() && (waiter.get((int) ((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS) == thread) &&
                            expire > timeNow && !thread.isInterrupted()) {
                        LockSupport.parkNanos((expire - timeNow) >> 2);
                        timeNow = System.nanoTime();
                    }
                    if (thread.isInterrupted()) {
                        // we are not waiting we are interrupted
                        while (!waiter.compareAndSet((int) ((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS, thread, null) &&
                                waiter.get(CACHE_LINE_REFS) == thread) {
                            LockSupport.parkNanos(PARK_TIMEOUT);
                        }
                        throw new InterruptedException();
                    }
                    return;
                }
            } finally {
                this.waitCount.decrement();
                this.waitCache = this.waitCount.longValue();
            }
        }
    }

    @Override
    public void await() throws InterruptedException {
        for (; ; ) {

            try {
                final long waitCount = this.waitCount.sum();
                long waitSequence = waitCount;
                this.waitCount.increment();
                waitCache = waitCount + 1;

                final Thread t = Thread.currentThread();

                if (waitCount == 0) {
                    int spin = 0;
                    // first thread spinning
                    while (test() && !t.isInterrupted()) {
                        spin = ConcurrentCondition.progressiveYield(spin);
                    }

                    if (t.isInterrupted()) {
                        throw new InterruptedException();
                    }

                    return;
                } else {

                    // wait to become a waiter
                    int spin = 0;
                    while (test() && !waiter.compareAndSet((int) (waitSequence++ & WAITER_MASK) + CACHE_LINE_REFS, null, t) && !t.isInterrupted()) {
                        if (spin < ConcurrentCondition.MAX_PROG_YIELD) {
                            spin = ConcurrentCondition.progressiveYield(spin);
                        } else {
                            LockSupport.parkNanos(MAX_WAITERS * ConcurrentCondition.PARK_TIMEOUT);
                        }
                    }

                    // are we a waiter?   wait until we are awakened
                    while (test() && (waiter.get((int) ((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS) == t) && !t.isInterrupted()) {
                        LockSupport.parkNanos(1_000_000L);
                    }

                    if (t.isInterrupted()) {
                        // we are not waiting we are interrupted
                        while (!waiter.compareAndSet((int) ((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS, t, null) && waiter.get(CACHE_LINE_REFS) == t) {
                            LockSupport.parkNanos(WAIT_TIME);
                        }

                        throw new InterruptedException();
                    }

                    return;

                }
            } finally {
                waitCount.decrement();
                waitCache = waitCount.sum();
            }
        }
    }

    @Override
    public void signal() {
        // only signal if somebody is blocking for it
        if (waitCache > 0 || (waitCache = waitCount.sum()) > 0) {
            long waitSequence = 0L;
            for (; ; ) {
                Thread t;
                while ((t = waiter.get((int) (waitSequence++ & WAITER_MASK) + CACHE_LINE_REFS)) != null) {
                    if (waiter.compareAndSet((int) ((waitSequence - 1) & WAITER_MASK) + CACHE_LINE_REFS, t, null)) {
                        LockSupport.unpark(t);
                    } else {
                        LockSupport.parkNanos(WAIT_TIME);
                    }

                    // go through all waiters once, or return if we are finished
                    if (((waitSequence & WAITER_MASK) == WAITER_MASK) || (waitCache = waitCount.sum()) == 0) {
                        return;
                    }
                }

                // go through all waiters once, or return if we are finished
                if (((waitSequence & WAITER_MASK) == WAITER_MASK) || (waitCache = waitCount.sum()) == 0) {
                    return;
                }
            }
        }
    }
}
