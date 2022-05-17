package lmax.example;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/17
 */
public class LongEventMain {
    public static void main(String[] args) throws InterruptedException {
        int bufferSize = 1024;

        final Disruptor<LongEvent> disruptor = new Disruptor<>(new LongEventFactory(), bufferSize, DaemonThreadFactory.INSTANCE);
        final EventHandlerGroup<LongEvent> handlerGroup = disruptor.handleEventsWith(new LongEventHandler());
        disruptor.start();
        final RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        final LongEventProducer producer = new LongEventProducer(ringBuffer);

        final ByteBuffer bb = ByteBuffer.allocate(8);
        for (int i = 0; true; i++) {
            bb.putLong(0, i);
            producer.onData(bb);
            Thread.sleep(1000L);
        }

    }
}
