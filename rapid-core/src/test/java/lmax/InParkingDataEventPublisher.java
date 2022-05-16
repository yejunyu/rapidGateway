package lmax;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/16
 */

class InParkingDataEventTranslator implements EventTranslator<InParkingDataEvent> {
    @Override
    public void translateTo(InParkingDataEvent event, long l) {
        this.generateTradeTransaction(event);
    }

    private InParkingDataEvent generateTradeTransaction(InParkingDataEvent event) {
        int num = (int) (Math.random() * 8000);
        num = num + 1000;
        event.setCarLicense("京 z" + num);
        System.out.println("Thread Id " + Thread.currentThread().getId() + " 写完一个event");
        return event;
    }
}

public class InParkingDataEventPublisher implements Runnable {
    Disruptor<InParkingDataEvent> disruptor;

    private CountDownLatch latch;

    private static int Loop = 10;

    public InParkingDataEventPublisher(Disruptor<InParkingDataEvent> disruptor, CountDownLatch latch) {
        this.disruptor = disruptor;
        this.latch = latch;
    }

    @Override
    public void run() {
        InParkingDataEventTranslator translator = new InParkingDataEventTranslator();
        for (int i = 0; i < Loop; i++) {
            disruptor.publishEvent(translator);
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        latch.countDown();
        System.out.println("生产者写完" + Loop + "个消息");
    }
}
