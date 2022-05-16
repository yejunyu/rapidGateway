package lmax;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/16
 */
public class DisruptorTest {

    public static void main(String[] args) throws InterruptedException {
        final long start = System.currentTimeMillis();
        int bufferSize = 1024;
        // disruptor交给线程池处理 共计p1,c1,c2,c3 四个线程
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        // 构造缓冲区与事件生成
        final Disruptor<InParkingDataEvent> disruptor = new Disruptor<>(new EventFactory<InParkingDataEvent>() {
            @Override
            public InParkingDataEvent newInstance() {
                return new InParkingDataEvent();
            }
        }, bufferSize, executorService, ProducerType.SINGLE, new YieldingWaitStrategy());
        final EventHandlerGroup<InParkingDataEvent> handlerGroup = disruptor.handleEventsWith(new ParkingDataToKafkaHandler(), new ParkingDataInDbHandler());
        ParkingDataSmsHandler handler = new ParkingDataSmsHandler();
        // 声明 c1,c2(kafka 和 db 事件完成之后)发送 sms
        handlerGroup.then(handler);
        disruptor.start();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        executorService.submit(new InParkingDataEventPublisher(disruptor, countDownLatch));
        countDownLatch.await();
        disruptor.shutdown();
        executorService.shutdown();
        System.out.println("总耗时: " + (System.currentTimeMillis() - start));
    }
}
