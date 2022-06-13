package com.yejunyu.rapid.common.concurrent.queue.mpmc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by @author yejunyu on 2022/6/9
 *
 * @email : yyyejunyu@gmail.com
 */
public class MpmcBlockingQueue<E> extends MpmcConcurrentQueue<E> implements Serializable, Collection<E>, BlockingQueue<E> {

    /**
     * 如果队列是满的情况就阻塞
     */
    protected final ConcurrentCondition queNotFullCondition;
    /**
     * 如果队列是空的情况就阻塞
     */
    protected final ConcurrentCondition queNotEmptyCondition;

    public MpmcBlockingQueue(int capacity) {
        this(capacity, SpinPolicy.WAITING);
    }

    public MpmcBlockingQueue(final int capacity, final SpinPolicy spinPolicy) {
        super(capacity);
        switch (spinPolicy) {
            case BLOCKING:
                queNotEmptyCondition = new QueueNotEmpty();
                queNotFullCondition = new QueueNotFull();
                break;
            case SPINNING:
                queNotEmptyCondition = new SpinningQueueNotEmpty();
                queNotFullCondition = new SpinningQueueNotFull();
                break;
            case WAITING:
            default:
                queNotEmptyCondition = new WaitingQueueNotEmpty();
                queNotFullCondition = new WaitingQueueNotFull();
        }
    }

    public MpmcBlockingQueue(final int capacity, Collection<? extends E> collection) {
        this(capacity);
        for (E e : collection) {
            offer(e);
        }
    }

    public final boolean offer(E e) {
        if (super.offer(e)) {
            queNotEmptyCondition.signal();
            return true;
        } else {
            queNotEmptyCondition.signal();
            return false;
        }
    }

    public final E poll() {
        final E e = super.poll();
        queNotFullCondition.signal();
        return e;
    }

    @Override
    public void put(E e) throws InterruptedException {
        while (!offer(e)) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            queNotFullCondition.await();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        for (; ; ) {
            if (offer(e)) {
                return true;
            } else {
                if (!ConcurrentCondition.waitStatus(timeout, unit, queNotFullCondition)) {
                    return false;
                }
            }
        }
    }

    @Override
    public E take() throws InterruptedException {
        for (; ; ) {
            final E e = poll();
            if (e != null) {
                return e;
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            queNotEmptyCondition.await();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        for (; ; ) {
            final E e = poll();
            if (e != null) {
                return e;
            } else {
                if (!ConcurrentCondition.waitStatus(timeout, unit, queNotEmptyCondition)) {
                    return null;
                }
            }
        }
    }

    @Override
    public int remainingCapacity() {
        return capacity - size();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, size());
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (this == c) {
            throw new IllegalArgumentException("can not drain to self");
        }
        int nRead = 0;
        while (!isEmpty() && maxElements > 0) {
            final E e = poll();
            if (e != null) {
                c.add(e);
                nRead++;
            }
        }
        return nRead;
    }

    @Override
    public E remove() {
        return poll();
    }

    @Override
    public E element() {
        final E e = peek();
        if (e != null) {
            return e;
        }
        throw new NoSuchElementException();
    }

    @Override
    public Iterator<E> iterator() {
        return new RingIterator();
    }

    @Override
    public Object[] toArray() {
        E[] e = (E[]) new Object[size()];
        toArray(e);
        return e;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        remove((E[]) a);
        return a;

    }

    @Override
    public boolean add(E e) {
        if (offer(e)) {
            return true;
        }
        throw new IllegalStateException("queue is full");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            if (!offer(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    private boolean isFull() {
        return (tail.get() - head.get()) == capacity;
    }

    private class RingIterator implements Iterator<E> {
        int dx = 0;

        E lastObj = null;

        private RingIterator() {
        }

        @Override
        public boolean hasNext() {
            return dx < size();
        }

        @Override
        public E next() {
            final long pollPos = head.get();
            final int slot = (int) ((pollPos + dx++) & mask);
            lastObj = buffer[slot].entry;
            return lastObj;
        }

        @Override
        public void remove() {
            MpmcBlockingQueue.this.remove(lastObj);
        }
    }

    /**
     * condition used for signaling queue is full
     */
    private final class QueueNotFull extends AbstractCondition {

        @Override
        public boolean test() {
            return isFull();
        }
    }

    /**
     * condition used for signaling queue is empty
     */
    private final class QueueNotEmpty extends AbstractCondition {

        @Override
        public boolean test() {
            return isEmpty();
        }
    }

    private final class WaitingQueueNotFull extends AbstractConditionWaiting {
        @Override
        public boolean test() {
            return isFull();
        }
    }

    private final class WaitingQueueNotEmpty extends AbstractConditionWaiting {
        @Override
        public boolean test() {
            return isEmpty();
        }
    }

    private final class SpinningQueueNotFull extends AbstractConditionSpinning {
        @Override
        public boolean test() {
            return isFull();
        }
    }

    private final class SpinningQueueNotEmpty extends AbstractConditionSpinning {
        @Override
        public boolean test() {
            return isEmpty();
        }
    }
}
