package lmax.example.multiConsumer;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import lmax.example.LongEvent;
import lmax.example.LongEventFactory;
import lmax.example.LongEventProducer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * @author : YeJunyu
 * @description : <p>https://tech.meituan.com/2016/11/18/disruptor.html</p>
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/27
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        // 1. 创建 ringbuffer
        RingBuffer<LongEvent> ringBuffer = RingBuffer.create(ProducerType.MULTI, new LongEventFactory(), 1024 * 1024, new BlockingWaitStrategy());
        // 2. 通过 ringbuffer 创建一个sequenceBarrier屏障
        SequenceBarrier barrier = ringBuffer.newBarrier();
        // 3. 创建多个消费者
        Consumer[] consumers = new Consumer[10];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new Consumer("C-" + i);
        }
        // 4. 构建多消费者工作池
        WorkerPool<LongEvent> workerPool = new WorkerPool<>(
                ringBuffer, barrier, new EventExceptionHandler(), consumers
        );

        // 5. 设置多个消费者的 sequence 序号用于单独统计消费进度, 并且设置到 ringbuffer 中
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

        // 6. 启动 workerPool
        workerPool.start(Executors.newFixedThreadPool(5));

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < 100; i++) {
            LongEventProducer producer = new LongEventProducer(ringBuffer);
            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < 100; j++) {
                    producer.onData(ByteBuffer.wrap(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));
                }
            }).start();
        }
        Thread.sleep(2000L);
        System.err.println("----------线程创建完毕，开始生产数据----------");
        countDownLatch.countDown();
        Thread.sleep(10000L);
        System.err.println("任务总数:" + Consumer.getCount());
    }

    static class EventExceptionHandler implements ExceptionHandler<LongEvent> {

        @Override
        public void handleEventException(Throwable throwable, long l, LongEvent longEvent) {

        }

        @Override
        public void handleOnStartException(Throwable throwable) {

        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {

        }
    }
}
