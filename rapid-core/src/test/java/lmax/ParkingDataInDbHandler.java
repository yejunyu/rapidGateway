package lmax;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * @author : YeJunyu
 * @description :
 * @email : yyyejunyu@gmail.com
 * @date : 2022/5/16
 */
public class ParkingDataInDbHandler implements EventHandler<InParkingDataEvent>, WorkHandler<InParkingDataEvent> {
    @Override
    public void onEvent(InParkingDataEvent inParkingDataEvent, long sequence, boolean endOfBatch) throws Exception {
        this.onEvent(inParkingDataEvent);
    }

    @Override
    public void onEvent(InParkingDataEvent event) throws Exception {
        final long id = Thread.currentThread().getId();
        final String carLicense = event.getCarLicense();
        System.out.printf("Thread Id %s save %s into db ....%n", id, carLicense);
    }
}
