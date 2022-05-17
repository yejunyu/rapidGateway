package lmax.example;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/16
 */
public class LongEventProducer {

    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(ByteBuffer bb) {
        final long sequence = ringBuffer.next();
        try {
            final LongEvent longEvent = ringBuffer.get(sequence);
            longEvent.setValue(bb.getLong(0));
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
