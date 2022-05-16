package lmax;

import com.lmax.disruptor.EventHandler;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/16
 */
public class ParkingDataToKafkaHandler implements EventHandler<InParkingDataEvent> {
    @Override
    public void onEvent(InParkingDataEvent event, long l, boolean b) throws Exception {
        final long id = Thread.currentThread().getId();
        final String carLicense = event.getCarLicense();
        System.out.printf("Thread Id %s send %s in plaza messsage to kafka...%n", id, carLicense);
    }
}
