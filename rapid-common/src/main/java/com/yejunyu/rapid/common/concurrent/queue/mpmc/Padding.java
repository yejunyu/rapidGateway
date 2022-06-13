package com.yejunyu.rapid.common.concurrent.queue.mpmc;

/**
 * @author : YeJunyu
 * @description : 缓存填充行
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/7
 */
public class Padding {

    public static final int CACHE_LINE = Integer.getInteger("Intel.CacheLineSize", 64);
}
