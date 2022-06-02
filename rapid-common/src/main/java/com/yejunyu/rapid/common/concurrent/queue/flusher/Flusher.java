package com.yejunyu.rapid.common.concurrent.queue.flusher;

/**
 * @author : YeJunyu
 * @description : FLusher 接口
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/31
 */
public interface Flusher<E> {
    /**
     * 添加元素
     *
     * @param event 元素
     */
    void add(E event);

    /**
     * 添加多个元素
     *
     * @param event 元素
     */
    void add(E... event);

    /**
     * 尝试添加一个元素,成功返回 true
     *
     * @param event 元素
     */
    boolean tryAdd(E event);

    /**
     * 尝试添加多个元素,成功返回 true
     *
     * @param event 元素
     */
    boolean tryAdd(E... event);

    /**
     * 队列是否关闭
     *
     * @return
     */
    boolean isShutdown();

    /**
     * 开启队列
     */
    void start();

    /**
     * 关闭队列
     */
    void shutDown();
}
