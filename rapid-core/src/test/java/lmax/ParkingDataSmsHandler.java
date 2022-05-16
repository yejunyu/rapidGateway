package lmax;

import com.lmax.disruptor.EventHandler;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/16
 */
public class ParkingDataSmsHandler implements EventHandler<InParkingDataEvent> {
    @Override
    public void onEvent(InParkingDataEvent event, long sequence, boolean endOfBatch) throws Exception {
        long threadId = Thread.currentThread().getId();
        String carLicense = event.getCarLicense();
        System.out.printf("Thread Id %s send %s in plaza sms to user%n", threadId, carLicense);
    }


}
