package com.yejunyu.rapid.common.concurrent.queue.mpmc;

/**
 * @author : YeJunyu
 * @description : 2 次幂容器
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/2
 */
final class Capacity {

    public static final int MAX_POWER2 = 1 << 30;

    public static int getCapacity(int capacity) {
        int c = 2;
        if (capacity >= MAX_POWER2) {
            c = MAX_POWER2;
        } else {
            while (c < capacity) {
                c <<= 1;
            }
        }
        if (isPowerOf2(c)) {
            return c;
        } else {
            throw new RuntimeException("capacity is not a power of 2");
        }
    }


    private static boolean isPowerOf2(final int c) {
        return (c & (c - 1)) == 0;
    }
}
