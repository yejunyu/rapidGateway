package com.yejunyu.rapid.common.concurrent.queue.mpmc;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/7
 */
public class PaddingAtomicLong extends Padding {

    // 一个缓存行需要多少个 long 元素的填充
    private static final int CACHE_LINE_LONG = CACHE_LINE / Long.BYTES;

    private final AtomicLongArray atomicLongArray;

    public PaddingAtomicLong(final long l) {
        atomicLongArray = new AtomicLongArray(2 * CACHE_LINE_LONG);
        set(l);
    }

    void set(final long l) {
        atomicLongArray.set(CACHE_LINE_LONG, l);
    }

    long get() {
        return atomicLongArray.get(CACHE_LINE_LONG);
    }

    @Override
    public String toString() {
        return Long.toString(get());
    }

    public boolean compareAnsSet(final long expect, final long l) {
        return atomicLongArray.compareAndSet(CACHE_LINE_LONG, expect, l);
    }
}
