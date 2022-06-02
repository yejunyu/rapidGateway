package lmax.example.multiConsumer;

import com.lmax.disruptor.WorkHandler;
import lmax.example.LongEvent;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/27
 */
public class Consumer implements WorkHandler<LongEvent> {

    private String comsumerId;

    private static AtomicInteger count = new AtomicInteger(0);

    private Random random = new Random();

    public Consumer(String comsumerId) {
        this.comsumerId = comsumerId;
    }

    @Override
    public void onEvent(LongEvent longEvent) throws Exception {
        Thread.sleep(random.nextInt(5));
        System.out.println("当前消费者: " + this.comsumerId + ", 消费信息 id " + longEvent.getValue());
        count.incrementAndGet();
    }

    public static long getCount() {
        return count.get();
    }
}
