package com.fzk.vehicle.monitor.entity;

import com.fzk.stress.entity.Vehicle;
import com.fzk.vehicle.monitor.util.StatusPublishUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

@Data
@Slf4j
public class Monitor {
    private List<String> imeis;
    private static volatile Map<String,ScheduledFuture> scheduledFutureMap = new HashMap<>(16);


    public Monitor(List<String> imeis) {
        this.imeis = imeis;
    }

    public void detectRealTimeStatus() {
        int size = imeis.size();
        for (int i = 0; i < size; i++) {
            String imei = imeis.get(i);
            Vehicle vehicle = new Vehicle();
            vehicle.setBindingDeviceImei(imei);
            //模拟一半的车辆处理非ON档
            //处理非ON的设备
            StatusPublishUtil.handleParkingCarStatus(vehicle);

            /**测试验证定时器用*/
//            boolean flag = true;

            boolean flag = new Random().nextBoolean();
            log.info("ON分配，imei = {}, futureDrive = {}", imei, flag);
            if (flag) {
                //未来某个时刻启动并行驶一段轨迹
                int start = new Random().nextInt(20);
                //至少行驶5分钟
                int end = new Random().nextInt(60) + start + 5;

                /**测试验证定时器用*/
//                int start = 1;
//                int end = 2;

                log.info("车辆{}分钟后，进入驾驶状态，imei = {},start = {},end = {}", start, imei, start, end);
                StatusPublishUtil.driveTheCarInFuture(vehicle, start, end);
            }
        }
    }
}
