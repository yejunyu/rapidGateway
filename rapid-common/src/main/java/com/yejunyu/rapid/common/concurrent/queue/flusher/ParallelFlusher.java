package com.yejunyu.rapid.common.concurrent.queue.flusher;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/31
 */
public class ParallelFlusher<E> implements Flusher<E> {

    private RingBuffer<Holder> ringBuffer;

    private EventListener<E> eventListener;

    private WorkerPool<Holder> workerPool;

    private ExecutorService executorService;

    private EventTranslatorOneArg<Holder, E> eventTranslator;

    public ParallelFlusher(Builder<E> builder) {
        this.executorService = Executors.newFixedThreadPool(builder.threads,
                new ThreadFactoryBuilder().setNameFormat("ParallelFlusher-" + builder.namePrefix + "-pool-%d").build());
        this.eventListener = builder.listener;
        this.eventTranslator = new HolderEventTranslator();
        // 1. 构建 ringBuffer
        final RingBuffer<Holder> ringBuffer = RingBuffer.create(builder.producerType, new HolderEventFactory(), builder.bufferSize, builder.waitStrategy);
        // 2. 通过 ringBuffer 构建一个屏障
        final SequenceBarrier barrier = ringBuffer.newBarrier();
        // 3. 创建多个消费者组
        @SuppressWarnings("unchecked")
        WorkHandler<Holder>[] workHandlers = new WorkHandler[builder.threads];
        for (int i = 0; i < workHandlers.length; i++) {
            workHandlers[i] = new HolderWorkHandler();
        }
        // 4. 构建多消费者工作池
        WorkerPool<Holder> workerPool = new WorkerPool<>(ringBuffer, barrier, new HolderExceptionHandler(), workHandlers);
        // 5. 设置多个消费者 sequence 序号, 用于单独统计消费进度, 并且设置到 ringBuffer 中
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        this.workerPool = workerPool;
    }

    @Override
    public void add(E event) {
        RingBuffer<Holder> temp = ringBuffer;
        if (temp == null) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), event);
            return;
        }
        try {
            ringBuffer.publishEvent(this.eventTranslator, event);
        } catch (NullPointerException e) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), event);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(E... events) {
        RingBuffer<Holder> temp = ringBuffer;
        if (temp == null) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
            return;
        }
        try {
            ringBuffer.publishEvents(this.eventTranslator, events);
        } catch (NullPointerException e) {
            process(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
        }
    }

    @Override
    public boolean tryAdd(E event) {
        RingBuffer<Holder> temp = ringBuffer;
        if (temp == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvent(this.eventTranslator, event);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean tryAdd(E... events) {
        RingBuffer<Holder> temp = ringBuffer;
        if (temp == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvents(this.eventTranslator, events);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean isShutdown() {
        return ringBuffer == null;
    }

    @Override
    public void start() {
        this.ringBuffer = workerPool.start(executorService);
    }

    @Override
    public void shutDown() {
        RingBuffer<Holder> temp = ringBuffer;
        ringBuffer = null;
        if (temp == null) {
            return;
        }
        if (workerPool != null) {
            workerPool.drainAndHalt();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private static <E> void process(EventListener<E> listener, Throwable throwable, E event) {
        listener.onException(throwable, -1, event);
    }

    @SuppressWarnings("unchecked")
    private static <E> void process(EventListener<E> listener, Throwable throwable, E... events) {
        for (E event : events) {
            process(listener, throwable, event);
        }
    }

    private class Holder {
        private E event;

        public void setEvent(E event) {
            this.event = event;
        }

        @Override
        public String toString() {
            return "Holder{" +
                    "event=" + event +
                    '}';
        }
    }

    public interface EventListener<E> {
        void onEvent(E event) throws Exception;

        void onException(Throwable t, long sequence, E event);
    }

    public static class Builder<E> {
        private ProducerType producerType = ProducerType.MULTI;

        private int bufferSize = 16 * 1024;

        private int threads = 1;

        private String namePrefix = "";

        private WaitStrategy waitStrategy = new BlockingWaitStrategy();
        /**
         * 消费者监听
         */
        private EventListener<E> listener;

        public ParallelFlusher<E> build() {
            return new ParallelFlusher<>(this);
        }

        public Builder<E> setProducerType(ProducerType producerType) {
            Preconditions.checkNotNull(producerType);
            this.producerType = producerType;
            return this;
        }

        public Builder<E> setBufferSize(int bufferSize) {
            // buffersize 需要是 2 的次幂
            Preconditions.checkArgument(Integer.bitCount(bufferSize) == 1);
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<E> setThreads(int threads) {
            Preconditions.checkArgument(threads > 0);
            this.threads = threads;
            return this;
        }

        public Builder<E> setNamePrefix(String namePrefix) {
            Preconditions.checkNotNull(namePrefix);
            this.namePrefix = namePrefix;
            return this;
        }

        public Builder<E> setWaitStrategy(WaitStrategy waitStrategy) {
            Preconditions.checkNotNull(waitStrategy);
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder<E> setListener(EventListener<E> listener) {
            Preconditions.checkNotNull(listener);
            this.listener = listener;
            return this;
        }
    }

    private class HolderEventFactory implements EventFactory<Holder> {

        @Override
        public Holder newInstance() {
            return new Holder();
        }
    }

    private class HolderWorkHandler implements WorkHandler<Holder> {

        @Override
        public void onEvent(Holder holder) throws Exception {
            eventListener.onEvent(holder.event);
            holder.setEvent(null);
        }
    }

    private class HolderExceptionHandler implements ExceptionHandler<Holder> {

        @Override
        public void handleEventException(Throwable throwable, long l, Holder holder) {
            try {
                eventListener.onException(throwable, l, holder.event);
            } catch (Exception e) {
                // ignore
            } finally {
                holder.setEvent(null);
            }
        }

        @Override
        public void handleOnStartException(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }
    }

    private class HolderEventTranslator implements EventTranslatorOneArg<Holder, E> {

        @Override
        public void translateTo(Holder holder, long l, E e) {
            holder.setEvent(e);
        }
    }
}
