package com.yejunyu.rapid.common.concurrent.queue.mpmc;

/**
 * @author : YeJunyu
 * @description : multi producer multi consumer Concurrent Queue
 * @email : yyyejunyu@gmail.com
 * @date : 2022/6/7
 */
public class MpmcConcurrentQueue<E> implements ConcurrentQueue<E> {

    protected final int capacity;

    // mask 11111111
    final long mask;

    final Cell<E>[] buffer;

    // 环形队列,需要一个头指针和一个尾指针
    // 头部计数器
    final PaddingAtomicLong head = new PaddingAtomicLong(0L);

    // 尾部计数器
    final PaddingAtomicLong tail = new PaddingAtomicLong(0L);

    @SuppressWarnings("unchecked")
    public MpmcConcurrentQueue(final int capacity) {
        int c = 2;
        while (c < capacity) {
            c <<= 1;
        }
        this.capacity = c;
        mask = this.capacity - 1;
        buffer = new Cell[this.capacity];
        // 缓存预加载
        for (int i = 0; i < this.capacity; i++) {
            buffer[i] = new Cell<>(i);
        }
    }

    /**
     * 从尾部插入数据
     *
     * @param e
     * @return
     */
    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        Cell<E> cell;
        long tail = this.tail.get();
        for (; ; ) {
            // 取得当前的 cell
            cell = buffer[(int) (tail & mask)];
            // 获取当前 cell 的 seq
            final long seq = cell.seq.get();
            // 获取dif 差值
            final long dif = seq - tail;
            // 正常情况下 dif 为 0
            if (dif == 0) {
                // cas tail +1
                if (this.tail.compareAnsSet(tail, tail + 1)) {
                    break;
                }
            } else if (dif < 0) {
                return false;
            } else {
                // 并发情况下可能 seq>tail, 那么以 tail 的值为准, 更新 cell 的 seq
                tail = this.tail.get();
            }
        }
        cell.entry = e;
        // cell 的 seq 自增
        cell.seq.set(tail + 1);
        return true;
    }

    /**
     * 从头部取出数据
     *
     * @return
     */
    @Override
    public E poll() {
        Cell<E> cell;
        long head = this.head.get();
        for (; ; ) {
            cell = buffer[(int) (head & mask)];
            final long seq = cell.seq.get();
            // head+1, 因为插入一个数据时 seq+1 了
            final long dif = seq - (head + 1);
            // 正常情况 dif 为 0
            if (dif == 0) {
                if (this.head.compareAnsSet(head, head + 1)) {
                    break;
                }
            }
        }
        try {
            return cell.entry;
        } finally {
            cell.entry = null;
            // 下次再取该元素时应该轮了一圈了 seq 应为 head+mask+1
            cell.seq.set(head + mask + 1);
        }
    }

    @Override
    public E peek() {
        return buffer[(int) (head.get() & mask)].entry;
    }

    @Override
    public int size() {
        return (int) Math.max(tail.get() - head.get(), 0);
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean isEmpty() {
        return head.get() == tail.get();
    }

    @Override
    public boolean contains(Object o) {
        long h = head.get();
        long t = tail.get();
        for (long i = h; i < t; i++) {
            final int slot = (int) (i & mask);
            if (buffer[slot].entry != null && buffer[slot].entry.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int remove(E[] e) {
        int nRead = 0;
        while (nRead < e.length && !isEmpty()) {
            final E entry = poll();
            if (entry != null) {
                e[nRead++] = entry;
            }
        }
        return nRead;
    }

    @Override
    public void clear() {
        while (!isEmpty()) {
            poll();
        }
    }

    /**
     * 消除伪共享的对象
     *
     * @param <R>
     */
    protected static final class Cell<R> {
        // 计数器
        final PaddingAtomicLong seq = new PaddingAtomicLong(0L);
        // 实际的内容
        R entry;

        public Cell(final long s) {
            seq.set(s);
            entry = null;
        }
    }
}
