package com.yejunyu.rapid.common.concurrent.queue.mpmc;

/**
 * @author : YeJunyu
 * @description : 队列接口
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/2
 */
public interface ConcurrentQueue<E> {

    boolean offer(E e);

    E poll();

    E peek();

    int size();

    int capacity();

    boolean isEmpty();

    boolean contains(Object o);

    int remove(E[] e);

    void clear();
}
