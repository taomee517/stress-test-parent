package com.fzk.vehicle.monitor.util;

import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import com.fzk.stress.entity.Vehicle;
import com.fzk.stress.util.DateTimeUtil;
import com.fzk.stress.util.HashedWheelTimerUtil;
import com.fzk.stress.util.VehicleStatusBuilder;
import com.fzk.vehicle.monitor.task.DrivingCarStatusPublishTask;
import com.fzk.vehicle.monitor.task.OnChangeEventTask;
import com.fzk.vehicle.monitor.task.ParkingCarStatusPublishTask;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.fzk.stress.cache.TopicCenter.CAR_STATUS_PREFIX;
import static com.fzk.stress.constants.Configuration.NONE_ON_REPORT_INTERVAL;
import static com.fzk.stress.constants.Configuration.ON_REPORT_INTERVAL;

@Slf4j
public class StatusPublishUtil {
    private static volatile Map<String,Integer> indexMap = new HashMap<>(16);
    private static HashedWheelTimer timer = HashedWheelTimerUtil.instance().getTimer();

    public static void publishStatus(Vehicle vehicle){
        String hexTime = DateTimeUtil.timeToHexString(DateTimeUtil.getDatetime());
        vehicle.setGpsTime(hexTime);
        String imei = vehicle.getBindingDeviceImei();
        Integer srcLastStatusIndex = indexMap.get(imei);
        int lastStatusIndex = 0;
        if(Objects.nonNull(srcLastStatusIndex) && srcLastStatusIndex!=9999){
            lastStatusIndex = srcLastStatusIndex;
        }
        int currentStatusIndex = lastStatusIndex + 1;
        indexMap.put(imei, currentStatusIndex);
        String statusMsg = VehicleStatusBuilder.buildAllStatusMessage(vehicle,lastStatusIndex);
        String topic = StringUtils.join(CAR_STATUS_PREFIX,imei);
        log.info("发布状态消息，imei = {},statusMsg = {}", imei, statusMsg);
        if (RedisService.publish(topic,statusMsg) == 0) {
            String key = TopicCenter.buildDelayMessageKey(imei); ;
            RedisService.lpush(key,statusMsg);
        }
    }

    public static void handleDrivingCarStatus(Vehicle vehicle){
        timer.newTimeout(new DrivingCarStatusPublishTask(vehicle), ON_REPORT_INTERVAL, TimeUnit.SECONDS);
    }

    public static void handleParkingCarStatus(Vehicle vehicle){
        timer.newTimeout(new ParkingCarStatusPublishTask(vehicle), NONE_ON_REPORT_INTERVAL, TimeUnit.SECONDS);
    }

    public static void driveTheCarInFuture(Vehicle vehicle,int start, int end){
        timer.newTimeout(new OnChangeEventTask(vehicle,true), start,TimeUnit.MINUTES);
        timer.newTimeout(new OnChangeEventTask(vehicle,false), end,TimeUnit.MINUTES);
    }
}
